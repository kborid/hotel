package com.huicheng.hotel.android.ui.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * @author kborid
 * @date 2017/3/21 0021
 */
public class CustomScrollView extends ScrollView {

    private Context context;
    private int mMaxOverDistance = 150;

    public CustomScrollView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public CustomScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public CustomScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        float density = context.getResources().getDisplayMetrics().density;
        mMaxOverDistance = (int) (mMaxOverDistance * density);
    }

    @Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, mMaxOverDistance, isTouchEvent);
    }
}
