package com.huicheng.hotel.android.ui.custom;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.ui.glide.CustomReqURLFormatModelImpl;
import com.prj.sdk.util.Utils;

import java.util.List;

/**
 * @author kborid
 * @date 2016/8/15
 */
public class CustomNoAutoScrollBannerLayout extends RelativeLayout implements ViewPager.OnPageChangeListener {

    private Context context;
    private ViewPager viewpager;
    private LinearLayout indicator_lay;
    private int positionIndex = 0;

    public CustomNoAutoScrollBannerLayout(Context context) {
        this(context, null);
    }

    public CustomNoAutoScrollBannerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomNoAutoScrollBannerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.custom_notautoscroll_viewpager_layout, this);
        findViews();
        initListener();
    }

    private void initListener() {
        viewpager.addOnPageChangeListener(this);
    }

    private void findViews() {
        viewpager = (ViewPager) findViewById(R.id.viewpager);
        indicator_lay = (LinearLayout) findViewById(R.id.indicator_lay);
    }

    public void setImageResourcePaths(List<String> list) {
        if (list != null && list.size() > 0) {
            viewpager.setAdapter(new CustomPagerAdapter(context, list));
            if (list.size() > 0) {
                viewpager.setOffscreenPageLimit(list.size());
            }
            initIndicatorLay(list.size());
        }
    }


    private void initIndicatorLay(int count) {
        indicator_lay.removeAllViews();
        if (count > 1) {
            for (int i = 0; i < count; i++) {
                View view = new View(context);
                view.setBackgroundResource(R.drawable.indicator_room_detail);
                view.setEnabled(false);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Utils.dip2px(10), Utils.dip2px(10));
                if (i > 0) {
                    lp.leftMargin = Utils.dip2px(8);
                }
                view.setLayoutParams(lp);
                indicator_lay.addView(view);
            }
            indicator_lay.getChildAt(positionIndex).setEnabled(true);
        }
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
    }

    @Override
    public void onPageSelected(int position) {
        indicator_lay.getChildAt(position).setEnabled(true);
        indicator_lay.getChildAt(positionIndex).setEnabled(false);
        positionIndex = position;
    }

    private class CustomPagerAdapter extends PagerAdapter {

        private Context context;
        private List<String> list;

        CustomPagerAdapter(Context context, List<String> list) {
            this.context = context;
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return (view == object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            View view = LayoutInflater.from(context).inflate(R.layout.vp_custom_banner_item, null);
            RoundedTopImageView iv_bg = (RoundedTopImageView) view.findViewById(R.id.iv_bg);
            Glide.with(context)
                    .load(new CustomReqURLFormatModelImpl(list.get(position)))
                    .crossFade()
                    .centerCrop()
                    .override(680, 500)
                    .into(iv_bg);

            //如果View已经在之前添加到了一个父组件，则必须先remove，否则会抛出IllegalStateException。
            ViewParent vp = view.getParent();
            if (vp != null) {
                ViewGroup parent = (ViewGroup) vp;
                parent.removeView(view);
            }
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
        }
    }
}
