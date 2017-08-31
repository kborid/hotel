package com.huicheng.hotel.android.ui.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.HotelOrderManager;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.control.AMapLocationControl;
import com.huicheng.hotel.android.tools.CityStringUtils;
import com.huicheng.hotel.android.ui.activity.Hotel0YuanHomeActivity;
import com.huicheng.hotel.android.ui.activity.HotelCalendarChooseActivity;
import com.huicheng.hotel.android.ui.activity.HotelListActivity;
import com.huicheng.hotel.android.ui.activity.LocationChooseActivity;
import com.huicheng.hotel.android.ui.base.BaseFragment;
import com.huicheng.hotel.android.ui.custom.CommonBannerLayout;
import com.huicheng.hotel.android.ui.custom.calendar.SimpleMonthAdapter;
import com.huicheng.hotel.android.ui.dialog.CustomDialog;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.prj.sdk.constants.BroadCastConst;
import com.prj.sdk.util.BitmapUtils;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;
import com.prj.sdk.widget.CustomToast;

import java.lang.reflect.Field;
import java.util.Calendar;

/**
 * Fragment home
 */
public class HotelPagerFragment extends BaseFragment implements View.OnClickListener {

    private static final String TAG = "HotelPagerFragment";
    private static boolean isFirstLoad = false;

    private CommonBannerLayout banner_lay;
    private TextView tv_city;
    private EditText et_keyword;
    private ImageView iv_location;
    private ImageView iv_reset;
    private ImageView iv_voice;
    private TextView tv_next_search;
    private TextView tv_info;
    private ImageButton btn_zero;
    private Button btn_night, btn_hhy;

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
        LogUtil.i(TAG, "onCreateView()");
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
        LogUtil.i(TAG, "newInstance()");
        Fragment fragment = new HotelPagerFragment();
        Bundle bundle = new Bundle();
        bundle.putString("key", key);
        fragment.setArguments(bundle);
        return fragment;
    }

    protected void onVisible() {
        super.onVisible();
        LogUtil.i(TAG, "onVisible()");
        if (isFirstLoad) {
            isFirstLoad = false;
        }
        banner_lay.startBanner();
        HotelOrderManager.getInstance().reset();
        String tempProvince = SharedPreferenceUtil.getInstance().getString(AppConst.PROVINCE, "", false);
        String tempCity = SharedPreferenceUtil.getInstance().getString(AppConst.CITY, "", false);
        HotelOrderManager.getInstance().setCityStr(CityStringUtils.getProvinceCityString(tempProvince, tempCity, "-"));
        tv_city.setText(CityStringUtils.getProvinceCityString(tempProvince, tempCity, " "));
    }

    protected void onInvisible() {
        super.onInvisible();
        LogUtil.i(TAG, "onInvisible()");
        banner_lay.stopBanner();
    }

    @Override
    protected void initViews(View view) {
        super.initViews(view);
        LogUtil.i(TAG, "initViews()");
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
        tv_info = (TextView) view.findViewById(R.id.tv_info);
        btn_zero = (ImageButton) view.findViewById(R.id.btn_zero);
        Bitmap bm = BitmapUtils.getAlphaBitmap(getResources().getDrawable(R.drawable.iv_freeonenight_blue), getResources().getColor(mMainColor));
        btn_zero.setImageBitmap(bm);
        btn_night = (Button) view.findViewById(R.id.btn_ygr);
        btn_hhy = (Button) view.findViewById(R.id.btn_hhy);

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
        LogUtil.i(TAG, "initParams()");
        String province = SharedPreferenceUtil.getInstance().getString(AppConst.PROVINCE, "", false);
        String city = SharedPreferenceUtil.getInstance().getString(AppConst.CITY, "", false);
        if (StringUtil.notEmpty(province) && StringUtil.notEmpty(city)) {
            tv_city.setText(city + " " + province);
            if (city.equals(province)) {
                tv_city.setText(city);
            }
        } else {
            AMapLocationControl.getInstance().startLocationAlways(getActivity(), new AMapLocationListener() {

                @Override
                public void onLocationChanged(AMapLocation aMapLocation) {
                    if (null != aMapLocation) {
                        if (aMapLocation.getErrorCode() == 0) {
                            //定位成功回调信息，设置相关消息
                            AMapLocationControl.getInstance().stopLocation();
                            try {
                                SharedPreferenceUtil.getInstance().setString(AppConst.LOCATION_LON, String.valueOf(aMapLocation.getLongitude()), false);
                                SharedPreferenceUtil.getInstance().setString(AppConst.LOCATION_LAT, String.valueOf(aMapLocation.getLatitude()), false);
                                String loc_province = aMapLocation.getProvince().replace("省", "");
                                String loc_city = aMapLocation.getCity().replace("市", "");
                                SharedPreferenceUtil.getInstance().setString(AppConst.LOCATION_PROVINCE, loc_province, false);
                                SharedPreferenceUtil.getInstance().setString(AppConst.LOCATION_CITY, loc_city, false);
                                SharedPreferenceUtil.getInstance().setString(AppConst.LOCATION_SITEID, String.valueOf(aMapLocation.getAdCode()), false);

                                SharedPreferenceUtil.getInstance().setString(AppConst.PROVINCE, loc_province, false);
                                SharedPreferenceUtil.getInstance().setString(AppConst.CITY, loc_city, false);
                                SharedPreferenceUtil.getInstance().setString(AppConst.SITEID, String.valueOf(aMapLocation.getAdCode()), false);

                                HotelOrderManager.getInstance().setCityStr(CityStringUtils.getProvinceCityString(loc_province, loc_city, "-"));
                                tv_city.setText(CityStringUtils.getProvinceCityString(loc_province, loc_city, " "));

                                showHaiNanAd(loc_province);

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
            });
        }

        if (AppConst.ISDEVELOP) {
            btn_zero.setVisibility(View.VISIBLE);
        } else {
            btn_zero.setVisibility(View.GONE);
        }

        banner_lay.setImageResource(SessionContext.getBannerList());
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        llp.width = Utils.mScreenWidth;
        llp.height = (int) ((float) llp.width / 25 * 14);
        banner_lay.setLayoutParams(llp);

        // 初始化时间，今天到明天 1晚
        Calendar calendar = Calendar.getInstance();
        beginTime = calendar.getTime().getTime();
        calendar.add(Calendar.DAY_OF_MONTH, +1); //+1今天的时间加一天
        endTime = calendar.getTime().getTime();
        tv_date.setText(DateUtil.getDay("M月d日", beginTime) + "-" + DateUtil.getDay("M月d日", endTime));
    }

    @Override
    public void initListeners() {
        super.initListeners();
        LogUtil.i(TAG, "initListeners()");
        tv_city.setOnClickListener(this);
        iv_location.setOnClickListener(this);
        iv_reset.setOnClickListener(this);
        iv_voice.setOnClickListener(this);
        tv_next_search.setOnClickListener(this);
        tv_info.setOnClickListener(this);
        btn_zero.setOnClickListener(this);
        btn_night.setOnClickListener(this);
        btn_hhy.setOnClickListener(this);
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
                Intent resIntent = new Intent(getActivity(), LocationChooseActivity.class);
                startActivityForResult(resIntent, 0x01);
            }
            break;
            case R.id.iv_reset:
                et_keyword.setText("");
                break;
            case R.id.iv_voice:
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

                HotelOrderManager.getInstance().setBeginTime(beginTime);
                HotelOrderManager.getInstance().setEndTime(endTime);
                SimpleMonthAdapter.CalendarDay begin = new SimpleMonthAdapter.CalendarDay(beginTime);
                SimpleMonthAdapter.CalendarDay end = new SimpleMonthAdapter.CalendarDay(endTime);
                HotelOrderManager.getInstance().setDateStr((begin.getMonth() + 1) + "." + begin.getDay() /*+ DateUtil.dateToWeek2(begin.getDate())*/ + " - " + (end.getMonth() + 1) + "." + end.getDay()/* + DateUtil.dateToWeek2(end.getDate())*/);

                if (StringUtil.isEmpty(tv_city.getText().toString())) {
                    CustomToast.show("定位失败，请打开GPS或手动选择城市", CustomToast.LENGTH_SHORT);
                    return;
                }

                et_keyword.setFocusable(false);
                et_keyword.setFocusableInTouchMode(true);
//                intent = new Intent(getActivity(), HotelCalendarChooseActivity.class);
                intent = new Intent(getActivity(), HotelListActivity.class);
                intent.putExtra("index", 0);
                intent.putExtra("keyword", et_keyword.getText().toString());
                intent.putExtra("priceIndex", mPriceIndex);
                break;
            case R.id.tv_info:
                CustomDialog dialog = new CustomDialog(getActivity());
                dialog.setTitle(getString(R.string.hsqString));
                dialog.setMessage(getString(R.string.hsqNote2));
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();
                break;
            case R.id.btn_zero:
                if (!SessionContext.isLogin()) {
                    getActivity().sendBroadcast(new Intent(BroadCastConst.UNLOGIN_ACTION));
                    return;
                }
                intent = new Intent(getActivity(), Hotel0YuanHomeActivity.class);
                break;
            case R.id.btn_ygr:
                if (!SessionContext.isLogin()) {
                    getActivity().sendBroadcast(new Intent(BroadCastConst.UNLOGIN_ACTION));
                    return;
                }
                intent = new Intent(getActivity(), HotelCalendarChooseActivity.class);
                HotelOrderManager.getInstance().reset();
                intent.putExtra("index", 2);
                break;
            case R.id.btn_hhy:
                if (!SessionContext.isLogin()) {
                    getActivity().sendBroadcast(new Intent(BroadCastConst.UNLOGIN_ACTION));
                    return;
                }
                intent = new Intent(getActivity(), HotelCalendarChooseActivity.class);
                HotelOrderManager.getInstance().reset();
                intent.putExtra("index", 3);
                break;
            case R.id.tv_price:
            case R.id.iv_pull:
                PopupMenu popup = new PopupMenu(getActivity(), tv_price, Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
                getActivity().getMenuInflater().inflate(R.menu.hotel_home_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        mPriceIndex = item.getOrder();
                        tv_price.setText(item.getTitle());
                        return false;
                    }
                });
                popup.show();
                break;
            case R.id.tv_date:
            case R.id.iv_date: {
                Intent resIntent = new Intent(getActivity(), HotelCalendarChooseActivity.class);
                resIntent.putExtra("beginTime", beginTime);
                resIntent.putExtra("endTime", endTime);
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

    @Override
    public void onResume() {
        super.onResume();
        LogUtil.i(TAG, "onResume()");
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
        if (requestCode == 0x02) {
            if (null != data) {
                beginTime = data.getLongExtra("beginTime", beginTime);
                endTime = data.getLongExtra("endTime", endTime);
                tv_date.setText(DateUtil.getDay("M月d日", beginTime) + "-" + DateUtil.getDay("M月d日", endTime));
            }
        }
    }
}
