package thinking.Control;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import thinking.model.OllamaChatRequest;
import okhttp3.*;
import thinking.model.OllamaResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class OllamaTask {
    private static final String OLLAMA_URL = "http://localhost:14735/api/chat";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String interruptString = "你上一条回复被中断了。请参考中断的回复内容，为我重新生成一个完整的内容"
            ;
    OkHttpClient client;
    OllamaChatRequest chat;
    StringBuilder full= new StringBuilder();
    AtomicBoolean canceled;
    Call call;
    public OllamaTask() {
        client = new OkHttpClient();
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
    }
//    获取答案
    public OllamaResult getAnswer() throws IOException {
        if (canceled.get()) {
            ensureSingleResumePrompt();
            canceled.set(false);
        }
        String payload = MAPPER.writeValueAsString(this.chat);
        Request request = new Request.Builder()
                .url(OLLAMA_URL)
                .post(RequestBody.create(payload, MediaType.parse("application/json")))
                .build();
        call = client.newCall(request);
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
                    if(canceled.get()) {
                        if(!turnDelta.isEmpty()){
                            this.chat.addAssistant(turnDelta.toString());
                        }
                        call.cancel();
                        return new OllamaResult(false,true,turnDelta.toString());
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
                        if(!turnDelta.isEmpty()){
                            full = turnDelta;
                        }
                        break;
                    }
                }
            }catch (Exception e){
//                interrupt中断造成的异常不算错误
                if (!canceled.get()) throw e;
            }
        }
        return new OllamaResult(true,false,full.toString());
    }
//    设置中断
    public void interrupt() {
        Call call = this.call;
        if(call != null) {
//            将堵塞在读数据的call强制唤醒并取消当前进程
            call.cancel();
        }
        canceled.set(true);
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
}
