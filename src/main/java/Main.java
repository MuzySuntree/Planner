import Control.OllamaTask;
import model.OllamaChatRequest;

public class Main {
    public static void main(String[] args) throws Exception {

        // 任务A：故意让它回答长一点，方便中断
        OllamaChatRequest chatA = new OllamaChatRequest(
                "你是一个只输出JSON的决策助手。",
                "温度过高了，请给出详细的决策JSON，包含：decision、priority、reason、next_events(不少于10条)。"
        );
        OllamaTask taskA = new OllamaTask(chatA);

        // 任务B：短任务
        OllamaChatRequest chatB = new OllamaChatRequest(
                "你是一个只输出JSON的决策助手。",
                "现在门铃响了，给出一个简短决策JSON：decision、priority、next_event(1条)。"
        );
        OllamaTask taskB = new OllamaTask(chatB);

        System.out.println("=== Start Task A (will be interrupted) ===");

        Thread tA = new Thread(() -> {
            try {
                boolean finished = taskA.getAnswer();
                System.out.println("\n\n[Task A] finished=" + finished);
            } catch (Exception e) {
                System.out.println("\n\n[Task A] ERROR: " + e.getMessage());
            }
        });
        tA.start();

        // 等 2 秒后触发“高优先级任务”来抢占
        Thread.sleep(4000);

        System.out.println("\n\n=== Interrupt Task A, run Task B ===");
        taskA.interrupt();

        // 等待 A 的线程真正退出（保证中断已落盘到 chat 上下文）
        tA.join();

        System.out.println("\n\n=== Start Task B ===");
        boolean bFinished = taskB.getAnswer();
        System.out.println("\n\n[Task B] finished=" + bFinished);

        // 恢复 A
        System.out.println("\n\n=== Resume Task A ===");
        boolean aFinishedAfterResume = taskA.getAnswer();
        System.out.println("\n\n[Task A] finished after resume=" + aFinishedAfterResume);

        System.out.println("\n\n=== Done ===");
    }
}
