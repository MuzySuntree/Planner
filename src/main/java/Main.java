import Control.AiScheduler;
import Control.CommWorker;

public class Main {
    public static void main(String[] args) throws Exception {
        AiScheduler scheduler = new AiScheduler();

        Thread aiThread = new Thread(scheduler::runLoop, "ai-worker");
        Thread commThread = new Thread(new CommWorker(scheduler), "comm-worker");

        aiThread.start();
        commThread.start();
    }
}
