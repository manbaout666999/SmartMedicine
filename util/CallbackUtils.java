package com.example.smartmedicine.util;

import android.os.Handler;
import android.os.Looper;

import com.example.smartmedicine.api.ApiConfig;

/**
 * 统一回调接口封装（简化API调用的成功/失败/加载状态处理）
 * @param <T> 回调数据泛型
 */
public abstract class CallbackUtils<T> {
    // 主线程Handler（确保回调在UI线程执行）
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    /**
     * 加载中回调（可选，用于显示加载动画）
     */
    public void onLoading() {}

    /**
     * 成功回调（主线程执行）
     * @param data 响应数据
     */
    public abstract void onSuccess(T data);

    /**
     * 失败回调（主线程执行）
     * @param errorCode 错误码
     * @param errorMsg 错误信息
     */
    public abstract void onFailure(int errorCode, String errorMsg);

    /**
     * 切换到主线程执行回调
     * @param runnable 回调任务
     */
    protected void postToMainThread(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run(); // 已在主线程，直接执行
        } else {
            MAIN_HANDLER.post(runnable); // 切换到主线程
        }
    }

    /**
     * 快捷创建网络错误回调
     */
    public static <T> CallbackUtils<T> networkErrorCallback() {
        return new CallbackUtils<T>() {
            @Override
            public void onSuccess(T data) {}

            @Override
            public void onFailure(int errorCode, String errorMsg) {
                LogUtils.e("Callback", "网络错误：" + errorMsg);
            }
        };
    }

    /**
     * 快捷创建默认错误回调
     */
    public static <T> CallbackUtils<T> defaultErrorCallback() {
        return new CallbackUtils<T>() {
            @Override
            public void onSuccess(T data) {}

            @Override
            public void onFailure(int errorCode, String errorMsg) {
                LogUtils.e("Callback", "错误码：" + errorCode + "，错误信息：" + errorMsg);
            }
        };
    }
}