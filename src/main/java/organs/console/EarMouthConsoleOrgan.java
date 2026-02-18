package organs.console;

import common.Envelope;
import common.EnvelopeFactory;
import common.EventTypes;
import common.Payloads;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 示例器官：Ear(输入Telemetry) + Mouth(执行console.print命令)。
 */
public class EarMouthConsoleOrgan {
    private static final String ORGAN_ID = "earmouth-console";

    private final String host;
    private final int port;
    private Channel channel;
    private EventLoopGroup group;
    private final ExecutorService stdinThread = Executors.newSingleThreadExecutor(r -> new Thread(r, "console-ear"));

    // commandId 幂等缓存（最近 N 条）
    private final int dedupeSize = 100;
    private final Map<String, Envelope> commandResultCache = new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Envelope> eldest) {
            return size() > dedupeSize;
        }
    };

    public EarMouthConsoleOrgan(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws InterruptedException {
        group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(new LineBasedFrameDecoder(65536));
                        ch.pipeline().addLast(new StringDecoder(StandardCharsets.UTF_8));
                        ch.pipeline().addLast(new StringEncoder(StandardCharsets.UTF_8));
                        ch.pipeline().addLast(new OrganClientHandler(EarMouthConsoleOrgan.this));
                    }
                });

        channel = bootstrap.connect(host, port).sync().channel();
        register();
        startReadLoop();
    }

    private void register() {
        List<Payloads.Capability> capabilities = List.of(
                new Payloads.Capability("console.readLine", Map.of(), Map.of("text", "string")),
                new Payloads.Capability("console.print", Map.of("text", "string"), Map.of())
        );
        Payloads.OrganRegistration reg = new Payloads.OrganRegistration(ORGAN_ID, capabilities, true, "session-" + UUID.randomUUID());
        Envelope env = EnvelopeFactory.of(EventTypes.ORGAN_REGISTER, "organ:" + ORGAN_ID, "statehub", EnvelopeFactory.newCorrelationId(), null, reg);
        send(env);
    }

    private void startReadLoop() {
        stdinThread.submit(() -> {
            Scanner scanner = new Scanner(System.in);
            while (!Thread.currentThread().isInterrupted()) {
                if (!scanner.hasNextLine()) {
                    continue;
                }
                String line = scanner.nextLine();
                Payloads.OrganTelemetry telemetry = new Payloads.OrganTelemetry(ORGAN_ID, "console.readLine", Map.of("text", line));
                Envelope env = EnvelopeFactory.of(EventTypes.ORGAN_TELEMETRY, "organ:" + ORGAN_ID, "statehub",
                        EnvelopeFactory.newCorrelationId(), null, telemetry);
                send(env);
            }
        });
    }

    void onCommand(Envelope envelope) {
        synchronized (commandResultCache) {
            if (commandResultCache.containsKey(envelope.commandId())) {
                send(commandResultCache.get(envelope.commandId()));
                return;
            }
        }

        Payloads.OrganCommand cmd = common.Jsons.MAPPER.convertValue(envelope.payload(), Payloads.OrganCommand.class);
        if (!"console.print".equals(cmd.capabilityId())) {
            return;
        }
        String text = String.valueOf(cmd.args().getOrDefault("text", ""));
        System.out.println("[ConsoleMouth] " + text);

        Payloads.OrganResult result = new Payloads.OrganResult(ORGAN_ID, "console.print", true, Map.of("lastPrinted", text), "ok");
        Envelope response = EnvelopeFactory.of(EventTypes.ORGAN_RESULT, "organ:" + ORGAN_ID, "statehub",
                envelope.correlationId(), envelope.commandId(), result);
        synchronized (commandResultCache) {
            commandResultCache.put(envelope.commandId(), response);
        }
        send(response);
    }

    private void send(Envelope envelope) {
        try {
            channel.writeAndFlush(common.Jsons.MAPPER.writeValueAsString(envelope) + "\n");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        stdinThread.shutdownNow();
        if (channel != null) {
            channel.close();
        }
        if (group != null) {
            group.shutdownGracefully();
        }
    }
}
