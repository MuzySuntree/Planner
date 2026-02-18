package common;

/**
 * 全局事件类型常量，避免字符串散落在各模块。
 */
public final class EventTypes {
    private EventTypes() {}

    public static final String BRAIN_USER_COMMAND = "brain.userCommand";
    public static final String BRAIN_PLAN_SUBMITTED = "brain.planSubmitted";
    public static final String SCHEDULER_COMMAND = "scheduler.command";
    public static final String SCHEDULER_COMMAND_RESULT = "scheduler.commandResult";
    public static final String SCHEDULER_COMMAND_TIMEOUT = "scheduler.commandTimeout";
    public static final String SCHEDULER_OUTPUT = "scheduler.output";
    public static final String ORGAN_COMMAND = "organ.command";
    public static final String ORGAN_RESULT = "organ.result";
    public static final String ORGAN_TELEMETRY = "organ.telemetry";
    public static final String ORGAN_REGISTER = "organ.register";
}
