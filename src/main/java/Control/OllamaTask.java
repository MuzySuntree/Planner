package Control;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.OllamaChatRequest;
import okhttp3.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class OllamaTask {
    private static final String OLLAMA_URL = "http://localhost:14735/api/chat";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private volatile boolean interrupt = false;
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
        FINISHED,
    }
    private TaskState taskState;

    public OllamaTask(OllamaChatRequest chat) {
        client = new OkHttpClient.Builder()
                // 流式建议不要 60s callTimeout；否则可能半路断
                .callTimeout(Duration.ofSeconds(0))
                .build();
        this.chat = chat;
        taskState = TaskState.PENDING;
    }
//    获取答案
    public boolean getAnswer() throws Exception {
        if (taskState == TaskState.INTERRUPTED) {
            this.chat.addUser("你上一条回复被中断了，请从中断处继续，不要重复已输出部分");
            interrupt = false;
        }
        taskState = TaskState.RUNNING;
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
                    if(interrupt){
//                        设置中断状态
                        this.taskState = TaskState.INTERRUPTED;
                        if(!turnDelta.isEmpty()){
                            this.chat.addAssistant(turnDelta.toString());
                            full.append(turnDelta);
                        }
                        call.cancel();
                        return false;
                    }

                    if (line.isBlank()) continue;

                    JsonNode json = MAPPER.readTree(line);

                    // 增量片段
                    String delta = json.path("message").path("content").asText("");
                    if (!delta.isEmpty()) {
                        System.out.print(delta);      // 逐段打印（流式效果）
                        turnDelta.append(delta);           // 累积完整输出
                    }

                    // 结束标志
                    if (json.path("done").asBoolean(false)) {
                        this.taskState = TaskState.FINISHED;
                        if(!turnDelta.isEmpty()){
                            this.chat.addAssistant(turnDelta.toString());
                            full.append(turnDelta);
                        }
                        break;
                    }
                }
            }catch (Exception e){
//                interrupt中断造成的异常不算错误
                if (!interrupt) throw e;
            }
        }
        return true;
    }
//    设置中断
    public void interrupt() {
        this.interrupt = true;
    }
}
