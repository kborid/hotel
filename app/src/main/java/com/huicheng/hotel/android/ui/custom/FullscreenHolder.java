package com.huicheng.hotel.android.ui.custom;

/**
 * @author kborid
 * @date 2017/3/27 0027
 */

import android.content.Context;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * 全屏容器界面
 */
public class FullscreenHolder extends FrameLayout {

    public FullscreenHolder(Context ctx) {
        super(ctx);
        setBackgroundColor(ctx.getResources().getColor(android.R.color.black));
    }

    @Override
    public boolean onTouchEvent(MotionEvent evt) {
        return true;
    }
}
