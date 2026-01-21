package com.example.smartmedicine.api;

import com.example.smartmedicine.base.BaseResponse;
// 步骤1-1：删除未使用的import（解决“未使用的import语句”警告）
// import com.example.smartmedicine.base.MedicineInfo;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 药品AI分析管理类（封装AI接口调用逻辑）
 */
public class MedicineAIManager {
    // 步骤1-2：添加final修饰符（解决“字段可能为final”警告）
    private static final MedicineApi aiMedicineApi;

    // 初始化AI接口实例
    static {
        aiMedicineApi = RetrofitClient.createService(MedicineApi.class, RetrofitClient.getAIInstance());
    }

    /**
     * 调用AI分析药品（异步回调）
     * @param userId 用户ID
     * @param medicineName 药品名称
     * @param healthTag 用户健康标签（如"高血压"）
     * @param callback 结果回调
     */
    public static void analyzeMedicine(String userId, String medicineName, String healthTag,
                                       final OnAIAnalysisCallback callback) {
        // 构建AI请求体
        MedicineApi.MedicineAIAnalysisRequest request =
                new MedicineApi.MedicineAIAnalysisRequest(userId, medicineName, healthTag);
        // 调用AI接口
        Call<BaseResponse<MedicineApi.MedicineAIAnalysisResponse>> call =
                aiMedicineApi.analyzeMedicineByAI(request);
        call.enqueue(new Callback<BaseResponse<MedicineApi.MedicineAIAnalysisResponse>>() {
            @Override
            public void onResponse(Call<BaseResponse<MedicineApi.MedicineAIAnalysisResponse>> call,
                                   Response<BaseResponse<MedicineApi.MedicineAIAnalysisResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<MedicineApi.MedicineAIAnalysisResponse> baseResp = response.body();
                    // 步骤1-3：将getMessage()改为getMsg()（解决核心编译错误）
                    if (baseResp.isSuccess()) {
                        callback.onSuccess(baseResp.getData());
                    } else {
                        callback.onError(baseResp.getCode(), baseResp.getMsg());
                    }
                } else {
                    callback.onError(ApiConfig.ERROR_CODE_API, "AI接口响应失败");
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<MedicineApi.MedicineAIAnalysisResponse>> call, Throwable t) {
                callback.onError(ApiConfig.ERROR_CODE_NETWORK, "网络错误：" + t.getMessage());
            }
        });
    }

    // AI分析回调接口
    public interface OnAIAnalysisCallback {
        void onSuccess(MedicineApi.MedicineAIAnalysisResponse analysisResult);
        void onError(int errorCode, String errorMsg);
    }
}