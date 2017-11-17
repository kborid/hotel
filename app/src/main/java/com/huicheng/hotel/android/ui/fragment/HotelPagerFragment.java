package com.huicheng.hotel.android.ui.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.huicheng.hotel.android.common.HotelOrderManager;
import com.huicheng.hotel.android.common.NetURL;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.control.AMapLocationControl;
import com.huicheng.hotel.android.net.RequestBeanBuilder;
import com.huicheng.hotel.android.net.bean.HomeBannerInfoBean;
import com.huicheng.hotel.android.permission.PermissionsDef;
import com.huicheng.hotel.android.tools.CityParseUtils;
import com.huicheng.hotel.android.ui.activity.CalendarChooseActivity;
import com.huicheng.hotel.android.ui.activity.PermissionsActivity;
import com.huicheng.hotel.android.ui.activity.hotel.HotelCityChooseActivity;
import com.huicheng.hotel.android.ui.activity.hotel.HotelListActivity;
import com.huicheng.hotel.android.ui.base.BaseFragment;
import com.huicheng.hotel.android.ui.custom.CommonBannerLayout;
import com.huicheng.hotel.android.ui.dialog.CustomToast;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.List;

/**
 * Fragment home
 */
public class HotelPagerFragment extends BaseFragment implements View.OnClickListener, DataCallback, AMapLocationControl.MyLocationListener {

    private static boolean isFirstLoad = false;

    private CommonBannerLayout banner_lay;
    private TextView tv_city;
    private EditText et_keyword;
    private ImageView iv_location;
    private ImageView iv_reset;
    private ImageView iv_voice;
    private TextView tv_next_search;

    private TextView tv_price;
    private int mPriceIndex = 0;
    private ImageView iv_pull;

    private TextView tv_date;
    private ImageView iv_date;
    private long beginTime, endTime;

    // 海南诚信广告Popup
    private boolean isAdShowed = false;
    private PopupWindow mAdHaiNanPopupWindow = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        isFirstLoad = true;
        getArguments().getString("key");
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_pager_home, container, false);
        initTypedArrayValue();
        initViews(view);
        initParams();
        initListeners();
        return view;
    }

    public static Fragment newInstance(String key) {
        Fragment fragment = new HotelPagerFragment();
        Bundle bundle = new Bundle();
        bundle.putString("key", key);
        fragment.setArguments(bundle);
        return fragment;
    }

    protected void onVisible() {
        super.onVisible();
        if (isFirstLoad) {
            isFirstLoad = false;
        }
        if (SessionContext.getBannerList().size() == 0) {
            requestMainBannerInfo();
        } else {
            banner_lay.startBanner();
        }
    }

    protected void onInvisible() {
        super.onInvisible();
        banner_lay.stopBanner();
    }

    @Override
    protected void initViews(View view) {
        super.initViews(view);
        banner_lay = (CommonBannerLayout) view.findViewById(R.id.banner_lay);

        ((TextView) view.findViewById(R.id.tv_city_label)).getPaint().setFakeBoldText(true);
        ((TextView) view.findViewById(R.id.tv_key_label)).getPaint().setFakeBoldText(true);
        ((TextView) view.findViewById(R.id.tv_price_label)).getPaint().setFakeBoldText(true);
        ((TextView) view.findViewById(R.id.tv_date_label)).getPaint().setFakeBoldText(true);
        tv_city = (TextView) view.findViewById(R.id.tv_city);
        tv_city.getPaint().setFakeBoldText(true);
        et_keyword = (EditText) view.findViewById(R.id.et_keyword);

        iv_location = (ImageView) view.findViewById(R.id.iv_location);
        iv_reset = (ImageView) view.findViewById(R.id.iv_reset);
        iv_reset.setEnabled(false);
        iv_voice = (ImageView) view.findViewById(R.id.iv_voice);
        tv_next_search = (TextView) view.findViewById(R.id.tv_next_search);
        tv_next_search.getPaint().setFakeBoldText(true);

        tv_price = (TextView) view.findViewById(R.id.tv_price);
        iv_pull = (ImageView) view.findViewById(R.id.iv_pull);

        tv_date = (TextView) view.findViewById(R.id.tv_date);
        iv_date = (ImageView) view.findViewById(R.id.iv_date);

        // 海南诚信认证广告
        View adView = LayoutInflater.from(getActivity()).inflate(R.layout.pw_ad_hainan_layout, null);
        mAdHaiNanPopupWindow = new PopupWindow(adView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
        mAdHaiNanPopupWindow.getContentView().findViewById(R.id.iv_ad_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdHaiNanPopupWindow.dismiss();
            }
        });
        int option = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
        mAdHaiNanPopupWindow.setSoftInputMode(option);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
    protected void initParams() {
        super.initParams();
        String province = SharedPreferenceUtil.getInstance().getString(AppConst.PROVINCE, "", false);
        String city = SharedPreferenceUtil.getInstance().getString(AppConst.CITY, "", false);
        if (StringUtil.isEmpty(province) || StringUtil.isEmpty(city)) {
            AMapLocationControl.getInstance().startLocation();
            AMapLocationControl.getInstance().registerLocationListener(this);
        }
        tv_city.setText(CityParseUtils.getProvinceCityString(province, city, " "));
        HotelOrderManager.getInstance().setCityStr(CityParseUtils.getProvinceCityString(province, city, "-"));

        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        llp.width = Utils.mScreenWidth;
        llp.height = (int) ((float) llp.width / 25 * 14);
        banner_lay.setLayoutParams(llp);
        if (SessionContext.getBannerList().size() > 0) {
            banner_lay.setImageResource(SessionContext.getBannerList());
        }

        // 初始化时间，今天到明天 1晚
        Calendar calendar = Calendar.getInstance();
        beginTime = calendar.getTime().getTime();
        calendar.add(Calendar.DAY_OF_MONTH, +1); //+1今天的时间加一天
        endTime = calendar.getTime().getTime();
        HotelOrderManager.getInstance().setBeginTime(beginTime);
        HotelOrderManager.getInstance().setEndTime(endTime);
        HotelOrderManager.getInstance().setDateStr(DateUtil.getDay("M.d", beginTime) + " - " + DateUtil.getDay("M.d", endTime));
        tv_date.setText(DateUtil.getDay("M月d日", beginTime) + "-" + DateUtil.getDay("M月d日", endTime));
    }

    @Override
    public void initListeners() {
        super.initListeners();
        tv_city.setOnClickListener(this);
        iv_location.setOnClickListener(this);
        iv_reset.setOnClickListener(this);
        iv_voice.setOnClickListener(this);
        tv_next_search.setOnClickListener(this);
        tv_price.setOnClickListener(this);
        iv_pull.setOnClickListener(this);
        tv_date.setOnClickListener(this);
        iv_date.setOnClickListener(this);

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
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.tv_city:
            case R.id.iv_location: {
                Intent resIntent = new Intent(getActivity(), HotelCityChooseActivity.class);
                startActivityForResult(resIntent, 0x01);
            }
            break;
            case R.id.iv_reset:
                et_keyword.setText("");
                break;
            case R.id.iv_voice:
                if (PRJApplication.getPermissionsChecker(getActivity()).lacksPermissions(PermissionsDef.MIC_PERMISSION)) {
                    PermissionsActivity.startActivityForResult(getActivity(), PermissionsDef.PERMISSION_REQ_CODE, PermissionsDef.MIC_PERMISSION);
                    return;
                }
                RecognizerDialog mDialog = new RecognizerDialog(getActivity(), null);
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
            case R.id.tv_next_search:
                if (StringUtil.isEmpty(tv_city.getText().toString())) {
                    CustomToast.show("定位失败，请打开GPS或手动选择城市", CustomToast.LENGTH_SHORT);
                    return;
                }
                HotelOrderManager.getInstance().reset();
                HotelOrderManager.getInstance().setBeginTime(beginTime);
                HotelOrderManager.getInstance().setEndTime(endTime);
                HotelOrderManager.getInstance().setDateStr(DateUtil.getDay("M.d", beginTime) + " - " + DateUtil.getDay("M.d", endTime));

//                intent = new Intent(getActivity(), CalendarChooseActivity.class);
                intent = new Intent(getActivity(), HotelListActivity.class);
                intent.putExtra("index", 0);
                intent.putExtra("keyword", et_keyword.getText().toString());
                intent.putExtra("priceIndex", mPriceIndex);
                break;
//            case R.id.tv_price:
//            case R.id.iv_pull:
//                PopupMenu popup = new PopupMenu(getActivity(), tv_price, Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
//                getActivity().getMenuInflater().inflate(R.menu.hotel_home_menu, popup.getMenu());
//                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//                    @Override
//                    public boolean onMenuItemClick(MenuItem item) {
//                        mPriceIndex = item.getOrder();
//                        tv_price.setText(item.getTitle());
//                        return false;
//                    }
//                });
//                popup.show();
//                break;
            case R.id.tv_date:
            case R.id.iv_date: {
                Intent resIntent = new Intent(getActivity(), CalendarChooseActivity.class);
                resIntent.putExtra("isTitleCanClick", true);
//                resIntent.putExtra("beginTime", beginTime);
//                resIntent.putExtra("endTime", endTime);
                startActivityForResult(resIntent, 0x02);
            }
            break;
            default:
                break;
        }
        if (null != intent) {
            startActivity(intent);
        }
    }

    private void requestMainBannerInfo() {
        LogUtil.i(TAG, "requestHotelBanner()");
        RequestBeanBuilder b = RequestBeanBuilder.create(false);
        ResponseData d = b.syncRequest(b);
        d.path = NetURL.HOTEL_BANNER;
        d.flag = AppConst.HOTEL_BANNER;
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isAdShowed) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    String provinice = SharedPreferenceUtil.getInstance().getString(AppConst.PROVINCE, "", false);
                    showHaiNanAd(provinice);
                }
            }, 300);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AMapLocationControl.getInstance().unRegisterLocationListener(this);
    }

    private void showHaiNanAdPopupWindow() {
        mAdHaiNanPopupWindow.showAtLocation(getView(), Gravity.CENTER, 0, 0);
    }

    private void showHaiNanAd(String province) {
        if (province.contains("海南")) {
            isAdShowed = true;
            showHaiNanAdPopupWindow();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Activity.RESULT_OK != resultCode) {
            return;
        }
        String tempProvince = SharedPreferenceUtil.getInstance().getString(AppConst.PROVINCE, "", false);
        String tempCity = SharedPreferenceUtil.getInstance().getString(AppConst.CITY, "", false);
        HotelOrderManager.getInstance().setCityStr(CityParseUtils.getProvinceCityString(tempProvince, tempCity, "-"));
        tv_city.setText(CityParseUtils.getProvinceCityString(tempProvince, tempCity, " "));
        if (requestCode == 0x02) {
            if (null != data) {
                beginTime = data.getLongExtra("beginTime", beginTime);
                endTime = data.getLongExtra("endTime", endTime);
                tv_date.setText(DateUtil.getDay("M月d日", beginTime) + "-" + DateUtil.getDay("M月d日", endTime));
            }
        }
    }

    @Override
    public void preExecute(ResponseData request) {

    }

    @Override
    public void notifyMessage(ResponseData request, ResponseData response) throws Exception {
        if (response != null && response.body != null) {
            if (request.flag == AppConst.HOTEL_BANNER) {
                LogUtil.i(TAG, "json = " + response.body.toString());
                List<HomeBannerInfoBean> temp = JSON.parseArray(response.body.toString(), HomeBannerInfoBean.class);
                SessionContext.setBannerList(temp);
                if (null != banner_lay) {
                    banner_lay.setImageResource(temp);
                }
            }
        }
    }

    @Override
    public void notifyError(ResponseData request, ResponseData response, Exception e) {

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

                    HotelOrderManager.getInstance().setCityStr(CityParseUtils.getProvinceCityString(province, city, "-"));
                    tv_city.setText(CityParseUtils.getProvinceCityString(province, city, " "));

                    showHaiNanAd(province);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                LogUtil.e(TAG, "location Error, ErrCode:"
                        + aMapLocation.getErrorCode() + ", errInfo:"
                        + aMapLocation.getErrorInfo());
            }
        }
    }
}
