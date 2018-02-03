package com.huicheng.hotel.android.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.huicheng.hotel.android.R;

import java.util.ArrayList;

/**
 * @author kborid
 * @date 2018/2/3 0003.
 */

public class AddressChooserAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<String> mList = new ArrayList<>();
    private int mSelectedIndex;

    public AddressChooserAdapter(Context context, ArrayList<String> list) {
        this.context = context;
        this.mList = list;
        mSelectedIndex = -1;
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
        final ViewHolder viewHolder;
        if (null == convertView) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.lv_plane_address_item, null);
            viewHolder.iv_flag = (ImageView) convertView.findViewById(R.id.iv_flag);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        convertView.findViewById(R.id.action_lay).setVisibility(View.GONE);

        if (position == mSelectedIndex) {
            viewHolder.iv_flag.setSelected(true);
        } else {
            viewHolder.iv_flag.setSelected(false);
        }

        viewHolder.iv_flag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectedIndex(v.isSelected() ? -1 : position);
            }
        });

        return convertView;
    }

    public void setSelectedIndex(int index) {
        this.mSelectedIndex = index;
        notifyDataSetChanged();
    }

    class ViewHolder {
        ImageView iv_flag;
    }
}
