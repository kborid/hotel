package com.huicheng.hotel.android.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.huicheng.hotel.android.R;

import java.util.ArrayList;

/**
 * @author kborid
 * @date 2018/2/3 0003.
 */

public class AddressManagerAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<String> mList = new ArrayList<>();

    public AddressManagerAdapter(Context context, ArrayList<String> list) {
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
    public View getView(int position, View convertView, ViewGroup parent) {
        if (null == convertView) {
            convertView = LayoutInflater.from(context).inflate(R.layout.lv_plane_address_item, null);
        }
        convertView.findViewById(R.id.flag_lay).setVisibility(View.INVISIBLE);
        return convertView;
    }
}
