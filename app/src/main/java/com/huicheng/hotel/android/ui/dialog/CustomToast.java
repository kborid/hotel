package com.huicheng.hotel.android.ui.dialog;

import android.widget.Toast;

import com.prj.sdk.app.AppContext;

/**
 * Toast显示内容 解决Toast叠加显示
 */
public class CustomToast {
    public static final int LENGTH_SHORT = Toast.LENGTH_SHORT;
    public static final int LENGTH_LONG = Toast.LENGTH_LONG;

    private static Toast mToast;

    /**
     * 关闭显示
     */
    public static void cancel() {
        if (mToast != null)
            mToast.cancel();
    }

    /**
     * 显示Toast
     *
     * @param text
     * @param duration
     */
    public static void show(CharSequence text, int duration) {
        // if (mToast == null) {
        // mToast = Toast.makeText(AppContext.mMainContext, text, duration);
        // } else {
        // mToast.setDuration(duration);
        // mToast.setText(text);
        // }

        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(AppContext.mAppContext, text, duration);
        mToast.setDuration(duration);
        mToast.setText(text);

        mToast.show();
    }
}