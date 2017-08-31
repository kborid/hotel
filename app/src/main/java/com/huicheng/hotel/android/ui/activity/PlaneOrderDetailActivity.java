package com.huicheng.hotel.android.ui.activity;

import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.huicheng.hotel.android.BuildConfig;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.huicheng.hotel.android.ui.custom.CommonLoadingWidget;
import com.huicheng.hotel.android.ui.custom.MyCommWebViewClient;
import com.prj.sdk.constants.BroadCastConst;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.Utils;

/**
 * @author kborid
 * @date 2017/3/10 0010
 */
public class PlaneOrderDetailActivity extends BaseActivity {

    private static final String TAG = "PlaneOrderDetailActivity";

    private WebView mWebView;
    private CommonLoadingWidget common_loading_widget;
    private String url;
    private int mMainColorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_plane_orderdetail_layout);
        TypedArray ta = obtainStyledAttributes(R.styleable.MyTheme);
        mMainColorId = ta.getInt(R.styleable.MyTheme_mainColor, R.color.mainColor);
        ta.recycle();

        initViews();
        initParams();
        initListeners();
    }

    @Override
    public void initViews() {
        super.initViews();
        mWebView = (WebView) findViewById(R.id.webview);
        common_loading_widget = (CommonLoadingWidget) findViewById(R.id.common_loading_widget);
    }

    @Override
    public void dealIntent() {
        super.dealIntent();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.getString("path") != null) {
                url = bundle.getString("path");
            }
        }
    }

    @Override
    public void initParams() {
        super.initParams();
        btn_back.setImageResource(R.drawable.iv_back_white);

        WebSettings webSetting = mWebView.getSettings();
        webSetting.setJavaScriptEnabled(true);
        webSetting.setJavaScriptCanOpenWindowsAutomatically(true);
        webSetting.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSetting.setSupportZoom(false);
        webSetting.setUseWideViewPort(true);// 将图片调整到适合webview的大小
        webSetting.setLoadWithOverviewMode(true);// 充满全屏。
        mWebView.setHorizontalScrollBarEnabled(false);// 水平不显示
        mWebView.setVerticalScrollBarEnabled(false); // 垂直不显示
        webSetting.setAllowFileAccess(true);// 允许访问文件数据
        webSetting.setDomStorageEnabled(true);// 开启Dom存储Api(启用地图、定位之类的都需要)
        webSetting.setRenderPriority(WebSettings.RenderPriority.HIGH);// 提高渲染的优先级
        // 应用可以有缓存
        webSetting.setAppCacheEnabled(true);// 开启 Application Caches 功能
        webSetting.setAppCachePath(Utils.getFolderDir("webCache"));
        webSetting.setGeolocationEnabled(true); // 启用地理定位
        webSetting.setDefaultTextEncodingName("utf-8");
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(webSetting.getUserAgentString()).append(" Android/").append("Hotel").append("/").append(BuildConfig.VERSION_NAME);// 名字+wuhou+版本号
            webSetting.setUserAgentString(sb.toString());// 追加修改ua特征标识（名字+包名+版本号）使得web端正确判断
        } catch (Exception e) {
            e.printStackTrace();
        }

        mWebView.setBackgroundColor(mMainColorId); // 设置背景色
        mWebView.getBackground().setAlpha(0); // 设置填充透明度 范围：0-255

        mWebView.loadUrl(url);
    }

    @Override
    public void initListeners() {
        super.initListeners();
        mWebView.setWebViewClient(new MyCommWebViewClient(TAG, mWebView, this));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        common_loading_widget.closeLoading();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        if (data.hasExtra("pay_result")) {
            Intent intent = new Intent(BroadCastConst.ACTION_PAY_STATUS);
            intent.putExtra("info", data.getExtras().getString("pay_result"));
            LogUtil.i(TAG, "pay_result = " + data.getExtras().getString("pay_result"));
            intent.putExtra("type", "unionPay");
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }
}
