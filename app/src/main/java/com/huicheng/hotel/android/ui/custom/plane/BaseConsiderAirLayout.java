package com.huicheng.hotel.android.ui.custom.plane;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.huicheng.hotel.android.requestbuilder.bean.PlaneFlightInfoBean;

import java.util.List;

/**
 * @auth kborid
 * @date 2018/1/3 0003.
 */

public abstract class BaseConsiderAirLayout extends LinearLayout implements IPlaneConsiderNotifyListener {
    protected Context context;

    public BaseConsiderAirLayout(Context context) {
        this(context, null);
    }

    public BaseConsiderAirLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseConsiderAirLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initParams();
        setListeners();
    }

    protected abstract void initParams();

    protected abstract void setListeners();

    protected abstract void updateDataInfo(List<PlaneFlightInfoBean> list);

    protected void refreshViewSelectedStatus(int index) {
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).setSelected(i == index);
        }
    }



    public float[] getOffTimeLayoutValue() {
        throw new UnsupportedOperationException();
    }

    public int[] getAirCompanyValue(){
        throw new UnsupportedOperationException();
    }

    public int[] getAirportValue() {
        throw new UnsupportedOperationException();
    }

    public int getFlightType() {
        throw new UnsupportedOperationException();
    }

    public int getFlightCang() {
        throw new UnsupportedOperationException();
    }
}
