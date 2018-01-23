package com.prj.sdk.util;

import android.util.Log;

import com.prj.sdk.BuildConfig;

/**
 * 对android自带日志的一个简单封装，方便调用
 */
public class LogUtil {

    private static final String TAG = "ABC";

    private static long startTime = 0;

    public static boolean isDebug() {
        return BuildConfig.DEBUG;
    }

    // 冗余信息输出
    public static void v(String subTag, String msg) {
        if (isDebug()) {
            Log.v(TAG, subTag + ": [" + Thread.currentThread().getName() + "]:" + msg);
        }
    }

    public static void v(String msg) {
        if (isDebug()) {
            Log.v(TAG, "[" + Thread.currentThread().getName() + "]:" + msg);
        }
    }

    // 调试信息输出
    public static void d(String subTag, String msg) {
        if (isDebug()) {
            Log.d(TAG, subTag + ": [" + Thread.currentThread().getName() + "]:" + msg);
        }
    }

    public static void d(String msg) {
        if (isDebug()) {
            Log.d(TAG, "[" + Thread.currentThread().getName() + "]:" + msg);
        }
    }

    // 提示信息输出
    public static void i(String subTag, String msg) {
        if (isDebug()) {
            Log.i(TAG, subTag + ": [" + Thread.currentThread().getName() + "]:" + msg);
        }
    }

    public static void i(String msg) {
        if (isDebug()) {
            Log.i(TAG, "[" + Thread.currentThread().getName() + "]:" + msg);
        }
    }

    // 警告信息输出
    public static void w(String subTag, String msg) {
        if (isDebug()) {
            Log.w(TAG, subTag + ": [" + Thread.currentThread().getName() + "]:" + msg);
        }
    }

    public static void w(String msg) {
        if (isDebug()) {
            Log.w(TAG, "[" + Thread.currentThread().getName() + "]:" + msg);
        }
    }

    // 错误信息输出
    public static void e(String subTag, String msg) {
        if (isDebug()) {
            Log.e(TAG, subTag + ": [" + Thread.currentThread().getName() + "]:" + msg);
        }
    }

    public static void e(String msg) {
        if (isDebug()) {
            Log.e(TAG, "[" + Thread.currentThread().getName() + "]:" + msg);
        }
    }

    /*
     * 记录方法调用的开始时间
     */
    public static void startTime() {
        startTime = System.currentTimeMillis();
        d("调用开始时间", "sta-t time:" + startTime);
    }

    /*
     * 记录方法调用的使用时间
     */
    public static void useTime() {
        long endTime = System.currentTimeMillis();
        d("调用花费时间", "cos-t time:" + (endTime - startTime));
    }
}
