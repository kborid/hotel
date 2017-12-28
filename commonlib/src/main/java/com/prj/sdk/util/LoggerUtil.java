package com.prj.sdk.util;

import com.orhanobut.logger.LogLevel;
import com.orhanobut.logger.Logger;
import com.prj.sdk.BuildConfig;

/**
 * 对Logger的一个简单封装，方便打印长log
 */
public class LoggerUtil {
    public static final String TAG = "cn.abcbooking";

    public static void init() {
        LogLevel logLevel = BuildConfig.DEBUG ? LogLevel.FULL : LogLevel.NONE;
        Logger.init(TAG)
                .setMethodCount(3)                 // 决定打印多少行（每一行代表一个方法）默认：2
//                .hideThreadInfo()               // 隐藏线程信息 默认：显示
                .setLogLevel(logLevel);       // 是否显示Log 默认：LogLevel.FULL（全部显示）
    }

    // 冗余信息输出
    public static void v(String msg) {
        Logger.v(msg);
    }

    // 调试信息输出
    public static void d(String msg) {
        Logger.d(msg);
    }

    // 提示信息输出
    public static void i(String msg) {
        Logger.i(msg);
    }

    // 警告信息输出
    public static void w(String msg) {
        Logger.w(msg);
    }

    // 错误信息输出
    public static void e(String msg) {
        Logger.e(msg);
    }
}
