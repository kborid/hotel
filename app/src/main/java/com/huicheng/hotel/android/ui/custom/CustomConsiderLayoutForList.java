package com.huicheng.hotel.android.ui.custom;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;

/**
 * @author kborid
 * @date 2016/11/14 0014
 */
public class CustomConsiderLayoutForList extends RelativeLayout implements View.OnClickListener {

    private final String TAG = getClass().getSimpleName();

    private Context context;

    private int pointIndex, gradeIndex, typeIndex, priceIndex;
    private CommonSingleSelLayout pointLay, gradeLay, typeLay;
    private CustomConsiderPriceLayout priceLay;
    private Button btn_select;


    public CustomConsiderLayoutForList(Context context) {
        this(context, null);
    }

    public CustomConsiderLayoutForList(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomConsiderLayoutForList(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        LayoutInflater.from(context).inflate(R.layout.pw_consider_layout_forlist, this);
        findViews();
        initParamers();
        setClickListeners();
    }

    private void findViews() {
        pointLay = (CommonSingleSelLayout) findViewById(R.id.pointLay);
        gradeLay = (CommonSingleSelLayout) findViewById(R.id.gradeLay);
        typeLay = (CommonSingleSelLayout) findViewById(R.id.typeLay);
        priceLay = (CustomConsiderPriceLayout) findViewById(R.id.priceLay);
        btn_select = (Button) findViewById(R.id.btn_select);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void initParamers() {
        pointLay.setData(context.getResources().getStringArray(R.array.HotelPoint));
        gradeLay.setData(context.getResources().getStringArray(R.array.HotelStar));
        typeLay.setData(context.getResources().getStringArray(R.array.HotelType));
        priceLay.setData(context.getResources().getStringArray(R.array.HotelPrice));
    }

    private void setClickListeners() {
        btn_select.setOnClickListener(this);
    }

    public void initConsiderConfig() {
        resetConsiderConfig();
        saveConsiderConfig();
    }

    public void reloadConsiderConfig() {
        LogUtil.i(TAG, "reloadConsiderConfig()");
        pointIndex = SharedPreferenceUtil.getInstance().getInt(AppConst.CONSIDER_POINT, -1);
        pointLay.setSelected(pointIndex);
        gradeIndex = SharedPreferenceUtil.getInstance().getInt(AppConst.CONSIDER_GRADE, -1);
        gradeLay.setSelected(gradeIndex);
        typeIndex = SharedPreferenceUtil.getInstance().getInt(AppConst.CONSIDER_TYPE, -1);
        typeLay.setSelected(typeIndex);
        priceIndex = SharedPreferenceUtil.getInstance().getInt(AppConst.CONSIDER_PRICE, -1);
        priceLay.setSelected(priceIndex);
        LogUtil.i(TAG, "pointIndex = " + pointIndex + ", gradeIndex = " + gradeIndex + ", typeIndex = " + typeIndex + ", priceIndex = " + priceIndex);
    }

    public void clearPointConditionConfig() {
        pointIndex = pointLay.resetSelectedIndex();
        SharedPreferenceUtil.getInstance().setInt(AppConst.CONSIDER_POINT, pointIndex);
    }

    public void resetConsiderConfig() {
        LogUtil.i(TAG, "resetRestoreConfig()");
        pointIndex = pointLay.resetSelectedIndex();
        gradeIndex = gradeLay.resetSelectedIndex();
        typeIndex = typeLay.resetSelectedIndex();
        priceIndex = priceLay.resetSelectedIndex();
        LogUtil.i(TAG, "pointIndex = " + pointIndex + ", gradeIndex = " + gradeIndex + ", typeIndex = " + typeIndex + ", priceIndex = " + priceIndex);
    }

    private void saveConsiderConfig() {
        LogUtil.i(TAG, "saveConsiderConfig()");
        pointIndex = pointLay.getSelectedIndex();
        gradeIndex = gradeLay.getSelectedIndex();
        typeIndex = typeLay.getSelectedIndex();
        priceIndex = priceLay.getSelectedIndex();
        LogUtil.i(TAG, "pointIndex = " + pointIndex + ", gradeIndex = " + gradeIndex + ", typeIndex = " + typeIndex + ", priceIndex = " + priceIndex);
        SharedPreferenceUtil.getInstance().setInt(AppConst.CONSIDER_POINT, pointIndex);
        SharedPreferenceUtil.getInstance().setInt(AppConst.CONSIDER_GRADE, gradeIndex);
        SharedPreferenceUtil.getInstance().setInt(AppConst.CONSIDER_TYPE, typeIndex);
        SharedPreferenceUtil.getInstance().setInt(AppConst.CONSIDER_PRICE, priceIndex);
    }

    public String getConsiderString() {
        StringBuilder sb = new StringBuilder();
        if (pointIndex != -1) {
            if (StringUtil.notEmpty(sb.toString())) {
                sb.append(" / ");
            }
            sb.append(pointLay.getSelectedItem());
        }
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
            case R.id.btn_select:
                saveConsiderConfig();
                if (null != listener) {
                    listener.onResult(getConsiderString());
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

        void onResult(String str);
    }
}
