package com.huicheng.hotel.android.ui.activity.plane;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.support.design.widget.TabLayout;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.PlaneCommDef;
import com.huicheng.hotel.android.common.PlaneOrderManager;
import com.huicheng.hotel.android.common.RequestCodeDef;
import com.huicheng.hotel.android.requestbuilder.bean.AddressInfoBean;
import com.huicheng.hotel.android.requestbuilder.bean.CityAirportInfoBean;
import com.huicheng.hotel.android.ui.activity.BaseMainActivity;
import com.huicheng.hotel.android.ui.activity.hotel.HotelCalendarChooseActivity;
import com.huicheng.hotel.android.ui.dialog.CustomToast;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.Utils;

import java.lang.reflect.Field;
import java.util.Date;

public class PlaneMainActivity extends BaseMainActivity {

    private WindowManager mWindowManager;
    private TabLayout tabs;
    private int selectedIndex = 0;

    private ImageView iv_change;
    private CityAirportInfoBean off, on;
    private LinearLayout off_land_city_info, on_land_city_info;
    private TextView tv_off_city, tv_on_city;
    private TextView tv_off_piny, tv_on_piny;
    private int height;
    private LinearLayout off_date_lay, on_date_lay;
    private TextView tv_off_date, tv_off_week;
    private TextView tv_on_date, tv_on_week;

    private TextView tv_next_search;

    private ImageView copyViewOff, copyViewOn;
    private int[] mOffLocation;
    private int[] mOnLocation;

    public void initViews() {
        super.initViews();
        initContentLayout(LayoutInflater.from(this).inflate(R.layout.layout_content_plane, null));
        tabs = (TabLayout) findViewById(R.id.tabs);
        off_land_city_info = (LinearLayout) findViewById(R.id.off_land_city_info);
        on_land_city_info = (LinearLayout) findViewById(R.id.on_land_city_info);
        tv_off_city = (TextView) findViewById(R.id.tv_off_city);
        tv_off_piny = (TextView) findViewById(R.id.tv_off_piny);
        tv_on_city = (TextView) findViewById(R.id.tv_on_city);
        tv_on_piny = (TextView) findViewById(R.id.tv_on_piny);

        off_date_lay = (LinearLayout) findViewById(R.id.off_date_lay);
        tv_off_date = (TextView) findViewById(R.id.tv_off_date);
        tv_off_week = (TextView) findViewById(R.id.tv_off_week);
        on_date_lay = (LinearLayout) findViewById(R.id.on_date_lay);
        tv_on_date = (TextView) findViewById(R.id.tv_on_date);
        tv_on_week = (TextView) findViewById(R.id.tv_on_week);
        iv_change = (ImageView) findViewById(R.id.iv_change);
        tv_next_search = (TextView) findViewById(R.id.tv_next_search);
    }

    @Override
    public void initParams() {
        super.initParams();
        mWindowManager = getWindowManager();
        tabs.addTab(tabs.newTab().setText(getString(R.string.plane_single)), PlaneCommDef.FLIGHT_SINGLE, true);
        tabs.addTab(tabs.newTab().setText(getString(R.string.plane_double)), PlaneCommDef.FLIGHT_GOBACK, false);
        setIndicator(tabs, 54, 54);
        refreshPlaneStateAndInfo(PlaneCommDef.FLIGHT_SINGLE);
        PlaneOrderManager.instance.setFlightType(PlaneCommDef.FLIGHT_SINGLE);
        //初始化起飞
        PlaneOrderManager.instance.setGoFlightOffDate(beginTime);
        PlaneOrderManager.instance.setBackFlightOffDate(endTime);
        //test
        off = new CityAirportInfoBean();
        off.cityname = "上海";
        off.pinyin = "SHANGHAI";
        on = new CityAirportInfoBean();
        on.cityname = "北京";
        on.pinyin = "BEIJING";
        PlaneOrderManager.instance.setFlightOnAirportInfo(on);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        LogUtil.i(TAG, "onWindowFocusChanged()");
        height = off_land_city_info.getHeight();
    }

    @Override
    public void initListeners() {
        super.initListeners();
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
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
        off_land_city_info.setOnClickListener(this);
        on_land_city_info.setOnClickListener(this);
        iv_change.setOnClickListener(this);
        off_date_lay.setOnClickListener(this);
        on_date_lay.setOnClickListener(this);
        tv_next_search.setOnClickListener(this);
        tv_next_search.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent intent = new Intent(PlaneMainActivity.this, PlaneAddrChooserActivity.class);
                startActivityForResult(intent, RequestCodeDef.REQ_CODE_ADDRESS_MANAGER);
                return true;
            }
        });
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.off_date_lay:
            case R.id.on_date_lay: {
                Intent resIntent = new Intent(this, HotelCalendarChooseActivity.class);
                resIntent.putExtra("isTitleCanClick", true);
//                resIntent.putExtra("beginTime", beginTime);
//                resIntent.putExtra("endTime", endTime);
                startActivityForResult(resIntent, REQUEST_CODE_DATE);
                break;
            }
            case R.id.tv_next_search:
                PlaneOrderManager.instance.reset();
                updateBeginTimeEndTime();
                PlaneOrderManager.instance.setFlightType((selectedIndex == 0) ? PlaneCommDef.FLIGHT_SINGLE : PlaneCommDef.FLIGHT_GOBACK);
                PlaneOrderManager.instance.setGoFlightOffDate(beginTime);
                PlaneOrderManager.instance.setBackFlightOffDate(endTime);
                PlaneOrderManager.instance.setFlightOffAirportInfo(off);
                PlaneOrderManager.instance.setFlightOnAirportInfo(on);
                Intent nextIntent = new Intent(this, PlaneFlightListActivity.class);
                startActivity(nextIntent);
                break;
            case R.id.off_land_city_info:
                Intent offIntent = new Intent(this, PlaneAirportChooserActivity.class);
                offIntent.putExtra("type", "OFF");
                startActivityForResult(offIntent, REQUEST_CODE_AIRPORT);
                break;
            case R.id.on_land_city_info:
                Intent onIntent = new Intent(this, PlaneAirportChooserActivity.class);
                onIntent.putExtra("type", "ON");
                startActivityForResult(onIntent, REQUEST_CODE_AIRPORT);
                break;
            case R.id.iv_change:
                doAnim();
                iv_change.setEnabled(false);
                break;
        }
    }

    private void refreshPlaneStateAndInfo(int pos) {
        if (pos == PlaneCommDef.FLIGHT_SINGLE) {
            tv_off_date.setText(DateUtil.getDay("M月d日", beginTime));
            tv_off_week.setText(DateUtil.dateToWeek2(new Date(beginTime)));
            on_date_lay.setEnabled(false);
            tv_on_date.setText("");
            tv_on_week.setText("— —");
        } else {
            tv_off_date.setText(DateUtil.getDay("M月d日", beginTime));
            tv_off_week.setText(DateUtil.dateToWeek2(new Date(beginTime)));
            on_date_lay.setEnabled(true);
            tv_on_date.setText(DateUtil.getDay("M月d日", endTime));
            tv_on_week.setText(DateUtil.dateToWeek2(new Date(endTime)));
        }
    }

    /**
     * 交换动画入口
     */
    private void doAnim() {
        //创建出镜像view
        createCopyView();
        //隐藏原有view
//        off_land_city_info.setVisibility(View.INVISIBLE);
//        on_land_city_info.setVisibility(View.INVISIBLE);
        //开启镜像view的动画
        offAnim(mOffLocation[1] - Utils.mStatusBarHeight, height);
        onAnim(mOnLocation[1] - Utils.mStatusBarHeight, -height);
    }

    private void offAnim(final int defY, int offset) {
        //隐藏原有的view，增加100ms动画避免闪烁
        ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(off_land_city_info, "alpha", 1f, 0f);
        alphaAnim.setDuration(100);
        alphaAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                off_land_city_info.setVisibility(View.INVISIBLE);
            }
        });
        alphaAnim.start();

        ValueAnimator offAnim = ValueAnimator.ofInt(0, offset);
        offAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int animatedValue = (int) valueAnimator.getAnimatedValue();
                WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) copyViewOff.getLayoutParams();
                layoutParams.y = defY + animatedValue;
                mWindowManager.updateViewLayout(copyViewOff, layoutParams);
            }
        });
        offAnim.setDuration(600);
        offAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                CityAirportInfoBean bean = on;
                on = off;
                off = bean;

                refreshTextDisplay();
                off_land_city_info.setAlpha(1f);
                off_land_city_info.setVisibility(View.VISIBLE);
                mWindowManager.removeView(copyViewOff);
                copyViewOff = null;
                iv_change.setEnabled(true);
            }
        });
        offAnim.start();
    }

    private void onAnim(final int defY, int offset) {
        //隐藏原有的view，增加100ms动画避免闪烁
        ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(on_land_city_info, "alpha", 1f, 0f);
        alphaAnim.setDuration(100);
        alphaAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                on_land_city_info.setVisibility(View.INVISIBLE);
            }
        });
        alphaAnim.start();

        ValueAnimator onAnim = ValueAnimator.ofInt(0, offset);
        onAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int animatedValue = (int) valueAnimator.getAnimatedValue();
                WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) copyViewOn.getLayoutParams();
                layoutParams.y = defY + animatedValue;
                mWindowManager.updateViewLayout(copyViewOn, layoutParams);
            }
        });
        onAnim.setDuration(600);
        onAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                on_land_city_info.setAlpha(1f);
                on_land_city_info.setVisibility(View.VISIBLE);
                mWindowManager.removeView(copyViewOn);
                copyViewOn = null;
            }
        });
        onAnim.start();
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
        if (requestCode == RequestCodeDef.REQ_CODE_ADDRESS_SET_DEFAULT) {
            if (null != data) {
                AddressInfoBean bean = (AddressInfoBean) data.getSerializableExtra("address");
                CustomToast.show(bean.province + bean.city + bean.area + bean.address, CustomToast.LENGTH_LONG);
            }
        }
        if (requestCode == REQUEST_CODE_DATE) {
            if (null != data) {
                isSelectedDate = true;
                beginTime = data.getLongExtra("beginTime", beginTime);
                endTime = data.getLongExtra("endTime", endTime);
                PlaneOrderManager.instance.setGoFlightOffDate(beginTime);
                PlaneOrderManager.instance.setBackFlightOffDate(endTime);
                refreshPlaneStateAndInfo(selectedIndex);
            }
        } else if (requestCode == REQUEST_CODE_AIRPORT) {
            String airport = data.getStringExtra("type");
            CityAirportInfoBean bean = (CityAirportInfoBean) data.getSerializableExtra("cityAirport");
            if ("OFF".equals(airport)) {
                off = bean;
            } else {
                on = bean;
            }
            refreshTextDisplay();
        }
        requestWeatherInfo(beginTime);
    }

    private void refreshTextDisplay() {
        if (null != off) {
            tv_off_city.setText(off.cityname);
            tv_off_piny.setText(off.pinyin);
        }
        if (null != on) {
            tv_on_city.setText(on.cityname);
            tv_on_piny.setText(on.pinyin);
        }
    }

    /**
     * 通过反射设置TabLayout的Indicator的宽度
     */
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

    /**
     * 创建镜像view
     */
    private ImageView createCopyView(int x, int y, Bitmap bitmap) {
        WindowManager.LayoutParams mLayoutParams = new WindowManager.LayoutParams();
        mLayoutParams.format = PixelFormat.TRANSLUCENT; //图片之外其他地方透明
        mLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        mLayoutParams.x = x; //设置imageView的原点
        mLayoutParams.y = y - Utils.mStatusBarHeight;
        mLayoutParams.alpha = 1f; //设置透明度
        mLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        ImageView copyView = new ImageView(this);
        copyView.setImageBitmap(bitmap);
        mWindowManager.addView(copyView, mLayoutParams); //添加该View到window
        return copyView;
    }

    /**
     * 创建镜像view
     */
    private void createCopyView() {
        mOffLocation = new int[2];
        mOnLocation = new int[2];
        //获取相对window的坐标
        off_land_city_info.getLocationInWindow(mOffLocation);
        on_land_city_info.getLocationInWindow(mOnLocation);
        //缓存layout
        off_land_city_info.setDrawingCacheEnabled(true);
        Bitmap offCacheBitmap = Bitmap.createBitmap(off_land_city_info.getDrawingCache());
        off_land_city_info.destroyDrawingCache();
        on_land_city_info.setDrawingCacheEnabled(true);
        Bitmap onCacheBitmap = Bitmap.createBitmap(on_land_city_info.getDrawingCache());
        on_land_city_info.destroyDrawingCache();

        //创建出两个镜像view
        copyViewOff = createCopyView(mOffLocation[0], mOffLocation[1], offCacheBitmap);
        copyViewOn = createCopyView(mOnLocation[0], mOnLocation[1], onCacheBitmap);
        //释放bitmap资源
        offCacheBitmap = null;
        onCacheBitmap = null;
    }
}
