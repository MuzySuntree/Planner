package eventbus.task.configure;

import eventbus.model.Event;

import java.util.Optional;

public interface EventTask {
    void runOnce();                 // 执行（可能完成/中断/失败）
    void requestInterrupt();         // 请求抢占
    void cancel();                   // 取消任务
    Optional<Event> completionEvent();    //获取结果，可能会返回结果，也可能并没有结果

    int getPriority();
//    状态改变后的回调函数
    void setEventTaskCallBack(EventTaskCallBack eventTaskCallBack);

    EventTaskState getTaskState();       //任务当前处于的生命周期
}
