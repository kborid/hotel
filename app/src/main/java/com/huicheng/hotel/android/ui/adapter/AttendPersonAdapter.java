package com.huicheng.hotel.android.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.ui.glide.CustomReqURLFormatModelImpl;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class AttendPersonAdapter extends BaseAdapter {

    private List<PersonInfo> mList;
    private Context mContext;

    public AttendPersonAdapter(Context context, List<PersonInfo> list) {
        mList = list;
        mContext = context;
    }

    @Override
    public int getCount() {
        return mList == null ? 0 : mList.size();
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
        final ViewHolder viewHolder;
        if (null == convertView) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.lv_attendperson_item, null);
            viewHolder.iv_photo = (CircleImageView) convertView.findViewById(R.id.iv_photo);
            viewHolder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.tv_name.setText(mList.get(position).attentusername);
        String url = mList.get(position).attentuserheadphoto;
        Glide.with(mContext)
                .load(new CustomReqURLFormatModelImpl(url))
                .placeholder(R.drawable.def_photo)
                .crossFade()
                .centerCrop()
                .override(150, 150)
                .into(viewHolder.iv_photo);

        return convertView;
    }

    class ViewHolder {
        CircleImageView iv_photo;
        TextView tv_name;
    }
}
