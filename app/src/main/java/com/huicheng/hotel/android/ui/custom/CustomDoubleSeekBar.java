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
    private static final int PROGRESS_MAX = 24;
    /*
     * private static final int[] PRESSED_STATE_SET = {
     * android.R.attr.state_focused, android.R.attr.state_pressed,
     * android.R.attr.state_selected, android.R.attr.state_window_focused, };
     */
    private static final int[] STATE_NORMAL = {android.R.attr.state_empty};
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

    private double defaultScreenLeft = 0; //默认前滑块位置百分比
    private double defaultScreenRight = PROGRESS_MAX;

    private double mThumbLeftX = 0;     //前滑块中心坐标
    private double mThumbRightX = 0;    //后滑块中心坐标
    private int mDistance = 0;      //总刻度是固定距离 两边各去掉半个滑块距离

    private static int DP_5_VALUE;
    private static int DP_10_VALUE;
    private int mThumbMarginTop = 0;   //滑动块顶部距离上边框距离，也就是距离字体顶部的距离
    private int mTextTimeSize = 0; //字体大小

    private int mFlag = CLICK_INVAILD;
    private OnSeekBarChangeListener mBarChangeListener;

    public CustomDoubleSeekBar(Context context) {
        this(context, null);
    }

    public CustomDoubleSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomDoubleSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Resources resources = getResources();
        DP_5_VALUE = (int) (5 * resources.getDisplayMetrics().density + 0.5f);
        DP_10_VALUE = (int) (10 * resources.getDisplayMetrics().density + 0.5f);
        mThumbMarginTop = DP_10_VALUE * 2;
        mTextTimeSize = (int) (12 * resources.getDisplayMetrics().scaledDensity + 0.5f);
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
    }

    //默认执行，计算view的宽高,在onDraw()之前
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
//        int height = MeasureSpec.getSize(heightMeasureSpec);
        mProgressBarWidth = width - DP_10_VALUE - DP_5_VALUE;
        mDistance = mProgressBarWidth - mThumbWidth;
        mThumbLeftX = mThumbWidth / 2;
        mThumbRightX = mThumbWidth / 2 + mDistance;

        mThumbLeftX = defaultScreenLeft / PROGRESS_MAX * mDistance + mThumbWidth / 2;
        mThumbRightX = defaultScreenRight / PROGRESS_MAX * mDistance + mThumbWidth / 2;
        setMeasuredDimension(width, mThumbHeight + mThumbMarginTop);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint text_Paint = new Paint();
        text_Paint.setTextAlign(Paint.Align.CENTER);
        text_Paint.setAntiAlias(true);
        text_Paint.setColor(Color.parseColor("#d4d5da"));
        text_Paint.setTextSize(mTextTimeSize);

        int aaa = mThumbMarginTop + mThumbHeight / 2 - mProgressBarHeight / 2;
        int bbb = aaa + mProgressBarHeight;

        //白色，不会动
        mUnSelectedProgressBarBg.setBounds(mThumbWidth / 2 + DP_10_VALUE, aaa, mProgressBarWidth - mThumbWidth / 2 + DP_5_VALUE, bbb);
        mUnSelectedProgressBarBg.draw(canvas);

        //蓝色，中间部分会动
        mSelectedProgressBarBg.setBounds((int) mThumbLeftX + DP_10_VALUE, aaa, (int) mThumbRightX + DP_5_VALUE, bbb);
        mSelectedProgressBarBg.draw(canvas);

        //前滑块
        mThumbLeft.setBounds((int) (mThumbLeftX - mThumbWidth / 2 + DP_10_VALUE), mThumbMarginTop, (int) (mThumbLeftX + mThumbWidth / 2 + DP_10_VALUE), mThumbHeight + mThumbMarginTop);
        mThumbLeft.draw(canvas);

        //后滑块
        mThumbRight.setBounds((int) (mThumbRightX - mThumbWidth / 2 + DP_10_VALUE), mThumbMarginTop, (int) (mThumbRightX + mThumbWidth / 2 + DP_10_VALUE), mThumbHeight + mThumbMarginTop);
        mThumbRight.draw(canvas);

        double progressLeft = (mThumbLeftX - mThumbWidth / 2) * PROGRESS_MAX / mDistance;
        double progressRight = (mThumbRightX - mThumbWidth / 2) * PROGRESS_MAX / mDistance;
        System.out.println("mThumbLeftX = " + mThumbLeftX + ", mThumbRightX= " + mThumbRightX);
        System.out.println("progressLeft = " + progressLeft + ", progressRight= " + progressRight);
        String Left = String.format("%1$02d:00", (int) progressLeft);
        String Right = String.format("%1$02d:00", (int) progressRight);
        canvas.drawText(Left, (int) (mThumbLeftX - (mThumbWidth - text_Paint.measureText(Left)) / 2) + DP_5_VALUE, mThumbMarginTop / 2, text_Paint);
        canvas.drawText(Right, (int) (mThumbRightX - (mThumbWidth - text_Paint.measureText(Right)) / 2) + DP_5_VALUE, mThumbMarginTop / 2, text_Paint);

        if (mBarChangeListener != null) {
            mBarChangeListener.onProgressChanged(this, progressLeft, progressRight);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            if (mBarChangeListener != null) {
                mBarChangeListener.onProgressBefore();
            }
            mFlag = getAreaFlag(e);
//            Log.d(TAG, "e.getX: " + e.getX() + "mFlag: " + mFlag);
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
                    mThumbLeftX = e.getX();
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
                    mThumbRightX = e.getX();
//                    if (mThumbRightX <= mThumbLeftX) {
//                        mThumbLeftX = (mThumbRightX - mDuration >= 0) ? (mThumbRightX - mDuration) : 0;
//                        mThumbRightX = mThumbLeftX + mDuration;
//                    }
                }
            }
            refresh();

        } else if (e.getAction() == MotionEvent.ACTION_MOVE) {
            if (mFlag == CLICK_ON_LEFT) {
                if (e.getX() <= mThumbWidth / 2) {
                    mThumbLeftX = mThumbWidth / 2;
                } else if (e.getX() >= mProgressBarWidth - mThumbWidth / 2 - calculateDistanceByPercent(4)) {
                    mThumbLeftX = mThumbWidth / 2 + mDistance - calculateDistanceByPercent(4);
                    mThumbRightX = mThumbWidth / 2 + mDistance;
                } else {
                    mThumbLeftX = e.getX();
                    if (mThumbRightX - mThumbLeftX <= calculateDistanceByPercent(4)) {
                        mThumbRightX = (mThumbLeftX <= mDistance + mThumbWidth / 2 - calculateDistanceByPercent(4)) ? (mThumbLeftX + calculateDistanceByPercent(4)) : (mDistance + mThumbWidth / 2);
                    }
                }
            } else if (mFlag == CLICK_ON_RIGHT) {
                if (e.getX() < mThumbWidth / 2 + calculateDistanceByPercent(4)) {
                    mThumbRightX = mThumbWidth / 2 + calculateDistanceByPercent(4);
                    mThumbLeftX = mThumbWidth / 2;
                } else if (e.getX() > mProgressBarWidth - mThumbWidth / 2) {
                    mThumbRightX = mThumbWidth / 2 + mDistance;
                } else {
                    mThumbRightX = e.getX();
                    if (mThumbRightX - mThumbLeftX <= calculateDistanceByPercent(4)) {
                        mThumbLeftX = (mThumbRightX >= mThumbWidth / 2 + calculateDistanceByPercent(4)) ? (mThumbRightX - calculateDistanceByPercent(4)) : mThumbWidth / 2;
                    }
                }
            }
            refresh();
        } else if (e.getAction() == MotionEvent.ACTION_UP) {
            mThumbLeft.setState(STATE_NORMAL);
            mThumbRight.setState(STATE_NORMAL);

            if (mBarChangeListener != null) {
                mBarChangeListener.onProgressAfter();
            }
            //这两个for循环 是用来自动对齐刻度的，注释后，就可以自由滑动到任意位置
//            for (int i = 0; i < money.length; i++) {
//            	 if(Math.abs(mThumbLeftX-i* ((mScollBarWidth-mThumbWidth)/ (money.length-1)))<=(mScollBarWidth-mThumbWidth)/(money.length-1)/2){
//            		 mprogressLeft=i;
//                     mThumbLeftX =i* ((mScollBarWidth-mThumbWidth)/(money.length-1));
//                     invalidate();
//                     break;
//                }
//			}
//
//            for (int i = 0; i < money.length; i++) {
//            	  if(Math.abs(mThumbRightX-i* ((mScollBarWidth-mThumbWidth)/(money.length-1) ))<(mScollBarWidth-mThumbWidth)/(money.length-1)/2){
//            		  mprogressRight=i;
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
    public void setProgressLeft(double progressLeft) {
        this.defaultScreenLeft = progressLeft;
        mThumbLeftX = calculateDistanceByPercent(progressLeft) + mThumbWidth / 2;
        refresh();
    }

    //设置后滑块的值
    public void setProgressRight(double progressRight) {
        this.defaultScreenRight = progressRight;
        mThumbRightX = calculateDistanceByPercent(progressRight) + mThumbWidth / 2;
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
        public void onProgressChanged(CustomDoubleSeekBar seekBar, double progressLeft, double progressRight);

        //滑动后
        public void onProgressAfter();
    }

    private double calculateDistanceByPercent(double percent) {
        return percent * mDistance / PROGRESS_MAX;
    }
}
