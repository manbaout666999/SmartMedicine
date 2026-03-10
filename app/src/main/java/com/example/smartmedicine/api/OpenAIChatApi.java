// OpenAIChatApi.java
package com.example.smartmedicine.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

import java.util.List;
import java.util.Map;

public interface OpenAIChatApi {

    @POST("chat/completions")
    Call<ChatCompletionResponse> chat(@Body ChatCompletionRequest request);

    @POST("chat/completions")
    Call<ChatCompletionResponse> chatStream(@Body ChatCompletionRequest request);

    // 请求体
    class ChatCompletionRequest {
        private String model;
        private List<Message> messages;
        private boolean stream;

        public ChatCompletionRequest(String model, List<Message> messages) {
            this(model, messages, false);
        }

        public ChatCompletionRequest(String model, List<Message> messages, boolean stream) {
            this.model = model;
            this.messages = messages;
            this.stream = stream;
        }

        // Getters（Retrofit 需要）
        public String getModel() { return model; }
        public List<Message> getMessages() { return messages; }
        public boolean isStream() { return stream; }
    }

    // 消息（支持纯文本和多模态内容）
    class Message {
        private String role;
        private Object content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public Message(String role, List<Map<String, Object>> content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() { return role; }
        public Object getContent() { return content; }
    }

    // 响应体
    class ChatCompletionResponse {
        private List<Choice> choices;

        public List<Choice> getChoices() { return choices; }
    }

    class Choice {
        private TextMessage message;

        public TextMessage getMessage() { return message; }
    }

    class TextMessage {
        private String role;
        private String content;

        public String getRole() { return role; }
        public String getContent() { return content; }
    }
}