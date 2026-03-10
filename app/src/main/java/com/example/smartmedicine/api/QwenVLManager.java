package com.example.smartmedicine.api;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QwenVLManager {
    private static final String TAG = "QwenVLManager";
    private static final OpenAIChatApi chatApi;

    static {
        chatApi = RetrofitClient.createService(OpenAIChatApi.class, RetrofitClient.getChatAIInstance());
    }

    public interface OnVLCallback {
        void onSuccess(String medicineName, String dosage);
        void onError(String errorMsg);
    }

    public interface OnMedicineDetailCallback {
        void onSuccess(String medicineName, String dosage, String details, String suggestedTime);
        void onError(String errorMsg);
    }

    public static void recognizeAndGetDetails(File imageFile, final OnMedicineDetailCallback callback) {
        if (imageFile == null || !imageFile.exists()) {
            callback.onError("图片文件不存在");
            return;
        }

        recognizeMedicine(imageFile, new OnVLCallback() {
            @Override
            public void onSuccess(String medicineName, String dosage) {
                if (medicineName != null && !medicineName.isEmpty()) {
                    getMedicineDetails(medicineName, dosage, callback);
                } else {
                    callback.onError("未能识别药品名称");
                }
            }

            @Override
            public void onError(String errorMsg) {
                callback.onError(errorMsg);
            }
        });
    }

    private static void recognizeMedicine(File imageFile, final OnVLCallback callback) {
        try {
            String base64Image = encodeImageToBase64(imageFile);
            String imageUrl = "data:image/jpeg;base64," + base64Image;

            List<OpenAIChatApi.Message> messages = new ArrayList<>();

            List<Map<String, Object>> contentList = new ArrayList<>();
            
            Map<String, Object> textContent = new HashMap<>();
            textContent.put("type", "text");
            textContent.put("text", "请识别这张药盒图片，只告诉我药品的名称，不要其他信息。");
            contentList.add(textContent);

            Map<String, Object> imageContent = new HashMap<>();
            imageContent.put("type", "image_url");
            Map<String, String> imageUrlMap = new HashMap<>();
            imageUrlMap.put("url", imageUrl);
            imageContent.put("image_url", imageUrlMap);
            contentList.add(imageContent);

            messages.add(new OpenAIChatApi.Message("user", contentList));

            OpenAIChatApi.ChatCompletionRequest request = new OpenAIChatApi.ChatCompletionRequest(
                    "qwen-vl-max",
                    messages
            );

            Call<OpenAIChatApi.ChatCompletionResponse> call = chatApi.chat(request);
            call.enqueue(new Callback<OpenAIChatApi.ChatCompletionResponse>() {
                @Override
                public void onResponse(Call<OpenAIChatApi.ChatCompletionResponse> call, Response<OpenAIChatApi.ChatCompletionResponse> response) {
                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            OpenAIChatApi.ChatCompletionResponse body = response.body();
                            if (body.getChoices() != null && !body.getChoices().isEmpty()) {
                                OpenAIChatApi.Choice firstChoice = body.getChoices().get(0);
                                if (firstChoice.getMessage() != null) {
                                    String content = firstChoice.getMessage().getContent();
                                    if (content != null && !content.trim().isEmpty()) {
                                        Log.d(TAG, "VL Response: " + content);
                                        callback.onSuccess(content.trim(), "");
                                        return;
                                    }
                                }
                            }
                            callback.onError("未能识别药品名称");
                        } else {
                            String errorMsg = "VL API失败: " + response.code();
                            if (response.errorBody() != null) {
                                errorMsg += " - " + response.errorBody().string();
                            }
                            Log.e(TAG, errorMsg);
                            callback.onError(errorMsg);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "解析响应失败", e);
                        callback.onError("解析失败: " + e.getMessage());
                    }
                }

                @Override
                public void onFailure(Call<OpenAIChatApi.ChatCompletionResponse> call, Throwable t) {
                    Log.e(TAG, "网络请求失败", t);
                    callback.onError("网络失败: " + t.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "准备请求失败", e);
            callback.onError("准备失败: " + e.getMessage());
        }
    }

    private static void getMedicineDetails(String medicineName, String initialDosage, final OnMedicineDetailCallback callback) {
        String userMessage = "请提供关于\"" + medicineName + "\"的详细信息，包括：\n" +
                           "1. 药品名称\n" +
                           "2. 主要适用症\n" +
                           "3. 用法用量（例如：每日2次，每次1片）\n" +
                           "4. 建议的服用时间（例如：早8:00，晚8:00，只返回具体时间，格式为HH:MM）\n" +
                           "5. 注意事项\n" +
                           "请以清晰的格式返回，使用中文回答。";

        AIChatManager.sendChatMessage(userMessage, new AIChatManager.OnChatCallback() {
            @Override
            public void onSuccess(String reply) {
                String dosage = extractDosageFromText(reply);
                if (dosage == null || dosage.isEmpty()) {
                    dosage = initialDosage.isEmpty() ? "请按说明服用" : initialDosage;
                }
                String suggestedTime = extractSuggestedTimeFromText(reply);
                callback.onSuccess(medicineName, dosage, reply, suggestedTime);
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                Log.e(TAG, "获取药品详细信息失败: " + errorMsg);
                String dosage = initialDosage.isEmpty() ? "请按说明服用" : initialDosage;
                callback.onSuccess(medicineName, dosage, "", "");
            }
        });
    }

    private static String extractSuggestedTimeFromText(String text) {
        String[] timeKeywords = {"服用时间", "建议时间", "时间", "早", "晚", "中午", "上午", "下午"};
        for (String keyword : timeKeywords) {
            int index = text.indexOf(keyword);
            if (index != -1) {
                int start = Math.max(0, index - 10);
                int end = Math.min(text.length(), index + 30);
                String segment = text.substring(start, end);
                
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d{1,2}):(\\d{2})");
                java.util.regex.Matcher matcher = pattern.matcher(segment);
                if (matcher.find()) {
                    return matcher.group(1) + ":" + matcher.group(2);
                }
            }
        }
        return "";
    }

    private static String encodeImageToBase64(File file) throws IOException {
        FileInputStream fis = null;
        ByteArrayOutputStream baos = null;
        try {
            fis = new FileInputStream(file);
            baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            byte[] bytes = baos.toByteArray();
            return android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ignored) {}
            }
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException ignored) {}
            }
        }
    }

    private static String extractDosageFromText(String text) {
        String[] keywords = {"用法用量", "用量", "服用", "剂量", "用法"};
        for (String keyword : keywords) {
            int index = text.indexOf(keyword);
            if (index != -1) {
                int start = index + keyword.length();
                int end = text.indexOf("\n", start);
                if (end == -1) {
                    end = text.indexOf("。", start);
                }
                if (end == -1) {
                    end = text.indexOf("!", start);
                }
                if (end == -1) {
                    end = text.length();
                }
                String dosage = text.substring(start, end).trim();
                dosage = dosage.replaceAll("[:：]", "").trim();
                if (!dosage.isEmpty() && dosage.length() < 50) {
                    return dosage;
                }
            }
        }
        return "";
    }
}
