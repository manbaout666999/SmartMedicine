// AIChatApi.java
package com.example.smartmedicine.api;

import com.example.smartmedicine.base.BaseResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AIChatApi {

    /**
     * AI 聊天对话接口
     */
    @POST(ApiConfig.PATH_AI_CHAT)
    Call<BaseResponse<String>> chat(@Body ChatRequest request);

    class ChatRequest {
        private String userId;
        private String message;
        private String sessionId; // 可选，用于上下文

        public ChatRequest(String userId, String message) {
            this.userId = userId;
            this.message = message;
            this.sessionId = "default"; // 简化处理
        }

        // Getters
        public String getUserId() { return userId; }
        public String getMessage() { return message; }
        public String getSessionId() { return sessionId; }
    }
}