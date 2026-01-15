package com.example.smartmedicine.api;

import android.util.Log;

import com.example.smartmedicine.util.LogUtils;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Retrofit实例管理类（单例模式，统一初始化、拦截器配置）
 */
public class RetrofitClient {
    // 原有健康服务Retrofit实例
    private static Retrofit retrofit;
    // AI服务Retrofit实例（新增）
    private static Retrofit retrofitAI;
    private static OkHttpClient okHttpClient;

    // 私有构造方法（防止外部实例化）
    private RetrofitClient() {}

    /**
     * 获取原有健康服务Retrofit实例（双重校验锁单例）
     */
    public static Retrofit getInstance() {
        if (retrofit == null) {
            synchronized (RetrofitClient.class) {
                if (retrofit == null) {
                    okHttpClient = getOkHttpClient(false); // 不添加AI请求头
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
     * 新增：获取AI服务的Retrofit实例（独立BaseURL+AI请求头）
     */
    public static Retrofit getAIInstance() {
        if (retrofitAI == null) {
            synchronized (RetrofitClient.class) {
                if (retrofitAI == null) {
                    okHttpClient = getOkHttpClient(true); // 添加AI请求头
                    retrofitAI = new Retrofit.Builder()
                            .baseUrl(ApiConfig.BASE_URL_AI) // AI服务独立BaseURL
                            .client(okHttpClient)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                }
            }
        }
        return retrofitAI;
    }

    /**
     * 新增：通用创建Service方法（支持指定Retrofit实例）
     */
    public static <T> T createService(Class<T> serviceClass, Retrofit retrofit) {
        return retrofit.create(serviceClass);
    }

    /**
     * 原有创建Service方法（兼容旧代码）
     */
    public static <T> T createService(Class<T> serviceClass) {
        return getInstance().create(serviceClass);
    }

    /**
     * 重构：通用OkHttpClient创建（支持是否添加AI请求头）
     */
    private static OkHttpClient getOkHttpClient(boolean isAI) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(ApiConfig.REQUEST_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(ApiConfig.REQUEST_TIMEOUT, TimeUnit.MILLISECONDS)
                .writeTimeout(ApiConfig.REQUEST_TIMEOUT, TimeUnit.MILLISECONDS)
                .addInterceptor(getLoggingInterceptor());

        // 根据是否AI服务，添加对应请求头
        if (isAI) {
            builder.addInterceptor(getAIHeaderInterceptor());
        } else {
            builder.addInterceptor(getHeaderInterceptor());
        }
        return builder.build();
    }

    /**
     * 原有日志拦截器（保留）
     */
    private static Interceptor getLoggingInterceptor() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(
                message -> LogUtils.d("Retrofit", message)
        );
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return loggingInterceptor;
    }

    /**
     * 原有健康服务请求头拦截器（保留）
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
     * 新增：AI服务请求头拦截器（添加AI密钥）
     */
    private static Interceptor getAIHeaderInterceptor() {
        return chain -> {
            Request originalRequest = chain.request();
            Request newRequest = originalRequest.newBuilder()
                    .addHeader(ApiConfig.HEADER_AI_API_KEY, ApiConfig.AI_API_KEY_VALUE) // AI服务密钥
                    .addHeader(ApiConfig.HEADER_CONTENT_TYPE, ApiConfig.CONTENT_TYPE_JSON)
                    .build();
            Response response = chain.proceed(newRequest);
            LogUtils.d("Retrofit-AI", "AI Response Code: " + response.code());
            return response;
        };
    }
}