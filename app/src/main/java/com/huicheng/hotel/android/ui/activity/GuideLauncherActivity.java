package com.huicheng.hotel.android.ui.activity;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @auth kborid
 * @date 2017/8/28.
 */

public class GuideLauncherActivity extends BaseActivity {


    final private int[] mGuideLaunchResId = {
            R.drawable.guide_launch1,
            R.drawable.guide_launch2,
            R.drawable.guide_launch3,
            R.drawable.guide_launch4
    };
    private List<Integer> mList = new ArrayList<>();

    private ViewPager viewpager;
    private LinearLayout indicator_lay;
    private Button btn_open;

    private int positionIndex = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_guidelauncher_layout);
        initViews();
        initParams();
        initListeners();
    }

    @Override
    public void initViews() {
        super.initViews();
        viewpager = (ViewPager) findViewById(R.id.viewpager);
        indicator_lay = (LinearLayout) findViewById(R.id.indicator_lay);
        btn_open = (Button) findViewById(R.id.btn_open);
    }

    @Override
    public void initParams() {
        super.initParams();
        for (int mResId : mGuideLaunchResId) {
            mList.add(mResId);
        }
        viewpager.setAdapter(new GuideLauncherAdapter(this, mList));
        viewpager.setOffscreenPageLimit(mGuideLaunchResId.length);
        initIndicatorLay();
    }

    private void initIndicatorLay() {
        indicator_lay.removeAllViews();
        for (int aMGuideLaunchResId : mGuideLaunchResId) {
            View view = new View(this);
            view.setBackgroundResource(R.drawable.guide_launch_indicator_selector);
            view.setEnabled(false);
            indicator_lay.addView(view);
        }
        indicator_lay.getChildAt(positionIndex).setEnabled(true);
        resizeIndicatorLayout();
    }

    private void resizeIndicatorLayout() {
        for (int i = 0; i < mGuideLaunchResId.length; i++) {
            int widthValue = indicator_lay.getChildAt(i).isEnabled() ? Utils.dip2px(24) : Utils.dip2px(9);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(widthValue, Utils.dip2px(9));
            if (i > 0) {
                lp.leftMargin = Utils.dip2px(4);
            }
            indicator_lay.getChildAt(i).setLayoutParams(lp);
        }
    }

    @Override
    public void initListeners() {
        super.initListeners();
        btn_open.setOnClickListener(this);
        viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == mGuideLaunchResId.length - 1) {
                    indicator_lay.setVisibility(View.GONE);
                    btn_open.setVisibility(View.VISIBLE);
                } else {
                    indicator_lay.setVisibility(View.VISIBLE);
                    btn_open.setVisibility(View.GONE);
                }

                indicator_lay.getChildAt(position).setEnabled(true);
                indicator_lay.getChildAt(positionIndex).setEnabled(false);
                positionIndex = position;
                resizeIndicatorLayout();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.btn_open:
                SharedPreferenceUtil.getInstance().setBoolean(AppConst.IS_FIRST_LAUNCH, false);
                Intent intent = new Intent(this, GuideSwitchActivity.class);
                startActivity(intent);
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private class GuideLauncherAdapter extends PagerAdapter {
        private Context context;
        private List<Integer> list = new ArrayList<>();

        GuideLauncherAdapter(Context context, List<Integer> list) {
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
            ImageView imageView = new ImageView(context);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageResource(list.get(position));
            container.addView(imageView, position);
            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}
