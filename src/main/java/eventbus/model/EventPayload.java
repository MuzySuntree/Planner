package eventbus.model;

public sealed interface EventPayload permits EventAIToScheduler,EventSchedulerToState, EventSchedulerToAI, EventStateToScheduler, EventStateToDevice, EventDeviceToState{
}
