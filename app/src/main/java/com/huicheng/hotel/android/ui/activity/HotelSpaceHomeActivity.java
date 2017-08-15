package com.huicheng.hotel.android.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.HotelOrderManager;
import com.huicheng.hotel.android.common.NetURL;
import com.huicheng.hotel.android.net.RequestBeanBuilder;
import com.huicheng.hotel.android.net.bean.HotelSpaceBasicInfoBean;
import com.huicheng.hotel.android.net.bean.HotelSpaceTieInfoBean;
import com.huicheng.hotel.android.ui.adapter.CommonGridViewPicsAdapter;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.huicheng.hotel.android.ui.custom.FullscreenHolder;
import com.huicheng.hotel.android.ui.custom.MyListViewWidget;
import com.huicheng.hotel.android.ui.custom.NoScrollGridView;
import com.huicheng.hotel.android.ui.custom.RoundedAllImageView;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;
import com.prj.sdk.widget.CustomToast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author kborid
 * @date 2017/3/20 0020
 */
public class HotelSpaceHomeActivity extends BaseActivity implements DataCallback {
    private static final String TAG = "HotelSpaceHomeActivity";
    /**
     * 视频全屏参数
     */
    protected static final FrameLayout.LayoutParams COVER_SCREEN_PARAMS = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    private View customView;
    private FrameLayout fullscreenContainer;
    private WebChromeClient.CustomViewCallback myCallBack = null;

    private MyListViewWidget listview;
    private List<HotelSpaceTieInfoBean> list = new ArrayList<>();
    private HotelSpaceItemAdapter adapter;
    private HotelSpaceBasicInfoBean hotelSpaceBasicInfoBean = null;
    private int pageIndex = 0;
    private static final int PAGESIZE = 10;
    private LinearLayout root_lay;
    private RoundedAllImageView iv_hotel_bg;
    private TextView tv_tie_count, tv_fans_count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_spacehome_layout);
        initViews();
        initParams();
        initListeners();
        requestHotelSpaceBasicInfo();
    }

    @Override
    public void initViews() {
        super.initViews();
        ScrollView scrollview = (ScrollView) findViewById(R.id.scrollview);
        root_lay = (LinearLayout) findViewById(R.id.root_lay);
        iv_hotel_bg = (RoundedAllImageView) findViewById(R.id.iv_hotel_bg);
        tv_tie_count = (TextView) findViewById(R.id.tv_tie_count);
        tv_fans_count = (TextView) findViewById(R.id.tv_fans_count);
        listview = (MyListViewWidget) findViewById(R.id.listview);
        adapter = new HotelSpaceItemAdapter(this, list);
        listview.setAdapter(adapter);

        //没有数据时，显示空view提示
        TextView emptyView = new TextView(this);
        emptyView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        emptyView.setText("酒店暂未发布帖子");
        emptyView.setPadding(0, Utils.dip2px(30), 0, Utils.dip2px(30));
        emptyView.setGravity(Gravity.CENTER);
        emptyView.setTextColor(getResources().getColor(R.color.searchHintColor));
        emptyView.setVisibility(View.GONE);
        ((ViewGroup) listview.getParent()).addView(emptyView);
        listview.setEmptyView(emptyView);

        if (scrollview != null) {
            scrollview.smoothScrollTo(0, 0);
        }
    }

    @Override
    public void dealIntent() {
        super.dealIntent();
    }

    @Override
    public void initParams() {
        super.initParams();
        btn_right.setImageResource(R.drawable.iv_favorite_gray);
        if (HotelOrderManager.getInstance().getHotelDetailInfo().isPopup) {
            btn_right.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void initListeners() {
        super.initListeners();
        btn_right.setOnClickListener(this);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LogUtil.i(TAG, "position = " + position);
                Intent intent = new Intent(HotelSpaceHomeActivity.this, HotelSpaceDetailActivity.class);
                intent.putExtra("hotelId", HotelOrderManager.getInstance().getHotelDetailInfo().id);
                intent.putExtra("articleId", list.get(position).id);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.btn_right:
                showAddVipDialog(this, HotelOrderManager.getInstance().getHotelDetailInfo());
                break;
            default:
                break;
        }
    }

    @Override
    protected void requestHotelVip2(String email, String idcode, String realname, String traveltype) {
        super.requestHotelVip2(email, idcode, realname, traveltype);
        RequestBeanBuilder b = RequestBeanBuilder.create(true);

        b.addBody("email", email);
        b.addBody("hotelId", String.valueOf(HotelOrderManager.getInstance().getHotelDetailInfo().id));
        b.addBody("idcode", idcode);
        b.addBody("realname", realname);
        b.addBody("traveltype", traveltype);

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.HOTEL_VIP;
        d.flag = AppConst.HOTEL_VIP;

        if (!isProgressShowing()) {
            showProgressDialog(this);
        }

        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void requestHotelSpaceBasicInfo() {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("hotelId", String.valueOf(HotelOrderManager.getInstance().getHotelDetailInfo().id));

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.HOTEL_SPACE;
        d.flag = AppConst.HOTEL_SPACE;

        if (!isProgressShowing()) {
            showProgressDialog(this);
        }

        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void requestHotelSpaceTiesInfo(int pageIndex) {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("hotelId", String.valueOf(HotelOrderManager.getInstance().getHotelDetailInfo().id));
        b.addBody("pageIndex", String.valueOf(pageIndex));
        b.addBody("pageSize", String.valueOf(PAGESIZE));

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.HOTEL_TIE;
        d.flag = AppConst.HOTEL_TIE;
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void refreshHotelSpaceBasicInfo() {
        if (hotelSpaceBasicInfoBean != null) {
            root_lay.setVisibility(View.VISIBLE);
            tv_center_title.setText(hotelSpaceBasicInfoBean.hotelName + "的空间");
            loadImage(iv_hotel_bg, R.drawable.def_hotel_banner, hotelSpaceBasicInfoBean.pic, 1920, 1080);
            tv_tie_count.setText(String.valueOf(hotelSpaceBasicInfoBean.articleCnt));
            tv_fans_count.setText(String.valueOf(hotelSpaceBasicInfoBean.vipCnt));
        } else {
            root_lay.setVisibility(View.GONE);
        }
    }

    /**
     * 视频播放全屏
     **/
    private void showCustomView(View view, WebChromeClient.CustomViewCallback callback) {
        // if a view already exists then immediately terminate the new one
        if (customView != null) {
            callback.onCustomViewHidden();
            return;
        }

        hideBottomAndStatusBar();
        FrameLayout decor = (FrameLayout) getWindow().getDecorView();
        fullscreenContainer = new FullscreenHolder(HotelSpaceHomeActivity.this);
        fullscreenContainer.addView(view, COVER_SCREEN_PARAMS);
        decor.addView(fullscreenContainer, COVER_SCREEN_PARAMS);
        customView = view;
        myCallBack = callback;
    }

    /**
     * 隐藏视频全屏
     */
    private void hideCustomView() {
        if (customView == null) {
            return;
        }
        showBottomAndStatusBar();
        FrameLayout decor = (FrameLayout) getWindow().getDecorView();
        decor.removeView(fullscreenContainer);
        fullscreenContainer = null;
        customView = null;
        myCallBack.onCustomViewHidden();
//        webview.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopPlay();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void preExecute(ResponseData request) {

    }

    @Override
    public void notifyMessage(ResponseData request, ResponseData response) throws Exception {
        if (response != null && response.body != null) {
            if (request.flag == AppConst.HOTEL_SPACE) {
                LogUtil.i(TAG, "json = " + response.body.toString());
                hotelSpaceBasicInfoBean = JSON.parseObject(response.body.toString(), HotelSpaceBasicInfoBean.class);
                requestHotelSpaceTiesInfo(pageIndex);
            } else if (request.flag == AppConst.HOTEL_TIE) {
                removeProgressDialog();
                LogUtil.i(TAG, "json = " + response.body.toString());
                List<HotelSpaceTieInfoBean> temp = JSON.parseArray(response.body.toString(), HotelSpaceTieInfoBean.class);
                if (temp.size() > 0) {
                    list.clear();
                    list.addAll(temp);
                }
                adapter.notifyDataSetChanged();
                refreshHotelSpaceBasicInfo();
            } else if (request.flag == AppConst.HOTEL_VIP) {
                removeProgressDialog();
                LogUtil.i(TAG, "Json = " + response.body.toString());
                CustomToast.show("您已成为该酒店会员", CustomToast.LENGTH_SHORT);
            }
        }
    }

    class HotelSpaceItemAdapter extends BaseAdapter {
        private Context context;
        private List<HotelSpaceTieInfoBean> list = new ArrayList<>();

        public HotelSpaceItemAdapter(Context context, List<HotelSpaceTieInfoBean> list) {
            this.context = context;
            this.list = list;
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

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder viewHolder;
            if (null == convertView) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.lv_hotelspace_item, null);
                viewHolder.tv_time_label = (TextView) convertView.findViewById(R.id.tv_time_label);
                viewHolder.tv_content = (TextView) convertView.findViewById(R.id.tv_content);
                viewHolder.tv_content.setLines(4);
                viewHolder.tv_content.setTextSize(13);
                viewHolder.tv_content.setTextColor(context.getResources().getColor(R.color.calendarTextColor));
                viewHolder.tv_content.setEllipsize(TextUtils.TruncateAt.END);
                viewHolder.gridview = (NoScrollGridView) convertView.findViewById(R.id.gridview);
                viewHolder.webview = (WebView) convertView.findViewById(R.id.webview);
                WebSettings webSettings = viewHolder.webview.getSettings();
                webSettings.setJavaScriptEnabled(true);
                webSettings.setUseWideViewPort(true); // 关键点
                webSettings.setAllowFileAccess(true); // 允许访问文件
                webSettings.setSupportZoom(true); // 支持缩放
                webSettings.setLoadWithOverviewMode(true);
                webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
                viewHolder.webview.setWebChromeClient(new WebChromeClient() {
                    /*** 视频播放相关的方法 **/
                    @Override
                    public View getVideoLoadingProgressView() {
                        return LayoutInflater
                                .from(context).inflate(
                                        R.layout.video_loading_progress, null);
                    }

                    @Override
                    public Bitmap getDefaultVideoPoster() {
                        LogUtil.i(TAG, "poster = " + super.getDefaultVideoPoster());
                        return super.getDefaultVideoPoster();
                    }

                    @Override
                    public void onShowCustomView(View view, CustomViewCallback callback) {
                        showCustomView(view, callback);
                    }

                    @Override
                    public void onHideCustomView() {
                        hideCustomView();
                    }
                });
                viewHolder.webview.setBackgroundColor(context.getResources().getColor(R.color.black)); // 设置背景色
                viewHolder.webview.getBackground().setAlpha(0); // 设置填充透明度 范围：0-255

                viewHolder.tv_share = (TextView) convertView.findViewById(R.id.tv_space_share);
                viewHolder.tv_comment = (TextView) convertView.findViewById(R.id.tv_space_comment);
                viewHolder.tv_support = (TextView) convertView.findViewById(R.id.tv_space_support);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            // 设置数据
            final HotelSpaceTieInfoBean bean = list.get(position);
            String thisBlankDate = DateUtil.getDateBlank(System.currentTimeMillis(), bean.createTimeStamp);
            if (0 == position) {
                viewHolder.tv_time_label.setVisibility(View.VISIBLE);
                viewHolder.tv_time_label.setText(thisBlankDate);
            } else {
                String lastBlankDate = DateUtil.getDateBlank(System.currentTimeMillis(), list.get(position - 1).createTimeStamp);
                if (lastBlankDate.equals(thisBlankDate)) {
                    viewHolder.tv_time_label.setVisibility(View.INVISIBLE);
                } else {
                    viewHolder.tv_time_label.setVisibility(View.VISIBLE);
                    viewHolder.tv_time_label.setText(thisBlankDate);
                }
            }

            if (StringUtil.notEmpty(bean.content)) {
                viewHolder.tv_content.setVisibility(View.VISIBLE);
                viewHolder.tv_content.setText(bean.content);
            } else {
                viewHolder.tv_content.setVisibility(View.GONE);
            }

            if (StringUtil.notEmpty(bean.picUrl)) {
                String[] picArray = bean.picUrl.split(",");
                List<String> pictureList = Arrays.asList(picArray);
                viewHolder.gridview.setVisibility(View.VISIBLE);
                viewHolder.gridview.setClickable(false);
                viewHolder.gridview.setPressed(false);
                viewHolder.gridview.setEnabled(false);
                int size = pictureList.size();
                if (size > 9) {
                    pictureList = pictureList.subList(0, 9);
                }
                CommonGridViewPicsAdapter adapter = new CommonGridViewPicsAdapter(context, pictureList, Utils.dip2px(115));
                viewHolder.gridview.setAdapter(adapter);
            } else {
                viewHolder.gridview.setVisibility(View.GONE);
            }

            if (StringUtil.notEmpty(bean.videoUrl)) {
                viewHolder.webview.setVisibility(View.VISIBLE);
                String url = bean.videoUrl;
                if (!bean.videoUrl.contains(NetURL.SPACE_VIDEO)) {
                    url = NetURL.SPACE_VIDEO + url;
                }
                viewHolder.webview.loadUrl(url);
            } else {
                viewHolder.webview.setVisibility(View.GONE);
            }

            viewHolder.tv_share.setText(String.valueOf(bean.shareCnt));
            viewHolder.tv_comment.setText(String.valueOf(bean.replyCnt));
            viewHolder.tv_support.setText(String.valueOf(bean.praiseCnt));

            viewHolder.tv_share.setClickable(false);
            viewHolder.tv_comment.setClickable(false);
            viewHolder.tv_support.setClickable(false);
            viewHolder.tv_share.setEnabled(false);
            viewHolder.tv_comment.setEnabled(false);
            viewHolder.tv_support.setEnabled(false);

            return convertView;
        }

        class ViewHolder {
            TextView tv_time_label;
            TextView tv_content;
            NoScrollGridView gridview;
            WebView webview;
            TextView tv_share;
            TextView tv_comment;
            TextView tv_support;
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (customView != null) {
                    hideCustomView();
                } else {
                    stopPlay();
                    finish();
                }
                return true;
            default:
                return super.onKeyUp(keyCode, event);
        }
    }

    private void stopPlay() {
        LogUtil.i(TAG, "Stop play webview video");
        for (int i = 0; i < list.size(); i++) {
            WebView webView = (WebView) listview.getChildAt(i).findViewById(R.id.webview);
            if (webView.isShown()) {
                LogUtil.i(TAG, "index = " + i);
                webView.reload();
            }
        }
    }
}
