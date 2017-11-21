package com.huicheng.hotel.android.ui.activity.hotel;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.amap.api.location.AMapLocation;
import com.huicheng.hotel.android.PRJApplication;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.HotelCommDef;
import com.huicheng.hotel.android.common.HotelOrderManager;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.control.AMapLocationControl;
import com.huicheng.hotel.android.permission.PermissionsDef;
import com.huicheng.hotel.android.tools.CityParseUtils;
import com.huicheng.hotel.android.ui.activity.BaseMainActivity;
import com.huicheng.hotel.android.ui.activity.CalendarChooseActivity;
import com.huicheng.hotel.android.ui.activity.PermissionsActivity;
import com.huicheng.hotel.android.ui.activity.UcOrdersActivity;
import com.huicheng.hotel.android.ui.custom.CustomConsiderLayoutForHome;
import com.huicheng.hotel.android.ui.dialog.CustomToast;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.prj.sdk.constants.BroadCastConst;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;

import java.lang.reflect.Field;
import java.util.Date;

public class HotelMainActivity extends BaseMainActivity implements AMapLocationControl.MyLocationListener {

    private static final int REQUEST_CODE_CITY = 0x01;
    private static final int REQUEST_CODE_DATE = 0x02;

    private LinearLayout order_lay;
    private TextView tv_city;
    private TextView tv_next_search;
    private TextView tv_in_date, tv_days, tv_out_date;
    private EditText et_keyword;
    private ImageView iv_reset;
    private ImageView iv_voice;
    private TextView tv_consider;

    private int typeIndex = -1, gradeIndex = -1, priceIndex = -1;
    private CustomConsiderLayoutForHome mConsiderLayout = null;
    private PopupWindow mConsiderPopupWindow = null;

    // 海南诚信广告Popup
    private boolean isAdShowed = false;
    private PopupWindow mAdHaiNanPopupWindow = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootView = LayoutInflater.from(this).inflate(R.layout.layout_content_hotel, null);
        initContentLayout(rootView);
        initViews();
        initParams();
        initListeners();
    }

    public void initViews() {
        super.initViews();
        tv_city = (TextView) findViewById(R.id.tv_city);
        tv_next_search = (TextView) findViewById(R.id.tv_next_search);
        order_lay = (LinearLayout) findViewById(R.id.order_lay);

        tv_in_date = (TextView) findViewById(R.id.tv_in_date);
        tv_days = (TextView) findViewById(R.id.tv_days);
        tv_out_date = (TextView) findViewById(R.id.tv_out_date);

        et_keyword = (EditText) findViewById(R.id.et_keyword);
        iv_reset = (ImageView) findViewById(R.id.iv_reset);
        iv_reset.setEnabled(false);
        iv_voice = (ImageView) findViewById(R.id.iv_voice);
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
        mConsiderPopupWindow.setAnimationStyle(R.style.share_anmi);
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

        //海南诚信认证广告
        View adView = LayoutInflater.from(this).inflate(R.layout.pw_ad_hainan_layout, null);
        mAdHaiNanPopupWindow = new PopupWindow(adView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
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
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1f;
                getWindow().setAttributes(lp);
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

    @Override
    public void initParams() {
        super.initParams();
        //初始化时间，今天到明天 1晚
        HotelOrderManager.getInstance().setBeginTime(beginTime);
        HotelOrderManager.getInstance().setEndTime(endTime);
        HotelOrderManager.getInstance().setDateStr(DateUtil.getDay("M.d", beginTime) + " - " + DateUtil.getDay("M.d", endTime));
        tv_in_date.setText(formatDateForBigDay(DateUtil.getDay("M月d日", beginTime)));
        tv_out_date.setText(formatDateForBigDay(DateUtil.getDay("M月d日", endTime)));
        tv_days.setText(String.format(getString(R.string.duringNightStr), DateUtil.getGapCount(new Date(beginTime), new Date(endTime))));

        //地点信息
        tv_city.setHint("正在定位当前城市");
        AMapLocationControl.getInstance().startLocation();
        AMapLocationControl.getInstance().registerLocationListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        //重置consider
        mConsiderLayout.reloadConsiderConfig(typeIndex, gradeIndex, priceIndex);

        //OpenInstall Event 分发
        dispatchOpenInstallEvent();
    }

    private void dispatchOpenInstallEvent() {
        LogUtil.i(TAG, "dispatchOpenInstallEvent()");
        if (SessionContext.getOpenInstallAppData() != null) {
            JSONObject mJson = JSON.parseObject(SessionContext.getOpenInstallAppData().getData());
            if (null != mJson && mJson.containsKey("channel")) {
                String channel = mJson.getString("channel");
                if (HotelCommDef.SHARE_HOTEL.equals(channel)) {
                    long beginDate = Long.valueOf(mJson.getString("beginDate"));
                    long endDate = Long.valueOf(mJson.getString("endDate"));
                    HotelOrderManager.getInstance().setBeginTime(beginDate);
                    HotelOrderManager.getInstance().setEndTime(endDate);
                    Intent intent = new Intent(this, HotelDetailActivity.class);
                    intent.putExtra("hotelId", Integer.valueOf(mJson.getString("hotelID")));
                    startActivity(intent);
                    SessionContext.setOpenInstallAppData(null);
                } else if (HotelCommDef.SHARE_ROOM.equals(channel)) {
                    long beginDate = Long.valueOf(mJson.getString("beginDate"));
                    long endDate = Long.valueOf(mJson.getString("endDate"));
                    HotelOrderManager.getInstance().setBeginTime(beginDate);
                    HotelOrderManager.getInstance().setEndTime(endDate);
                    Intent intent = new Intent(this, HotelRoomDetailActivity.class);
                    intent.putExtra("hotelId", Integer.valueOf(mJson.getString("hotelID")));
                    intent.putExtra("roomId", Integer.valueOf(mJson.getString("roomID")));
                    intent.putExtra("roomType", Integer.valueOf(mJson.getString("hotelType")));
                    startActivity(intent);
                    SessionContext.setOpenInstallAppData(null);
                } else if (HotelCommDef.SHARE_FREE.equals(channel)) {
                    Intent intent = new Intent(this, Hotel0YuanHomeActivity.class);
                    startActivity(intent);
                    SessionContext.setOpenInstallAppData(null);
                } else if (HotelCommDef.SHARE_TIE.equals(channel)) {
                    Intent intent = new Intent(this, HotelSpaceDetailActivity.class);
                    intent.putExtra("hotelId", Integer.valueOf(mJson.getString("hotelID")));
                    intent.putExtra("articleId", Integer.valueOf(mJson.getString("blogID")));
                    startActivity(intent);
                    SessionContext.setOpenInstallAppData(null);
                } else {
                    LogUtil.d("HotelMainActivity", "warning~~~");
                }
            }
        }
    }

    private void showHaiNanAdPopupWindow() {
        // 设置背景颜色变暗
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.35f;
        getWindow().setAttributes(lp);
        mAdHaiNanPopupWindow.showAtLocation(getWindow().getDecorView(), Gravity.CENTER, 0, 0);
    }

    private void showHaiNanAd(String province) {
        if (province.contains("海南")) {
            myHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isAdShowed = true;
                    showHaiNanAdPopupWindow();
                }
            }, 500);
        }
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
        order_lay.setOnClickListener(this);
        iv_reset.setOnClickListener(this);
        iv_voice.setOnClickListener(this);
        et_keyword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    tv_next_search.performClick();
                    return true;
                }
                return false;
            }
        });

        et_keyword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                iv_reset.setEnabled(StringUtil.notEmpty(s));
            }
        });
        tv_consider.setOnClickListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AMapLocationControl.getInstance().unRegisterLocationListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.tv_city: {
                Intent resIntent = new Intent(this, HotelCityChooseActivity.class);
                startActivityForResult(resIntent, REQUEST_CODE_CITY);
                break;
            }
            case R.id.order_lay:
                if (!SessionContext.isLogin()) {
                    sendBroadcast(new Intent(BroadCastConst.UNLOGIN_ACTION));
                    return;
                }
                intent = new Intent(this, UcOrdersActivity.class);
                break;
            case R.id.tv_next_search:
                if (StringUtil.isEmpty(tv_city.getText().toString())) {
                    CustomToast.show("城市定位失败，请打开GPS或手动选择城市", CustomToast.LENGTH_SHORT);
                    return;
                }
                HotelOrderManager.getInstance().reset();
                HotelOrderManager.getInstance().setBeginTime(beginTime);
                HotelOrderManager.getInstance().setEndTime(endTime);
                HotelOrderManager.getInstance().setDateStr(DateUtil.getDay("M.d", beginTime) + " - " + DateUtil.getDay("M.d", endTime));
                intent = new Intent(this, HotelListActivity.class);
                intent.putExtra("index", 0);
                intent.putExtra("keyword", et_keyword.getText().toString());
                break;
            case R.id.tv_in_date:
            case R.id.tv_out_date: {
                Intent resIntent = new Intent(this, CalendarChooseActivity.class);
                resIntent.putExtra("isTitleCanClick", true);
//                resIntent.putExtra("beginTime", beginTime);
//                resIntent.putExtra("endTime", endTime);
                startActivityForResult(resIntent, REQUEST_CODE_DATE);
                break;
            }
            case R.id.iv_voice:
                if (PRJApplication.getPermissionsChecker(this).lacksPermissions(PermissionsDef.MIC_PERMISSION)) {
                    PermissionsActivity.startActivityForResult(this, PermissionsDef.PERMISSION_REQ_CODE, PermissionsDef.MIC_PERMISSION);
                    return;
                }
                RecognizerDialog mDialog = new RecognizerDialog(this, null);
                mDialog.setParameter(SpeechConstant.ASR_PTT, "0");
                mDialog.setParameter(SpeechConstant.ASR_SCH, "1");
                mDialog.setParameter(SpeechConstant.NLP_VERSION, "3.0");
                mDialog.setListener(new RecognizerDialogListener() {
                    @Override
                    public void onResult(RecognizerResult recognizerResult, boolean b) {
                        if (b) {
                            String jsonStr = recognizerResult.getResultString();
                            JSONObject mJson = JSON.parseObject(jsonStr);
                            if (mJson.containsKey("text")) {
                                et_keyword.setText(mJson.getString("text"));
                                et_keyword.setSelection(et_keyword.getText().length());
                            }
                        }
                    }

                    @Override
                    public void onError(SpeechError speechError) {
                        LogUtil.e(TAG, "voice error code:" + speechError.getErrorCode() + ", " + speechError.getErrorDescription());
                    }
                });
                mDialog.show();
                break;
            case R.id.iv_reset:
                et_keyword.setText("");
                et_keyword.setFocusable(false);
                et_keyword.setFocusableInTouchMode(true);
                break;
            case R.id.tv_consider:
                showConsiderPopupWindow();
                break;
        }
        if (null != intent) {
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.i(TAG, "onActivityResult() " + requestCode + ", " + resultCode);
        if (Activity.RESULT_OK != resultCode) {
            return;
        }

        final String tempProvince = SharedPreferenceUtil.getInstance().getString(AppConst.PROVINCE, "", false);
        String tempCity = SharedPreferenceUtil.getInstance().getString(AppConst.CITY, "", false);
        HotelOrderManager.getInstance().setCityStr(CityParseUtils.getProvinceCityString(tempProvince, tempCity, "-"));
        tv_city.setText(CityParseUtils.getCityString(tempCity));
        if (!isAdShowed) {
            showHaiNanAd(tempProvince);
        }
        if (requestCode == REQUEST_CODE_DATE) {
            if (null != data) {
                beginTime = data.getLongExtra("beginTime", beginTime);
                endTime = data.getLongExtra("endTime", endTime);
                HotelOrderManager.getInstance().setBeginTime(beginTime);
                HotelOrderManager.getInstance().setEndTime(endTime);
                HotelOrderManager.getInstance().setDateStr(DateUtil.getDay("M.d", beginTime) + " - " + DateUtil.getDay("M.d", endTime));
                tv_in_date.setText(formatDateForBigDay(DateUtil.getDay("M月d日", beginTime)));
                tv_out_date.setText(formatDateForBigDay(DateUtil.getDay("M月d日", endTime)));
                tv_days.setText(String.format(getString(R.string.duringNightStr), DateUtil.getGapCount(new Date(beginTime), new Date(endTime))));
            }
        }
        requestWeatherInfo(beginTime);
    }

    private SpannableString formatDateForBigDay(String date) {
        if (StringUtil.notEmpty(date)) {
            int startIndex = date.indexOf("月");
            int endIndex = date.indexOf("日");
            SpannableString ss = new SpannableString(date);
            ss.setSpan(new AbsoluteSizeSpan(21, true), startIndex + 1, endIndex,
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            return ss;
        }
        return new SpannableString("");
    }

    @Override
    public void onLocation(AMapLocation aMapLocation) {
        if (null != aMapLocation) {
            if (aMapLocation.getErrorCode() == 0) {
                //定位成功回调信息，设置相关消息
                try {
                    SharedPreferenceUtil.getInstance().setString(AppConst.LOCATION_LON, String.valueOf(aMapLocation.getLongitude()), false);
                    SharedPreferenceUtil.getInstance().setString(AppConst.LOCATION_LAT, String.valueOf(aMapLocation.getLatitude()), false);
                    String province = CityParseUtils.getProvinceString(aMapLocation.getProvince());
                    String city = CityParseUtils.getProvinceString(aMapLocation.getCity());
                    String siteId = String.valueOf(aMapLocation.getAdCode());
                    SharedPreferenceUtil.getInstance().setString(AppConst.PROVINCE, province, false);
                    SharedPreferenceUtil.getInstance().setString(AppConst.CITY, city, false);
                    SharedPreferenceUtil.getInstance().setString(AppConst.SITEID, siteId, false);

                    tv_city.setText(CityParseUtils.getCityString(city));
                    HotelOrderManager.getInstance().setCityStr(CityParseUtils.getProvinceCityString(province, city, "-"));

                    requestWeatherInfo(beginTime);

                    showHaiNanAd(province);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                tv_city.setHint("城市定位失败");
                LogUtil.e(TAG, "location Error, ErrCode:"
                        + aMapLocation.getErrorCode() + ", errInfo:"
                        + aMapLocation.getErrorInfo());
            }
        }
    }
}
