package com.huicheng.hotel.android.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.ui.glide.CustomReqURLFormatModelImpl;
import com.prj.sdk.util.Utils;

import java.util.List;

public class CommonGridViewPicsAdapter extends BaseAdapter {

    private Context mContext;
    private List<String> list;
    private int paddingValue = 0;
    private float widthHeightScale = 1f;

    public CommonGridViewPicsAdapter(Context context, List<String> list, int paddingValue) {
        this.mContext = context;
        this.list = list;
        this.paddingValue = paddingValue;
    }

    public CommonGridViewPicsAdapter(Context context, List<String> list, int paddingValue, float widthHeightScale) {
        this.mContext = context;
        this.list = list;
        this.paddingValue = paddingValue;
        this.widthHeightScale = widthHeightScale;
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

    private final class ViewHolder {
        ImageView imageView;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        String tempUrl = list.get(position);
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.gv_hotelspace_picture_item, parent, false);
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) viewHolder.imageView.getLayoutParams();
            lp.width = (int) ((float) (Utils.mScreenWidth - paddingValue) / 3);
            lp.height = (int) (lp.width * widthHeightScale);
            viewHolder.imageView.setLayoutParams(lp);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Glide.with(mContext)
                .load(new CustomReqURLFormatModelImpl(tempUrl))
                .placeholder(R.color.hintColor)
                .crossFade()
                .centerCrop()
                .override(500, 500)
                .into(viewHolder.imageView);

        return convertView;
    }
}
