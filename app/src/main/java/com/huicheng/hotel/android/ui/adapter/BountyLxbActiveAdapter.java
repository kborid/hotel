package com.huicheng.hotel.android.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.requestbuilder.bean.HomeBannerInfoBean;
import com.huicheng.hotel.android.ui.activity.HtmlActivity;
import com.huicheng.hotel.android.ui.glide.CustomReqURLFormatModelImpl;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kborid
 * @date 2018/1/26 0026.
 */

public class BountyLxbActiveAdapter extends BaseAdapter {

    private Context context;
    private List<HomeBannerInfoBean> mList = new ArrayList<>();

    public BountyLxbActiveAdapter(Context context, List<HomeBannerInfoBean> list) {
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
        ViewHolder viewHolder;
        if (null == convertView) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.lv_lxb_active_item, null);
            viewHolder.iv_bg = (ImageView) convertView.findViewById(R.id.iv_bg);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.width = Utils.mScreenWidth;
            lp.height = (int) ((float) lp.width * 104 / 360);
            viewHolder.iv_bg.setLayoutParams(lp);
            viewHolder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final HomeBannerInfoBean bean = mList.get(position);
        Glide.with(context)
                .load(new CustomReqURLFormatModelImpl(bean.url))
                .crossFade()
                .override(1080, 312)
                .into(viewHolder.iv_bg);
        viewHolder.tv_title.setText(bean.bnname);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!StringUtil.isEmpty(bean.target)) {
                    Intent intent = new Intent(context, HtmlActivity.class);
                    intent.putExtra("path", bean.target);
                    intent.putExtra("title", bean.bnname);
                    context.startActivity(intent);
                }
            }
        });
        return convertView;
    }

    private class ViewHolder {
        ImageView iv_bg;
        TextView tv_title;
    }
}
