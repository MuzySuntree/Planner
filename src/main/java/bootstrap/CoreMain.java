package bootstrap;

import brain.BrainStub;
import common.EnvelopeFactory;
import common.EventTypes;
import common.Payloads;
import eventbus.InMemoryEventBus;
import scheduler.SchedulerEngine;
import statehub.StateHub;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Brain + Scheduler + StateHub 同进程演示入口。
 */
public class CoreMain {
    public static void main(String[] args) throws Exception {
        InMemoryEventBus bus = new InMemoryEventBus();
        AtomicBoolean stopFlag = new AtomicBoolean(false);

        StateHub stateHub = new StateHub(bus, 9000);
        stateHub.start();

        BrainStub brain = new BrainStub(bus, stopFlag);
        brain.start();

        SchedulerEngine scheduler = new SchedulerEngine(bus);
        scheduler.start();

        bus.subscribe(EventTypes.SCHEDULER_OUTPUT, env -> {
            Payloads.SchedulerOutput output = common.Jsons.MAPPER.convertValue(env.payload(), Payloads.SchedulerOutput.class);
            System.out.println(output.text());
        });

        // 演示启动消息
        bus.publish(EnvelopeFactory.of(EventTypes.SCHEDULER_OUTPUT, "core", "earmouth-console", EnvelopeFactory.newCorrelationId(), null,
                new Payloads.SchedulerOutput("系统已启动，请在 ConsoleOrgan 侧输入 help/echo/exit")));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            stateHub.printSnapshot();
            stateHub.stop();
            bus.close();
        }));

        while (!stopFlag.get()) {
            Thread.sleep(300);
        }

        stateHub.printSnapshot();
        stateHub.stop();
        bus.close();
        System.exit(0);
    }
}
