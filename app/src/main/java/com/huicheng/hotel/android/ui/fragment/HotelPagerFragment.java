package com.huicheng.hotel.android.ui.fragment;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.HotelOrderManager;
import com.huicheng.hotel.android.common.NetURL;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.control.AMapLocationControl;
import com.huicheng.hotel.android.net.RequestBeanBuilder;
import com.huicheng.hotel.android.net.bean.HomeBannerInfoBean;
import com.huicheng.hotel.android.tools.CityStringUtils;
import com.huicheng.hotel.android.ui.activity.Hotel0YuanHomeActivity;
import com.huicheng.hotel.android.ui.activity.HotelCalendarChooseActivity;
import com.huicheng.hotel.android.ui.activity.LocationActivity2;
import com.huicheng.hotel.android.ui.base.BaseFragment;
import com.huicheng.hotel.android.ui.custom.CommonBannerLayout;
import com.huicheng.hotel.android.ui.dialog.CustomDialog;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.prj.sdk.constants.BroadCastConst;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.BitmapUtils;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;
import com.prj.sdk.widget.CustomToast;

import java.util.List;

/**
 * Fragment home
 */
public class HotelPagerFragment extends BaseFragment implements View.OnClickListener, DataCallback {

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
    private View price_line;
    private int mPriceIndex = 0;
    private ImageView iv_pull;

    // 海南诚信广告蒙层
    private RelativeLayout hainan_ad_lay;
    private ImageView iv_ad_close;
    private boolean isAdShowed = false;

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
            requestHotelBanner();
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
        banner_lay.stopBanner();
    }

    @Override
    protected void initViews(View view) {
        super.initViews(view);
        banner_lay = (CommonBannerLayout) view.findViewById(R.id.banner_lay);

        ((TextView) view.findViewById(R.id.tv_city_lable)).getPaint().setFakeBoldText(true);
        ((TextView) view.findViewById(R.id.tv_key_lable)).getPaint().setFakeBoldText(true);
        tv_city = (TextView) view.findViewById(R.id.tv_city);
        et_keyword = (EditText) view.findViewById(R.id.et_keyword);

        iv_location = (ImageView) view.findViewById(R.id.iv_location);
        iv_reset = (ImageView) view.findViewById(R.id.iv_reset);
        iv_reset.setEnabled(false);
        iv_voice = (ImageView) view.findViewById(R.id.iv_voice);
        tv_next_search = (TextView) view.findViewById(R.id.tv_next_search);
        tv_info = (TextView) view.findViewById(R.id.tv_info);
        btn_zero = (ImageButton) view.findViewById(R.id.btn_zero);
        Bitmap bm = BitmapUtils.getAlphaBitmap(getResources().getDrawable(R.drawable.iv_freeonenight_blue), getResources().getColor(mMainColor));
        btn_zero.setImageBitmap(bm);
        btn_night = (Button) view.findViewById(R.id.btn_ygr);
        btn_hhy = (Button) view.findViewById(R.id.btn_hhy);

        tv_price = (TextView) view.findViewById(R.id.tv_price);
        iv_pull = (ImageView) view.findViewById(R.id.iv_pull);
        price_line = view.findViewById(R.id.price_line);

        hainan_ad_lay = (RelativeLayout) view.findViewById(R.id.hainan_ad_lay);
        hainan_ad_lay.setLayoutAnimation(getAnimationController());
        iv_ad_close = (ImageView) view.findViewById(R.id.iv_ad_close);
    }

    @Override
    protected void initParams() {
        super.initParams();
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

                                showHaiNanAd(tv_city.getText().toString());

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

        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        llp.width = Utils.mScreenWidth;
        llp.height = (int) ((float) llp.width / 25 * 14);
        banner_lay.setLayoutParams(llp);
    }

    @Override
    public void initListeners() {
        super.initListeners();
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
        iv_ad_close.setOnClickListener(this);

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
            case R.id.iv_location:
                Intent resIntent = new Intent(getActivity(), LocationActivity2.class);
                startActivityForResult(resIntent, 0x01);
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

                if (!SessionContext.isLogin()) {
                    getActivity().sendBroadcast(new Intent(BroadCastConst.UNLOGIN_ACTION));
                    return;
                }

                if (StringUtil.isEmpty(tv_city.getText().toString())) {
                    CustomToast.show("定位失败，请打开GPS或手动选择城市", CustomToast.LENGTH_SHORT);
                    return;
                }

                et_keyword.setFocusable(false);
                et_keyword.setFocusableInTouchMode(true);
                intent = new Intent(getActivity(), HotelCalendarChooseActivity.class);
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
            case R.id.iv_ad_close:
                hainan_ad_lay.setVisibility(View.GONE);
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
        showHaiNanAd(tv_city.getText().toString());
    }

    private void showHaiNanAd(String city) {
        if (!isAdShowed) {
            if (city.contains("海口")) {
                isAdShowed = true;
                hainan_ad_lay.setVisibility(View.VISIBLE);
            } else {
                hainan_ad_lay.setVisibility(View.GONE);
            }
        }
    }

    private LayoutAnimationController getAnimationController() {
        LayoutAnimationController controller;
        AnimationSet set = new AnimationSet(true);
        Animation alpha = new AlphaAnimation(0f, 1f);
        Animation scale = new ScaleAnimation(0.5f, 1.0f, 0.5f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        set.addAnimation(alpha);
        set.addAnimation(scale);
        set.setDuration(500);
        controller = new LayoutAnimationController(set, 0.1f);
        controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
        return controller;
    }

    private void requestHotelBanner() {
        LogUtil.i(TAG, "requestHotelBanner()");
        RequestBeanBuilder b = RequestBeanBuilder.create(false);
        ResponseData d = b.syncRequest(b);
        d.path = NetURL.HOTEL_BANNER;
        d.flag = AppConst.HOTEL_BANNER;
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    @Override
    public void preExecute(ResponseData request) {
    }

    @Override
    public void notifyMessage(ResponseData request, ResponseData response) throws Exception {
        if (response != null && response.body != null) {
            if (request.flag == AppConst.HOTEL_BANNER) {
                LogUtil.i(TAG, "Hotel Main json = " + response.body.toString());
                List<HomeBannerInfoBean> temp = JSON.parseArray(response.body.toString(), HomeBannerInfoBean.class);
                if (temp.size() > 0) {
                    banner_lay.setImageResource(temp, "");
                }
            }
        }
    }

    @Override
    public void notifyError(ResponseData request, ResponseData response, Exception e) {
    }
}
