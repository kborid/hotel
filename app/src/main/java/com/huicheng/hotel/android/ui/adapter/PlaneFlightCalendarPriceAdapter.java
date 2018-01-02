package com.huicheng.hotel.android.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @auth kborid
 * @date 2017/12/27 0027.
 */

public class PlaneFlightCalendarPriceAdapter extends RecyclerView.Adapter<PlaneFlightCalendarPriceAdapter.ViewHolder> {

    private Context context;
    private LinearLayout.LayoutParams mLp = null;
    private List<Date> mList = new ArrayList<>();
    private int minPrice = 0;
    private long timestamp = 0;
    private int selectedIndex = 0;

    public PlaneFlightCalendarPriceAdapter(Context context, List<Date> list) {
        this.context = context;
        this.mList = list;
    }

    public void setMinPrice(int minPrice, long timestamp) {
        this.minPrice = minPrice;
        this.timestamp = timestamp;
    }

    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder hotelViewHolder;
        hotelViewHolder = new ViewHolder(LayoutInflater.from(context).inflate(R.layout.plane_date_item, parent, false));
        hotelViewHolder.setIsRecyclable(true);
        return hotelViewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        if (mLp == null) {
            mLp = new LinearLayout.LayoutParams((int) ((float) (Utils.mScreenWidth - Utils.dp2px(84)) / 6), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        viewHolder.root.setLayoutParams(mLp);
        Date date = mList.get(position);
        viewHolder.tv_week.setText(DateUtil.dateToWeek(date));
        viewHolder.tv_day.setText(DateUtil.getDay("d", date.getTime()));

        if (DateUtil.getGapCount(date, new Date(timestamp)) == 0 && minPrice > 0) {
            viewHolder.tv_price.setVisibility(View.VISIBLE);
            viewHolder.tv_price.setText(String.format(context.getResources().getString(R.string.rmbStr), String.valueOf(minPrice)));
        } else {
            viewHolder.tv_price.setVisibility(View.INVISIBLE);
        }

        if (selectedIndex == position) {
            viewHolder.root.setSelected(true);
        } else {
            viewHolder.root.setSelected(false);
        }

        viewHolder.root.setOnClickListener(new View.OnClickListener() {
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
        LinearLayout root;
        TextView tv_week;
        TextView tv_day;
        TextView tv_price;

        public ViewHolder(View itemView) {
            super(itemView);
            root = (LinearLayout) itemView.findViewById(R.id.root);
            tv_week = (TextView) itemView.findViewById(R.id.tv_week);
            tv_day = (TextView) itemView.findViewById(R.id.tv_day);
            tv_price = (TextView) itemView.findViewById(R.id.tv_price);
        }
    }

    private OnItemRecycleViewClickListener listener = null;

    public void setOnItemRecycleViewClickListener(OnItemRecycleViewClickListener listener) {
        this.listener = listener;
    }
}
