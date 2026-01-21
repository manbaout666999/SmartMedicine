package com.example.smartmedicine.api;

import com.example.smartmedicine.base.BaseResponse;
import com.example.smartmedicine.base.MedicineInfo;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * 药品相关API接口（Retrofit注解定义）
 */
public interface MedicineApi {
    // 原有接口（保留）
    @POST(ApiConfig.PATH_RECOGNIZE_MEDICINE)
    Call<BaseResponse<MedicineInfo>> recognizeMedicine(@Body MedicineRecognizeRequest request);

    @POST(ApiConfig.PATH_QUERY_MEDICINE)
    Call<BaseResponse<MedicineInfo>> queryMedicineInfo(@Body MedicineQueryRequest request);

    // ========== 新增AI药品分析接口 ==========
    /**
     * AI分析药品信息（判断药品适用症、禁忌、服用建议等）
     * @param request AI分析请求体（含药品名称/识别结果、用户健康数据）
     * @return AI分析结果（药品建议、风险提示等）
     */
    @POST(ApiConfig.PATH_AI_ANALYZE_MEDICINE)
    Call<BaseResponse<MedicineAIAnalysisResponse>> analyzeMedicineByAI(@Body MedicineAIAnalysisRequest request);

    // 原有请求体（保留）
    class MedicineRecognizeRequest {
        private String imageBase64;

        public MedicineRecognizeRequest(String imageBase64) {
            this.imageBase64 = imageBase64;
        }

        public String getImageBase64() {
            return imageBase64;
        }

        public void setImageBase64(String imageBase64) {
            this.imageBase64 = imageBase64;
        }
    }

    class MedicineQueryRequest {
        private String medicineName;

        public MedicineQueryRequest(String medicineName) {
            this.medicineName = medicineName;
        }

        public String getMedicineName() {
            return medicineName;
        }

        public void setMedicineName(String medicineName) {
            this.medicineName = medicineName;
        }
    }

    // ========== 新增AI相关请求/响应模型 ==========
    /**
     * AI药品分析请求体
     */
    class MedicineAIAnalysisRequest {
        private String userId;          // 用户ID（关联健康数据）
        private String medicineName;    // 药品名称（来自识别/查询结果）
        private String medicineId;      // 药品唯一标识（可选）
        private String userHealthTag;   // 用户健康标签（如高血压、糖尿病）

        public MedicineAIAnalysisRequest(String userId, String medicineName, String userHealthTag) {
            this.userId = userId;
            this.medicineName = medicineName;
            this.userHealthTag = userHealthTag;
        }

        // Getter & Setter
        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getMedicineName() {
            return medicineName;
        }

        public void setMedicineName(String medicineName) {
            this.medicineName = medicineName;
        }

        public String getMedicineId() {
            return medicineId;
        }

        public void setMedicineId(String medicineId) {
            this.medicineId = medicineId;
        }

        public String getUserHealthTag() {
            return userHealthTag;
        }

        public void setUserHealthTag(String userHealthTag) {
            this.userHealthTag = userHealthTag;
        }
    }

    /**
     * AI药品分析响应体
     */
    class MedicineAIAnalysisResponse {
        private String medicineName;    // 药品名称
        private String indication;      // 适用症
        private String contraindication;// 禁忌
        private String takeAdvice;      // 服用建议
        private String riskTip;         // 风险提示（针对用户健康标签）
        private int confidence;         // AI分析置信度（0-100）

        // Getter & Setter
        public String getMedicineName() {
            return medicineName;
        }

        public void setMedicineName(String medicineName) {
            this.medicineName = medicineName;
        }

        public String getIndication() {
            return indication;
        }

        public void setIndication(String indication) {
            this.indication = indication;
        }

        public String getContraindication() {
            return contraindication;
        }

        public void setContraindication(String contraindication) {
            this.contraindication = contraindication;
        }

        public String getTakeAdvice() {
            return takeAdvice;
        }

        public void setTakeAdvice(String takeAdvice) {
            this.takeAdvice = takeAdvice;
        }

        public String getRiskTip() {
            return riskTip;
        }

        public void setRiskTip(String riskTip) {
            this.riskTip = riskTip;
        }

        public int getConfidence() {
            return confidence;
        }

        public void setConfidence(int confidence) {
            this.confidence = confidence;
        }
    }
}