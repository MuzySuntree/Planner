package thinking.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OllamaChatRequest {

    // 指定模型
    public String model = "qwen3:8b";

    // 是否流式回复
    public Boolean stream = true;

    // 消息列表（注意：Ollama 字段名是 messages）
    public List<Message> messages = new ArrayList<>();

    // 生成参数
    public Options options = new Options();

    // 强制 JSON 输出（你做决策系统时很有用）
     public Object format; // "json" 或 schema 对象

    // 可选：保持模型常驻，减少冷启动
    @JsonProperty("keep_alive")
    public String keepAlive; // e.g. "5m"

    public OllamaChatRequest() {}

    public OllamaChatRequest(String system, String user) {
        // 推荐默认值（决策型）
        options.temperature = 0.2;
        options.top_p = 0.9;
        options.num_ctx = 4096;

        messages.add(Message.system(system));
        messages.add(Message.user(user));
    }

    public void addAssistant(String content) {
        messages.add(Message.assistant(content));
    }

    public void addUser(String content) {
        messages.add(Message.user(content));
    }

    public static class Message {
        public String role;
        public String content;

        public Message() {}

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

//        全局规则
        public static Message system(String content) { return new Message("system", content); }
//        当前输入
        public static Message user(String content) { return new Message("user", content); }
//        历史模型输出
        public static Message assistant(String content) { return new Message("assistant", content); }
    }

    public static class Options {
        // 随机性
        public Double temperature;
        public Double top_p;

        // 上下文窗口
        public Integer num_ctx;

        // 最大生成 token（非常建议设置，防止无限生成）
        public Integer num_predict;

        // 停止词
        public List<String> stop;
    }
}
