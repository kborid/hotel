package com.huicheng.hotel.android.common;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.os.Process;

import com.huicheng.hotel.android.PRJApplication;
import com.huicheng.hotel.android.ui.activity.LauncherActivity;
import com.huicheng.hotel.android.ui.dialog.CustomToast;
import com.prj.sdk.util.ActivityTack;
import com.prj.sdk.util.LogUtil;

import java.lang.Thread.UncaughtExceptionHandler;

/**
 * UncaughtException处理类,当程序发生Uncaught异常的时候。会调用该类的uncaughtException()方法
 */
public class CrashHandler implements UncaughtExceptionHandler {

    private final String TAG = getClass().getSimpleName();
    private UncaughtExceptionHandler mDefaultUEHandler;

    private static CrashHandler mInstance;

    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        if (mInstance == null) {
            mInstance = new CrashHandler();
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
     * 在这个方法中增加对发生异常时的处理工作，比如清除内存，注销登录。
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        LogUtil.e(TAG, "uncaughtException()" + ", thread name = " + thread.getName());
        if (!handleException(ex) && mDefaultUEHandler != null) {
            //系统默认异常处理
            mDefaultUEHandler.uncaughtException(thread, ex);
        } else {
            Intent intent = new Intent(PRJApplication.getInstance(), LauncherActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent restartIntent = PendingIntent.getActivity(
                    PRJApplication.getInstance(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            //退出并重启程序
            AlarmManager mgr = (AlarmManager) PRJApplication.getInstance().getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 2000, restartIntent);

            try {
                Thread.sleep(1500);
            } catch (Exception e) {
                e.printStackTrace();
            }
            ActivityTack.getInstanse().exit();
            Process.killProcess(Process.myPid());
        }
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex
     * @return true:如果处理了该异常信息;否则返回false.
     */
    private boolean handleException(Throwable ex) {
        if (null == ex) {
            return false;
        }

        //使用Toast处理异常信息
        ex.printStackTrace();
        new Thread() {
            @Override
            public void run() {
                super.run();
                Looper.prepare();
                CustomToast.show("程序发生异常，即将重启", CustomToast.LENGTH_SHORT);
                Looper.loop();
            }
        }.start();
        return true;
    }
}
