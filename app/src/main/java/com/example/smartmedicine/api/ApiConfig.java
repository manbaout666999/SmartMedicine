package com.example.smartmedicine.api;

/**
 * API常量配置类（集中管理BaseURL、接口路径、请求参数等）
 */
public class ApiConfig {

    // ========== 原有健康服务 ==========
    public static final String BASE_URL = "https://api.suanli.cn/v1/";
    public static final String PATH_RECOGNIZE_MEDICINE = "medicine/recognize";
    public static final String PATH_QUERY_MEDICINE = "medicine/query";
    public static final String PATH_SYNC_HEALTH_DATA = "health/sync";
    public static final String PATH_GET_HEALTH_REPORT = "health/report";
    // 阿里云百炼（北京地域，OpenAI 兼容）
    public static final String BASE_URL_CHAT_AI = "https://dashscope.aliyuncs.com/compatible-mode/v1/";
    public static final String CHAT_AI_API_KEY = "sk-61a23015df6e46f3847a0b5fcd24d106";

    // ========== 药品专用AI服务（私有）==========
    public static final String BASE_URL_MEDICINE_AI = "https://api.suanli.cn/v1/";
    public static final String PATH_AI_ANALYZE_MEDICINE = "medicine/analyze";
    public static final String AI_API_KEY_VALUE = "sk-1Hk1vVdqor1VHA2Pbu0xikpZMNG8hc1rPeMbtdXGUhq8V9F0";
    public static final String HEADER_AI_API_KEY = "X-AI-API-Key";

    // ========== 通用聊天AI服务（阿里云百炼，OpenAI兼容）==========
    public static final String PATH_AI_CHAT = "chat/completions"; // 注意：不要加 / 开头！Retrofit 会自动拼接

    // ========== 通用配置 ==========
    // 图片识别、健康数据同步等可能较慢，这里统一给到 60 秒
    public static final int REQUEST_TIMEOUT = 60 * 1000;
    // 注意：HEADER_API_KEY 是“请求头名称”，不要写成密钥本身
    public static final String HEADER_API_KEY = "X-API-Key";
    public static final String API_KEY_VALUE = "sk-1Hk1vVdqor1VHA2Pbu0xikpZMNG8hc1rPeMbtdXGUhq8V9F0";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_TYPE_JSON = "application/json";

    public static final int ERROR_CODE_NETWORK = 1001;    // 网络错误
    public static final int ERROR_CODE_API = 1002;        // 接口业务错误
    public static final int ERROR_CODE_PARSE = 1003;      // 数据解析错误
    public static final int ERROR_CODE_IMAGE = 1004;      // 图片处理错误
}