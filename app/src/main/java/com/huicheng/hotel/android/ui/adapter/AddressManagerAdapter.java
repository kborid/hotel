package com.huicheng.hotel.android.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.requestbuilder.bean.AddressInfoBean;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kborid
 * @date 2018/2/3 0003.
 */

public class AddressManagerAdapter extends BaseAdapter {
    private Context context;
    private List<AddressInfoBean> mList = new ArrayList<>();

    public AddressManagerAdapter(Context context, List<AddressInfoBean> list) {
        this.context = context;
        this.mList = list;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (null == convertView) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.lv_plane_address_item, null);
            viewHolder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            viewHolder.tv_phone = (TextView) convertView.findViewById(R.id.tv_phone);
            viewHolder.tv_address = (TextView) convertView.findViewById(R.id.tv_address);
            viewHolder.tv_edit = (TextView) convertView.findViewById(R.id.tv_edit);
            viewHolder.tv_delete = (TextView) convertView.findViewById(R.id.tv_delete);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        convertView.findViewById(R.id.flag_lay).setVisibility(View.INVISIBLE);

        AddressInfoBean bean = mList.get(position);
        viewHolder.tv_name.setText(bean.name);
        viewHolder.tv_phone.setText(bean.phone);
        viewHolder.tv_address.setText(bean.province + bean.city + bean.area + bean.address);

        viewHolder.tv_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != listener) {
                    listener.onEdit(position);
                }
            }
        });

        viewHolder.tv_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != listener) {
                    listener.onDelete(position);
                }
            }
        });
        return convertView;
    }

    class ViewHolder {
        TextView tv_name;
        TextView tv_phone;
        TextView tv_address;
        TextView tv_edit;
        TextView tv_delete;
    }

    private OnActionListener listener = null;

    public void setOnActionListener(OnActionListener listener) {
        this.listener = listener;
    }

    public interface OnActionListener {
        void onEdit(int position);

        void onDelete(int position);
    }
}
