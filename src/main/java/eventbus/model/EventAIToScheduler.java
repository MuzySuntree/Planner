package eventbus.model;

import plan.Plan;

public record EventAIToScheduler(Plan plan) implements EventPayload {
}
