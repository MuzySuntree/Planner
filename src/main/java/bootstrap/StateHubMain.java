package bootstrap;

import eventbus.InMemoryEventBus;
import statehub.StateHub;

/**
 * 独立启动 StateHub（MVP 中使用进程内 EventBus 演示，实际可替换外部总线）。
 */
public class StateHubMain {
    public static void main(String[] args) throws Exception {
        InMemoryEventBus bus = new InMemoryEventBus();
        StateHub stateHub = new StateHub(bus, 9000);
        stateHub.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            stateHub.stop();
            bus.close();
        }));
        Thread.currentThread().join();
    }
}
