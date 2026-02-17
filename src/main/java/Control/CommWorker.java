package Control;

import model.OllamaChatRequest;

public class CommWorker implements Runnable {

    private final AiScheduler scheduler;
    public CommWorker(AiScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void run() {
        // 示例：模拟两个任务到来
        try {
            Thread.sleep(500);

            var chatA = new OllamaChatRequest("你是一个只输出JSON的决策助手", "温度过高，生成详细决策JSON，事件>=10条");
            var taskA = new OllamaTask(chatA);
            scheduler.submit(new ScheduledTask(5, taskA));

            Thread.sleep(6500);
            var chatB = new OllamaChatRequest("你是一个只输出JSON的决策助手", "门铃响了，给出简短决策JSON");
            var taskB = new OllamaTask(chatB);
            scheduler.submit(new ScheduledTask(10, taskB)); // 更高优先级，触发抢占

            Thread.sleep(10000);
            var chatC = new OllamaChatRequest("你是一个只输出JSON的决策助手", "门铃响了，给出简短决策JSON");
            var taskC = new OllamaTask(chatC);
            scheduler.submit(new ScheduledTask(10, taskC)); // 更高优先级，触发抢占

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
