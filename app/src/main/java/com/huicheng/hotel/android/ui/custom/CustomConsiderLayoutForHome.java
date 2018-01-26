package com.huicheng.hotel.android.ui.custom;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.content.AppConst;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;

/**
 * @author kborid
 * @date 2016/11/14 0014
 */
public class CustomConsiderLayoutForHome extends RelativeLayout implements View.OnClickListener {

    private final String TAG = getClass().getSimpleName();

    private Context context;

    private int gradeIndex, typeIndex, priceIndex;
    private CommonSingleSelLayout typeLay, gradeLay;
    private CustomConsiderPriceLayout priceLay;
    private TextView tv_reset, tv_confirm;


    public CustomConsiderLayoutForHome(Context context) {
        this(context, null);
    }

    public CustomConsiderLayoutForHome(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomConsiderLayoutForHome(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        LayoutInflater.from(context).inflate(R.layout.pw_consider_layout_forhome, this);
        findViews();
        initParamers();
        setClickListeners();
    }

    private void findViews() {
        gradeLay = (CommonSingleSelLayout) findViewById(R.id.gradeLay);
        typeLay = (CommonSingleSelLayout) findViewById(R.id.typeLay);
        priceLay = (CustomConsiderPriceLayout) findViewById(R.id.priceLay);
        tv_reset = (TextView) findViewById(R.id.tv_reset);
        tv_confirm = (TextView) findViewById(R.id.tv_confirm);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void initParamers() {
        typeLay.setData(context.getResources().getStringArray(R.array.HotelType));
        priceLay.setData(context.getResources().getStringArray(R.array.HotelPrice));
        gradeLay.setData(context.getResources().getStringArray(R.array.HotelStar));
    }

    private void setClickListeners() {
        tv_reset.setOnClickListener(this);
        tv_confirm.setOnClickListener(this);
    }

    public void initConsiderConfig() {
        resetConsiderConfig();
        saveConsiderConfig();
    }

    public void reloadConsiderConfig() {
        LogUtil.i(TAG, "reloadConsiderConfig()");
        typeIndex = SharedPreferenceUtil.getInstance().getInt(AppConst.CONSIDER_TYPE, -1);
        typeLay.setSelected(typeIndex);
        gradeIndex = SharedPreferenceUtil.getInstance().getInt(AppConst.CONSIDER_GRADE, -1);
        gradeLay.setSelected(gradeIndex);
        priceIndex = SharedPreferenceUtil.getInstance().getInt(AppConst.CONSIDER_PRICE, -1);
        priceLay.setSelected(priceIndex);
        LogUtil.i(TAG, "typeIndex = " + typeIndex + ", gradeIndex = " + gradeIndex + ", priceIndex = " + priceIndex);
    }

    public void reloadConsiderConfig(int typeIndex, int gradeIndex, int priceIndex) {
        LogUtil.i(TAG, "setConsiderConfig()");
        SharedPreferenceUtil.getInstance().setInt(AppConst.CONSIDER_TYPE, typeIndex);
        typeLay.setSelected(typeIndex);
        SharedPreferenceUtil.getInstance().setInt(AppConst.CONSIDER_GRADE, gradeIndex);
        gradeLay.setSelected(gradeIndex);
        SharedPreferenceUtil.getInstance().setInt(AppConst.CONSIDER_PRICE, priceIndex);
        priceLay.setSelected(priceIndex);
        LogUtil.i(TAG, "typeIndex = " + typeIndex + ", gradeIndex = " + gradeIndex + ", priceIndex = " + priceIndex);
    }

    public void resetConsiderConfig() {
        LogUtil.i(TAG, "resetRestoreConfig()");
        typeIndex = typeLay.resetSelectedIndex();
        gradeIndex = gradeLay.resetSelectedIndex();
        priceIndex = priceLay.resetSelectedIndex();
        LogUtil.i(TAG, "typeIndex = " + typeIndex + ", gradeIndex = " + gradeIndex + ", priceIndex = " + priceIndex);
    }

    private void saveConsiderConfig() {
        LogUtil.i(TAG, "saveConsiderConfig()");
        typeIndex = typeLay.getSelectedIndex();
        gradeIndex = gradeLay.getSelectedIndex();
        priceIndex = priceLay.getSelectedIndex();
        LogUtil.i(TAG, "typeIndex = " + typeIndex + ", gradeIndex = " + gradeIndex + ", priceIndex = " + priceIndex);
        SharedPreferenceUtil.getInstance().setInt(AppConst.CONSIDER_TYPE, typeIndex);
        SharedPreferenceUtil.getInstance().setInt(AppConst.CONSIDER_GRADE, gradeIndex);
        SharedPreferenceUtil.getInstance().setInt(AppConst.CONSIDER_PRICE, priceIndex);
    }

    public String getConsiderString() {
        StringBuilder sb = new StringBuilder();
        if (gradeIndex != -1) {
            if (StringUtil.notEmpty(sb.toString())) {
                sb.append(" / ");
            }
            sb.append(gradeLay.getSelectedItem());
        }
        if (priceIndex != -1) {
            if (StringUtil.notEmpty(sb.toString())) {
                sb.append(" / ");
            }
            sb.append(priceLay.getSelectedItem());
        }
        if (typeIndex != -1) {
            if (StringUtil.notEmpty(sb.toString())) {
                sb.append(" / ");
            }
            sb.append(typeLay.getSelectedItem());
        }
        return sb.toString();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_reset:
                resetConsiderConfig();
                break;
            case R.id.tv_confirm:
                saveConsiderConfig();
                if (null != listener) {
                    listener.onResult(getConsiderString(), typeIndex, gradeIndex, priceIndex);
                    listener.onDismiss();
                }
                break;
        }
    }

    private OnConsiderLayoutListener listener = null;

    public void setOnConsiderLayoutListener(OnConsiderLayoutListener listener) {
        this.listener = listener;
    }

    public interface OnConsiderLayoutListener {
        void onDismiss();

        void onResult(String str, int type, int grade, int price);
    }
}
