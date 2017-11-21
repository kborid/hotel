package com.huicheng.hotel.android.ui.activity.plane;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.ui.activity.BaseMainActivity;
import com.huicheng.hotel.android.ui.activity.CalendarChooseActivity;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;

import java.lang.reflect.Field;
import java.util.Date;

public class PlaneMainActivity extends BaseMainActivity {

    private static final int REQUEST_CODE_DATE = 0x01;
    private static final int TAB_SINGLE = 0;
    private static final int TAB_DOUBLE = 1;
    private TabLayout tabs;
    private int selectedIndex = 0;
    private boolean isSingle = true;

    private LinearLayout off_date_lay, on_date_lay;
    private TextView tv_off_date, tv_off_week;
    private TextView tv_on_date, tv_on_week;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootView = LayoutInflater.from(this).inflate(R.layout.layout_content_plane, null);
        initContentLayout(rootView);
        initViews();
        initParams();
        initListeners();
    }

    public void initViews() {
        super.initViews();
        tabs = (TabLayout) findViewById(R.id.tabs);
        off_date_lay = (LinearLayout) findViewById(R.id.off_date_lay);
        tv_off_date = (TextView) findViewById(R.id.tv_off_date);
        tv_off_week = (TextView) findViewById(R.id.tv_off_week);
        on_date_lay = (LinearLayout) findViewById(R.id.on_date_lay);
        tv_on_date = (TextView) findViewById(R.id.tv_on_date);
        tv_on_week = (TextView) findViewById(R.id.tv_on_week);
    }

    @Override
    public void initParams() {
        super.initParams();
        tabs.addTab(tabs.newTab().setText(getString(R.string.plane_single)), TAB_SINGLE, true);
        tabs.addTab(tabs.newTab().setText(getString(R.string.plane_double)), TAB_DOUBLE, false);
        setIndicator(tabs, 54, 54);
        refreshPlaneStateAndInfo(0);
    }

    @Override
    public void initListeners() {
        super.initListeners();
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                System.out.println("onTabSelected() " + tab.getText() + ", " + tab.getPosition());
                selectedIndex = tab.getPosition();
                refreshPlaneStateAndInfo(selectedIndex);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        off_date_lay.setOnClickListener(this);
        on_date_lay.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.off_date_lay:
            case R.id.on_date_lay: {
                Intent resIntent = new Intent(this, CalendarChooseActivity.class);
                resIntent.putExtra("isTitleCanClick", true);
//                resIntent.putExtra("beginTime", beginTime);
//                resIntent.putExtra("endTime", endTime);
                startActivityForResult(resIntent, REQUEST_CODE_DATE);
                break;
            }
        }
    }

    private void refreshPlaneStateAndInfo(int pos) {
        if (pos == TAB_SINGLE) {
            isSingle = true;
            tv_off_date.setText(DateUtil.getDay("M月d日", beginTime));
            tv_off_week.setText(DateUtil.dateToWeek2(new Date(beginTime)));
            on_date_lay.setEnabled(false);
            tv_on_date.setText("");
            tv_on_week.setText("— —");
        } else {
            isSingle = false;
            tv_off_date.setText(DateUtil.getDay("M月d日", beginTime));
            tv_off_week.setText(DateUtil.dateToWeek2(new Date(beginTime)));
            on_date_lay.setEnabled(true);
            tv_on_date.setText(DateUtil.getDay("M月d日", endTime));
            tv_on_week.setText(DateUtil.dateToWeek2(new Date(endTime)));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.i(TAG, "onActivityResult() " + requestCode + ", " + resultCode);
        if (Activity.RESULT_OK != resultCode) {
            return;
        }
        if (requestCode == REQUEST_CODE_DATE) {
            if (null != data) {
                beginTime = data.getLongExtra("beginTime", beginTime);
                endTime = data.getLongExtra("endTime", endTime);
                refreshPlaneStateAndInfo(selectedIndex);
            }
        }
        requestWeatherInfo(beginTime);
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
}
