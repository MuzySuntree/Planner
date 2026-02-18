package statehub;

import common.Envelope;
import common.EnvelopeFactory;
import common.EventTypes;
import common.Payloads;
import eventbus.InMemoryEventBus;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

/**
 * StateHub：器官注册/路由/影子状态/等待池/去重/锁控制。
 */
public class StateHub {
    private final InMemoryEventBus eventBus;
    private final Map<String, OrganSession> organSessions = new ConcurrentHashMap<>();
    private final Map<String, Shadow> shadows = new ConcurrentHashMap<>();
    private final Map<String, Pending> waitPool = new ConcurrentHashMap<>();
    private final Set<String> processedResults = ConcurrentHashMap.newKeySet();
    private final Map<String, Lease> leases = new ConcurrentHashMap<>();
    private final ScheduledExecutorService timeoutChecker = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "statehub-timeout-checker"));

    private final int port;
    private Channel serverChannel;
    private EventLoopGroup boss;
    private EventLoopGroup worker;

    public StateHub(InMemoryEventBus eventBus, int port) {
        this.eventBus = eventBus;
        this.port = port;
    }

    public void start() throws InterruptedException {
        eventBus.subscribe(EventTypes.SCHEDULER_COMMAND, this::handleSchedulerCommand);
        startNettyServer();
        timeoutChecker.scheduleAtFixedRate(this::scanTimeout, 200, 200, TimeUnit.MILLISECONDS);
    }

    private void startNettyServer() throws InterruptedException {
        boss = new NioEventLoopGroup(1);
        worker = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(new LineBasedFrameDecoder(65536));
                        ch.pipeline().addLast(new StringDecoder(StandardCharsets.UTF_8));
                        ch.pipeline().addLast(new StringEncoder(StandardCharsets.UTF_8));
                        ch.pipeline().addLast(new StateHubServerHandler(StateHub.this));
                    }
                });
        serverChannel = bootstrap.bind(port).sync().channel();
        System.out.println("[StateHub] Netty server started at " + port);
    }

    private void handleSchedulerCommand(Envelope envelope) {
        Payloads.OrganCommand cmd = common.Jsons.MAPPER.convertValue(envelope.payload(), Payloads.OrganCommand.class);
        Shadow shadow = shadows.computeIfAbsent(cmd.organId(), k -> new Shadow());
        shadow.desiredState.putAll(cmd.args());

        if (!lock(cmd.organId(), envelope.commandId(), 5000)) {
            eventBus.publish(EnvelopeFactory.of(EventTypes.SCHEDULER_COMMAND_TIMEOUT, "statehub", "scheduler",
                    envelope.correlationId(), envelope.commandId(), Map.of("reason", "organ busy by lock")));
            return;
        }

        OrganSession session = organSessions.get(cmd.organId());
        if (session == null || !session.online) {
            unlock(cmd.organId(), envelope.commandId());
            eventBus.publish(EnvelopeFactory.of(EventTypes.SCHEDULER_COMMAND_TIMEOUT, "statehub", "scheduler",
                    envelope.correlationId(), envelope.commandId(), Map.of("reason", "organ offline")));
            return;
        }

        waitPool.put(envelope.commandId(), new Pending(envelope.correlationId(), System.currentTimeMillis() + 3000, 0, cmd.organId()));
        session.channel.writeAndFlush(toLine(envelope));
        System.out.printf("[StateHub] route commandId=%s to organ=%s%n", envelope.commandId(), cmd.organId());
    }

    void onIncomingFromOrgan(Channel channel, Envelope envelope) {
        switch (envelope.eventType()) {
            case EventTypes.ORGAN_REGISTER -> handleRegister(channel, envelope);
            case EventTypes.ORGAN_RESULT -> handleResult(envelope);
            case EventTypes.ORGAN_TELEMETRY -> eventBus.publish(EnvelopeFactory.of(
                    EventTypes.BRAIN_USER_COMMAND,
                    "statehub",
                    "brain",
                    envelope.correlationId(),
                    null,
                    new Payloads.UserCommand(envelope.payload().path("data").path("text").asText())
            ));
            default -> System.out.println("[StateHub] unknown eventType from organ: " + envelope.eventType());
        }
    }

    private void handleRegister(Channel channel, Envelope envelope) {
        Payloads.OrganRegistration reg = common.Jsons.MAPPER.convertValue(envelope.payload(), Payloads.OrganRegistration.class);
        organSessions.put(reg.organId(), new OrganSession(channel, reg.capabilities(), true, reg.session()));
        shadows.putIfAbsent(reg.organId(), new Shadow());
        System.out.printf("[StateHub] organ registered id=%s caps=%d%n", reg.organId(), reg.capabilities().size());
    }

    private void handleResult(Envelope envelope) {
        if (envelope.commandId() == null) {
            return;
        }
        if (!processedResults.add(envelope.commandId())) {
            System.out.printf("[StateHub] duplicate result commandId=%s ignored%n", envelope.commandId());
            return;
        }

        Pending pending = waitPool.remove(envelope.commandId());
        Payloads.OrganResult result = common.Jsons.MAPPER.convertValue(envelope.payload(), Payloads.OrganResult.class);
        Shadow shadow = shadows.computeIfAbsent(result.organId(), k -> new Shadow());
        if (result.data() != null) {
            shadow.reportedState.putAll(result.data());
        }
        unlock(result.organId(), envelope.commandId());

        if (pending != null) {
            eventBus.publish(EnvelopeFactory.of(EventTypes.SCHEDULER_COMMAND_RESULT, "statehub", "scheduler",
                    envelope.correlationId(), envelope.commandId(), result));
        }
    }

    private void scanTimeout() {
        long now = System.currentTimeMillis();
        for (Map.Entry<String, Pending> entry : waitPool.entrySet()) {
            if (entry.getValue().deadlineMs < now) {
                String commandId = entry.getKey();
                Pending pending = waitPool.remove(commandId);
                if (pending != null) {
                    unlock(pending.organId, commandId);
                    eventBus.publish(EnvelopeFactory.of(EventTypes.SCHEDULER_COMMAND_TIMEOUT, "statehub", "scheduler",
                            pending.correlationId, commandId, Map.of("deadlineMs", pending.deadlineMs, "retry", pending.retryCount)));
                }
            }
        }
    }

    public boolean lock(String resourceKey, String owner, long ttlMs) {
        long now = System.currentTimeMillis();
        Lease lease = leases.get(resourceKey);
        if (lease == null || lease.expireAt < now || lease.owner.equals(owner)) {
            leases.put(resourceKey, new Lease(owner, now + ttlMs));
            return true;
        }
        return false;
    }

    public void unlock(String resourceKey, String owner) {
        Lease lease = leases.get(resourceKey);
        if (lease != null && lease.owner.equals(owner)) {
            leases.remove(resourceKey);
        }
    }

    public void printSnapshot() {
        System.out.println("===== StateHub Snapshot =====");
        for (Map.Entry<String, Shadow> e : shadows.entrySet()) {
            System.out.printf("organ=%s desired=%s reported=%s%n", e.getKey(), e.getValue().desiredState, e.getValue().reportedState);
        }
    }

    public void stop() {
        timeoutChecker.shutdownNow();
        if (serverChannel != null) {
            serverChannel.close();
        }
        if (boss != null) {
            boss.shutdownGracefully();
        }
        if (worker != null) {
            worker.shutdownGracefully();
        }
    }

    private String toLine(Envelope envelope) {
        try {
            return common.Jsons.MAPPER.writeValueAsString(envelope) + "\n";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class OrganSession {
        final Channel channel;
        final java.util.List<Payloads.Capability> capabilities;
        final boolean online;
        final String session;

        private OrganSession(Channel channel, java.util.List<Payloads.Capability> capabilities, boolean online, String session) {
            this.channel = channel;
            this.capabilities = capabilities;
            this.online = online;
            this.session = session;
        }
    }

    private static class Shadow {
        final Map<String, Object> desiredState = new ConcurrentHashMap<>();
        final Map<String, Object> reportedState = new ConcurrentHashMap<>();
    }

    private record Pending(String correlationId, long deadlineMs, int retryCount, String organId) {}

    private record Lease(String owner, long expireAt) {}
}
