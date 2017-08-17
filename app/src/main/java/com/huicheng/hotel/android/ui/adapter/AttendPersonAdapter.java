package com.huicheng.hotel.android.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.huicheng.hotel.android.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.huicheng.hotel.android.ui.base.BaseFragment.loadImage;


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
        loadImage(viewHolder.iv_photo, R.drawable.def_photo, url, 0, 0);

        return convertView;
    }

    class ViewHolder {
        CircleImageView iv_photo;
        TextView tv_name;
    }
}
