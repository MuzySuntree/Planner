package model;

public class SchedulerEvent {
    private final int priority;
    private final String systemPrompt;
    private final String userPrompt;

    public SchedulerEvent(int priority, String systemPrompt, String userPrompt) {
        this.priority = priority;
        this.systemPrompt = systemPrompt;
        this.userPrompt = userPrompt;
    }

    public int getPriority() {
        return priority;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public String getUserPrompt() {
        return userPrompt;
    }
}
