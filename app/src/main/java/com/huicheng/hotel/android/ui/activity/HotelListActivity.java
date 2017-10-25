package com.huicheng.hotel.android.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.HotelCommDef;
import com.huicheng.hotel.android.common.HotelOrderManager;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.huicheng.hotel.android.ui.custom.CustomConsiderLayoutForList;
import com.huicheng.hotel.android.ui.fragment.FragmentTabAllDay;
import com.huicheng.hotel.android.ui.fragment.FragmentTabClock;
import com.huicheng.hotel.android.ui.fragment.FragmentTabYeGuiRen;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author kborid
 * @date 2016/11/1 0001
 */
public class HotelListActivity extends BaseActivity {

    private int index = 0;
    private long beginTime, endTime;
    private String hotelDateStr;

    private LinearLayout date_lay;
    private TextView tv_in_date, tv_out_date;
    private RelativeLayout search_lay;
    private TabLayout tabs;

    private LinearLayout consider_btn_lay;
    private LinearLayout sort_lay, select_lay;
    private TextView shadow_lay;
    private boolean isConsiderOpened = false;

    private CustomConsiderLayoutForList consider_lay;
    private String keyword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_hotellist_layout);
        initViews();
        initParams();
        initListeners();
    }

    @Override
    public void initViews() {
        super.initViews();
        date_lay = (LinearLayout) findViewById(R.id.date_lay);
        tv_in_date = (TextView) findViewById(R.id.tv_in_date);
        tv_out_date = (TextView) findViewById(R.id.tv_out_date);
        search_lay = (RelativeLayout) findViewById(R.id.search_lay);
        consider_btn_lay = (LinearLayout) findViewById(R.id.consider_btn_lay);
        sort_lay = (LinearLayout) findViewById(R.id.sort_lay);
        select_lay = (LinearLayout) findViewById(R.id.select_lay);

        shadow_lay = (TextView) findViewById(R.id.shadow_lay);
        consider_lay = (CustomConsiderLayoutForList) findViewById(R.id.consider_lay);
        ViewPager pager = (ViewPager) findViewById(R.id.viewpager);
        if (null != pager) {
            pager.setOffscreenPageLimit(3);
            pager.setAdapter(new myPagerAdapter(getSupportFragmentManager()));
            pager.setCurrentItem(index);
            pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    index = position;
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
            //向ViewPager绑定PagerSlidingTabStrip
            tabs = (TabLayout) findViewById(R.id.tabs);
            if (null != tabs) {
                tabs.setupWithViewPager(pager);
            }
        }
    }

    @Override
    public void dealIntent() {
        super.dealIntent();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            index = bundle.getInt("index");
            if (bundle.getString("keyword") != null) {
                keyword = bundle.getString("keyword");
            }
        }
    }

    @Override
    public void initParams() {
        super.initParams();
        beginTime = HotelOrderManager.getInstance().getBeginTime();
        endTime = HotelOrderManager.getInstance().getEndTime();
        hotelDateStr = HotelOrderManager.getInstance().getDateStr();

        tv_in_date.setText(DateUtil.getDay("住MM-dd", beginTime));
        tv_out_date.setText(DateUtil.getDay("离MM-dd", endTime));
        setIndicator(tabs, 30, 30);
        consider_lay.restoreConsiderConfig();
    }

    @Override
    public void initListeners() {
        super.initListeners();
        btn_back.setOnClickListener(this);
        btn_right.setOnClickListener(this);
        shadow_lay.setOnClickListener(this);
        date_lay.setOnClickListener(this);
        search_lay.setOnClickListener(this);
        sort_lay.setOnClickListener(this);
        select_lay.setOnClickListener(this);
        consider_lay.setOnConsiderLayoutListenre(new CustomConsiderLayoutForList.OnConsiderLayoutListenre() {
            @Override
            public void onDismiss() {
                if (isConsiderOpened){
                    closeConsiderAnim();
                    refreshHotelList();
                }
            }

            @Override
            public void onResult(String str) {
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        HotelOrderManager.getInstance().setBeginTime(beginTime);
        HotelOrderManager.getInstance().setEndTime(endTime);
        HotelOrderManager.getInstance().setDateStr(hotelDateStr);
    }

    private void refreshHotelList() {
        if (null != listenerList && listenerList.size() > 0) {
            for (OnUpdateHotelInfoListener listener : listenerList) {
                listener.onUpdate(keyword);
            }
        }
    }

    private void openConsiderAnim() {
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(consider_lay, "alpha", 0f, 1f);
        ObjectAnimator alphaAnimator2 = ObjectAnimator.ofFloat(shadow_lay, "alpha", 0f, 1f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(alphaAnimator).with(alphaAnimator2);
        animatorSet.setDuration(300);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                consider_lay.setVisibility(View.VISIBLE);
                shadow_lay.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet.start();

        isConsiderOpened = true;
    }

    private void closeConsiderAnim() {
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(consider_lay, "alpha", 1f, 0f);
        ObjectAnimator alphaAnimator2 = ObjectAnimator.ofFloat(shadow_lay, "alpha", 1f, 0f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(alphaAnimator).with(alphaAnimator2);
        animatorSet.setDuration(300);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                consider_lay.setVisibility(View.GONE);
                shadow_lay.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet.start();

        isConsiderOpened = false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                finish();
                break;
            case R.id.btn_right:
                Intent intent = new Intent(this, HotelMapActivity.class);
                intent.putExtra("index", index);
                startActivity(intent);
                break;
            case R.id.shadow_lay:
                if (isConsiderOpened) {
                    closeConsiderAnim();
                }
                break;
            case R.id.search_lay:
                Intent intentSearch = new Intent(this, SearchResultActivity.class);
                startActivity(intentSearch);
                break;
            case R.id.date_lay:
                Intent resIntent = new Intent(this, HotelCalendarChooseActivity.class);
//                resIntent.putExtra("beginTime", beginTime);
//                resIntent.putExtra("endTime", endTime);
                startActivityForResult(resIntent, 0x02);
                break;
            case R.id.sort_lay:
                break;
            case R.id.select_lay:
                openConsiderAnim();
                break;
        }
    }

    private class myPagerAdapter extends FragmentPagerAdapter {
        private String[] title = getResources().getStringArray(R.array.HotelListTab);

        myPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return FragmentTabAllDay.newInstance(HotelCommDef.ALLDAY, keyword);
                case 1:
                    return FragmentTabClock.newInstance(HotelCommDef.CLOCK, keyword);
                case 2:
                    return FragmentTabYeGuiRen.newInstance(HotelCommDef.YEGUIREN, keyword);
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return title.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return title[position];
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isConsiderOpened) {
                closeConsiderAnim();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public interface OnUpdateHotelInfoListener {
        void onUpdate(String keyword);
    }

    private static List<OnUpdateHotelInfoListener> listenerList = new ArrayList<>();

    public static void registerOnUpdateHotelInfoListener(OnUpdateHotelInfoListener listener) {
        if (!listenerList.contains(listener)) {
            listenerList.add(listener);
        }
    }

    public static void unRegisterOnUpdateHotelInfoListener(OnUpdateHotelInfoListener listener) {
        if (listenerList.contains(listener)) {
            listenerList.remove(listener);
        }
    }

    //通过反射设置TabLayout的Indicator的宽度
    private void setIndicator(TabLayout tabs, int leftDip, int rightDip) {
        Class<?> tabLayout = tabs.getClass();
        Field tabStrip = null;
        try {
            tabStrip = tabLayout.getDeclaredField("mTabStrip");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        tabStrip.setAccessible(true);
        LinearLayout llTab = null;
        try {
            llTab = (LinearLayout) tabStrip.get(tabs);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        int left = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, leftDip, Resources.getSystem().getDisplayMetrics());
        int right = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, rightDip, Resources.getSystem().getDisplayMetrics());

        for (int i = 0; i < llTab.getChildCount(); i++) {
            View child = llTab.getChildAt(i);
            child.setPadding(0, 0, 0, 0);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1);
            params.leftMargin = left;
            params.rightMargin = right;
            params.gravity = Gravity.CENTER;
            child.setLayoutParams(params);
            child.setBackgroundResource(0);
            child.invalidate();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.i(TAG, "onActivityResult() " + requestCode + ", " + resultCode);
        if (Activity.RESULT_OK != resultCode) {
            return;
        }
        if (requestCode == 0x02) {
            if (null != data) {
                beginTime = data.getLongExtra("beginTime", beginTime);
                endTime = data.getLongExtra("endTime", endTime);
                HotelOrderManager.getInstance().setBeginTime(beginTime);
                HotelOrderManager.getInstance().setEndTime(endTime);
                HotelOrderManager.getInstance().setDateStr(DateUtil.getDay("M.d", beginTime) + " - " + DateUtil.getDay("M.d", endTime));
                tv_in_date.setText(DateUtil.getDay("住MM-dd", beginTime));
                tv_out_date.setText(DateUtil.getDay("离MM-dd", endTime));
                refreshHotelList();
            }
        }
    }
}
