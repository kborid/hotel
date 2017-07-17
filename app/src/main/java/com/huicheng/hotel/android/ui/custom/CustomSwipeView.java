package com.huicheng.hotel.android.ui.custom;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.Utils;

/**
 * @author kborid
 * @date 2016/12/16 0016
 */
public class CustomSwipeView extends ViewGroup {
    private final String TAG = getClass().getSimpleName();

    private View itemView, deleteView;
    private ViewDragHelper mViewDragHelper;

    public CustomSwipeView(Context context) {
        super(context);
    }

    public CustomSwipeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomSwipeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    //布局完成后调用
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        deleteView = getChildAt(0);
        itemView = getChildAt(1);
        mViewDragHelper = ViewDragHelper.create(this, callback);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        itemView.measure(widthMeasureSpec, heightMeasureSpec); // 测量内容部分的大小
        deleteView.measure(widthMeasureSpec, heightMeasureSpec); // 测量删除部分的大小
        int size = MeasureSpec.getSize(heightMeasureSpec);
        int mode = MeasureSpec.getMode(heightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int itemViewWidth = itemView.getMeasuredWidth();
        int itemViewHeight = itemView.getMeasuredHeight();
        itemView.layout(0, 0, itemViewWidth, itemViewHeight); // 摆放内容部分的位置
        int deleteViewWidth = deleteView.getMeasuredWidth();
        int deleteViewHeight = deleteView.getMeasuredHeight();
        deleteView.layout(0, 0, deleteViewWidth, deleteViewHeight); // 摆放删除部分的位置
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        LogUtil.i(TAG, "onInterceptTouchEvent()");
//        final int action = MotionEventCompat.getActionMasked(event);
//        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
//            mViewDragHelper.cancel();
//            return false;
//        }
        return mViewDragHelper.shouldInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        LogUtil.i(TAG, "onTouchEvent()");
        //禁止处理手势
//        mViewDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    public void computeScroll() {
        if (mViewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private final ViewDragHelper.Callback callback = new ViewDragHelper.Callback() {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            //触摸的布局是否为MainView
            LogUtil.i(TAG, "tryCaptureView()");
            return itemView == child;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            //不需要检测垂直滑动，直接返回0
            return 0;
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            LogUtil.i(TAG, "getViewHorizontalDragRange()");
            return itemView == child ? child.getWidth() : 0;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if (left > 0) {
                return 0;
            } else if (-left > Utils.dip2px(40)) {
                return -Utils.dip2px(40);
            }
            return left;
        }

        @Override
        public void onViewReleased(View child, float xVel, float yVel) {
            //核心逻辑：滑动MainView超过一定距离就显示MenuView
            LogUtil.i(TAG, "onViewReleased()");
            if (itemView.getWidth() - itemView.getRight() < Utils.dip2px(20)) {
                isShowDelete(false);
                if (onSlideDeleteListener != null) {
                    onSlideDeleteListener.onClose(CustomSwipeView.this); // 调用接口打开的方法
                }
            } else {
                isShowDelete(true);
                if (onSlideDeleteListener != null) {
                    onSlideDeleteListener.onOpen(CustomSwipeView.this); // 调用接口打开的方法
                }
            }
//            super.onViewReleased(child, xVel, yVel);
        }
    };

    public void isShowDelete(boolean isShowDelete) {
        if (isShowDelete) {
            mViewDragHelper.smoothSlideViewTo(itemView, -Utils.dip2px(40), 0);
        } else {
            mViewDragHelper.smoothSlideViewTo(itemView, 0, 0);
        }
        ViewCompat.postInvalidateOnAnimation(CustomSwipeView.this);
    }

    // SlideDlete的接口
    public interface OnSlideDeleteListener {
        void onOpen(CustomSwipeView swipeView);

        void onClose(CustomSwipeView swipeView);
    }

    private OnSlideDeleteListener onSlideDeleteListener;

    public void setOnSlideDeleteListener(OnSlideDeleteListener onSlideDeleteListener) {
        this.onSlideDeleteListener = onSlideDeleteListener;
    }
}
