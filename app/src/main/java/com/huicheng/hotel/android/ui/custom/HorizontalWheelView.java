package com.huicheng.hotel.android.ui.custom;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class HorizontalWheelView extends HorizontalScrollView {
    public static final String TAG = HorizontalWheelView.class.getSimpleName();

    public static class OnWheelViewListener {
        public void onSelected(int selectedIndex, String item) {
        }
    }

    private Context context;
    private LinearLayout views;

    public HorizontalWheelView(Context context) {
        super(context);
        init(context);
    }

    public HorizontalWheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public HorizontalWheelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    List<String> items;

    private List<String> getItems() {
        return items;
    }

    public void setItems(List<String> list) {
        if (null == items) {
            items = new ArrayList<String>();
        }
        items.clear();
        items.addAll(list);

        // 前面和后面补全
        for (int i = 0; i < offset; i++) {
            items.add(0, "");
            items.add("");
        }

        initData();
    }


    public static final int OFF_SET_DEFAULT = 1;
    int offset = OFF_SET_DEFAULT; // 偏移量（需要在最前面和最后面补全）

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    int displayItemCount; // 每页显示的数量

    int selectedIndex = 1;


    private void init(Context context) {
        this.context = context;
        this.setHorizontalScrollBarEnabled(false);

        views = new LinearLayout(context);
        views.setOrientation(LinearLayout.HORIZONTAL);
        this.addView(views);

        scrollerTask = new Runnable() {

            public void run() {

                int newX = getScrollX();
                if (initialX - newX == 0) { // stopped
                    final int remainder = initialX % itemWidth;
                    final int divided = initialX / itemWidth;

                    if (remainder == 0) {
                        selectedIndex = divided + offset;
                        onSeletedCallBack();
                    } else {
                        if (remainder > itemWidth / 2) {
                            HorizontalWheelView.this.post(new Runnable() {
                                @Override
                                public void run() {
                                    HorizontalWheelView.this.smoothScrollTo(0, initialX - remainder + itemWidth);
                                    selectedIndex = divided + offset + 1;
                                    onSeletedCallBack();
                                }
                            });
                        } else {
                            HorizontalWheelView.this.post(new Runnable() {
                                @Override
                                public void run() {
                                    HorizontalWheelView.this.smoothScrollTo(0, initialX - remainder);
                                    selectedIndex = divided + offset;
                                    onSeletedCallBack();
                                }
                            });
                        }
                    }
                } else {
                    initialX = getScrollX();
                    HorizontalWheelView.this.postDelayed(scrollerTask, newCheck);
                }
            }
        };
    }

    int initialX;
    Runnable scrollerTask;
    int newCheck = 50;

    public void startScrollerTask() {
        initialX = getScrollX();
        this.postDelayed(scrollerTask, newCheck);
    }

    private void initData() {
        displayItemCount = offset * 2 + 1;

        for (String item : items) {
            views.addView(createView(item));
        }

        refreshItemView(0);
    }

    int itemWidth = 0;

    private TextView createView(String item) {
        TextView tv = new TextView(context);
        tv.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tv.setSingleLine(true);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        tv.setText(item);
        tv.setGravity(Gravity.CENTER);
        int padding = dp2px(15);
        tv.setPadding(padding, padding, padding, padding);
        if (0 == itemWidth) {
            itemWidth = getViewMeasuredWidth(tv);
            views.setLayoutParams(new LayoutParams(itemWidth * displayItemCount, ViewGroup.LayoutParams.MATCH_PARENT));
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) this.getLayoutParams();
            this.setLayoutParams(new LinearLayout.LayoutParams(itemWidth * displayItemCount, lp.height));
        }
        return tv;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        refreshItemView(l);

        if (l > oldl) {
            scrollDirection = SCROLL_DIRECTION_DOWN;
        } else {
            scrollDirection = SCROLL_DIRECTION_UP;
        }
    }

    private void refreshItemView(int x) {
        int position = x / itemWidth + offset;
        int remainder = x % itemWidth;
        int divided = x / itemWidth;

        if (remainder == 0) {
            position = divided + offset;
        } else {
            if (remainder > itemWidth / 2) {
                position = divided + offset + 1;
            }
        }

        int childSize = views.getChildCount();
        for (int i = 0; i < childSize; i++) {
            TextView itemView = (TextView) views.getChildAt(i);
            if (null == itemView) {
                return;
            }
            if (position == i) {
                itemView.setTextColor(Color.parseColor("#0288ce"));
            } else {
                itemView.setTextColor(Color.parseColor("#bbbbbb"));
            }
        }
    }

    /**
     * 获取选中区域的边界
     */
    int[] selectedAreaBorder;

    private int[] obtainSelectedAreaBorder() {
        if (null == selectedAreaBorder) {
            selectedAreaBorder = new int[2];
            selectedAreaBorder[0] = itemWidth * offset;
            selectedAreaBorder[1] = itemWidth * (offset + 1);
        }
        return selectedAreaBorder;
    }

    private int scrollDirection = -1;
    private static final int SCROLL_DIRECTION_UP = 0;
    private static final int SCROLL_DIRECTION_DOWN = 1;

    Paint paint;
    int viewWidth;

//    @Override
//    public void setBackgroundDrawable(Drawable background) {
//
//        if (viewWidth == 0) {
//            viewWidth = ((Activity) context).getWindowManager().getDefaultDisplay().getWidth();
//        }
//
//        if (null == paint) {
//            paint = new Paint();
//            paint.setColor(Color.parseColor("#83cde6"));
//            paint.setStrokeWidth(dp2px(1f));
//        }
//
//        background = new Drawable() {
//            @Override
//            public void draw(Canvas canvas) {
//                canvas.drawLine(viewWidth * 1 / 6, obtainSelectedAreaBorder()[0], viewWidth * 5 / 6, obtainSelectedAreaBorder()[0], paint);
//                canvas.drawLine(viewWidth * 1 / 6, obtainSelectedAreaBorder()[1], viewWidth * 5 / 6, obtainSelectedAreaBorder()[1], paint);
//            }
//
//            @Override
//            public void setAlpha(int alpha) {
//
//            }
//
//            @Override
//            public void setColorFilter(ColorFilter cf) {
//
//            }
//
//            @Override
//            public int getOpacity() {
//                return 0;
//            }
//        };
//
//        super.setBackgroundDrawable(background);
//
//    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
//        setBackgroundDrawable(null);
    }

    /**
     * 选中回调
     */
    private void onSeletedCallBack() {
        if (null != onWheelViewListener) {
            onWheelViewListener.onSelected(selectedIndex, items.get(selectedIndex));
        }

    }

    public void setSeletion(int position) {
        final int p = position;
        selectedIndex = p + offset;
        this.post(new Runnable() {
            @Override
            public void run() {
                HorizontalWheelView.this.smoothScrollTo(0, p * itemWidth);
            }
        });

    }

    public String getSeletedItem() {
        return items.get(selectedIndex);
    }

    public int getSeletedIndex() {
        return selectedIndex - offset;
    }

    @Override
    public void fling(int velocityX) {
        super.fling(velocityX / 3);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            startScrollerTask();
        }
        return super.onTouchEvent(ev);
    }

    private OnWheelViewListener onWheelViewListener;

    public OnWheelViewListener getOnWheelViewListener() {
        return onWheelViewListener;
    }

    public void setOnWheelViewListener(OnWheelViewListener onWheelViewListener) {
        this.onWheelViewListener = onWheelViewListener;
    }

    private int dp2px(float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private int getViewMeasuredWidth(View view) {
        int height = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        view.measure(height, expandSpec);
        return view.getMeasuredWidth();
    }

}