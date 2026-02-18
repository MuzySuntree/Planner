package bootstrap;

import organs.console.EarMouthConsoleOrgan;

public class ConsoleOrganMain {
    public static void main(String[] args) throws Exception {
        EarMouthConsoleOrgan organ = new EarMouthConsoleOrgan("127.0.0.1", 9000);
        organ.start();
        Runtime.getRuntime().addShutdownHook(new Thread(organ::stop));
        Thread.currentThread().join();
    }
}
