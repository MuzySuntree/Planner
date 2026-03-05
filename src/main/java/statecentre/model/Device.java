package statecentre.model;

import java.util.List;

//设备名称
public class Device {

//    设备名称
    public String name;
//    设备锁
    public Object lock;
//    是否可用
    public boolean active;
    //    能够做什么
    public String capability;
    public List<Action> actions;
}
