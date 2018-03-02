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

    private static final int TYPE_HOTEL = 0;
    private static final int TYPE_PLANE = 1;

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
//        viewHolder.setIsRecyclable(true);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        switch (holder.getItemViewType()) {
            case TYPE_HOTEL:
                OrderDetailInfoBean hotelOrderDetailBean = list.get(position);
                HotelOrderViewHolder hotelOrderViewHolder = (HotelOrderViewHolder) holder;
                hotelOrderViewHolder.tv_name.setText(hotelOrderDetailBean.hotelName);
                hotelOrderViewHolder.tv_in_date.setText(DateUtil.getDay("M月d日", hotelOrderDetailBean.beginDate));
                hotelOrderViewHolder.tv_out_date.setText(DateUtil.getDay("M月d日", hotelOrderDetailBean.endDate));
                hotelOrderViewHolder.tv_position.setText(hotelOrderDetailBean.hotelAddress);
                hotelOrderViewHolder.tv_phone.setText(hotelOrderDetailBean.hotelPhone);
                hotelOrderViewHolder.tv_status.setText(hotelOrderDetailBean.orderStatusName);
                hotelOrderViewHolder.root.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (null != listener) {
                            listener.OnItemClick(v, position);
                        }
                    }
                });
                break;
            case TYPE_PLANE:
                break;
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        return list.get(position).type;
    }

    class HotelOrderViewHolder extends RecyclerView.ViewHolder {
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

    class PlaneOrderViewHolder extends RecyclerView.ViewHolder {
        PlaneOrderViewHolder(View itemView) {
            super(itemView);
        }
    }

    private OnRecycleViewItemClickListener listener = null;

    public void setOnRecycleViewItemClickListener(OnRecycleViewItemClickListener listener) {
        this.listener = listener;
    }
}
