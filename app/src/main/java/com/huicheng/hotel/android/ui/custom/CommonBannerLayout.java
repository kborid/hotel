package com.huicheng.hotel.android.ui.custom;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.net.bean.MainBannerBean;
import com.huicheng.hotel.android.ui.activity.HtmlActivity;
import com.huicheng.hotel.android.ui.adapter.BannerImageAdapter;
import com.prj.sdk.net.image.ImageLoader;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author kborid
 * @date 2016/8/15
 */
public class CommonBannerLayout extends RelativeLayout implements ViewPager.OnPageChangeListener {

    private static final int DELAYTIME = 3000;
    private Context context;
    private ArrayList<View> bannerViews = new ArrayList<>();
    private ViewPager viewpager;
    private LinearLayout points_lay;
    private BannerImageAdapter mAdapter;
    private Handler myHandler = new Handler();
    private int pointIndex = 0;

    public CommonBannerLayout(Context context) {
        super(context);
        init(context);
    }

    public CommonBannerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CommonBannerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.comm_banner_layout, this);
        findViews();
        initListener();
    }

    private void initListener() {
        viewpager.addOnPageChangeListener(this);
    }

    private void findViews() {
        viewpager = (ViewPager) findViewById(R.id.viewpager);
        points_lay = (LinearLayout) findViewById(R.id.points_lay);
        mAdapter = new BannerImageAdapter(bannerViews);
        viewpager.setAdapter(mAdapter);
    }

    private void clearBannerBitmap() {
        for (int i = 0; i < bannerViews.size(); i++) {
            ImageView iv = (ImageView) bannerViews.get(i);
            iv.setImageBitmap(null);
        }
    }

    public void setImageResource(List<MainBannerBean> list, String domain) {
        clearBannerBitmap();
        bannerViews.clear();
        for (int i = 0; i < list.size(); i++) {
            final MainBannerBean bean = list.get(i);
            ImageView view = new ImageView(context);
            view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            view.setScaleType(ImageView.ScaleType.FIT_XY);
            loadImg(domain + bean.imgurls, view);
            view.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    String intentUrl = bean.linkurls;

                    // 添加友盟自定义事件
                    HashMap<String, String> map = new HashMap<>();
                    map.put("url", intentUrl);
                    MobclickAgent.onEvent(context, "ServiceDidTapped", map);

                    if (!StringUtil.isEmpty(intentUrl)) {
                        Intent intent = new Intent(context, HtmlActivity.class);
                        intent.putExtra("path", intentUrl);
                        intent.putExtra("title", bean.bnname);
                        context.startActivity(intent);
                    }
                }
            });
            bannerViews.add(view);
        }
        mAdapter.notifyDataSetChanged();
        initIndicator();
    }

    /**
     * 图片滚动线程
     */

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            viewpager.setCurrentItem(viewpager.getCurrentItem() + 1);
            myHandler.postDelayed(runnable, DELAYTIME);
        }
    };

    public void startBanner() {
        myHandler.removeCallbacks(runnable);
        if (bannerViews.size() > 1) {
            myHandler.postDelayed(runnable, DELAYTIME);
        }
    }

    public void stopBanner() {
        myHandler.removeCallbacks(runnable);
    }

    private void initIndicator() {
        int length = bannerViews.size();
        points_lay.removeAllViews();
        if (length <= 1) {
            return;
        } else {
            for (int i = 0; i < length; i++) {
                View view = new View(context);
                view.setBackgroundResource(R.drawable.indicator_sel);
                view.setEnabled(false);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Utils.dip2px(7), Utils.dip2px(7));
                if (i > 0) {
                    lp.leftMargin = Utils.dip2px(7);
                }
                view.setLayoutParams(lp);
                points_lay.addView(view);
            }
        }

        pointIndex = pointIndex % bannerViews.size();
        points_lay.getChildAt(pointIndex).setEnabled(true);
        startBanner();
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
    }

    @Override
    public void onPageSelected(int position) {
        if (bannerViews.size() <= 0) {
            return;
        }
        int newPosition = position % bannerViews.size();
        points_lay.getChildAt(newPosition).setEnabled(true);
        points_lay.getChildAt(pointIndex).setEnabled(false);
        pointIndex = newPosition;
    }

    private void loadImg(String url, final ImageView imageView) {
        if (url != null) {
            imageView.setBackgroundResource(R.color.hintColor);
            imageView.setTag(R.id.image_url, url);
            ImageLoader.getInstance().loadBitmap(new ImageLoader.ImageCallback() {
                @Override
                public void imageCallback(Bitmap bm, String url,
                                          String imageTag) {
                    if (bm != null) {
                        imageView.setImageBitmap(bm);
                    }
                }

            }, url, url, 480, 320, -1);
        }
    }
}
