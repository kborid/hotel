package com.huicheng.hotel.android.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.requestbuilder.bean.OrderDetailInfoBean;
import com.huicheng.hotel.android.ui.listener.OnRecycleViewItemClickListener;
import com.prj.sdk.util.DateUtil;

import java.util.List;

/**
 * @author kborid
 * @date 2018/2/26 0026.
 */

public class MainOrderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HOTEL = 1;
    private static final int TYPE_PLANE = 2;
    private static final int ONE_MINUTE = 1000 * 60;
    private static final int ONE_HOUR = ONE_MINUTE * 60;
    private static final int ONE_DAY = ONE_HOUR * 24;

    private Context context;
    private List<OrderDetailInfoBean> list;

    public MainOrderAdapter(Context context, List<OrderDetailInfoBean> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        switch (viewType) {
            case TYPE_HOTEL:
                viewHolder = new MainOrderAdapter.HotelOrderViewHolder(LayoutInflater.from(context).inflate(R.layout.lv_hotelorder_item, parent, false));
                break;
            case TYPE_PLANE:
                viewHolder = new MainOrderAdapter.PlaneOrderViewHolder(LayoutInflater.from(context).inflate(R.layout.lv_planeorder_item, parent, false));
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        OrderDetailInfoBean orderDetailBean = list.get(position);
        switch (holder.getItemViewType()) {
            case TYPE_HOTEL:
                HotelOrderViewHolder hotelOVH = (HotelOrderViewHolder) holder;
                hotelOVH.tv_name.setText(orderDetailBean.hotelName);
                hotelOVH.tv_in_date.setText(DateUtil.getDay("M月d日", orderDetailBean.beginDate));
                hotelOVH.tv_out_date.setText(DateUtil.getDay("M月d日", orderDetailBean.endDate));
                hotelOVH.tv_position.setText(orderDetailBean.hotelAddress);
                hotelOVH.tv_phone.setText(orderDetailBean.hotelPhone);
                hotelOVH.tv_status.setText(orderDetailBean.orderStatusName);
                hotelOVH.root.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (null != listener) {
                            listener.OnItemClick(v, position);
                        }
                    }
                });
                break;
            case TYPE_PLANE:
                PlaneOrderViewHolder planeOVH = (PlaneOrderViewHolder) holder;
                planeOVH.tv_name.setText(orderDetailBean.hotelName);
                planeOVH.tv_date.setText(DateUtil.getDay("M月d日", orderDetailBean.flydate));
                planeOVH.tv_off_time.setText(orderDetailBean.sTime);
                planeOVH.tv_on_time.setText(orderDetailBean.eTime);
                planeOVH.tv_off_airport.setText(orderDetailBean.sAirport);
                planeOVH.tv_on_airport.setText(orderDetailBean.eAirport);
                planeOVH.tv_flight.setText(orderDetailBean.flightNo);
                planeOVH.tv_passenger.setText(orderDetailBean.passengerNames);
                if (orderDetailBean.toFlyTime > 0) {
                    int day = (int) orderDetailBean.toFlyTime / ONE_DAY;
                    int hour = (int) (orderDetailBean.toFlyTime - day * ONE_DAY) / ONE_HOUR;
                    int min = (int) (orderDetailBean.toFlyTime - day * ONE_DAY - hour * ONE_HOUR) / ONE_MINUTE;
                    if (day > 0) {
                        planeOVH.tv_tofly.setText(String.format("%1$d天%2$d时%3$d分", day, hour, min));
                    } else {
                        planeOVH.tv_tofly.setText(String.format("%1$d时%2$d分", hour, min));
                    }
                } else {
                    planeOVH.tv_tofly.setText("已起飞");
                }
                planeOVH.tv_status.setText(orderDetailBean.orderStatusName);
                planeOVH.root.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (null != listener) {
                            listener.OnItemClick(v, position);
                        }
                    }
                });
                break;
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        return Integer.valueOf(list.get(position).orderType);
    }

    static class HotelOrderViewHolder extends RecyclerView.ViewHolder {
        FrameLayout root;
        TextView tv_name;
        TextView tv_in_date;
        TextView tv_out_date;
        TextView tv_position;
        TextView tv_phone;
        TextView tv_status;

        HotelOrderViewHolder(View itemView) {
            super(itemView);
            root = (FrameLayout) itemView.findViewById(R.id.root);
            tv_name = (TextView) itemView.findViewById(R.id.tv_name);
            tv_in_date = (TextView) itemView.findViewById(R.id.tv_in_date);
            tv_out_date = (TextView) itemView.findViewById(R.id.tv_out_date);
            tv_position = (TextView) itemView.findViewById(R.id.tv_position);
            tv_phone = (TextView) itemView.findViewById(R.id.tv_phone);
            tv_status = (TextView) itemView.findViewById(R.id.tv_status);
        }
    }

    static class PlaneOrderViewHolder extends RecyclerView.ViewHolder {
        FrameLayout root;
        TextView tv_name;
        TextView tv_date;
        TextView tv_off_time;
        TextView tv_off_airport;
        TextView tv_on_time;
        TextView tv_on_airport;
        TextView tv_flight;
        TextView tv_passenger;
        TextView tv_tofly;
        TextView tv_status;

        PlaneOrderViewHolder(View itemView) {
            super(itemView);
            root = (FrameLayout) itemView.findViewById(R.id.root);
            tv_name = (TextView) itemView.findViewById(R.id.tv_name);
            tv_date = (TextView) itemView.findViewById(R.id.tv_date);
            tv_off_time = (TextView) itemView.findViewById(R.id.tv_off_time);
            tv_on_time = (TextView) itemView.findViewById(R.id.tv_on_time);
            tv_off_airport = (TextView) itemView.findViewById(R.id.tv_off_airport);
            tv_on_airport = (TextView) itemView.findViewById(R.id.tv_on_airport);
            tv_flight = (TextView) itemView.findViewById(R.id.tv_flight);
            tv_passenger = (TextView) itemView.findViewById(R.id.tv_passenger);
            tv_tofly = (TextView) itemView.findViewById(R.id.tv_tofly);
            tv_status = (TextView) itemView.findViewById(R.id.tv_status);
        }
    }

    private OnRecycleViewItemClickListener listener = null;

    public void setOnRecycleViewItemClickListener(OnRecycleViewItemClickListener listener) {
        this.listener = listener;
    }
}
