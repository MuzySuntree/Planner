package common;

import java.util.List;
import java.util.Map;

/**
 * 全局 payload schema 定义。
 */
public final class Payloads {
    private Payloads() {}

    public record UserCommand(String text) {}

    public record PlanSubmitted(String planId, List<PlanStep> steps) {}

    public record PlanStep(int order, String organId, String capabilityId, Map<String, Object> args) {}

    public record OrganCommand(String organId, String capabilityId, Map<String, Object> args) {}

    public record OrganResult(String organId, String capabilityId, boolean success, Map<String, Object> data, String message) {}

    public record OrganTelemetry(String organId, String capabilityId, Map<String, Object> data) {}

    public record Capability(String capabilityId, Map<String, Object> inputSchema, Map<String, Object> outputSchema) {}

    public record OrganRegistration(String organId, List<Capability> capabilities, boolean online, String session) {}

    public record SchedulerOutput(String text) {}
}
