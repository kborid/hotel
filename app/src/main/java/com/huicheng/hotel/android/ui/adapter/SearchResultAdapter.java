package com.huicheng.hotel.android.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.net.bean.HotelInfoBean;

import java.util.ArrayList;
import java.util.List;

/**
 * @auth kborid
 * @date 2017/8/31.
 */

public class SearchResultAdapter extends BaseAdapter {
    private Context context;
    private List<HotelInfoBean> list = new ArrayList<>();

    public SearchResultAdapter(Context context, List<HotelInfoBean> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (null == convertView) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.lv_search_result_item, null);
            viewHolder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
            viewHolder.tv_summary = (TextView) convertView.findViewById(R.id.tv_summary);
            viewHolder.tv_point = (TextView) convertView.findViewById(R.id.tv_point);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.tv_title.setText(list.get(position).hotelName);
        viewHolder.tv_summary.setText(
                String.format(context.getResources().getString(R.string.rmbStr), String.valueOf(list.get(position).price))
                        + ", "
                        + list.get(position).hotelAddress);
        viewHolder.tv_point.setText(list.get(position).hotelGrade + "分");

        return convertView;
    }

    class ViewHolder {
        TextView tv_title;
        TextView tv_summary;
        TextView tv_point;
    }
}
