package scheduler;

import eventbus.SchedulerInterface;
import eventbus.commWorker.CommWorkerStateSubscribeDevice;
import eventbus.commWorker.CommWorkerStateSubscribeScheduler;
import eventbus.task.configure.EventTask;
import statecentre.model.Device;

import java.util.ArrayList;
import java.util.List;

public class StateScheduler implements SchedulerInterface {

    CommWorkerStateSubscribeScheduler commWorkerStateSubscribeScheduler = new CommWorkerStateSubscribeScheduler(this);
    CommWorkerStateSubscribeDevice commWorkerStateSubscribeDevice = new CommWorkerStateSubscribeDevice(this);

//    设备注册表
    private final List<Device>  devices = new ArrayList<>();

    public StateScheduler(){
        new Thread(commWorkerStateSubscribeScheduler).start();
    }

    @Override
    public void runLoop() {

    }

    @Override
    public void submit(EventTask t) {

    }
}
