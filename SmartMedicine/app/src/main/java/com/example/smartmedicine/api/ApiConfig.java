package com.example.smartmedicine.api;

/**
 * API常量配置类（集中管理BaseURL、接口路径、请求参数等）
 */
public class ApiConfig {
    // 基础URL（模拟AI健康管家API服务地址，可替换为实际接口地址）
    public static final String BASE_URL = "https://api.smarthealth.oppo.com/ai-health/";

    // ========== 新增AI服务配置 ==========
    // AI药品分析服务BaseURL（替换为实际AI服务地址）
    public static final String BASE_URL_AI = "https://ai-api.smarthealth.oppo.com/medicine-ai/";
    // AI药品判断接口路径
    public static final String PATH_AI_ANALYZE_MEDICINE = "medicine/analyze";
    // AI服务API密钥（若有）
    public static final String AI_API_KEY_VALUE = "OPPO_MEDICINE_AI_2026";
    public static final String HEADER_AI_API_KEY = "X-AI-API-Key";

    // 原有配置（保留）
    public static final String PATH_RECOGNIZE_MEDICINE = "medicine/recognize"; // 药品识别接口
    public static final String PATH_QUERY_MEDICINE = "medicine/query";         // 药品信息查询接口
    public static final String PATH_SYNC_HEALTH_DATA = "health/sync";          // 健康数据同步接口
    public static final String PATH_GET_HEALTH_REPORT = "health/report";       // 健康报告生成接口

    public static final int REQUEST_TIMEOUT = 10 * 1000;

    public static final String HEADER_API_KEY = "X-API-Key";
    public static final String API_KEY_VALUE = "OPPO_AI_HEALTH_2026_CONTEST";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_TYPE_JSON = "application/json";

    public static final int ERROR_CODE_NETWORK = 1001;    // 网络错误
    public static final int ERROR_CODE_API = 1002;        // 接口业务错误
    public static final int ERROR_CODE_PARSE = 1003;      // 数据解析错误
    public static final int ERROR_CODE_IMAGE = 1004;      // 图片处理错误
    // ========== 新增结束 ==========
}