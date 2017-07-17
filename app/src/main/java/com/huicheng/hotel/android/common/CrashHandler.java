package com.huicheng.hotel.android.common;

import android.content.Context;
import android.os.Process;

import com.prj.sdk.util.ActivityTack;
import com.prj.sdk.util.LogUtil;

import java.lang.Thread.UncaughtExceptionHandler;

/**
 * UncaughtException处理类,当程序发生Uncaught异常的时候。会调用该类的uncaughtException()方法
 *
 * @author LiaoBo
 */
public class CrashHandler implements UncaughtExceptionHandler {

    public static final String TAG = "CrashHandler";
    /**
     * RCSCrashHandler实例
     */
    private static CrashHandler mInstance;
    /**
     * 系统默认的UncaughtException处理类
     */
    private UncaughtExceptionHandler mDefaultUEHandler;
    /**
     * 程序的Context对象
     */
    private Context mContext;

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
     *
     * @param context 注册的Context对象
     */
    public void init(Context context) {
        mContext = context;
        mDefaultUEHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * 在这个方法中增加对发生异常时的处理工作，比如清除内存，注销登陆。
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        ex.printStackTrace();
        if (null != mDefaultUEHandler) {
            mDefaultUEHandler.uncaughtException(thread, ex);
        } else {
            LogUtil.d(TAG, "app crash");
            /*PackageManager pm = mContext.getPackageManager();
            Intent intent = pm.getLaunchIntentForPackage(mContext.getPackageName());
			mContext.startActivity(intent);		*/
//			MobclickAgent.reportError(mContext, ex);//将己捕获的错误，上传到友盟服务器
            ActivityTack.getInstanse().exit();
            Process.killProcess(Process.myPid());
            System.exit(1);
        }
    }
}
