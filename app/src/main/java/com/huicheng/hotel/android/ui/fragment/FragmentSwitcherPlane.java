package com.huicheng.hotel.android.ui.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
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
import com.huicheng.hotel.android.requestbuilder.bean.CityAirportInfoBean;
import com.huicheng.hotel.android.ui.activity.plane.PlaneAirportChooserActivity;
import com.huicheng.hotel.android.ui.activity.plane.PlaneCalendarChooseActivity;
import com.huicheng.hotel.android.ui.activity.plane.PlaneFlightListActivity;
import com.huicheng.hotel.android.ui.base.BaseFragment;
import com.huicheng.hotel.android.ui.dialog.CustomToast;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;

/**
 * @author kborid
 * @date 2018/2/9 0009.
 */

public class FragmentSwitcherPlane extends BaseFragment implements View.OnClickListener {
    private static final int REQUEST_CODE_DATE = 0x01;
    private static final int REQUEST_CODE_AIRPORT = 0x03;
    public static boolean isFirstLoad = false;
    private Bundle bundle = null;

    private WindowManager mWindowManager;
    private TabLayout tabs;
    private int mFlightType = PlaneCommDef.FLIGHT_SINGLE;
    private boolean isSelectedDate = false;
    private long beginTime, endTime;

    private ImageView iv_change;
    private CityAirportInfoBean off, on;
    private LinearLayout off_land_city_info, on_land_city_info;
    private TextView tv_off_city, tv_on_city;
    private TextView tv_off_piny, tv_on_piny;
    private int height;
    private LinearLayout off_date_lay, on_date_lay;
    private TextView tv_off_date, tv_off_week;
    private TextView tv_on_date, tv_on_week;

    private TextView tv_plane_search;

    private ImageView copyViewOff, copyViewOn;
    private int[] mOffLocation;
    private int[] mOnLocation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        isFirstLoad = true;
        bundle = getArguments();
        View view = inflater.inflate(R.layout.layout_content_plane, container, false);
        initTypedArrayValue();
        initViews(view);
        initParams();
        initListeners();
        return view;
    }

    public static Fragment newInstance() {
        Fragment fragment = new FragmentSwitcherPlane();
        fragment.setArguments(null);
        return fragment;
    }

    @Override
    protected void onVisible() {
        super.onVisible();
        if (isFirstLoad) {
            isFirstLoad = false;
        }
    }

    @Override
    protected void onInvisible() {
        super.onInvisible();
    }

    @Override
    protected void initTypedArrayValue() {
        super.initTypedArrayValue();
        TypedArray ta = getActivity().obtainStyledAttributes(R.styleable.MyTheme);
        mMainColor = ta.getResourceId(R.styleable.MyTheme_mainColor, R.color.plane_mainColor);
        mSwipeRefreshColor = ta.getResourceId(R.styleable.MyTheme_hotelRefreshColor, mMainColor);
        ta.recycle();
    }

    @Override
    protected void initViews(View view) {
        super.initViews(view);
        tabs = (TabLayout) view.findViewById(R.id.tabs);
        off_land_city_info = (LinearLayout) view.findViewById(R.id.off_land_city_info);
        on_land_city_info = (LinearLayout) view.findViewById(R.id.on_land_city_info);
        tv_off_city = (TextView) view.findViewById(R.id.tv_off_city);
        tv_off_piny = (TextView) view.findViewById(R.id.tv_off_piny);
        tv_on_city = (TextView) view.findViewById(R.id.tv_on_city);
        tv_on_piny = (TextView) view.findViewById(R.id.tv_on_piny);

        off_date_lay = (LinearLayout) view.findViewById(R.id.off_date_lay);
        tv_off_date = (TextView) view.findViewById(R.id.tv_off_date);
        tv_off_week = (TextView) view.findViewById(R.id.tv_off_week);
        on_date_lay = (LinearLayout) view.findViewById(R.id.on_date_lay);
        tv_on_date = (TextView) view.findViewById(R.id.tv_on_date);
        tv_on_week = (TextView) view.findViewById(R.id.tv_on_week);
        iv_change = (ImageView) view.findViewById(R.id.iv_change);
        tv_plane_search = (TextView) view.findViewById(R.id.tv_plane_search);
    }

    @Override
    protected void initParams() {
        super.initParams();
        mWindowManager = getActivity().getWindowManager();
        tabs.addTab(tabs.newTab().setText(getString(R.string.plane_single)), PlaneCommDef.FLIGHT_SINGLE, true);
        tabs.addTab(tabs.newTab().setText(getString(R.string.plane_double)), PlaneCommDef.FLIGHT_GOBACK, false);
        setIndicator(tabs, 54, 54);
        updateBeginTimeEndTime();
        refreshPlaneStateAndInfo(mFlightType);
        //test
        off = new CityAirportInfoBean();
        off.cityname = "上海";
        off.pinyin = "SHANGHAI";
        on = new CityAirportInfoBean();
        on.cityname = "北京";
        on.pinyin = "BEIJING";
        PlaneOrderManager.instance.setFlightOnAirportInfo(on);

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                height = off_land_city_info.getHeight();
            }
        });
    }

    private void updateBeginTimeEndTime() {
        if (!isSelectedDate) {
            Calendar calendar = Calendar.getInstance();
            beginTime = calendar.getTime().getTime();
            calendar.add(Calendar.DAY_OF_MONTH, +1); //+1今天的时间加一天
            endTime = calendar.getTime().getTime();
        }
    }

    @Override
    protected void initListeners() {
        super.initListeners();
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mFlightType = tab.getPosition();
                PlaneOrderManager.instance.setFlightType(mFlightType);
                refreshPlaneStateAndInfo(mFlightType);
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
        tv_plane_search.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.off_date_lay:
            case R.id.on_date_lay: {
                Intent resIntent = new Intent(getActivity(), PlaneCalendarChooseActivity.class);
                resIntent.putExtra("isTitleCanClick", true);
//                resIntent.putExtra("beginTime", beginTime);
//                resIntent.putExtra("endTime", endTime);
                startActivityForResult(resIntent, REQUEST_CODE_DATE);
                break;
            }
            case R.id.tv_plane_search:
                if (PlaneOrderManager.instance.isFlightGoBack()
                        && (StringUtil.isEmpty(tv_on_date.getText().toString()) || "— —".equals(tv_on_week.getText().toString()))) {
                    CustomToast.show("请选择返程日期", CustomToast.LENGTH_SHORT);
                    return;
                }
                PlaneOrderManager.instance.reset();
                updateBeginTimeEndTime();
                PlaneOrderManager.instance.setFlightType(mFlightType);
                PlaneOrderManager.instance.setGoFlightOffDate(beginTime);
                PlaneOrderManager.instance.setBackFlightOffDate(endTime);
                PlaneOrderManager.instance.setFlightOffAirportInfo(off);
                PlaneOrderManager.instance.setFlightOnAirportInfo(on);
                Intent nextIntent = new Intent(getActivity(), PlaneFlightListActivity.class);
                startActivity(nextIntent);
                break;
            case R.id.off_land_city_info:
                Intent offIntent = new Intent(getActivity(), PlaneAirportChooserActivity.class);
                offIntent.putExtra("type", "OFF");
                startActivityForResult(offIntent, REQUEST_CODE_AIRPORT);
                break;
            case R.id.on_land_city_info:
                Intent onIntent = new Intent(getActivity(), PlaneAirportChooserActivity.class);
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
            if (endTime > beginTime) {
                tv_on_date.setText(DateUtil.getDay("M月d日", endTime));
                tv_on_week.setText(DateUtil.dateToWeek2(new Date(endTime)));
            } else {
                tv_on_date.setText("");
                tv_on_week.setText("— —");
            }
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.i(TAG, "onActivityResult() " + requestCode + ", " + resultCode);
        if (Activity.RESULT_OK != resultCode) {
            return;
        }
        if (requestCode == REQUEST_CODE_DATE) {
            if (null != data) {
                isSelectedDate = true;
                beginTime = data.getLongExtra("beginTime", beginTime);
                endTime = data.getLongExtra("endTime", endTime);
                PlaneOrderManager.instance.setGoFlightOffDate(beginTime);
                PlaneOrderManager.instance.setBackFlightOffDate(endTime);
                refreshPlaneStateAndInfo(mFlightType);
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
        //TODO
//        requestWeatherInfo(beginTime);
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
        ImageView copyView = new ImageView(getActivity());
        copyView.setImageBitmap(bitmap);
        mWindowManager.addView(copyView, mLayoutParams); //添加该View到window
        return copyView;
    }

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
