package com.huicheng.hotel.android.ui.custom.plane;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.prj.sdk.util.Utils;

/**
 * @auth kborid
 * @date 2017/11/29 0029.
 */

public class ConsiderAirPortLayout extends LinearLayout implements IPlaneConsiderAction {

    private Context context;
    private TextView tv_off_airport, tv_on_airport;
    private LinearLayout off_lay, on_lay;

    public ConsiderAirPortLayout(Context context) {
        this(context, null);
    }

    public ConsiderAirPortLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ConsiderAirPortLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        LayoutInflater.from(context).inflate(R.layout.layout_plane_consider_airport, this);
        tv_off_airport = (TextView) findViewById(R.id.tv_off_airport);
        tv_on_airport = (TextView) findViewById(R.id.tv_on_airport);
        off_lay = (LinearLayout) findViewById(R.id.off_lay);
        on_lay = (LinearLayout) findViewById(R.id.on_lay);

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

    public void updateAirportInfo(){

    }

    @Override
    public void cancelConsiderConfig() {

    }

    @Override
    public void resetConsiderConfig() {

    }

    @Override
    public void saveConsiderConfig() {

    }

    @Override
    public void reloadConsiderConfig() {

    }
}
