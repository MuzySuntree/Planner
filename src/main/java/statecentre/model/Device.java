package statecentre.model;

import java.util.List;

//设备名称
public class Device {

//    设备名称
    String name;
//    设备锁
    Object lock;
//    是否可用
    boolean active;
    //    能够做什么
    String capability;
    List<Action> actions;
}
