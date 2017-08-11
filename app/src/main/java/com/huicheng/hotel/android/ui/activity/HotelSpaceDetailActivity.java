package com.huicheng.hotel.android.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.HotelCommDef;
import com.huicheng.hotel.android.common.NetURL;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.control.ShareControl;
import com.huicheng.hotel.android.net.RequestBeanBuilder;
import com.huicheng.hotel.android.net.bean.HotelSpaceTieCommentInfoBean;
import com.huicheng.hotel.android.net.bean.HotelSpaceTieInfoBean;
import com.huicheng.hotel.android.ui.adapter.CommonGridViewPicsAdapter;
import com.huicheng.hotel.android.ui.adapter.MySpaceCommentAdapter;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.huicheng.hotel.android.ui.custom.FullscreenHolder;
import com.huicheng.hotel.android.ui.custom.NoScrollGridView;
import com.huicheng.hotel.android.ui.custom.SimpleRefreshListView;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;
import com.prj.sdk.widget.CustomToast;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * @author kborid
 * @date 2017/3/21 0021
 */
public class HotelSpaceDetailActivity extends BaseActivity implements DataCallback {

    private static final String TAG = "HotelSpaceDetailActivity";
    private static final int PAGESIZE = 10;
    private WebView webview = null;
    protected static final FrameLayout.LayoutParams COVER_SCREEN_PARAMS = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    private View customView;
    private FrameLayout fullscreenContainer;
    private WebChromeClient.CustomViewCallback myCallBack = null;

    private View mHeaderView;
    private List<String> pictureList = new ArrayList<>();
    private NoScrollGridView gridview;
    private TextView tv_date, tv_comment_count;
    private TextView tv_content;
    private TextView tv_share, tv_comment, tv_support;

    private SimpleRefreshListView replyListView;
    private List<HotelSpaceTieCommentInfoBean> replyList = new ArrayList<>();
    private MySpaceCommentAdapter replyAdapter = null;

    private HotelSpaceTieInfoBean hotelSpaceTieInfoBean = null;
    private int hotelId;
    private int articleId;
    private int pageIndex = 0;
    private boolean isLoadMore = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_spacedetail_layout);
        initViews();
        initParams();
        initListeners();
        replyListView.refreshingHeaderView();
    }

    @Override
    public void initViews() {
        super.initViews();

        //headerView放帖子详细内容
        mHeaderView = LayoutInflater.from(this).inflate(R.layout.hotel_tiedetail_header_item, null);
        tv_date = (TextView) mHeaderView.findViewById(R.id.tv_date);
        tv_comment_count = (TextView) mHeaderView.findViewById(R.id.tv_comment_count);
        tv_content = (TextView) mHeaderView.findViewById(R.id.tv_content);
        gridview = (NoScrollGridView) mHeaderView.findViewById(R.id.gridview);
        webview = (WebView) mHeaderView.findViewById(R.id.webview);
        if (webview != null) {
            WebSettings webSettings = webview.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setUseWideViewPort(true); // 关键点
            webSettings.setAllowFileAccess(true); // 允许访问文件
            webSettings.setSupportZoom(true); // 支持缩放
            webSettings.setLoadWithOverviewMode(true);
            webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
            webview.setWebChromeClient(new WebChromeClient() {
                /*** 视频播放相关的方法 **/
                @Override
                public View getVideoLoadingProgressView() {
                    return LayoutInflater
                            .from(HotelSpaceDetailActivity.this).inflate(
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
            webview.setBackgroundColor(getResources().getColor(R.color.black)); // 设置背景色
            webview.getBackground().setAlpha(0); // 设置填充透明度 范围：0-255
        }
        //帖子操作
        tv_share = (TextView) mHeaderView.findViewById(R.id.tv_space_share);
        tv_comment = (TextView) mHeaderView.findViewById(R.id.tv_space_comment);
        tv_support = (TextView) mHeaderView.findViewById(R.id.tv_space_support);

        //帖子回复列表
        replyListView = (SimpleRefreshListView) findViewById(R.id.listview);
        replyAdapter = new MySpaceCommentAdapter(this, replyList);
        replyListView.setAdapter(replyAdapter);
        replyListView.addHeaderView(mHeaderView);
        replyListView.setPullRefreshEnable(true);
        replyListView.setPullLoadEnable(true);
    }

    @Override
    public void dealIntent() {
        super.dealIntent();
        Bundle bundle = getIntent().getExtras();
        if (null != bundle) {
            hotelId = bundle.getInt("hotelId");
            articleId = bundle.getInt("articleId");
        }
    }

    @Override
    public void initParams() {
        super.initParams();
        tv_center_title.setText("帖子详情");
        btn_right.setImageResource(R.drawable.iv_favorite_gray);
        btn_right.setVisibility(View.VISIBLE);
        if (!SessionContext.isLogin()) {
            SessionContext.initUserInfo();
        }
    }

    private void refreshTieDetailInfo() {
        if (hotelSpaceTieInfoBean != null) {
            tv_date.setText(DateUtil.getDay("MM月dd日 HH:mm", hotelSpaceTieInfoBean.createTimeStamp));
            tv_comment_count.setText("评论：" + hotelSpaceTieInfoBean.replyCnt);
            if (StringUtil.notEmpty(hotelSpaceTieInfoBean.content)) {
                tv_content.setVisibility(View.VISIBLE);
                tv_content.setText(Html.fromHtml(hotelSpaceTieInfoBean.content));
            } else {
                tv_content.setVisibility(View.GONE);
            }

            if (StringUtil.notEmpty(hotelSpaceTieInfoBean.picUrl)) {
                String[] picUrls = hotelSpaceTieInfoBean.picUrl.split(",");
                pictureList = Arrays.asList(picUrls);
                if (pictureList.size() != 0) {
                    gridview.setVisibility(View.VISIBLE);
                    gridview.setPressed(false);
                    int size = pictureList.size();
                    if (size > 9) {
                        pictureList = pictureList.subList(0, 9);
                    }
                    CommonGridViewPicsAdapter adapter = new CommonGridViewPicsAdapter(this, pictureList, Utils.dip2px(55));
                    gridview.setAdapter(adapter);
                } else {
                    gridview.setVisibility(View.GONE);
                }
            } else {
                gridview.setVisibility(View.GONE);
            }

            if (StringUtil.notEmpty(hotelSpaceTieInfoBean.videoUrl)) {
                webview.setVisibility(View.VISIBLE);
                String url = hotelSpaceTieInfoBean.videoUrl;
                if (!hotelSpaceTieInfoBean.videoUrl.contains(NetURL.SPACE_VIDEO)) {
                    url = NetURL.SPACE_VIDEO + url;
                }
                webview.loadUrl(url);
            } else {
                webview.setVisibility(View.GONE);
            }

            tv_share.setText(String.valueOf(hotelSpaceTieInfoBean.shareCnt));
            tv_comment.setText(String.valueOf(hotelSpaceTieInfoBean.replyCnt));
            tv_support.setText(String.valueOf(hotelSpaceTieInfoBean.praiseCnt));
        }
    }

    @Override
    public void initListeners() {
        super.initListeners();
        btn_right.setOnClickListener(this);
        tv_share.setOnClickListener(this);
        tv_comment.setOnClickListener(this);
        tv_support.setOnClickListener(this);
        webview.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                LogUtil.i(TAG, "url = " + url);
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LogUtil.i(TAG, "position = " + position);
                Intent intent = new Intent(HotelSpaceDetailActivity.this, ImageScaleActivity.class);
                intent.putExtra("url", pictureList.get(position));
                ImageView imageView = (ImageView) gridview.getChildAt(position).findViewById(R.id.imageView);
                int[] location = new int[2];
                imageView.getLocationOnScreen(location);
                intent.putExtra("locationX", location[0]);//必须
                intent.putExtra("locationY", location[1]);//必须
                intent.putExtra("width", imageView.getWidth());//必须
                intent.putExtra("height", imageView.getHeight());//必须
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        replyListView.setXListViewListener(new SimpleRefreshListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pageIndex = 0;
                isLoadMore = false;
                requestTieDetail(articleId);
            }

            @Override
            public void onLoadMore() {
                isLoadMore = true;
                requestTieCommentInfo(++pageIndex);
            }
        });
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.btn_right:
                requestHotelVip(hotelId);
                break;
            case R.id.tv_space_share:
                LogUtil.i(TAG, "share button onclick!!!");
                HashMap<String, String> params = new HashMap<>();
                params.put("type", "HotelZoneBlog");
                params.put("hotelID", String.valueOf(hotelId));
                params.put("blogID", String.valueOf(articleId));
                params.put("userID", SessionContext.mUser.user.userid);
                params.put("channel", HotelCommDef.SHARE_TIE);
                String url = SessionContext.getUrl(NetURL.SHARE, params);

                UMWeb web = new UMWeb(url);
                web.setTitle(hotelSpaceTieInfoBean.hotelName + "的空间");
                web.setThumb(new UMImage(this, BitmapFactory.decodeResource(getResources(), R.drawable.logo)));
                web.setDescription(hotelSpaceTieInfoBean.content);
                ShareControl.getInstance(this).openShareDisplay(web, new ShareResultListener() {
                    @Override
                    public void onShareResult(boolean isSuccess) {
                        if (isSuccess) {
                            requestTieShare();
                        }
                    }
                });

                break;
            case R.id.tv_space_comment:
                LogUtil.i(TAG, "comment button onclick!!!");
                Intent intent = new Intent(this, HotelSpacePublishActivity.class);
                intent.putExtra("hotelId", hotelId);
                intent.putExtra("tieDetail", hotelSpaceTieInfoBean);
                startActivity(intent);
                break;
            case R.id.tv_space_support:
                LogUtil.i(TAG, "support button onclick!!!");
                requestTieSupport();
                break;
            default:
                break;
        }
    }

    private void requestTieSupport() {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("replyId", "-1");
        b.addBody("hotelId", String.valueOf(hotelId));
        b.addBody("articleId", String.valueOf(articleId));

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.HOTEL_TIE_SUPPORT;
        d.flag = AppConst.HOTEL_TIE_SUPPORT;

        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void requestTieShare() {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("articleId", String.valueOf(articleId));

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.HOTEL_TIE_SHARE;
        d.flag = AppConst.HOTEL_TIE_SHARE;

        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void requestTieDetail(int articleId) {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("articleId", String.valueOf(articleId));

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.HOTEL_TIE_DETAIL;
        d.flag = AppConst.HOTEL_TIE_DETAIL;

        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void requestTieCommentInfo(int pageIndex) {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("hotelId", String.valueOf(hotelId));
        b.addBody("articleId", String.valueOf(hotelSpaceTieInfoBean.id));
        b.addBody("pageIndex", String.valueOf(pageIndex));
        b.addBody("pageSize", String.valueOf(PAGESIZE));

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.HOTEL_TIE_COMMENT;
        d.flag = AppConst.HOTEL_TIE_COMMENT;

        requestID = DataLoader.getInstance().loadData(this, d);
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
        fullscreenContainer = new FullscreenHolder(HotelSpaceDetailActivity.this);
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
        webview.setVisibility(View.VISIBLE);
    }

    private void requestHotelVip(int hotelId) {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("hotelId", String.valueOf(hotelId));

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.HOTEL_VIP;
        d.flag = AppConst.HOTEL_VIP;

        if (!isProgressShowing()) {
            showProgressDialog(this);
        }

        requestID = DataLoader.getInstance().loadData(this, d);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            webview.getClass().getMethod("onResume").invoke(webview, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            webview.getClass().getMethod("onPause").invoke(webview, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            if (request.flag == AppConst.HOTEL_TIE_COMMENT) {
                removeProgressDialog();
                LogUtil.i(TAG, "json = " + response.body.toString());
                List<HotelSpaceTieCommentInfoBean> temp = JSON.parseArray(response.body.toString(), HotelSpaceTieCommentInfoBean.class);
                if (temp != null) {
                    if (!isLoadMore) {
                        replyList.clear();
                        replyListView.setRefreshTime(DateUtil.getSecond(Calendar.getInstance().getTimeInMillis()));
                    } else {
                        isLoadMore = false;
                        if (temp.size() == 0) {
                            pageIndex--;
                            CustomToast.show("没有更多数据", CustomToast.LENGTH_SHORT);
                        }
                    }
                    replyListView.stopLoadMore();
                    replyListView.stopRefresh();
                    replyList.addAll(temp);
                    replyAdapter.notifyDataSetChanged();
                }
            } else if (request.flag == AppConst.HOTEL_TIE_DETAIL) {
                LogUtil.i(TAG, "json = " + response.body.toString());
                hotelSpaceTieInfoBean = JSON.parseObject(response.body.toString(), HotelSpaceTieInfoBean.class);
                refreshTieDetailInfo();
                requestTieCommentInfo(pageIndex);
            } else if (request.flag == AppConst.HOTEL_VIP) {
                removeProgressDialog();
                LogUtil.i(TAG, "Json = " + response.body.toString());
                CustomToast.show("您已成为该酒店会员", CustomToast.LENGTH_SHORT);
            } else if (request.flag == AppConst.HOTEL_TIE_SUPPORT) {
                LogUtil.i(TAG, "support json = " + response.body.toString());
                tv_support.setText(String.valueOf(hotelSpaceTieInfoBean.praiseCnt + 1));
            } else if (request.flag == AppConst.HOTEL_TIE_SHARE) {
                requestTieDetail(articleId);
            }
        }
    }

    @Override
    protected void onNotifyError(ResponseData request) {
        super.onNotifyError(request);
        replyListView.stopRefresh();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                /** 回退键 事件处理 优先级:视频播放全屏-网页回退-关闭页面 */
                if (customView != null) {
                    hideCustomView();
                } else {
                    webview.reload();
                    finish();
                }
                return true;
            default:
                return super.onKeyUp(keyCode, event);
        }
    }

    public interface ShareResultListener {
        void onShareResult(boolean isSuccess);
    }
}
