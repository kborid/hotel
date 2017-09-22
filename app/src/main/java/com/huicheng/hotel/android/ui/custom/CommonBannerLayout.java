package com.huicheng.hotel.android.ui.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.net.bean.HomeBannerInfoBean;
import com.huicheng.hotel.android.ui.adapter.BannerImageAdapter;
import com.prj.sdk.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kborid
 * @date 2016/8/15
 */
public class CommonBannerLayout extends RelativeLayout implements ViewPager.OnPageChangeListener {

    private final String TAG = getClass().getSimpleName();

    private static final int DELAY_TIME = 5000;
    private Context context;
    private ViewPager viewpager;
    private LinearLayout indicator_lay;
    private Handler myHandler = new Handler();
    private int positionIndex = 0;
    private CustomViewPagerScroller customViewPagerScroller;
    private List<HomeBannerInfoBean> mList = new ArrayList<>();

    private int mIndicatorResId;

    public CommonBannerLayout(Context context) {
        this(context, null);
    }

    public CommonBannerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CommonBannerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.comm_banner_layout, this);
        TypedArray ta = context.obtainStyledAttributes(R.styleable.MyTheme);
        mIndicatorResId = ta.getResourceId(R.styleable.MyTheme_indicatorSel, R.drawable.indicator_sel);
        ta.recycle();

        findViews();
        initListener();
    }

    private void initListener() {
        viewpager.addOnPageChangeListener(this);
    }

    private void findViews() {
        viewpager = (ViewPager) findViewById(R.id.viewpager);
        customViewPagerScroller = new CustomViewPagerScroller(context);
        customViewPagerScroller.setScrollDuration(1500);
        customViewPagerScroller.initViewPagerScroll(viewpager);
        indicator_lay = (LinearLayout) findViewById(R.id.indicator_lay);
    }

    public void setImageResource(List<HomeBannerInfoBean> list) {
        if (list != null && list.size() > 0) {
            mList.clear();
            mList.addAll(list);
            viewpager.setAdapter(new BannerImageAdapter(context, list));
            initIndicatorLay(list.size());
            //viewPager一个假的无限循环，初始位置是viewPager count的100倍
            viewpager.setCurrentItem(list.size() * 100);
            viewpager.setOffscreenPageLimit(list.size());
        }
    }

    /**
     * 图片滚动线程
     */

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            viewpager.setCurrentItem(viewpager.getCurrentItem() + 1);
            myHandler.postDelayed(runnable, DELAY_TIME);
        }
    };

    public void startBanner() {
        myHandler.removeCallbacks(runnable);
        if (mList != null && mList.size() > 1) {
            myHandler.postDelayed(runnable, DELAY_TIME);
        }
    }

    public void stopBanner() {
        myHandler.removeCallbacks(runnable);
    }

    private void initIndicatorLay(int count) {
        indicator_lay.removeAllViews();
        if (count >= 1) {
            for (int i = 0; i < count; i++) {
                View view = new View(context);
                view.setBackgroundResource(mIndicatorResId);
                view.setEnabled(false);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Utils.dip2px(7), Utils.dip2px(7));
                if (i > 0) {
                    lp.leftMargin = Utils.dip2px(7);
                }
                view.setLayoutParams(lp);
                indicator_lay.addView(view);
            }
            indicator_lay.getChildAt(positionIndex).setEnabled(true);
        }
        startBanner();
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
    }

    @Override
    public void onPageSelected(int position) {
        int newPosition = position % mList.size();
        indicator_lay.getChildAt(newPosition).setEnabled(true);
        if (positionIndex != newPosition) {
            indicator_lay.getChildAt(positionIndex).setEnabled(false);
            positionIndex = newPosition;
        }
    }
}
