package thinking.Control;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import thinking.model.OllamaChatRequest;
import okhttp3.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

public class OllamaTask {
    private static final String OLLAMA_URL = "http://localhost:14735/api/chat";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String interruptString = "你上一条回复被中断了。请参考中断的回复内容，为我重新生成一个完整的内容"
            ;
    OkHttpClient client;
    OllamaChatRequest chat;
    StringBuilder full= new StringBuilder();
    public OllamaTask() {
        client = new OkHttpClient();
    }
    public enum TaskState {
        PENDING,
        RUNNING,
        INTERRUPTED,
        FINISHED;
//        状态机规则
        private static final Map<TaskState, Set<TaskState>> ALLOWED = new EnumMap<>(TaskState.class);
        static {
            ALLOWED.put(PENDING, EnumSet.of(RUNNING));
            ALLOWED.put(RUNNING, EnumSet.of(INTERRUPTED,FINISHED));
            ALLOWED.put(INTERRUPTED, EnumSet.of(RUNNING,FINISHED));
            ALLOWED.put(FINISHED, EnumSet.noneOf(TaskState.class));
        }
        public boolean canTransitionTo(TaskState next) {
            return ALLOWED.getOrDefault(this, EnumSet.noneOf(TaskState.class)).contains(next);
        }
    }
    private volatile TaskState taskState;
    private final Object stateLock = new Object();
    public void setTaskState(TaskState next) {
        if(next == null) throw  new IllegalArgumentException("要达到的下一个状态不存在");
        synchronized(stateLock) {
            TaskState prev = taskState;
//            如果当前无状态，则默认为就绪状态
            if(prev == null) prev = TaskState.PENDING;
//            幂等，重复设置不报错
            if(prev == next) return;
//            检验状态跳转是否符合规则
            if(!prev.canTransitionTo(next)){
                throw new IllegalStateException("状态跳转非法：" + prev + "->" + next);
            }
            this.taskState = next;
        }
    }

    public OllamaTask(OllamaChatRequest chat) {
        client = new OkHttpClient.Builder()
                // 流式建议不要 60s callTimeout；否则可能半路断
                .callTimeout(Duration.ZERO)
                .readTimeout(Duration.ZERO)
                .connectTimeout(Duration.ofSeconds(10))
                .writeTimeout(Duration.ofSeconds(30))
                .build();
        this.chat = chat;
        setTaskState(TaskState.PENDING);
    }
//    获取答案
    public boolean getAnswer() throws Exception {
        if (taskState == TaskState.INTERRUPTED && !full.isEmpty()) {
            ensureSingleResumePrompt();
        }
        setTaskState(TaskState.RUNNING);
        String payload = MAPPER.writeValueAsString(this.chat);
        Request request = new Request.Builder()
                .url(OLLAMA_URL)
                .post(RequestBody.create(payload, MediaType.parse("application/json")))
                .build();
        Call call = client.newCall(request);
        try (Response resp = call.execute()) {
            if (!resp.isSuccessful()) {
                throw new RuntimeException("HTTP " + resp.code() + ": " + (resp.body() == null ? "" : resp.body().string()));
            }
            if (resp.body() == null) throw new RuntimeException("Empty body");

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(resp.body().byteStream(), StandardCharsets.UTF_8))) {
//                本轮生成的内容
                StringBuilder turnDelta = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
//                    中断开关
                    if(taskState == TaskState.INTERRUPTED) {
                        if(!turnDelta.isEmpty()){
                            this.chat.addAssistant(turnDelta.toString());
                        }
                        call.cancel();
                        return false;
                    }

                    if (line.isBlank()) continue;

                    JsonNode json = MAPPER.readTree(line);

                    // 增量片段
                    String delta = json.path("message").path("content").asText("");
                    if (!delta.isEmpty()) {
                        turnDelta.append(delta);           // 累积完整输出
                    }

                    // 结束标志
                    if (json.path("done").asBoolean(false)) {
                        setTaskState(TaskState.FINISHED);
                        if(!turnDelta.isEmpty()){
                            full = turnDelta;
                        }
                        break;
                    }
                }
            }catch (Exception e){
//                interrupt中断造成的异常不算错误
                if (taskState != TaskState.INTERRUPTED) throw e;
            }
        }
        return true;
    }
//    设置中断
    public void interrupt() {
        setTaskState(TaskState.INTERRUPTED);
    }
//    恢复对话指令
    private void ensureSingleResumePrompt() {
        List<OllamaChatRequest.Message> msgs = chat.messages;
        if (msgs == null || msgs.isEmpty()) return;

        // 1. 删除所有旧的恢复指令
        msgs.removeIf(m ->
                "user".equals(m.role) &&
                        interruptString.equals(m.content)
        );
        // 如果最后一条是 assistant 才追加恢复指令
        OllamaChatRequest.Message last = msgs.get(msgs.size() - 1);
        if (!("user".equals(last.role) && interruptString.equals(last.content))) {
            chat.addUser(interruptString);
        }
    }

    public StringBuilder getFull() {
        return this.full;
    }
    public void setFull(StringBuilder full) {
        this.full = full;
    }
}
