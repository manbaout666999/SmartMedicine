package com.example.smartmedicine.api;

import android.util.Log;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AIChatManager {

    private static final String TAG = "AIChatManager";
    private static final OpenAIChatApi chatApi;
    // 消息历史管理
    private static java.util.Map<String, java.util.List<OpenAIChatApi.Message>> messageHistoryMap = new java.util.HashMap<>();
    // 最大历史消息数
    private static final int MAX_HISTORY_MESSAGES = 10;

    static {
        chatApi = RetrofitClient.createService(OpenAIChatApi.class, RetrofitClient.getChatAIInstance());
    }

    // 模型类型枚举
    public enum ModelType {
        LIGHT("qwen-turbo", "轻量模型"),
        STANDARD("qwen-plus", "标准模型"),
        PREMIUM("qwen-max", "高级模型");

        private final String modelName;
        private final String description;

        ModelType(String modelName, String description) {
            this.modelName = modelName;
            this.description = description;
        }

        public String getModelName() {
            return modelName;
        }

        public String getDescription() {
            return description;
        }
    }

    // 默认模型
    private static ModelType defaultModel = ModelType.LIGHT;

    public static void setDefaultModel(ModelType model) {
        defaultModel = model;
    }

    public static ModelType getDefaultModel() {
        return defaultModel;
    }

    // 消息历史管理方法
    public static void addMessageToHistory(String sessionId, String role, String content) {
        if (!messageHistoryMap.containsKey(sessionId)) {
            messageHistoryMap.put(sessionId, new java.util.ArrayList<>());
        }
        java.util.List<OpenAIChatApi.Message> history = messageHistoryMap.get(sessionId);
        history.add(new OpenAIChatApi.Message(role, content));
        
        // 限制历史消息数量
        if (history.size() > MAX_HISTORY_MESSAGES) {
            history.subList(0, history.size() - MAX_HISTORY_MESSAGES).clear();
        }
    }

    public static java.util.List<OpenAIChatApi.Message> getMessageHistory(String sessionId) {
        if (!messageHistoryMap.containsKey(sessionId)) {
            return new java.util.ArrayList<>();
        }
        return java.util.Collections.unmodifiableList(messageHistoryMap.get(sessionId));
    }

    public static void clearMessageHistory(String sessionId) {
        messageHistoryMap.remove(sessionId);
    }

    public static void clearAllMessageHistory() {
        messageHistoryMap.clear();
    }

    public static void sendChatMessage(String userMessage, final OnChatCallback callback) {
        sendChatMessage(userMessage, defaultModel, callback);
    }

    public static void sendChatMessage(String userMessage, ModelType modelType, final OnChatCallback callback) {
        sendChatMessage("default", userMessage, modelType, callback);
    }

    public static void sendChatMessage(String sessionId, String userMessage, final OnChatCallback callback) {
        sendChatMessage(sessionId, userMessage, defaultModel, callback);
    }

    public static void sendChatMessage(String sessionId, String userMessage, ModelType modelType, final OnChatCallback callback) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            callback.onError(ApiConfig.ERROR_CODE_API, "消息内容不能为空");
            return;
        }

        String model = modelType.getModelName();

        // 构建消息列表，包含系统消息、历史消息和当前消息
        java.util.List<OpenAIChatApi.Message> messages = new java.util.ArrayList<>();
        messages.add(new OpenAIChatApi.Message("system", "你是一个专业的医疗助手，请基于医学知识提供准确、安全的用药建议。"));
        messages.addAll(getMessageHistory(sessionId));
        messages.add(new OpenAIChatApi.Message("user", userMessage));

        OpenAIChatApi.ChatCompletionRequest request = new OpenAIChatApi.ChatCompletionRequest(model, messages);

        Call<OpenAIChatApi.ChatCompletionResponse> call = chatApi.chat(request);
        call.enqueue(new Callback<OpenAIChatApi.ChatCompletionResponse>() {
            @Override
            public void onResponse(Call<OpenAIChatApi.ChatCompletionResponse> call, Response<OpenAIChatApi.ChatCompletionResponse> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        OpenAIChatApi.ChatCompletionResponse body = response.body();
                        // 🔒 逐层空值检查（防止崩溃）
                        if (body.getChoices() != null && !body.getChoices().isEmpty()) {
                            OpenAIChatApi.Choice firstChoice = body.getChoices().get(0);
                            if (firstChoice.getMessage() != null) {
                                String content = firstChoice.getMessage().getContent();
                                if (content != null && !content.trim().isEmpty()) {
                                    // 将AI回复添加到消息历史
                                    addMessageToHistory(sessionId, "assistant", content.trim());
                                    callback.onSuccess(content.trim());
                                    return;
                                }
                            }
                        }
                        Log.e(TAG, "AI 返回结果异常: choices 或 message 为空");
                        callback.onError(ApiConfig.ERROR_CODE_PARSE, "AI 返回内容为空或格式错误");
                    } else {
                        // 处理 HTTP 错误（如 401/503）
                        String errorMsg = "请求失败: HTTP " + response.code();
                        if (response.errorBody() != null) {
                            try {
                                String errorBody = response.errorBody().string();
                                Log.e(TAG, "API Error: " + errorBody);
                                errorMsg += " - " + errorBody;
                            } catch (Exception e) {
                                Log.e(TAG, "读取 errorBody 失败", e);
                            }
                        }
                        callback.onError(ApiConfig.ERROR_CODE_API, errorMsg);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "解析响应异常", e);
                    callback.onError(ApiConfig.ERROR_CODE_PARSE, "内部错误: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<OpenAIChatApi.ChatCompletionResponse> call, Throwable t) {
                Log.e(TAG, "网络请求失败", t);
                String msg = "网络连接失败";
                if (t.getMessage() != null) {
                    msg += ": " + t.getMessage();
                }
                callback.onError(ApiConfig.ERROR_CODE_NETWORK, msg);
            }
        });
    }

    public static void sendChatMessageStream(String userMessage, final OnChatStreamCallback callback) {
        sendChatMessageStream("default", userMessage, defaultModel, callback);
    }

    public static void sendChatMessageStream(String userMessage, ModelType modelType, final OnChatStreamCallback callback) {
        sendChatMessageStream("default", userMessage, modelType, callback);
    }

    public static void sendChatMessageStream(String sessionId, String userMessage, final OnChatStreamCallback callback) {
        sendChatMessageStream(sessionId, userMessage, defaultModel, callback);
    }

    public static void sendChatMessageStream(String sessionId, String userMessage, ModelType modelType, final OnChatStreamCallback callback) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            callback.onError(ApiConfig.ERROR_CODE_API, "消息内容不能为空");
            return;
        }

        String model = modelType.getModelName();

        // 构建消息列表，包含系统消息、历史消息和当前消息
        java.util.List<OpenAIChatApi.Message> messages = new java.util.ArrayList<>();
        messages.add(new OpenAIChatApi.Message("system", "你是一个专业的医疗助手，请基于医学知识提供准确、安全的用药建议。"));
        messages.addAll(getMessageHistory(sessionId));
        messages.add(new OpenAIChatApi.Message("user", userMessage));

        OpenAIChatApi.ChatCompletionRequest request = new OpenAIChatApi.ChatCompletionRequest(model, messages, true);

        Call<OpenAIChatApi.ChatCompletionResponse> call = chatApi.chatStream(request);
        call.enqueue(new Callback<OpenAIChatApi.ChatCompletionResponse>() {
            @Override
            public void onResponse(Call<OpenAIChatApi.ChatCompletionResponse> call, Response<OpenAIChatApi.ChatCompletionResponse> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        OpenAIChatApi.ChatCompletionResponse body = response.body();
                        // 🔒 逐层空值检查（防止崩溃）
                        if (body.getChoices() != null && !body.getChoices().isEmpty()) {
                            OpenAIChatApi.Choice firstChoice = body.getChoices().get(0);
                            if (firstChoice.getMessage() != null) {
                                String content = firstChoice.getMessage().getContent();
                                if (content != null && !content.trim().isEmpty()) {
                                    // 将AI回复添加到消息历史
                                    addMessageToHistory(sessionId, "assistant", content.trim());
                                    callback.onSuccess(content.trim());
                                    return;
                                }
                            }
                        }
                        Log.e(TAG, "AI 返回结果异常: choices 或 message 为空");
                        callback.onError(ApiConfig.ERROR_CODE_PARSE, "AI 返回内容为空或格式错误");
                    } else {
                        // 处理 HTTP 错误（如 401/503）
                        String errorMsg = "请求失败: HTTP " + response.code();
                        if (response.errorBody() != null) {
                            try {
                                String errorBody = response.errorBody().string();
                                Log.e(TAG, "API Error: " + errorBody);
                                errorMsg += " - " + errorBody;
                            } catch (Exception e) {
                                Log.e(TAG, "读取 errorBody 失败", e);
                            }
                        }
                        callback.onError(ApiConfig.ERROR_CODE_API, errorMsg);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "解析响应异常", e);
                    callback.onError(ApiConfig.ERROR_CODE_PARSE, "内部错误: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<OpenAIChatApi.ChatCompletionResponse> call, Throwable t) {
                Log.e(TAG, "网络请求失败", t);
                String msg = "网络连接失败";
                if (t.getMessage() != null) {
                    msg += ": " + t.getMessage();
                }
                callback.onError(ApiConfig.ERROR_CODE_NETWORK, msg);
            }
        });
    }

    public interface OnChatCallback {
        void onSuccess(String reply);
        void onError(int errorCode, String errorMsg);
    }

    public interface OnChatStreamCallback {
        void onStreamData(String chunk);
        void onSuccess(String fullResponse);
        void onError(int errorCode, String errorMsg);
    }
}