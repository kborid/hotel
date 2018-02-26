package com.huicheng.hotel.android.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.requestbuilder.bean.OrderDetailInfoBean;

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
                viewHolder = new MainOrderAdapter.HotelOrderViewHolder(LayoutInflater.from(context).inflate(R.layout.lv_planeorder_item, parent, false));
                break;
        }
//        viewHolder.setIsRecyclable(true);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case TYPE_HOTEL:
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
        public HotelOrderViewHolder(View itemView) {
            super(itemView);
        }
    }

    class PlaneOrderViewHolder extends RecyclerView.ViewHolder {
        public PlaneOrderViewHolder(View itemView) {
            super(itemView);
        }
    }
}
