package com.prj.sdk.app;

import android.content.Context;

/**
 * 记录应用全局信息
 */
public final class AppContext {

    /*
     * 初始化上下文
     */
    public static Context mAppContext = null; // Application的上下文

    public static void init(Context context) {
        mAppContext = context.getApplicationContext();
    }
}
