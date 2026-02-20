package eventbus.task.configure;

//状态回调函数
public interface EventTaskCallBack {
    void onFinished(EventTask eventTask);
    void onFailed(EventTask eventTask, Throwable throwable);
    void onCancelled(EventTask eventTask);
    void onInterrupt(EventTask eventTask);
    void onInterruptRequested(EventTask eventTask);
}
