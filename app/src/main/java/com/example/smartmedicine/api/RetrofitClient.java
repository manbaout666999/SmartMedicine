package com.example.smartmedicine.api;

import com.example.smartmedicine.util.LogUtils;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.File;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * Retrofit实例管理类（单例模式，统一初始化、拦截器配置）
 */
public class RetrofitClient {
    // 原有健康服务Retrofit实例
    private static Retrofit retrofit;
    // 药品AI服务Retrofit实例（使用 X-AI-API-Key）
    private static Retrofit retrofitMedicineAI;
    // 聊天AI服务Retrofit实例（阿里云百炼，OpenAI兼容，使用 Bearer Token）
    private static Retrofit retrofitChatAI;

    // 私有构造方法（防止外部实例化）
    private RetrofitClient() {}

    /**
     * 获取原有健康服务Retrofit实例
     */
    public static Retrofit getInstance() {
        if (retrofit == null) {
            synchronized (RetrofitClient.class) {
                if (retrofit == null) {
                    OkHttpClient okHttpClient = getOkHttpClient(HeaderType.HEALTH);
                    retrofit = new Retrofit.Builder()
                            .baseUrl(ApiConfig.BASE_URL)
                            .client(okHttpClient)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                }
            }
        }
        return retrofit;
    }

    /**
     * 获取药品AI服务实例（保留原有逻辑）
     */
    public static Retrofit getMedicineAIInstance() {
        if (retrofitMedicineAI == null) {
            synchronized (RetrofitClient.class) {
                if (retrofitMedicineAI == null) {
                    OkHttpClient okHttpClient = getOkHttpClient(HeaderType.MEDICINE_AI);
                    retrofitMedicineAI = new Retrofit.Builder()
                            .baseUrl(ApiConfig.BASE_URL_MEDICINE_AI)
                            .client(okHttpClient)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                }
            }
        }
        return retrofitMedicineAI;
    }

    /**
     * 获取聊天AI服务实例（阿里云百炼，OpenAI兼容）
     */
    public static Retrofit getChatAIInstance() {
        if (retrofitChatAI == null) {
            synchronized (RetrofitClient.class) {
                if (retrofitChatAI == null) {
                    // 聊天 AI 单独使用更长超时（例如 180 秒）
                    OkHttpClient okHttpClient = getOkHttpClient(HeaderType.CHAT_AI, 180_000L);
                    retrofitChatAI = new Retrofit.Builder()
                            .baseUrl(ApiConfig.BASE_URL_CHAT_AI)
                            .client(okHttpClient)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                }
            }
        }
        return retrofitChatAI;
    }

    /**
     * 通用创建Service方法
     */
    public static <T> T createService(Class<T> serviceClass, Retrofit retrofit) {
        return retrofit.create(serviceClass);
    }

    public static <T> T createService(Class<T> serviceClass) {
        return getInstance().create(serviceClass);
    }

    // 枚举区分请求头类型
    private enum HeaderType {
        HEALTH,
        MEDICINE_AI,
        CHAT_AI
    }

    // ==================== OkHttpClient 创建逻辑 ====================

    /**
     * 默认超时版本（用于 HEALTH 和 MEDICINE_AI）
     */
    private static OkHttpClient getOkHttpClient(HeaderType type) {
        return getOkHttpClient(type, ApiConfig.REQUEST_TIMEOUT);
    }

    /**
     * 自定义超时版本（用于 CHAT_AI）
     */
    private static OkHttpClient getOkHttpClient(HeaderType type, long timeoutMs) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .readTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .writeTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .addInterceptor(getLoggingInterceptor())
                // 避免部分服务端对 HTTP/2 支持不稳定导致 "stream was reset: NO_ERROR"
                .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                .retryOnConnectionFailure(true)
                // 添加缓存机制
                .addInterceptor(getCacheInterceptor())
                .cache(getCache());

        switch (type) {
            case HEALTH:
                builder.addInterceptor(getHeaderInterceptor());
                break;
            case MEDICINE_AI:
                builder.addInterceptor(getMedicineAIHeaderInterceptor());
                break;
            case CHAT_AI:
                builder.addInterceptor(getChatAIHeaderInterceptor());
                break;
        }
        return builder.build();
    }

    // ==================== 拦截器 ====================

    /**
     * 健康服务请求头（X-API-Key）
     */
    private static Interceptor getHeaderInterceptor() {
        return chain -> {
            Request originalRequest = chain.request();
            Request newRequest = originalRequest.newBuilder()
                    .addHeader(ApiConfig.HEADER_API_KEY, ApiConfig.API_KEY_VALUE)
                    .addHeader(ApiConfig.HEADER_CONTENT_TYPE, ApiConfig.CONTENT_TYPE_JSON)
                    .build();
            Response response = chain.proceed(newRequest);
            LogUtils.d("Retrofit", "Response Code: " + response.code());
            return response;
        };
    }

    /**
     * 药品AI请求头（X-AI-API-Key）
     */
    private static Interceptor getMedicineAIHeaderInterceptor() {
        return chain -> {
            Request originalRequest = chain.request();
            Request newRequest = originalRequest.newBuilder()
                    .addHeader(ApiConfig.HEADER_AI_API_KEY, ApiConfig.AI_API_KEY_VALUE)
                    .addHeader(ApiConfig.HEADER_CONTENT_TYPE, ApiConfig.CONTENT_TYPE_JSON)
                    .build();
            Response response = chain.proceed(newRequest);
            LogUtils.d("Retrofit-MedicineAI", "Medicine AI Response Code: " + response.code());
            return response;
        };
    }

    /**
     * 聊天AI请求头（Bearer Token for suanli.cn）
     */
    private static Interceptor getChatAIHeaderInterceptor() {
        return chain -> {
            Request originalRequest = chain.request();
            Request newRequest = originalRequest.newBuilder()
                    .addHeader("Authorization", "Bearer " + ApiConfig.CHAT_AI_API_KEY)
                    .addHeader(ApiConfig.HEADER_CONTENT_TYPE, ApiConfig.CONTENT_TYPE_JSON)
                    .build();
            Response response = chain.proceed(newRequest);
            LogUtils.d("Retrofit-ChatAI", "Chat AI Response Code: " + response.code());
            return response;
        };
    }

    /**
     * 日志拦截器
     */
    private static Interceptor getLoggingInterceptor() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(
                message -> LogUtils.d("Retrofit", message)
        );
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return loggingInterceptor;
    }

    /**
     * 缓存拦截器
     */
    private static Interceptor getCacheInterceptor() {
        return chain -> {
            Response response = chain.proceed(chain.request());
            // 缓存时间设置为1小时
            return response.newBuilder()
                    .header("Cache-Control", "public, max-age=3600")
                    .build();
        };
    }

    /**
     * 获取缓存
     */
    private static Cache getCache() {
        // 缓存大小为10MB
        return new Cache(new File(System.getProperty("java.io.tmpdir"), "okhttp-cache"), 10 * 1024 * 1024);
    }

    /**
     * 获取视觉AI模型使用的OkHttpClient
     */
    public static OkHttpClient getOkHttpClientForVL() {
        return new OkHttpClient.Builder()
                .connectTimeout(180, TimeUnit.SECONDS)
                .readTimeout(180, TimeUnit.SECONDS)
                .writeTimeout(180, TimeUnit.SECONDS)
                .addInterceptor(getVLHeaderInterceptor())
                .addInterceptor(getLoggingInterceptor())
                .retryOnConnectionFailure(true)
                .build();
    }

    /**
     * 视觉AI模型请求头拦截器
     */
    private static Interceptor getVLHeaderInterceptor() {
        return chain -> {
            Request originalRequest = chain.request();
            Request newRequest = originalRequest.newBuilder()
                    .addHeader("Authorization", "Bearer " + ApiConfig.CHAT_AI_API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response response = chain.proceed(newRequest);
            LogUtils.d("Retrofit-VL", "VL Response Code: " + response.code());
            return response;
        };
    }
}