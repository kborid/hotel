package com.prj.sdk.app;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

/**
 * 记录应用全局信息
 *
 * @author LiaoBo
 */
public final class AppContext {

    public static final Handler mMainHandler = new Handler(
            Looper.getMainLooper()); // 公共Handler
    /*
     * 初始化上下文
     */
    public static Context mAppContext = null; // Application的上下文

    public static void init(Context context) {
        mAppContext = context.getApplicationContext();
    }
}
