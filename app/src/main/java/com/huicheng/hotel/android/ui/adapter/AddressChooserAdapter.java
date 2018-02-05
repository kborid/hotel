package com.huicheng.hotel.android.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.requestbuilder.bean.AddressInfoBean;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kborid
 * @date 2018/2/3 0003.
 */

public class AddressChooserAdapter extends BaseAdapter {
    private Context context;
    private List<AddressInfoBean> mList = new ArrayList<>();
    private int mSelectedIndex;
    private int defaultIndex;

    public AddressChooserAdapter(Context context, List<AddressInfoBean> list) {
        this.context = context;
        this.mList = list;
        mSelectedIndex = -1;
        defaultIndex = -1;
    }

    public int getDefaultIndex() {
        return defaultIndex;
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
            viewHolder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            viewHolder.tv_phone = (TextView) convertView.findViewById(R.id.tv_phone);
            viewHolder.tv_address = (TextView) convertView.findViewById(R.id.tv_address);
            viewHolder.iv_flag = (ImageView) convertView.findViewById(R.id.iv_flag);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        convertView.findViewById(R.id.action_lay).setVisibility(View.GONE);

        AddressInfoBean bean = mList.get(position);

        //如果有默认，则显示勾选默认flag
        defaultIndex = bean.isdefault ? position : -1;
        //首次显示列表时，设置默认勾选flag;如果没有默认，则都不勾选
        if (mSelectedIndex == -1) {
            mSelectedIndex = defaultIndex;
        }

        if (position == mSelectedIndex) {
            viewHolder.iv_flag.setSelected(true);
        } else {
            viewHolder.iv_flag.setSelected(false);
        }

        viewHolder.tv_name.setText(bean.name);
        viewHolder.tv_phone.setText(bean.phone);
        viewHolder.tv_address.setText(bean.province + bean.city + bean.area + bean.address);

        viewHolder.iv_flag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectedIndex(v.isSelected() ? -1 : position);
                if (null != listener) {
                    listener.onCheck(position);
                }
            }
        });

        return convertView;
    }

    public void setSelectedIndex(int index) {
        this.mSelectedIndex = index;
        notifyDataSetChanged();
    }

    class ViewHolder {
        TextView tv_name;
        TextView tv_phone;
        TextView tv_address;
        ImageView iv_flag;
    }

    private OnFlagCheckedListener listener = null;

    public void setOnFlagCheckedListener(OnFlagCheckedListener listener) {
        this.listener = listener;
    }

    public interface OnFlagCheckedListener {
        void onCheck(int position);
    }
}
