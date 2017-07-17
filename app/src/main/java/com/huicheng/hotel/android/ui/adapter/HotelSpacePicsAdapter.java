package com.huicheng.hotel.android.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.huicheng.hotel.android.R;
import com.prj.sdk.net.image.ImageLoader;
import com.prj.sdk.net.image.ImageLoader.ImageCallback;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;

import java.util.List;

public class HotelSpacePicsAdapter extends BaseAdapter {

    private Context mContext;
    private List<String> list;
    private int paddingValue = 0;

    public HotelSpacePicsAdapter(Context context, List<String> list, int paddingValue) {
        this.mContext = context;
        this.list = list;
        this.paddingValue = paddingValue;
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
            lp.height = lp.width;
            viewHolder.imageView.setLayoutParams(lp);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        setImgView(viewHolder.imageView, tempUrl);

        return convertView;
    }

    private void setImgView(final ImageView view, String url) {
        if (StringUtil.isEmpty(url)) {
            return;
        }
        view.setImageResource(R.color.hintColor);
        ImageLoader.getInstance().loadBitmap(new ImageCallback() {
            @Override
            public void imageCallback(Bitmap bm, String url, String imageTag) {
                if (bm != null) {
                    view.setImageBitmap(bm);
                }
            }

        }, url, url, 1920, 1080, 0);
    }
}
