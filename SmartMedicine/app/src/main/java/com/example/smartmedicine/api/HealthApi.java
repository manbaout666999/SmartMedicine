package com.example.smartmedicine.api;

import com.example.smartmedicine.base.BaseResponse;
import com.example.smartmedicine.base.HealthData;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * 健康数据相关API接口（Retrofit注解定义）
 */
public interface HealthApi {
    /**
     * 同步健康数据到服务器（支持批量同步）
     * @param request 同步请求体（含用户ID、健康数据列表）
     * @return 同步结果（成功/失败提示）
     */
    @POST(ApiConfig.PATH_SYNC_HEALTH_DATA)
    Call<BaseResponse<String>> syncHealthData(@Body HealthDataSyncRequest request);

    /**
     * 生成健康报告（周/月报告）
     * @param request 报告请求体（含用户ID、报告类型）
     * @return 健康报告（JSON格式字符串）
     */
    @POST(ApiConfig.PATH_GET_HEALTH_REPORT)
    Call<BaseResponse<String>> getHealthReport(@Body HealthReportRequest request);

    /**
     * 健康数据同步请求体
     */
    class HealthDataSyncRequest {
        private String userId;         // 用户唯一标识（模拟登录后获取）
        private List<HealthData> dataList; // 健康数据列表

        public HealthDataSyncRequest(String userId, List<HealthData> dataList) {
            this.userId = userId;
            this.dataList = dataList;
        }

        // Getter & Setter
        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public List<HealthData> getDataList() {
            return dataList;
        }

        public void setDataList(List<HealthData> dataList) {
            this.dataList = dataList;
        }
    }

    /**
     * 健康报告请求体
     */
    class HealthReportRequest {
        public static final String TYPE_WEEK = "week";   // 周报告
        public static final String TYPE_MONTH = "month"; // 月报告

        private String userId;     // 用户唯一标识
        private String reportType; // 报告类型（week/month）

        public HealthReportRequest(String userId, String reportType) {
            this.userId = userId;
            this.reportType = reportType;
        }

        // Getter & Setter
        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getReportType() {
            return reportType;
        }

        public void setReportType(String reportType) {
            this.reportType = reportType;
        }
    }
}