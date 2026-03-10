// OpenAIChatManager.java
package com.example.smartmedicine.api;

import com.example.smartmedicine.base.BaseResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OpenAIChatManager {

    private static final OpenAIChatApi chatApi;

    static {
        // 使用阿里云百炼 Retrofit 实例
        chatApi = RetrofitClient.createService(
                OpenAIChatApi.class,
                RetrofitClient.getChatAIInstance()
        );
    }

    /**
     * 发送聊天消息到阿里云百炼（OpenAI 兼容接口）
     */
    public static void sendChatMessage(String userMessage, final OnChatCallback callback) {
        java.util.List<OpenAIChatApi.Message> messages = java.util.Arrays.asList(
                new OpenAIChatApi.Message("system", "你是一个专业的医疗助手，请基于医学知识提供准确、安全的用药建议。"),
                new OpenAIChatApi.Message("user", userMessage)
        );
        OpenAIChatApi.ChatCompletionRequest request = new OpenAIChatApi.ChatCompletionRequest(
                "qwen3.5-plus",
                messages
        );

        Call<OpenAIChatApi.ChatCompletionResponse> call = chatApi.chat(request);
        call.enqueue(new Callback<OpenAIChatApi.ChatCompletionResponse>() {
            @Override
            public void onResponse(Call<OpenAIChatApi.ChatCompletionResponse> call, Response<OpenAIChatApi.ChatCompletionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String reply = response.body().getChoices().get(0).getMessage().getContent();
                    callback.onSuccess(reply);
                } else {
                    String errorMsg = "HTTP " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += ": " + response.errorBody().string();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    callback.onError(ApiConfig.ERROR_CODE_API, errorMsg);
                }
            }

            @Override
            public void onFailure(Call<OpenAIChatApi.ChatCompletionResponse> call, Throwable t) {
                callback.onError(ApiConfig.ERROR_CODE_NETWORK, t.getMessage());
            }
        });
    }

    public interface OnChatCallback {
        void onSuccess(String reply);
        void onError(int errorCode, String errorMsg);
    }
}