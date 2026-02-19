package eventbus;

import eventbus.model.Event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

//事件总线
public class EventBus {
//    订阅者集合
    private static final List<Consumer<Event>> subscribers = new CopyOnWriteArrayList<>();

//    添加订阅者
    public static void subscribe(Consumer<Event> subscriber) {
        subscribers.add(subscriber);
    }

//    发布事件
    public static void publish(Event event) {
//        把总线里的所有订阅者全部执行accept接口
        for (Consumer<Event> subscriber : subscribers) {
            subscriber.accept(event);
        }
    }
}
