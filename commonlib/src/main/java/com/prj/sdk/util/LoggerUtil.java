package com.prj.sdk.util;

import com.orhanobut.logger.LogLevel;
import com.orhanobut.logger.Logger;
import com.prj.sdk.BuildConfig;

/**
 * 对android自带日志的一个简单封装，方便调用
 */
public class LoggerUtil {
    public static final String TAG = "cn.abcbooking";

    public static void init() {
        LogLevel logLevel = BuildConfig.DEBUG ? LogLevel.FULL : LogLevel.NONE;
        Logger.init(TAG)
                .setMethodCount(0)                 // 决定打印多少行（每一行代表一个方法）默认：2
                .hideThreadInfo()               // 隐藏线程信息 默认：显示
                .setLogLevel(logLevel);       // 是否显示Log 默认：LogLevel.FULL（全部显示）
    }

    // 冗余信息输出
    public static void v(String tag, String msg) {
        Logger.init(tag);
        Logger.v(msg);
    }

    public static void v(String msg) {
        Logger.init(TAG);
        Logger.v(msg);
    }

    // 调试信息输出
    public static void d(String tag, String msg) {
        Logger.init(tag);
        Logger.d(msg);
    }

    public static void d(String msg) {
        Logger.init(TAG);
        Logger.d(msg);
    }

    // 提示信息输出
    public static void i(String tag, String msg) {
        Logger.init(tag);
        Logger.i(msg);
    }

    public static void i(String msg) {
        Logger.init(TAG);
        Logger.i(msg);
    }

    // 警告信息输出
    public static void w(String tag, String msg) {
        Logger.init(tag);
        Logger.w(msg);
    }

    public static void w(String msg) {
        Logger.init(TAG);
        Logger.w(msg);
    }

    // 错误信息输出
    public static void e(String tag, String msg) {
        Logger.init(tag);
        Logger.e(msg);
    }

    public static void e(String msg) {
        Logger.init(TAG);
        Logger.e(msg);
    }
}
