package com.huicheng.hotel.android.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.amap.api.location.AMapLocation;
import com.huicheng.hotel.android.PRJApplication;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.HotelOrderManager;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.content.AppConst;
import com.huicheng.hotel.android.content.NetURL;
import com.huicheng.hotel.android.control.AMapLocationControl;
import com.huicheng.hotel.android.control.LocationInfo;
import com.huicheng.hotel.android.permission.PermissionsActivity;
import com.huicheng.hotel.android.permission.PermissionsDef;
import com.huicheng.hotel.android.requestbuilder.RequestBeanBuilder;
import com.huicheng.hotel.android.requestbuilder.bean.WeatherInfoBean;
import com.huicheng.hotel.android.tools.CityParseUtils;
import com.huicheng.hotel.android.ui.activity.UcBountyMainActivity;
import com.huicheng.hotel.android.ui.activity.UcCouponsActivity;
import com.huicheng.hotel.android.ui.activity.UcOrdersActivity;
import com.huicheng.hotel.android.ui.activity.hotel.HotelAllSearchActivity;
import com.huicheng.hotel.android.ui.activity.hotel.HotelCalendarChooseActivity;
import com.huicheng.hotel.android.ui.activity.hotel.HotelCityChooseActivity;
import com.huicheng.hotel.android.ui.activity.hotel.HotelListActivity;
import com.huicheng.hotel.android.ui.base.BaseFragment;
import com.huicheng.hotel.android.ui.custom.CustomConsiderLayoutForHome;
import com.huicheng.hotel.android.ui.dialog.CustomToast;
import com.prj.sdk.constants.BroadCastConst;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.net.data.ResponseData;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;

/**
 * @author kborid
 * @date 2018/2/9 0009.
 */

public class FragmentSwitcherHotel extends BaseFragment implements View.OnClickListener, DataCallback, AMapLocationControl.MyLocationListener {
    private static final int REQUEST_CODE_DATE = 0x01;
    private static final int REQUEST_CODE_CITY = 0x02;

    public static boolean isFirstLoad = false;
    private Bundle bundle = null;
    private boolean isSelectedDate = false;
    private long beginTime, endTime;

    private LinearLayout coupon_lay;
    private LinearLayout order_lay;
    private LinearLayout bounty_lay;

    private TextView tv_city;
    private TextView tv_hotel_search;
    private TextView tv_in_date, tv_days, tv_out_date;
    private TextView tv_search;
    private TextView tv_consider;
    private TextView tv_curr_position;

    //酒店首页筛选
    private CustomConsiderLayoutForHome mConsiderLayout = null;
    private PopupWindow mConsiderPopupWindow = null;
    private int typeIndex = -1, gradeIndex = -1, priceIndex = -1;

    //海南诚信广告Popup
    protected boolean isAdShowed = false;
    private View mAdHaiNanView = null;
    private PopupWindow mAdHaiNanPopupWindow = null;

    private static Handler myHandler = new Handler(Looper.getMainLooper());
    private AMapLocation aMapLocation = null;
    private boolean isRequestPermission = false;
    private boolean hasRejectPermission = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        isFirstLoad = true;
        bundle = getArguments();
        View view = inflater.inflate(R.layout.layout_content_hotel, container, false);
        initTypedArrayValue();
        initViews(view);
        initParams();
        initListeners();
        initPopupWindows();
        return view;
    }

    public static Fragment newInstance() {
        Fragment fragment = new FragmentSwitcherHotel();
        return fragment;
    }

    @Override
    protected void onVisible() {
        super.onVisible();
        if (isFirstLoad) {
            isFirstLoad = false;
            //如果画面跳转，则不定位，也不获取权限
            if (!isRequestPermission && !hasRejectPermission) {
                if (!LocationInfo.instance.isLocated()) {
                    myHandler.postDelayed(requestPermissionRunnable, 500);
                }
            }
            if (SessionContext.isLogin()) {
                requestUserMenusStatus();
            }
        }
    }

    private Runnable requestPermissionRunnable = new Runnable() {
        @Override
        public void run() {
            if (isVisible) {
                myHandler.removeCallbacksAndMessages(null);
                if (PRJApplication.getPermissionsChecker(getActivity()).lacksPermissions(PermissionsDef.LOCATION_PERMISSION)) {
                    PermissionsActivity.startActivityForResult(getActivity(), PermissionsDef.PERMISSION_REQ_CODE, PermissionsDef.LOCATION_PERMISSION);
                    return;
                }
                AMapLocationControl.getInstance().startLocationOnce(FragmentSwitcherHotel.this);
            } else {
                myHandler.postDelayed(requestPermissionRunnable, 500);
            }
        }
    };


    @Override
    protected void onInvisible() {
        super.onInvisible();
    }

    @Override
    protected void initViews(View view) {
        super.initViews(view);
        tv_city = (TextView) view.findViewById(R.id.tv_city);
        tv_hotel_search = (TextView) view.findViewById(R.id.tv_hotel_search);

        tv_in_date = (TextView) view.findViewById(R.id.tv_in_date);
        tv_days = (TextView) view.findViewById(R.id.tv_days);
        tv_out_date = (TextView) view.findViewById(R.id.tv_out_date);

        coupon_lay = (LinearLayout) view.findViewById(R.id.coupon_lay);
        order_lay = (LinearLayout) view.findViewById(R.id.order_lay);
        bounty_lay = (LinearLayout) view.findViewById(R.id.bounty_lay);
        tv_search = (TextView) view.findViewById(R.id.tv_search);
        tv_consider = (TextView) view.findViewById(R.id.tv_consider);

        //首页筛选菜单
        mConsiderLayout = new CustomConsiderLayoutForHome(getActivity());
        mConsiderLayout.initConsiderConfig();
        //评分筛选条件初始化
        SharedPreferenceUtil.getInstance().setInt(AppConst.CONSIDER_POINT, -1);
        mConsiderLayout.setOnConsiderLayoutListener(new CustomConsiderLayoutForHome.OnConsiderLayoutListener() {
            @Override
            public void onDismiss() {
                if (null != mConsiderPopupWindow) {
                    mConsiderPopupWindow.dismiss();
                }
            }

            @Override
            public void onResult(String str, int type, int grade, int price) {
                tv_consider.setText(str);
                typeIndex = type;
                gradeIndex = grade;
                priceIndex = price;
            }
        });
        mConsiderPopupWindow = new PopupWindow(mConsiderLayout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        mConsiderPopupWindow.setAnimationStyle(R.style.share_anim);
        mConsiderPopupWindow.setFocusable(true);
        mConsiderPopupWindow.setOutsideTouchable(true);
        mConsiderPopupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        mConsiderPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                //重置consider
                WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
                lp.alpha = 1f;
                getActivity().getWindow().setAttributes(lp);
            }
        });

        tv_curr_position = (TextView) view.findViewById(R.id.tv_curr_position);
    }

    @Override
    protected void initParams() {
        super.initParams();
        updateBeginTimeEndTime(); //初始化时间，1晚，今天到明天
        HotelOrderManager.getInstance().setBeginTime(beginTime);
        HotelOrderManager.getInstance().setEndTime(endTime);
        HotelOrderManager.getInstance().setDateStr(DateUtil.getDay("M.d", beginTime) + " - " + DateUtil.getDay("M.d", endTime));
        tv_in_date.setText(formatDateForBigDay(DateUtil.getDay("M月d日 ", beginTime) + DateUtil.dateToWeek2(new Date(beginTime))));
        tv_out_date.setText(formatDateForBigDay(DateUtil.getDay("M月d日 ", endTime) + DateUtil.dateToWeek2(new Date(endTime))));
        tv_days.setText(String.format(getString(R.string.duringNightStr), DateUtil.getGapCount(new Date(beginTime), new Date(endTime))));

        //地点信息
        tv_city.setHint("正在定位当前城市");
    }

    private void initPopupWindows() {
        //海南诚信认证广告
        if (null == mAdHaiNanView) {
            mAdHaiNanView = LayoutInflater.from(getActivity()).inflate(R.layout.pw_ad_hainan_layout, null);
            if (null == mAdHaiNanPopupWindow) {
                mAdHaiNanPopupWindow = new PopupWindow(mAdHaiNanView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
                mAdHaiNanPopupWindow.getContentView().findViewById(R.id.iv_ad_close).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mAdHaiNanPopupWindow.dismiss();
                    }
                });
                mAdHaiNanPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

                    @Override
                    public void onDismiss() {
                        //重置consider
                        WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
                        lp.alpha = 1f;
                        getActivity().getWindow().setAttributes(lp);
                    }
                });
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    int option = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
                    mAdHaiNanPopupWindow.setSoftInputMode(option);
                    try {
                        Field mLayoutInScreen = PopupWindow.class.getDeclaredField("mLayoutInScreen");
                        mLayoutInScreen.setAccessible(true);
                        mLayoutInScreen.set(mAdHaiNanPopupWindow, true);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void showHaiNanAdPopupWindow() {
        // 设置背景颜色变暗
        WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
        lp.alpha = 0.35f;
        getActivity().getWindow().setAttributes(lp);
        mAdHaiNanPopupWindow.showAtLocation(getActivity().getWindow().getDecorView(), Gravity.CENTER, 0, 0);
    }

    public void showHaiNanAd(String province) {
        if (province.contains("海南")) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    isAdShowed = true;
                    showHaiNanAdPopupWindow();
                }
            }, 500);
        }
    }

    private void updateBeginTimeEndTime() {
        if (!isSelectedDate) {
            Calendar calendar = Calendar.getInstance();
            beginTime = calendar.getTime().getTime();
            calendar.add(Calendar.DAY_OF_MONTH, +1); //+1今天的时间加一天
            endTime = calendar.getTime().getTime();
        }
    }

    private void requestUserMenusStatus() {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        ResponseData d = b.syncRequest(b);
        d.path = NetURL.MENUS_STATUS;
        d.flag = AppConst.MENUS_STATUS;
        DataLoader.getInstance().loadData(this, d);
    }

    private void updateUserMenuStatus(String str) {
        JSONObject mJson = JSON.parseObject(str);
        boolean couponflag = mJson.containsKey("couponflag") ? mJson.getBoolean("couponflag") : false;
        int couponResId = couponflag ? R.drawable.iv_my_coupon_red : R.drawable.iv_my_coupon_normal;
        ((ImageView) coupon_lay.findViewById(R.id.iv_coupon_icon)).setImageResource(couponResId);

        boolean orderflag = mJson.containsKey("orderflag") ? mJson.getBoolean("orderflag") : false;
        int orderResId = orderflag ? R.drawable.iv_my_order_red : R.drawable.iv_my_order_normal;
        ((ImageView) order_lay.findViewById(R.id.iv_order_icon)).setImageResource(orderResId);

        boolean bountyflag = mJson.containsKey("bountyflag") ? mJson.getBoolean("bountyflag") : false;
        int bountyResId = bountyflag ? R.drawable.iv_my_bounty_red : R.drawable.iv_my_bounty_normal;
        ((ImageView) bounty_lay.findViewById(R.id.iv_bounty_icon)).setImageResource(bountyResId);
    }

    private void showConsiderPopupWindow() {
        mConsiderLayout.reloadConsiderConfig(typeIndex, gradeIndex, priceIndex);
        // 设置背景颜色变暗
        WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
        lp.alpha = 0.5f;
        getActivity().getWindow().setAttributes(lp);
        mConsiderPopupWindow.showAtLocation(getActivity().getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
    }

    @Override
    protected void initListeners() {
        super.initListeners();
        tv_city.setOnClickListener(this);
        tv_in_date.setOnClickListener(this);
        tv_out_date.setOnClickListener(this);
        tv_hotel_search.setOnClickListener(this);
        coupon_lay.setOnClickListener(this);
        order_lay.setOnClickListener(this);
        bounty_lay.setOnClickListener(this);
        tv_search.setOnClickListener(this);
        tv_consider.setOnClickListener(this);
        tv_curr_position.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.tv_city: {
                updateBeginTimeEndTime();
                HotelOrderManager.getInstance().setBeginTime(beginTime);
                HotelOrderManager.getInstance().setEndTime(endTime);
                Intent resIntent = new Intent(getActivity(), HotelCityChooseActivity.class);
                startActivityForResult(resIntent, REQUEST_CODE_CITY);
                break;
            }
            case R.id.coupon_lay:
                if (!SessionContext.isLogin()) {
                    getActivity().sendBroadcast(new Intent(BroadCastConst.UNLOGIN_ACTION));
                    return;
                }
                ((ImageView) coupon_lay.findViewById(R.id.iv_coupon_icon)).setImageResource(R.drawable.iv_my_coupon_normal);
                intent = new Intent(getActivity(), UcCouponsActivity.class);
                break;
            case R.id.order_lay:
                if (!SessionContext.isLogin()) {
                    getActivity().sendBroadcast(new Intent(BroadCastConst.UNLOGIN_ACTION));
                    return;
                }
                ((ImageView) order_lay.findViewById(R.id.iv_order_icon)).setImageResource(R.drawable.iv_my_order_normal);
                intent = new Intent(getActivity(), UcOrdersActivity.class);
                break;
            case R.id.bounty_lay:
                if (!SessionContext.isLogin()) {
                    getActivity().sendBroadcast(new Intent(BroadCastConst.UNLOGIN_ACTION));
                    return;
                }
                ((ImageView) bounty_lay.findViewById(R.id.iv_bounty_icon)).setImageResource(R.drawable.iv_my_bounty_normal);
                intent = new Intent(getActivity(), UcBountyMainActivity.class);
                break;
            case R.id.tv_hotel_search:
                if (StringUtil.isEmpty(tv_city.getText().toString())) {
                    CustomToast.show("城市定位失败，请打开GPS或手动选择城市", CustomToast.LENGTH_SHORT);
                    return;
                }
                HotelOrderManager.getInstance().reset();
                updateBeginTimeEndTime();
                HotelOrderManager.getInstance().setBeginTime(beginTime);
                HotelOrderManager.getInstance().setEndTime(endTime);
                HotelOrderManager.getInstance().setDateStr(DateUtil.getDay("M.d", beginTime) + " - " + DateUtil.getDay("M.d", endTime));
                intent = new Intent(getActivity(), HotelListActivity.class);
                intent.putExtra("index", 0);
                intent.putExtra("keyword", "");
                if (LocationInfo.instance.isMyLoc() && null != aMapLocation && aMapLocation.getLatitude() > 0 && aMapLocation.getLongitude() > 0) {
                    intent.putExtra("landmark", tv_city.getText().toString());
                    intent.putExtra("isLandMark", true);
                    intent.putExtra("lonLat", String.format("%1$f|%2$f", aMapLocation.getLatitude(), aMapLocation.getLongitude()));
                    intent.putExtra("siteId", aMapLocation.getAdCode());
                }

                break;
            case R.id.tv_in_date:
            case R.id.tv_out_date: {
                Intent resIntent = new Intent(getActivity(), HotelCalendarChooseActivity.class);
                resIntent.putExtra("isTitleCanClick", true);
//                resIntent.putExtra("beginTime", beginTime);
//                resIntent.putExtra("endTime", endTime);
                startActivityForResult(resIntent, REQUEST_CODE_DATE);
                break;
            }
            case R.id.tv_search:
                updateBeginTimeEndTime();
                HotelOrderManager.getInstance().setBeginTime(beginTime);
                HotelOrderManager.getInstance().setEndTime(endTime);
                HotelOrderManager.getInstance().setDateStr(DateUtil.getDay("M.d", beginTime) + " - " + DateUtil.getDay("M.d", endTime));
                intent = new Intent(getActivity(), HotelAllSearchActivity.class);
                break;
            case R.id.tv_consider:
                showConsiderPopupWindow();
                break;
            case R.id.tv_curr_position:
                tv_city.setHint("正在获取当前位置");
                tv_city.setText("");
                LocationInfo.instance.setIsMyLoc(true);
                //权限检查
                if (PRJApplication.getPermissionsChecker(getActivity()).lacksPermissions(PermissionsDef.LOCATION_PERMISSION)) {
                    PermissionsActivity.startActivityForResult(getActivity(), PermissionsDef.PERMISSION_REQ_CODE, PermissionsDef.LOCATION_PERMISSION);
                    return;
                }
                AMapLocationControl.getInstance().startLocationOnce(this);
                break;
        }
        if (null != intent) {
            startActivity(intent);
        }
    }

    private void requestWeatherInfo(long timeStamp) {
        RequestBeanBuilder b = RequestBeanBuilder.create(false);
        b.addBody("cityname", LocationInfo.instance.getCity());
        b.addBody("siteid", LocationInfo.instance.getCityCode());
        b.addBody("date", DateUtil.getDay("yyyyMMdd", timeStamp));
        ResponseData d = b.syncRequest(b);
        d.path = NetURL.WEATHER;
        d.flag = AppConst.WEATHER;
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void refreshCityDisplay(String city) {
        String tempProvince = LocationInfo.instance.getProvince();
        HotelOrderManager.getInstance().setCityStr(CityParseUtils.getProvinceCityString(tempProvince, city, "-"));
        tv_city.setText(CityParseUtils.getCityString(city));
        if (!isAdShowed) {
            showHaiNanAd(tempProvince);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.i(TAG, "onActivityResult() " + requestCode + ", " + resultCode);

        if (requestCode == PermissionsDef.PERMISSION_REQ_CODE) {
            isRequestPermission = true;
            if (resultCode == PermissionsDef.PERMISSIONS_GRANTED) {
                hasRejectPermission = false;
                //地点信息
                tv_city.setHint("正在定位当前城市");
                AMapLocationControl.getInstance().startLocationOnce(this);
            } else {
                hasRejectPermission = true;
                tv_city.setHint("城市定位失败");
            }
        }

        if (Activity.RESULT_OK != resultCode) {
            return;
        }

        if (requestCode == REQUEST_CODE_CITY) {
            tv_city.setHint("正在定位当前城市");
            LocationInfo.instance.setIsMyLoc(false);
            aMapLocation = null;
            refreshCityDisplay(LocationInfo.instance.getCity());
            if (AMapLocationControl.getInstance().isStart()) {
                AMapLocationControl.getInstance().stopLocation();
            }
        } else if (requestCode == REQUEST_CODE_DATE) {
            if (null != data) {
                isSelectedDate = true;
                beginTime = data.getLongExtra("beginTime", beginTime);
                endTime = data.getLongExtra("endTime", endTime);
                HotelOrderManager.getInstance().setBeginTime(beginTime);
                HotelOrderManager.getInstance().setEndTime(endTime);
                HotelOrderManager.getInstance().setDateStr(DateUtil.getDay("M.d", beginTime) + " - " + DateUtil.getDay("M.d", endTime));
                tv_in_date.setText(formatDateForBigDay(DateUtil.getDay("M月d日 ", beginTime) + DateUtil.dateToWeek2(new Date(beginTime))));
                tv_out_date.setText(formatDateForBigDay(DateUtil.getDay("M月d日 ", endTime) + DateUtil.dateToWeek2(new Date(endTime))));
                tv_days.setText(String.format(getString(R.string.duringNightStr), DateUtil.getGapCount(new Date(beginTime), new Date(endTime))));
            }
        }
        requestWeatherInfo(beginTime);
    }

    private SpannableString formatDateForBigDay(String date) {
        if (StringUtil.notEmpty(date)) {
            int endIndex = date.indexOf("周");
            SpannableString ss = new SpannableString(date);
            ss.setSpan(new AbsoluteSizeSpan(21, true), 0, endIndex,
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            ss.setSpan(new ForegroundColorSpan(Color.parseColor("#989898")), endIndex, date.length(),
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            return ss;
        }
        return new SpannableString("");
    }

    @Override
    public void onLocation(boolean isSuccess, AMapLocation aMapLocation) {
        LogUtil.i(TAG, "onLocation()");
        if (isSuccess && aMapLocation != null) {
            LocationInfo.instance.setInfo(String.valueOf(aMapLocation.getLongitude()),
                    String.valueOf(aMapLocation.getLatitude()),
                    CityParseUtils.getProvinceString(aMapLocation.getProvince()),
                    CityParseUtils.getProvinceString(aMapLocation.getCity()),
                    String.valueOf(aMapLocation.getAdCode()));
            String tmp = CityParseUtils.getCityString(LocationInfo.instance.getCity());
            LogUtil.i("\n" + aMapLocation.toString().replace("#", "\n"));
            if (LocationInfo.instance.isMyLoc()) {
                this.aMapLocation = aMapLocation;
                tmp = aMapLocation.getPoiName();
                if (StringUtil.isEmpty(tmp)) {
                    tmp = aMapLocation.getStreet() + aMapLocation.getStreetNum();
                }
            }
            refreshCityDisplay(tmp);
            requestWeatherInfo(beginTime);
        } else {
            tv_city.setHint("城市定位失败");
        }
    }

    @Override
    public void preExecute(ResponseData request) {

    }

    @Override
    public void notifyMessage(ResponseData request, ResponseData response) throws Exception {
        if (response != null && response.body != null) {
            if (request.flag == AppConst.MENUS_STATUS) {
                LogUtil.i(TAG, "json = " + response.body.toString());
                if (StringUtil.notEmpty(response.body.toString()) && !"{}".equals(response.body.toString())) {
                    updateUserMenuStatus(response.body.toString());
                }
            } else if (request.flag == AppConst.WEATHER) {
                LogUtil.i(TAG, "json = " + response.body.toString());
                if (StringUtil.notEmpty(response.body.toString()) && !"{}".equals(response.body.toString())) {
                    WeatherInfoBean bean = JSON.parseObject(response.body.toString(), WeatherInfoBean.class);
//                    banner_lay.updateWeatherInfo(beginTime, bean);
                } else {
//                    banner_lay.updateWeatherInfo(beginTime, null);
                }
            }
        }
    }

    @Override
    public void notifyError(ResponseData request, ResponseData response, Exception e) {
        if (response != null && response.data != null) {
            if (request.flag == AppConst.WEATHER) {
//                banner_lay.updateWeatherInfo(beginTime, null);
            }
        }
    }
}
