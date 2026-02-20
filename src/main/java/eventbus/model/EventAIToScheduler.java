package eventbus.model;

public record EventAIToScheduler() implements EventPayload {
//    任务执行方式
    public enum TaskTimes{
        IMMEDIATE,              //立即执行
        TIMEOUT,                //延迟执行
        INTERVAL,                //周期执行
        CONDITIONAL             //条件性执行
    }
}
