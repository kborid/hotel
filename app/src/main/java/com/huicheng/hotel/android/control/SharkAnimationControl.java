package com.huicheng.hotel.android.control;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

import com.prj.sdk.util.DisplayUtil;

/**
 * @author kborid
 * @date 2016/10/9 0009
 */
public class SharkAnimationControl {
    private static SharkAnimationControl instance = null;

    private static final int ICON_WIDTH = 80;
    private static final int ICON_HEIGHT = 94;
    private static final float DEGREE_0 = 1.8f;
    private static final float DEGREE_1 = -2.0f;
    private static final float DEGREE_2 = 2.0f;
    private static final float DEGREE_3 = -1.5f;
    private static final float DEGREE_4 = 1.5f;
    private static final int ANIMATION_DURATION = 100;
    private int mCount = 0;

    private SharkAnimationControl() {
    }

    public static SharkAnimationControl getInstance() {
        if (instance == null) {
            synchronized (SharkAnimationControl.class) {
                if (instance == null) {
                    instance = new SharkAnimationControl();
                }
            }
        }

        return instance;
    }

    public void sharkAnimation(final View v, final boolean isAlwaysShark) {
        float rotate = 0;
        int c = mCount++ % 5;
        if (c == 0) {
            rotate = DEGREE_0;
        } else if (c == 1) {
            rotate = DEGREE_1;
        } else if (c == 2) {
            rotate = DEGREE_2;
        } else if (c == 3) {
            rotate = DEGREE_3;
        } else {
            rotate = DEGREE_4;
        }
        float mDensity = DisplayUtil.getScaledDensity();
        final RotateAnimation mra = new RotateAnimation(rotate, -rotate, ICON_WIDTH * mDensity / 2, ICON_HEIGHT * mDensity / 2);
        final RotateAnimation mrb = new RotateAnimation(-rotate, rotate, ICON_WIDTH * mDensity / 2, ICON_HEIGHT * mDensity / 2);

        mra.setDuration(ANIMATION_DURATION);
        mrb.setDuration(ANIMATION_DURATION);

        mra.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                if (isAlwaysShark) {
                    mra.reset();
                    v.startAnimation(mrb);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationStart(Animation animation) {
            }
        });

        mrb.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                if (isAlwaysShark) {
                    mrb.reset();
                    v.startAnimation(mra);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationStart(Animation animation) {
            }
        });
        v.startAnimation(mra);
    }
}
