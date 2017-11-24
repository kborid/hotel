package com.huicheng.hotel.android.ui.activity.hotel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.HotelCommDef;
import com.huicheng.hotel.android.common.HotelOrderManager;
import com.huicheng.hotel.android.ui.activity.CalendarChooseActivity;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.huicheng.hotel.android.ui.custom.CustomConsiderLayoutForList;
import com.huicheng.hotel.android.ui.custom.CustomSortLayout;
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
    private EditText et_search;
    private TabLayout tabs;

    private LinearLayout sort_lay, consider_lay;

    private CustomConsiderLayoutForList customConsiderLayoutForList;
    private PopupWindow mConsiderWindow;
    private CustomSortLayout customSortLayout;
    private PopupWindow mSortPopupWindow;


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
        View searchView = LayoutInflater.from(this).inflate(R.layout.layout_hotellist_search, null);
        setTitleContentView(searchView);
        setRightButtonResource(R.drawable.iv_map);
        date_lay = (LinearLayout) findViewById(R.id.date_lay);
        tv_in_date = (TextView) findViewById(R.id.tv_in_date);
        tv_out_date = (TextView) findViewById(R.id.tv_out_date);
        search_lay = (RelativeLayout) findViewById(R.id.search_lay);
        et_search = (EditText) findViewById(R.id.et_search);
        sort_lay = (LinearLayout) findViewById(R.id.sort_lay);
        consider_lay = (LinearLayout) findViewById(R.id.consider_lay);

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

        //筛选框Popup Window
        customConsiderLayoutForList = new CustomConsiderLayoutForList(this);
        customConsiderLayoutForList.setOnConsiderLayoutListener(new CustomConsiderLayoutForList.OnConsiderLayoutListener() {
            @Override
            public void onDismiss() {
                mConsiderWindow.dismiss();
                refreshHotelList();
            }

            @Override
            public void onResult(String str) {
            }
        });

        mConsiderWindow = new PopupWindow(customConsiderLayoutForList, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        mConsiderWindow.setAnimationStyle(R.style.consider_anmi);
        mConsiderWindow.setFocusable(true);
        mConsiderWindow.setOutsideTouchable(true);
        mConsiderWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        mConsiderWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1f;
                getWindow().setAttributes(lp);
            }
        });


        //排序popup window
        customSortLayout = new CustomSortLayout(this);
        customSortLayout.setOnSortResultListener(new CustomSortLayout.OnSortResultListener() {
            @Override
            public void doAction() {
                refreshHotelList();
            }

            @Override
            public void dismiss() {
                mSortPopupWindow.dismiss();
            }
        });
        mSortPopupWindow = new PopupWindow(customSortLayout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        mSortPopupWindow.setAnimationStyle(R.style.consider_anmi);
        mSortPopupWindow.setFocusable(true);
        mSortPopupWindow.setOutsideTouchable(true);
        mSortPopupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        mSortPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1f;
                getWindow().setAttributes(lp);
            }
        });
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
        et_search.setText(keyword);
        beginTime = HotelOrderManager.getInstance().getBeginTime();
        endTime = HotelOrderManager.getInstance().getEndTime();
        hotelDateStr = HotelOrderManager.getInstance().getDateStr();

        tv_in_date.setText(DateUtil.getDay("住MM-dd", beginTime));
        tv_out_date.setText(DateUtil.getDay("离MM-dd", endTime));
        setIndicator(tabs, 30, 30);
    }

    @Override
    public void initListeners() {
        super.initListeners();
        date_lay.setOnClickListener(this);
        search_lay.setOnClickListener(this);
        sort_lay.setOnClickListener(this);
        consider_lay.setOnClickListener(this);

        et_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    et_search.setFocusable(false);
                    et_search.setFocusableInTouchMode(true);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    refreshHotelList();
                    return true;
                }
                return false;
            }
        });

        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                keyword = s.toString();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != customConsiderLayoutForList) {
            customConsiderLayoutForList.clearPointConditionConfig();
            customConsiderLayoutForList = null;
        }
    }

    private void showSortPopupWindow() {
        // 设置背景颜色变暗
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;
        getWindow().setAttributes(lp);
        mSortPopupWindow.showAtLocation(getWindow().getDecorView(), Gravity.CENTER, 0, 0);
    }

    private void showConsiderPopupWindow() {
        customConsiderLayoutForList.reloadConsiderConfig();
        // 设置背景颜色变暗
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;
        getWindow().setAttributes(lp);
        mConsiderWindow.showAtLocation(getWindow().getDecorView(), Gravity.CENTER, 0, 0);
    }

    private void refreshHotelList() {
        if (null != listenerList && listenerList.size() > 0) {
            for (OnUpdateHotelInfoListener listener : listenerList) {
                listener.onUpdate(keyword);
            }
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.btn_right:
                Intent intent = new Intent(this, HotelMapActivity.class);
                intent.putExtra("index", index);
                startActivity(intent);
                break;
            case R.id.date_lay:
                Intent resIntent = new Intent(this, CalendarChooseActivity.class);
//                resIntent.putExtra("beginTime", beginTime);
//                resIntent.putExtra("endTime", endTime);
                startActivityForResult(resIntent, 0x02);
                break;
            case R.id.sort_lay:
                showSortPopupWindow();
                break;
            case R.id.consider_lay:
                showConsiderPopupWindow();
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
