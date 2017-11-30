package com.huicheng.hotel.android.ui.custom;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.huicheng.hotel.android.R;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.Utils;

import java.math.BigDecimal;

/**
 * @auth kborid
 * @date 2017/11/30 0030.
 */

public class CustomDoubleSeekBar extends View {
    private static final String TAG = "CustomDoubleSeekBar";
    private static final int CLICK_ON_LEFT = 1;      //点击在前滑块上
    private static final int CLICK_ON_RIGHT = 2;     //点击在后滑块上
    private static final int CLICK_IN_LEFT_AREA = 3;
    private static final int CLICK_IN_RIGHT_AREA = 4;
    private static final int CLICK_OUT_AREA = 5;
    private static final int CLICK_INVAILD = 0;
    /*
     * private static final int[] PRESSED_STATE_SET = {
     * android.R.attr.state_focused, android.R.attr.state_pressed,
     * android.R.attr.state_selected, android.R.attr.state_window_focused, };
     */
    private static final int[] STATE_NORMAL = {};
    private static final int[] STATE_PRESSED = {
            android.R.attr.state_pressed, android.R.attr.state_window_focused,
    };
    private Drawable mSelectedProgressBarBg;        //滑动条滑动后背景图
    private Drawable mUnSelectedProgressBarBg;        //滑动条未滑动背景图
    private Drawable mThumbLeft;         //前滑块
    private Drawable mThumbRight;        //后滑块

    private int mProgressBarWidth;     //控件宽度=滑动条宽度+滑动块宽度
    private int mProgressBarHeight;    //滑动条高度

    private int mThumbWidth;        //滑动块宽度
    private int mThumbHeight;       //滑动块高度

    private double mThumbLeftX = 0;     //前滑块中心坐标
    private double mThumbRightX = 0;    //后滑块中心坐标
    private int mDistance = 0;      //总刻度是固定距离 两边各去掉半个滑块距离

    private int mThumbMarginTop = 0;   //滑动块顶部距离上边框距离，也就是距离字体顶部的距离

    private int mFlag = CLICK_INVAILD;
    private OnSeekBarChangeListener mBarChangeListener;


    private double defaultScreenLow = 0;    //默认前滑块位置百分比
    private double defaultScreenHigh = 24;  //默认后滑块位置百分比


    public CustomDoubleSeekBar(Context context) {
        this(context, null);
    }

    public CustomDoubleSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomDoubleSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Resources resources = getResources();
        mUnSelectedProgressBarBg = resources.getDrawable(R.drawable.seekbar_unselected_bg);
        mSelectedProgressBarBg = resources.getDrawable(R.drawable.seekbar_selected_bg);
        mThumbLeft = resources.getDrawable(R.drawable.seekbar_thumb);
        mThumbRight = resources.getDrawable(R.drawable.seekbar_thumb);

        mThumbLeft.setState(STATE_NORMAL);
        mThumbRight.setState(STATE_NORMAL);

        mProgressBarWidth = mUnSelectedProgressBarBg.getIntrinsicWidth();
        mProgressBarHeight = mUnSelectedProgressBarBg.getIntrinsicHeight();
        mThumbWidth = mThumbLeft.getIntrinsicWidth();
        mThumbHeight = mThumbLeft.getIntrinsicHeight();

        mThumbMarginTop = Utils.dp2px(20);
    }

    //默认执行，计算view的宽高,在onDraw()之前
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
//        int height = MeasureSpec.getSize(heightMeasureSpec);
        mProgressBarWidth = width - Utils.dp2px(10);
        mDistance = width - mThumbWidth - Utils.dp2px(10);
        mThumbLeftX = mThumbWidth / 2 + Utils.dp2px(5);
        mThumbRightX = mDistance + mThumbWidth / 2;
        mThumbLeftX = formatDouble(defaultScreenLow / 24 * (mDistance)) + mThumbWidth / 2 + Utils.dp2px(5);
        mThumbRightX = formatDouble(defaultScreenHigh / 24 * (mDistance)) + mThumbWidth / 2;
        setMeasuredDimension(width, mThumbHeight + mThumbMarginTop);
    }


    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint text_Paint = new Paint();
        text_Paint.setTextAlign(Paint.Align.CENTER);
        text_Paint.setAntiAlias(true);
        text_Paint.setColor(Color.parseColor("#d4d5da"));
        text_Paint.setTextSize(Utils.sp2px(12));

        int aaa = mThumbMarginTop + mThumbHeight / 2 - mProgressBarHeight / 2;
        int bbb = aaa + mProgressBarHeight;

        //白色，不会动
        mUnSelectedProgressBarBg.setBounds(mThumbWidth / 2 + Utils.dp2px(5), aaa, mProgressBarWidth - mThumbWidth / 2, bbb);
        mUnSelectedProgressBarBg.draw(canvas);

        //蓝色，中间部分会动
        mSelectedProgressBarBg.setBounds((int) mThumbLeftX, aaa, (int) mThumbRightX, bbb);
        mSelectedProgressBarBg.draw(canvas);

        //前滑块
        mThumbLeft.setBounds((int) (mThumbLeftX - mThumbWidth / 2), mThumbMarginTop, (int) (mThumbLeftX + mThumbWidth / 2), mThumbHeight + mThumbMarginTop);
        mThumbLeft.draw(canvas);

        //后滑块
        mThumbRight.setBounds((int) (mThumbRightX - mThumbWidth / 2), mThumbMarginTop, (int) (mThumbRightX + mThumbWidth / 2), mThumbHeight + mThumbMarginTop);
        mThumbRight.draw(canvas);

        double progressLow = formatDouble((mThumbLeftX - mThumbWidth / 2) * 24 / mDistance);
        double progressHigh = formatDouble((mThumbRightX - mThumbWidth / 2) * 24 / mDistance);
        String low = String.format("%1$d:00", (int) progressLow);
        String high = String.format("%1$d:00", (int) progressHigh);
        LogUtil.d(TAG, "onDraw-->mThumbLeftX: " + mThumbLeftX + "  mThumbRightX: " + mThumbRightX + "  progressLow: " + progressLow + "  progressHigh: " + progressHigh);
        canvas.drawText(low, (int) (mThumbLeftX - (mThumbWidth - text_Paint.measureText(low)) / 2), mThumbMarginTop / 2, text_Paint);
        canvas.drawText(high, (int) (mThumbRightX - (mThumbWidth - text_Paint.measureText(high)) / 2), mThumbMarginTop / 2, text_Paint);

        if (mBarChangeListener != null) {
            mBarChangeListener.onProgressChanged(this, progressLow, progressHigh);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        //按下
        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            if (mBarChangeListener != null) {
                mBarChangeListener.onProgressBefore();
            }
            mFlag = getAreaFlag(e);
//            Log.d(TAG, "e.getX: " + e.getX() + "mFlag: " + mFlag);
//            Log.d("ACTION_DOWN", "------------------");
            if (mFlag == CLICK_ON_LEFT) {
                mThumbLeft.setState(STATE_PRESSED);
            } else if (mFlag == CLICK_ON_RIGHT) {
                mThumbRight.setState(STATE_PRESSED);
            } else if (mFlag == CLICK_IN_LEFT_AREA) {
                mThumbLeft.setState(STATE_PRESSED);
                //如果点击0-mThumbWidth/2坐标
                if (e.getX() < 0 || e.getX() <= mThumbWidth / 2) {
                    mThumbLeftX = mThumbWidth / 2;
                } else if (e.getX() > mProgressBarWidth - mThumbWidth / 2) {
//                    mThumbLeftX = mDistance - mDuration;
                    mThumbLeftX = mThumbWidth / 2 + mDistance;
                } else {
                    mThumbLeftX = formatDouble(e.getX());
//                    if (mThumbRightX<= mThumbLeftX) {
//                        mThumbRightX = (mThumbLeftX + mDuration <= mDistance) ? (mThumbLeftX + mDuration)
//                                : mDistance;
//                        mThumbLeftX = mThumbRightX - mDuration;
//                    }
                }
            } else if (mFlag == CLICK_IN_RIGHT_AREA) {
                mThumbRight.setState(STATE_PRESSED);
//                if (e.getX() < mDuration) {
//                    mThumbRightX = mDuration;
//                    mThumbLeftX = mThumbRightX - mDuration;
//                } else if (e.getX() >= mScollBarWidth - mThumbWidth/2) {
//                    mThumbRightX = mDistance + mThumbWidth/2;
                if (e.getX() >= mProgressBarWidth - mThumbWidth / 2) {
                    mThumbRightX = mDistance + mThumbWidth / 2;
                } else {
                    mThumbRightX = formatDouble(e.getX());
//                    if (mThumbRightX <= mThumbLeftX) {
//                        mThumbLeftX = (mThumbRightX - mDuration >= 0) ? (mThumbRightX - mDuration) : 0;
//                        mThumbRightX = mThumbLeftX + mDuration;
//                    }
                }
            }
            //设置进度条
            refresh();

            //移动move
        } else if (e.getAction() == MotionEvent.ACTION_MOVE) {
//            Log.d("ACTION_MOVE", "------------------");
            if (mFlag == CLICK_ON_LEFT) {
                if (e.getX() < 0 || e.getX() <= mThumbWidth / 2 + Utils.dp2px(5)) {
                    mThumbLeftX = mThumbWidth / 2 + Utils.dp2px(5);
                } else if (e.getX() >= mProgressBarWidth - mThumbWidth / 2) {
                    mThumbLeftX = mThumbWidth / 2 + mDistance;
                    mThumbRightX = mThumbLeftX;
                } else {
                    mThumbLeftX = formatDouble(e.getX());
                    if (mThumbRightX - mThumbLeftX <= 0) {
                        mThumbRightX = (mThumbLeftX <= mDistance + mThumbWidth / 2) ? (mThumbLeftX) : (mDistance + mThumbWidth / 2);
                    }
                }
            } else if (mFlag == CLICK_ON_RIGHT) {
                if (e.getX() < mThumbWidth / 2 + Utils.dp2px(5)) {
                    mThumbRightX = mThumbWidth / 2 + Utils.dp2px(5);
                    mThumbLeftX = mThumbRightX;
                } else if (e.getX() > mProgressBarWidth - mThumbWidth / 2) {
                    mThumbRightX = mThumbWidth / 2 + mDistance;
                } else {
                    mThumbRightX = formatDouble(e.getX());
                    if (mThumbRightX - mThumbLeftX <= 0) {
                        mThumbLeftX = (mThumbRightX >= mThumbWidth / 2 + Utils.dp2px(5)) ? (mThumbRightX) : mThumbWidth / 2 + Utils.dp2px(5);
                    }
                }
            }
            //设置进度条
            refresh();
            //抬起
        } else if (e.getAction() == MotionEvent.ACTION_UP) {
//            Log.d("ACTION_UP", "------------------");
            mThumbLeft.setState(STATE_NORMAL);
            mThumbRight.setState(STATE_NORMAL);

            if (mBarChangeListener != null) {
                mBarChangeListener.onProgressAfter();
            }
            //这两个for循环 是用来自动对齐刻度的，注释后，就可以自由滑动到任意位置
//            for (int i = 0; i < money.length; i++) {
//            	 if(Math.abs(mThumbLeftX-i* ((mScollBarWidth-mThumbWidth)/ (money.length-1)))<=(mScollBarWidth-mThumbWidth)/(money.length-1)/2){
//            		 mprogressLow=i;
//                     mThumbLeftX =i* ((mScollBarWidth-mThumbWidth)/(money.length-1));
//                     invalidate();
//                     break;
//                }
//			}
//
//            for (int i = 0; i < money.length; i++) {
//            	  if(Math.abs(mThumbRightX-i* ((mScollBarWidth-mThumbWidth)/(money.length-1) ))<(mScollBarWidth-mThumbWidth)/(money.length-1)/2){
//            		  mprogressHigh=i;
//                	   mThumbRightX =i* ((mScollBarWidth-mThumbWidth)/(money.length-1));
//                       invalidate();
//                       break;
//                }
//			}
        }
        return true;
    }

    public int getAreaFlag(MotionEvent e) {
        int top = mThumbMarginTop;
        int bottom = mThumbHeight + mThumbMarginTop;
        if (e.getY() >= top && e.getY() <= bottom && e.getX() >= (mThumbLeftX - mThumbWidth / 2) && e.getX() <= mThumbLeftX + mThumbWidth / 2) {
            return CLICK_ON_LEFT;
        } else if (e.getY() >= top && e.getY() <= bottom && e.getX() >= (mThumbRightX - mThumbWidth / 2) && e.getX() <= (mThumbRightX + mThumbWidth / 2)) {
            return CLICK_ON_RIGHT;
        } else if (e.getY() >= top
                && e.getY() <= bottom
                && ((e.getX() >= 0 && e.getX() < (mThumbLeftX - mThumbWidth / 2)) || ((e.getX() > (mThumbLeftX + mThumbWidth / 2))
                && e.getX() <= ((double) mThumbRightX + mThumbLeftX) / 2))) {
            return CLICK_IN_LEFT_AREA;
        } else if (e.getY() >= top
                && e.getY() <= bottom
                && (((e.getX() > ((double) mThumbRightX + mThumbLeftX) / 2) && e.getX() < (mThumbRightX - mThumbWidth / 2)) || (e
                .getX() > (mThumbRightX + mThumbWidth / 2) && e.getX() <= mProgressBarWidth))) {
            return CLICK_IN_RIGHT_AREA;
        } else if (!(e.getX() >= 0 && e.getX() <= mProgressBarWidth && e.getY() >= top && e.getY() <= bottom)) {
            return CLICK_OUT_AREA;
        } else {
            return CLICK_INVAILD;
        }
    }

    //更新滑块
    private void refresh() {
        invalidate();
    }

    //设置前滑块的值
    public void setProgressLow(double progressLow) {
        this.defaultScreenLow = progressLow;
        mThumbLeftX = formatDouble(progressLow / 24 * (mDistance)) + mThumbWidth / 2;
        refresh();
    }

    //设置后滑块的值
    public void setProgressHigh(double progressHigh) {
        this.defaultScreenHigh = progressHigh;
        mThumbRightX = formatDouble(progressHigh / 24 * (mDistance)) + mThumbWidth / 2;
        refresh();
    }

    public void setOnSeekBarChangeListener(OnSeekBarChangeListener mListener) {
        this.mBarChangeListener = mListener;
    }

    //回调函数，在滑动时实时调用，改变输入框的值
    public interface OnSeekBarChangeListener {
        //滑动前
        public void onProgressBefore();

        //滑动时
        public void onProgressChanged(CustomDoubleSeekBar seekBar, double progressLow,
                                      double progressHigh);

        //滑动后
        public void onProgressAfter();
    }

    public static double formatDouble(double pDouble) {
        BigDecimal bd = new BigDecimal(pDouble);
        BigDecimal bd1 = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
        pDouble = bd1.doubleValue();
        return pDouble;
    }
}
