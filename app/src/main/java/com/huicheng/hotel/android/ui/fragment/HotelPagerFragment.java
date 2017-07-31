package com.huicheng.hotel.android.ui.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.HotelOrderManager;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.ui.activity.Hotel0YuanHomeActivity;
import com.huicheng.hotel.android.ui.activity.HotelCalendarChooseActivity;
import com.huicheng.hotel.android.ui.activity.LocationActivity2;
import com.huicheng.hotel.android.ui.base.BaseFragment;
import com.huicheng.hotel.android.ui.dialog.CustomDialog;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.prj.sdk.constants.BroadCastConst;
import com.prj.sdk.util.BitmapUtils;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;

/**
 * Fragment home
 */
public class HotelPagerFragment extends BaseFragment implements View.OnClickListener {

    private static final String TAG = "HotelPagerFragment";
    private static boolean isFirstLoad = false;

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
        HotelOrderManager.getInstance().reset();
        String tempProvince = SharedPreferenceUtil.getInstance().getString(AppConst.PROVINCE, "", false);
        String tempCity = SharedPreferenceUtil.getInstance().getString(AppConst.CITY, "", false);
        if (StringUtil.notEmpty(tempProvince) && tempProvince.equals(tempCity)) {
            HotelOrderManager.getInstance().setCityStr(tempProvince);
        } else {
            HotelOrderManager.getInstance().setCityStr(tempCity + "-" + tempProvince);
        }
    }

    protected void onInvisible() {
        super.onInvisible();
    }

    @Override
    protected void initViews(View view) {
        super.initViews(view);
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
        }
        if (AppConst.ISDEVELOP) {
            btn_zero.setVisibility(View.VISIBLE);
        }
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
                resIntent.putExtra("city", tv_city.getText().toString());
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
                PopupMenu popup = new PopupMenu(getActivity(), et_keyword);
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
            default:
                break;
        }
        if (null != intent) {
            startActivity(intent);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Activity.RESULT_OK != resultCode) {
            return;
        }

        if (requestCode == 0x01) {
            if (null != data) {
                String province = SharedPreferenceUtil.getInstance().getString(AppConst.PROVINCE, "", false);
                String city = SharedPreferenceUtil.getInstance().getString(AppConst.CITY, "", false);
                tv_city.setText(city + " " + province);
                if (city.equals(province)) {
                    tv_city.setText(city);
                }
            }
        }
    }
}
