package com.huicheng.hotel.android.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.huicheng.hotel.android.net.bean.HomeBannerInfoBean;
import com.huicheng.hotel.android.ui.activity.HtmlActivity;
import com.huicheng.hotel.android.ui.glide.CustomReqURLFormatModelImpl;
import com.prj.sdk.util.StringUtil;

import java.util.List;

public class BannerImageAdapter extends PagerAdapter {

    private Context context;
    private List<HomeBannerInfoBean> list;

    public BannerImageAdapter(Context context, List<HomeBannerInfoBean> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return (view == object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        position %= list.size();
        if (position < 0) {
            position = list.size() + position;
        }

        final HomeBannerInfoBean bean = list.get(position);
        ImageView view = new ImageView(context);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        view.setScaleType(ImageView.ScaleType.FIT_XY);
        Glide.with(context)
                .load(new CustomReqURLFormatModelImpl(bean.url))
                .crossFade()
                .override(750, 480)
                .into(view);
        view.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String intentUrl = bean.target;

                if (!StringUtil.isEmpty(intentUrl)) {
                    Intent intent = new Intent(context, HtmlActivity.class);
                    intent.putExtra("path", intentUrl);
                    intent.putExtra("title", bean.bnname);
                    context.startActivity(intent);
                }
            }
        });

        //如果View已经在之前添加到了一个父组件，则必须先remove，否则会抛出IllegalStateException。
        ViewParent vp = view.getParent();
        if (vp != null) {
            ViewGroup parent = (ViewGroup) vp;
            parent.removeView(view);
        }
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
    }
}
