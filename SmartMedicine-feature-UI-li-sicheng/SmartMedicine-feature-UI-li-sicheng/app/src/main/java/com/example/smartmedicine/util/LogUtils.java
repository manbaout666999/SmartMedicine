package com.example.smartmedicine.util;

import android.util.Log;

/**
 * 日志工具类（统一日志管理，便于上线时关闭日志）
 */
public class LogUtils {
    private static final boolean DEBUG = true; // 调试模式开关（上线时改为false）
    private static final String TAG = "SmartMedicine"; // 日志统一标签

    /**
     * 调试日志（Debug）
     * @param msg 日志信息
     */
    public static void d(String msg) {
        if (DEBUG) {
            Log.d(TAG, msg);
        }
    }

    /**
     * 调试日志（带自定义标签）
     * @param tag 标签
     * @param msg 日志信息
     */
    public static void d(String tag, String msg) {
        if (DEBUG) {
            Log.d(tag, msg);
        }
    }

    /**
     * 错误日志（Error）
     * @param msg 日志信息
     */
    public static void e(String msg) {
        if (DEBUG) {
            Log.e(TAG, msg);
        }
    }

    /**
     * 错误日志（带自定义标签）
     * @param tag 标签
     * @param msg 日志信息
     */
    public static void e(String tag, String msg) {
        if (DEBUG) {
            Log.e(tag, msg);
        }
    }

    /**
     * 信息日志（Info）
     * @param msg 日志信息
     */
    public static void i(String msg) {
        if (DEBUG) {
            Log.i(TAG, msg);
        }
    }

    /**
     * 警告日志（Warn）
     * @param msg 日志信息
     */
    public static void w(String msg) {
        if (DEBUG) {
            Log.w(TAG, msg);
        }
    }
}