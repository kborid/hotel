package com.huicheng.hotel.android.ui.custom.plane;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.PlaneOrderManager;
import com.huicheng.hotel.android.requestbuilder.bean.PlaneFlightInfoBean;
import com.prj.sdk.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @auth kborid
 * @date 2017/11/29 0029.
 */

public class ConsiderAirportLayout extends BaseConsiderAirLayout {

    private TextView tv_off_airport, tv_on_airport;
    private LinearLayout off_lay, on_lay;

    private ArrayList<String> offAirports = new ArrayList<>();
    private ArrayList<String> onAirports = new ArrayList<>();
    private int offOriginIndex, onOriginIndex;
    private int offIndex, onIndex;

    public ConsiderAirportLayout(Context context) {
        super(context);
        offAirports = new ArrayList<>();
        offAirports.add("不限");
        offIndex = 0;
        offOriginIndex = 0;
        onAirports = new ArrayList<>();
        onAirports.add("不限");
        onIndex = 0;
        onOriginIndex = 0;
    }

    @Override
    protected void initParams() {
        LayoutInflater.from(context).inflate(R.layout.layout_plane_consider_airport, this);
        tv_off_airport = (TextView) findViewById(R.id.tv_off_airport);
        tv_on_airport = (TextView) findViewById(R.id.tv_on_airport);
        off_lay = (LinearLayout) findViewById(R.id.off_lay);
        on_lay = (LinearLayout) findViewById(R.id.on_lay);
        tv_off_airport.setText(String.format("%1$s起飞", PlaneOrderManager.instance.getFlightOffAirportInfo().cityname));
        tv_on_airport.setText(String.format("%1$s降落", PlaneOrderManager.instance.getFlightOnAirportInfo().cityname));
    }

    @Override
    protected void setListeners() {
        for (int i = 0; i < off_lay.getChildCount(); i++) {
            View item = off_lay.getChildAt(i);
            final int finalI = i;
            item.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    offIndex = finalI;
                    refreshViewSelectedStatus(offIndex, onIndex);
                }
            });
        }
        for (int i = 0; i < on_lay.getChildCount(); i++) {
            View item = on_lay.getChildAt(i);
            final int finalI = i;
            item.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onIndex = finalI;
                    refreshViewSelectedStatus(offIndex, onIndex);
                }
            });
        }
    }

    private void refreshViewSelectedStatus(int offIndex, int onIndex) {
        for (int i = 0; i < off_lay.getChildCount(); i++) {
            off_lay.getChildAt(i).setSelected(i == offIndex);
        }
        for (int i = 0; i < on_lay.getChildCount(); i++) {
            on_lay.getChildAt(i).setSelected(i == onIndex);
        }
    }

    @Override
    protected void updateDataInfo(List<PlaneFlightInfoBean> list) {
        if (list != null && list.size() > 0) {
            tv_off_airport.setText(String.format("%1$s起飞", PlaneOrderManager.instance.getFlightOffAirportInfo().cityname));
            tv_on_airport.setText(String.format("%1$s降落", PlaneOrderManager.instance.getFlightOnAirportInfo().cityname));

            //初始化机场信息
            offAirports.clear();
            offAirports.add("不限");
            onAirports.clear();
            onAirports.add("不限");
            for (int i = 0; i < list.size(); i++) {
                PlaneFlightInfoBean bean = list.get(i);
                if (!offAirports.contains(bean.dptAirport)) {
                    offAirports.add(bean.dptAirport);
                }
                if (!onAirports.contains(bean.arrAirport)) {
                    onAirports.add(bean.arrAirport);
                }
            }

            off_lay.removeAllViews();
            for (int i = 0; i < offAirports.size(); i++) {
                if (i == 0 && offAirports.get(0).equals("不限")) {
                    off_lay.addView(LayoutInflater.from(context).inflate(R.layout.lv_plane_consider_airport_all_item, null));
                } else {
                    off_lay.addView(LayoutInflater.from(context).inflate(R.layout.lv_plane_consider_airport_other_item, null));
                }
                View itemView = off_lay.getChildAt(i);
                itemView.setPadding(0, Utils.dp2px(10), 0, 0);
                ImageView iv_air_logo = (ImageView) itemView.findViewById(R.id.iv_air_logo);
                iv_air_logo.setVisibility(GONE);
                TextView tv_title = (TextView) itemView.findViewById(R.id.tv_title);
                tv_title.setText(offAirports.get(i));
            }

            on_lay.removeAllViews();
            for (int i = 0; i < onAirports.size(); i++) {
                if (i == 0 && onAirports.get(0).equals("不限")) {
                    on_lay.addView(LayoutInflater.from(context).inflate(R.layout.lv_plane_consider_airport_all_item, null));
                } else {
                    on_lay.addView(LayoutInflater.from(context).inflate(R.layout.lv_plane_consider_airport_other_item, null));
                }
                View itemView = on_lay.getChildAt(i);
                itemView.setPadding(0, Utils.dp2px(10), 0, 0);
                ImageView iv_air_logo = (ImageView) itemView.findViewById(R.id.iv_air_logo);
                iv_air_logo.setVisibility(GONE);
                TextView tv_title = (TextView) itemView.findViewById(R.id.tv_title);
                tv_title.setText(onAirports.get(i));
            }
            refreshViewSelectedStatus(offIndex, onIndex);
        }
        setListeners();
    }

    @Override
    public int[] getFlightConditionValue() {
        return new int[]{offIndex, onIndex};
    }

    @Override
    public List<String> getDataList(int index) {
        if (index == 1) {
            return onAirports;
        } else {
            return offAirports;
        }
    }

    @Override
    public void cancel() {
        offIndex = offOriginIndex;
        onIndex = onOriginIndex;
    }

    @Override
    public void reset() {
        offIndex = 0;
        onIndex = 0;
        refreshViewSelectedStatus(offIndex, onIndex);
    }

    @Override
    public void save() {
        offOriginIndex = offIndex;
        onOriginIndex = onIndex;
    }

    @Override
    public void reload() {
        offIndex = offOriginIndex;
        onIndex = onOriginIndex;
        refreshViewSelectedStatus(offIndex, onIndex);
    }
}
