package com.huicheng.hotel.android.common;

import android.os.Process;

import com.prj.sdk.util.ActivityTack;
import com.prj.sdk.util.LogUtil;

import java.lang.Thread.UncaughtExceptionHandler;

/**
 * UncaughtException处理类,当程序发生Uncaught异常的时候。会调用该类的uncaughtException()方法
 */
public class RCSCrashHandler implements UncaughtExceptionHandler {

    private final String TAG = getClass().getSimpleName();
    private UncaughtExceptionHandler mDefaultUEHandler;

    private static RCSCrashHandler mInstance;

    private RCSCrashHandler() {
    }

    public static RCSCrashHandler getInstance() {
        if (mInstance == null) {
            mInstance = new RCSCrashHandler();
        }
        return mInstance;
    }

    /**
     * 初始化，获取系统默认的UncaughtException处理器, 设置该RCSCrashHandler为程序的默认处理器
     */
    public void init() {
        mDefaultUEHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * 在这个方法中增加对发生异常时的处理工作，比如清除内存，注销登陆。
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        LogUtil.e(TAG, "uncaughtException()");
        ex.printStackTrace();
        if (null != mDefaultUEHandler) {
            mDefaultUEHandler.uncaughtException(thread, ex);
        } else {
            LogUtil.e(TAG, "mDefaultUEHandler = null, App Crash");
            ActivityTack.getInstanse().exit();
            Process.killProcess(Process.myPid());
            System.exit(1);
        }
    }
}
