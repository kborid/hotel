package com.huicheng.hotel.android.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.PlaneCommDef;
import com.huicheng.hotel.android.requestbuilder.bean.PlaneFlightInfoBean;
import com.prj.sdk.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @auth kborid
 * @date 2017/12/27 0027.
 */

public class PlaneFlightItemAdapter extends RecyclerView.Adapter<PlaneFlightItemAdapter.ViewHolder> {
    private Context context;
    private List<PlaneFlightInfoBean> mList = new ArrayList<>();
    private PlaneFlightInfoBean minBean = null;

    public PlaneFlightItemAdapter(Context context, List<PlaneFlightInfoBean> list) {
        this.context = context;
        this.mList = list;
    }

    public void updateMinFlightItemInfo(PlaneFlightInfoBean bean) {
        this.minBean = bean;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder hotelViewHolder;
        hotelViewHolder = new ViewHolder(LayoutInflater.from(context).inflate(R.layout.lv_plane_flight_item, parent, false));
        hotelViewHolder.setIsRecyclable(true);
        return hotelViewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        final PlaneFlightInfoBean bean = mList.get(position);

        viewHolder.tv_off_time.setText(bean.dptTime);
        viewHolder.tv_off_airport.setText(bean.dptAirport);
        viewHolder.tv_off_terminal.setText(bean.dptTerminal);
        viewHolder.tv_on_time.setText(bean.arrTime);
        viewHolder.tv_on_airport.setText(bean.arrAirport);
        viewHolder.tv_on_terminal.setText(bean.arrTerminal);
        if (bean.stop) {
            viewHolder.stopover_lay.setVisibility(View.VISIBLE);
            viewHolder.tv_stop_city.setText("经停" + bean.stopCityName);
        } else {
            viewHolder.stopover_lay.setVisibility(View.GONE);
        }
        viewHolder.tv_flight_during.setText(bean.flightTimes);
        viewHolder.tv_flight_price.setText(String.valueOf((int) bean.barePrice));
        viewHolder.tv_flight_carrier.setText(bean.carrier);
        viewHolder.tv_flight_code.setText(bean.flightNum);
        viewHolder.tv_flight_name.setText(bean.flightTypeFullName);
        if (StringUtil.notEmpty(bean.carrier)
                && PlaneCommDef.AIR_ICON_CODE.containsKey(bean.carrier)
                && PlaneCommDef.AIR_ICON_CODE.get(bean.carrier) != 0) {
            viewHolder.iv_flight_icon.setImageResource(PlaneCommDef.AIR_ICON_CODE.get(bean.carrier));
            viewHolder.iv_flight_icon.setVisibility(View.VISIBLE);
        } else {
            viewHolder.iv_flight_icon.setVisibility(View.GONE);
        }

        String correct = "99.99%";
        if (StringUtil.notEmpty(correct)) {
            viewHolder.tv_flight_percentInTime.setText(correct);
            viewHolder.zdl_lay.setVisibility(View.VISIBLE);
        } else {
            viewHolder.zdl_lay.setVisibility(View.GONE);
        }

        if (bean.equals(minBean)) {
            viewHolder.tv_tag_lowest.setVisibility(View.VISIBLE);
        } else {
            viewHolder.tv_tag_lowest.setVisibility(View.GONE);
        }

        // 点击事件
        viewHolder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != listener) {
                    listener.OnItemClick(v, position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout rootView;
        TextView tv_off_time;
        TextView tv_off_airport;
        TextView tv_off_terminal;
        TextView tv_on_time;
        TextView tv_on_airport;
        TextView tv_on_terminal;
        LinearLayout stopover_lay;
        TextView tv_stop_city;
        TextView tv_flight_during;
        TextView tv_flight_price;
        ImageView iv_flight_icon;
        TextView tv_flight_carrier;
        TextView tv_flight_code;
        TextView tv_flight_name;
        LinearLayout zdl_lay;
        TextView tv_flight_percentInTime;
        TextView tv_tag_lowest;

        public ViewHolder(View itemView) {
            super(itemView);
            rootView = (RelativeLayout) itemView.findViewById(R.id.rootView);
            tv_off_time = (TextView) itemView.findViewById(R.id.tv_off_time);
            tv_off_airport = (TextView) itemView.findViewById(R.id.tv_off_airport);
            tv_off_terminal = (TextView) itemView.findViewById(R.id.tv_off_terminal);
            tv_on_time = (TextView) itemView.findViewById(R.id.tv_on_time);
            tv_on_airport = (TextView) itemView.findViewById(R.id.tv_on_airport);
            tv_on_terminal = (TextView) itemView.findViewById(R.id.tv_on_terminal);
            stopover_lay = (LinearLayout) itemView.findViewById(R.id.stopover_lay);
            tv_stop_city = (TextView) itemView.findViewById(R.id.tv_stop_city);
            tv_flight_during = (TextView) itemView.findViewById(R.id.tv_flight_during);
            tv_flight_price = (TextView) itemView.findViewById(R.id.tv_flight_price);
            iv_flight_icon = (ImageView) itemView.findViewById(R.id.iv_flight_icon);
            tv_flight_carrier = (TextView) itemView.findViewById(R.id.tv_flight_carrier);
            tv_flight_code = (TextView) itemView.findViewById(R.id.tv_flight_code);
            tv_flight_name = (TextView) itemView.findViewById(R.id.tv_flight_name);
            zdl_lay = (LinearLayout) itemView.findViewById(R.id.zdl_lay);
            tv_flight_percentInTime = (TextView) itemView.findViewById(R.id.tv_flight_percentInTime);
            tv_tag_lowest = (TextView) itemView.findViewById(R.id.tv_tag_lowest);
        }
    }

    private OnItemRecycleViewClickListener listener = null;

    public void setOnItemRecycleViewClickListener(OnItemRecycleViewClickListener listener) {
        this.listener = listener;
    }
}