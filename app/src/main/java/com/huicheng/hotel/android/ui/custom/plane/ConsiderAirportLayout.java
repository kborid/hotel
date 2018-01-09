package com.huicheng.hotel.android.ui.custom.plane;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.requestbuilder.bean.PlaneFlightInfoBean;
import com.prj.sdk.util.Utils;

import java.util.List;

/**
 * @auth kborid
 * @date 2017/11/29 0029.
 */

public class ConsiderAirportLayout extends BaseConsiderAirLayout {

    private TextView tv_off_airport, tv_on_airport;
    private LinearLayout off_lay, on_lay;

    public ConsiderAirportLayout(Context context) {
        super(context);
    }

    public ConsiderAirportLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ConsiderAirportLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initParams() {
        LayoutInflater.from(context).inflate(R.layout.layout_plane_consider_airport, this);
        tv_off_airport = (TextView) findViewById(R.id.tv_off_airport);
        tv_on_airport = (TextView) findViewById(R.id.tv_on_airport);
        off_lay = (LinearLayout) findViewById(R.id.off_lay);
        on_lay = (LinearLayout) findViewById(R.id.on_lay);
    }

    @Override
    protected void setListeners() {
    }

    @Override
    protected void updateDataInfo(List<PlaneFlightInfoBean> list) {
        if (list != null && list.size() > 0) {
            off_lay.removeAllViews();
            for (int i = 0; i < 3; i++) {
                View item = LayoutInflater.from(context).inflate(R.layout.lv_plane_consider_airport_item, null);
                TextView tv_title = (TextView) item.findViewById(R.id.tv_title);
                tv_title.append(String.valueOf(i));
                item.setPadding(0, Utils.dp2px(10), 0, 0);
                off_lay.addView(item);
            }

            on_lay.removeAllViews();
            for (int i = 3; i < 6; i++) {
                View item = LayoutInflater.from(context).inflate(R.layout.lv_plane_consider_airport_item, null);
                TextView tv_title = (TextView) item.findViewById(R.id.tv_title);
                tv_title.append(String.valueOf(i));
                item.setPadding(0, Utils.dp2px(10), 0, 0);
                on_lay.addView(item);
            }
        }
    }

    @Override
    public int[] getFlightConditionValue() {
        return new int[2];
    }

    @Override
    public void cancel() {

    }

    @Override
    public void reset() {

    }

    @Override
    public void save() {

    }

    @Override
    public void reload() {

    }
}
