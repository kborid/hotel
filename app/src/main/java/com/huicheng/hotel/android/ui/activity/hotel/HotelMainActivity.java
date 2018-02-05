package com.huicheng.hotel.android.ui.activity.hotel;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
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
import com.huicheng.hotel.android.common.ShareTypeDef;
import com.huicheng.hotel.android.content.AppConst;
import com.huicheng.hotel.android.content.NetURL;
import com.huicheng.hotel.android.control.AMapLocationControl;
import com.huicheng.hotel.android.control.LocationInfo;
import com.huicheng.hotel.android.permission.PermissionsActivity;
import com.huicheng.hotel.android.permission.PermissionsDef;
import com.huicheng.hotel.android.requestbuilder.RequestBeanBuilder;
import com.huicheng.hotel.android.tools.CityParseUtils;
import com.huicheng.hotel.android.ui.activity.BaseMainActivity;
import com.huicheng.hotel.android.ui.activity.UcBountyMainActivity;
import com.huicheng.hotel.android.ui.activity.UcCouponsActivity;
import com.huicheng.hotel.android.ui.activity.UcOrdersActivity;
import com.huicheng.hotel.android.ui.custom.CustomConsiderLayoutForHome;
import com.huicheng.hotel.android.ui.dialog.CustomToast;
import com.prj.sdk.constants.BroadCastConst;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.net.data.ResponseData;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;

import java.util.Date;

public class HotelMainActivity extends BaseMainActivity implements AMapLocationControl.MyLocationListener {

    private LinearLayout coupon_lay;
    private LinearLayout order_lay;
    private LinearLayout bounty_lay;

    private TextView tv_city;
    private TextView tv_next_search;
    private TextView tv_in_date, tv_days, tv_out_date;
    private TextView tv_search;
    private TextView tv_consider;

    private CustomConsiderLayoutForHome mConsiderLayout = null;
    private PopupWindow mConsiderPopupWindow = null;
    private int typeIndex = -1, gradeIndex = -1, priceIndex = -1;

    private TextView tv_curr_position;
    private AMapLocation aMapLocation = null;
    private boolean isRequestPermission = false;
    private boolean hasRejectPermission = false;
    private boolean isOnPause = false;


    public void initViews() {
        super.initViews();
        initContentLayout(LayoutInflater.from(this).inflate(R.layout.layout_content_hotel, null));

        tv_city = (TextView) findViewById(R.id.tv_city);
        tv_next_search = (TextView) findViewById(R.id.tv_next_search);

        tv_in_date = (TextView) findViewById(R.id.tv_in_date);
        tv_days = (TextView) findViewById(R.id.tv_days);
        tv_out_date = (TextView) findViewById(R.id.tv_out_date);

        coupon_lay = (LinearLayout) findViewById(R.id.coupon_lay);
        order_lay = (LinearLayout) findViewById(R.id.order_lay);
        bounty_lay = (LinearLayout) findViewById(R.id.bounty_lay);
        tv_search = (TextView) findViewById(R.id.tv_search);
        tv_consider = (TextView) findViewById(R.id.tv_consider);

        //首页筛选菜单
        mConsiderLayout = new CustomConsiderLayoutForHome(this);
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
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1f;
                getWindow().setAttributes(lp);
            }
        });

        tv_curr_position = (TextView) findViewById(R.id.tv_curr_position);
    }

    @Override
    public void initParams() {
        super.initParams();
        HotelOrderManager.getInstance().setBeginTime(beginTime);
        HotelOrderManager.getInstance().setEndTime(endTime);
        HotelOrderManager.getInstance().setDateStr(DateUtil.getDay("M.d", beginTime) + " - " + DateUtil.getDay("M.d", endTime));
        tv_in_date.setText(formatDateForBigDay(DateUtil.getDay("M月d日 ", beginTime) + DateUtil.dateToWeek2(new Date(beginTime))));
        tv_out_date.setText(formatDateForBigDay(DateUtil.getDay("M月d日 ", endTime) + DateUtil.dateToWeek2(new Date(endTime))));
        tv_days.setText(String.format(getString(R.string.duringNightStr), DateUtil.getGapCount(new Date(beginTime), new Date(endTime))));

        //地点信息
        tv_city.setHint("正在定位当前城市");
    }

    @Override
    public void onResume() {
        super.onResume();
        isOnPause = false;

        //重置consider
        mConsiderLayout.reloadConsiderConfig(typeIndex, gradeIndex, priceIndex);
        //男性女性界面初始化
//        int newSkinIndex = SharedPreferenceUtil.getInstance().getInt(AppConst.SKIN_INDEX, 0);
//        if (oldSkinIndex != newSkinIndex) {
//            oldSkinIndex = newSkinIndex;
//            isReStarted = true;
//            recreate();
//        }

        //第每次启动时，如果用户未登录，则显示侧滑
        if (SessionContext.isFirstLaunchDoAction(getClass().getSimpleName()) &&
                !SessionContext.isLogin()) {
            sendBroadcast(new Intent(BroadCastConst.UNLOGIN_ACTION));
        }

        //如果登录状态，则获取用户当前消息、状态等
        if (SessionContext.isLogin()) {
            requestUserMenusStatus();
        }

        //重置consider
        mConsiderLayout.reloadConsiderConfig(typeIndex, gradeIndex, priceIndex);

        //OpenInstall Event 分发
        boolean jump = dispatchOpenInstallEvent();

        //如果画面跳转，则不定位，也不获取权限
        if (!jump && !isRequestPermission && !hasRejectPermission) {
            //如果已经定位且定位的不是当前位置，则显示当前定位城市；否则，不做任何处理
            if (LocationInfo.instance.isLocated() && !LocationInfo.instance.isMyLoc()) {
                refreshCityDisplay(LocationInfo.instance.getCity());
            } else if (!LocationInfo.instance.isLocated() && StringUtil.isEmpty(tv_city.getText().toString())) {
                myHandler.postDelayed(requestPermissionRunnable, 500);
            }
        }
    }

    private Runnable requestPermissionRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isOnPause) {
                myHandler.removeCallbacksAndMessages(null);
                if (PRJApplication.getPermissionsChecker(HotelMainActivity.this).lacksPermissions(PermissionsDef.LOCATION_PERMISSION)) {
                    PermissionsActivity.startActivityForResult(HotelMainActivity.this, PermissionsDef.PERMISSION_REQ_CODE, PermissionsDef.LOCATION_PERMISSION);
                    return;
                }
                AMapLocationControl.getInstance().startLocationOnce(HotelMainActivity.this);
            } else {
                myHandler.postDelayed(requestPermissionRunnable, 500);
            }
        }
    };

    private boolean dispatchOpenInstallEvent() {
        LogUtil.i(TAG, "dispatchOpenInstallEvent()");
        if (SessionContext.getOpenInstallAppData() != null) {
            JSONObject mJson = JSON.parseObject(SessionContext.getOpenInstallAppData().getData());
            if (null != mJson && mJson.containsKey("channel")) {
                String channel = mJson.getString("channel");
                if (ShareTypeDef.SHARE_HOTEL.equals(channel)) {
//                    long beginDate = Long.valueOf(mJson.getString("beginDate"));
//                    long endDate = Long.valueOf(mJson.getString("endDate"));
                    HotelOrderManager.getInstance().setBeginTime(beginTime);
                    HotelOrderManager.getInstance().setEndTime(endTime);
                    Intent intent = new Intent(this, HotelDetailActivity.class);
                    intent.putExtra("hotelId", Integer.valueOf(mJson.getString("hotelID")));
                    startActivity(intent);
                    SessionContext.setOpenInstallAppData(null);
                    return true;
                } else if (ShareTypeDef.SHARE_ROOM.equals(channel)) {
//                    long beginDate = Long.valueOf(mJson.getString("beginDate"));
//                    long endDate = Long.valueOf(mJson.getString("endDate"));
                    HotelOrderManager.getInstance().setBeginTime(beginTime);
                    HotelOrderManager.getInstance().setEndTime(endTime);
                    Intent intent = new Intent(this, HotelRoomDetailActivity.class);
                    intent.putExtra("hotelId", Integer.valueOf(mJson.getString("hotelID")));
                    intent.putExtra("roomId", Integer.valueOf(mJson.getString("roomID")));
                    intent.putExtra("roomType", Integer.valueOf(mJson.getString("hotelType")));
                    startActivity(intent);
                    SessionContext.setOpenInstallAppData(null);
                    return true;
                } else if (ShareTypeDef.SHARE_FREE.equals(channel)) {
                    Intent intent = new Intent(this, Hotel0YuanHomeActivity.class);
                    startActivity(intent);
                    SessionContext.setOpenInstallAppData(null);
                    return true;
                } else if (ShareTypeDef.SHARE_TIE.equals(channel)) {
                    Intent intent = new Intent(this, HotelSpaceDetailActivity.class);
                    intent.putExtra("hotelId", Integer.valueOf(mJson.getString("hotelID")));
                    intent.putExtra("articleId", Integer.valueOf(mJson.getString("blogID")));
                    startActivity(intent);
                    SessionContext.setOpenInstallAppData(null);
                    return true;
                } else {
                    LogUtil.d("HotelMainActivity", "warning~~~");
                    return false;
                }
            }
            return false;
        }
        return false;
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
        ((ImageView) findViewById(R.id.iv_coupon_icon)).setImageResource(couponResId);

        boolean orderflag = mJson.containsKey("orderflag") ? mJson.getBoolean("orderflag") : false;
        int orderResId = orderflag ? R.drawable.iv_my_order_red : R.drawable.iv_my_order_normal;
        ((ImageView) findViewById(R.id.iv_order_icon)).setImageResource(orderResId);

        boolean bountyflag = mJson.containsKey("bountyflag") ? mJson.getBoolean("bountyflag") : false;
        int bountyResId = bountyflag ? R.drawable.iv_my_bounty_red : R.drawable.iv_my_bounty_normal;
        ((ImageView) findViewById(R.id.iv_bounty_icon)).setImageResource(bountyResId);
    }

    private void showConsiderPopupWindow() {
        mConsiderLayout.reloadConsiderConfig(typeIndex, gradeIndex, priceIndex);
        // 设置背景颜色变暗
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;
        getWindow().setAttributes(lp);
        mConsiderPopupWindow.showAtLocation(getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
    }

    @Override
    public void initListeners() {
        super.initListeners();
        tv_city.setOnClickListener(this);
        tv_in_date.setOnClickListener(this);
        tv_out_date.setOnClickListener(this);
        tv_next_search.setOnClickListener(this);
        coupon_lay.setOnClickListener(this);
        order_lay.setOnClickListener(this);
        bounty_lay.setOnClickListener(this);
        tv_search.setOnClickListener(this);
        tv_consider.setOnClickListener(this);
        tv_curr_position.setOnClickListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        isOnPause = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocationInfo.instance.reset();
        if (myHandler != null) {
            myHandler.removeCallbacksAndMessages(null);
            myHandler = null;
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        Intent intent = null;
        switch (v.getId()) {
            case R.id.tv_city: {
                updateBeginTimeEndTime();
                HotelOrderManager.getInstance().setBeginTime(beginTime);
                HotelOrderManager.getInstance().setEndTime(endTime);
                Intent resIntent = new Intent(this, HotelCityChooseActivity.class);
                startActivityForResult(resIntent, REQUEST_CODE_CITY);
                break;
            }
            case R.id.coupon_lay:
                if (!SessionContext.isLogin()) {
                    sendBroadcast(new Intent(BroadCastConst.UNLOGIN_ACTION));
                    return;
                }
                ((ImageView) findViewById(R.id.iv_coupon_icon)).setImageResource(R.drawable.iv_my_coupon_normal);
                intent = new Intent(this, UcCouponsActivity.class);
                break;
            case R.id.order_lay:
                if (!SessionContext.isLogin()) {
                    sendBroadcast(new Intent(BroadCastConst.UNLOGIN_ACTION));
                    return;
                }
                ((ImageView) findViewById(R.id.iv_order_icon)).setImageResource(R.drawable.iv_my_order_normal);
                intent = new Intent(this, UcOrdersActivity.class);
                break;
            case R.id.bounty_lay:
                if (!SessionContext.isLogin()) {
                    sendBroadcast(new Intent(BroadCastConst.UNLOGIN_ACTION));
                    return;
                }
                ((ImageView) findViewById(R.id.iv_bounty_icon)).setImageResource(R.drawable.iv_my_bounty_normal);
                intent = new Intent(this, UcBountyMainActivity.class);
                break;
            case R.id.tv_next_search:
                if (StringUtil.isEmpty(tv_city.getText().toString())) {
                    CustomToast.show("城市定位失败，请打开GPS或手动选择城市", CustomToast.LENGTH_SHORT);
                    return;
                }
                HotelOrderManager.getInstance().reset();
                updateBeginTimeEndTime();
                HotelOrderManager.getInstance().setBeginTime(beginTime);
                HotelOrderManager.getInstance().setEndTime(endTime);
                HotelOrderManager.getInstance().setDateStr(DateUtil.getDay("M.d", beginTime) + " - " + DateUtil.getDay("M.d", endTime));
                intent = new Intent(this, HotelListActivity.class);
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
                Intent resIntent = new Intent(this, HotelCalendarChooseActivity.class);
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
                intent = new Intent(this, HotelAllSearchActivity.class);
                break;
            case R.id.tv_consider:
                showConsiderPopupWindow();
                break;
            case R.id.tv_curr_position:
                tv_city.setHint("正在获取当前位置");
                tv_city.setText("");
                LocationInfo.instance.setIsMyLoc(true);
                //权限检查
                if (PRJApplication.getPermissionsChecker(this).lacksPermissions(PermissionsDef.LOCATION_PERMISSION)) {
                    PermissionsActivity.startActivityForResult(this, PermissionsDef.PERMISSION_REQ_CODE, PermissionsDef.LOCATION_PERMISSION);
                    return;
                }
                AMapLocationControl.getInstance().startLocationOnce(this);
                break;
        }
        if (null != intent) {
            startActivity(intent);
        }
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
    public void onNotifyMessage(ResponseData request, ResponseData response) {
        super.onNotifyMessage(request, response);
        if (response != null && response.body != null) {
            if (request.flag == AppConst.MENUS_STATUS) {
                LogUtil.i(TAG, "json = " + response.body.toString());
                if (StringUtil.notEmpty(response.body.toString()) && !"{}".equals(response.body.toString())) {
                    updateUserMenuStatus(response.body.toString());
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
//            int startIndex = date.indexOf("月");
//            int endIndex = date.indexOf("日");
            int endIndex = date.indexOf("周");
            SpannableString ss = new SpannableString(date);
            ss.setSpan(new AbsoluteSizeSpan(21, true), 0, endIndex,
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
}
