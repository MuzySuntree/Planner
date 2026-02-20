package thinking.model;

public record OllamaResult(
        boolean finished,      // true=正常完成
        boolean interrupted,   // true=被取消/抢占
        String text            // 本轮完整输出
) {}
