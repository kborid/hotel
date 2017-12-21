package com.huicheng.hotel.android.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.ui.JSBridge.RegisterHandler;
import com.huicheng.hotel.android.ui.JSBridge.WVJBWebViewClient;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.huicheng.hotel.android.ui.custom.CommonLoadingWidget;
import com.huicheng.hotel.android.ui.dialog.CustomToast;
import com.prj.sdk.constants.BroadCastConst;
import com.prj.sdk.util.Utils;
import com.prj.sdk.widget.webview.ChooserFileController;
import com.prj.sdk.widget.webview.WebChromeClientCompat;

/**
 * 中间件处理，加载html5应用数据
 */
@SuppressLint("SetJavaScriptEnabled")
public class HtmlActivity extends BaseActivity {

    private static final String CSS_STYLE = "<style>* {font-size:40px;padding:10px;}</style>";
    private WebView mWebView;
    private CommonLoadingWidget common_loading_widget;
    private String mID, URL, /*mTitle,*/
            loginUrl;
    private RelativeLayout title_lay;
    private ImageView iv_back, iv_close;
    private ChooserFileController mCtrl;

    private boolean isJSTest = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initMainWindow();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_html_act);
        initViews();
        initParams();
        initListeners();
        if (AppConst.ISDEVELOP) {
            if (isJSTest) {
                mWebView.loadUrl("file:///android_asset/ExampleApp.html");
                RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                rlp.setMargins(0, Utils.mStatusBarHeight + Utils.dp2px(50), 0, 0);
                mWebView.setLayoutParams(rlp);
            }
        }
    }

    public void initViews() {
        super.initViews();
        mWebView = (WebView) findViewById(R.id.webview);
        common_loading_widget = (CommonLoadingWidget) findViewById(R.id.common_loading_widget);
        title_lay = (RelativeLayout) findViewById(R.id.title_lay);
        iv_back = (ImageView) findViewById(R.id.iv_back);
        iv_close = (ImageView) findViewById(R.id.iv_close);
    }

    @Override
    public void dealIntent() {
        super.dealIntent();
        Bundle bundle = getIntent().getExtras();
        if (null != bundle) {
            isJSTest = bundle.getBoolean("ISJS");
            if (bundle.getString("path") != null) {
                URL = bundle.getString("path");
                if (URL != null && !URL.startsWith("http")) {
                    URL = "http://" + URL;
                }
            }
//            if (bundle.getString("title") != null) {
//                mTitle = bundle.getString("title");
//            }
//            if (bundle.getString("id") != null) {
//                mID = bundle.getString("id");
//            }
        }
    }

    public void initParams() {
        super.initParams();
//        tv_center_title.setText(mTitle);

        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.dp2px(50));
        rlp.setMargins(0, Utils.mStatusBarHeight, 0, 0);
        title_lay.setLayoutParams(rlp);

        mCtrl = new ChooserFileController(this);

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
        // 应用可以有数据库
        webSetting.setDatabaseEnabled(true);// 启用数据库
        webSetting.setDatabasePath(Utils.getFolderDir("webDatabase"));
        webSetting.setGeolocationEnabled(true); // 启用地理定位
        webSetting.setDefaultTextEncodingName("utf-8");
        try {
            StringBuilder sb = new StringBuilder();
            String pkName = this.getPackageName();
            String versionName = this.getPackageManager().getPackageInfo(pkName, 0).versionName;
            sb.append(webSetting.getUserAgentString()).append(" Android/").append("Hotel").append("/").append(versionName); //Android/Hotel/versionName
            webSetting.setUserAgentString(sb.toString());// 追加修改ua特征标识（名字+包名+版本号）使得web端正确判断
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT >= 19) { // 控制图片加载处理，提高view加载速度
            webSetting.setLoadsImagesAutomatically(true);
        } else {
            webSetting.setLoadsImagesAutomatically(false);
        }
        mWebView.loadUrl(URL);
    }

    public void initListeners() {
        super.initListeners();
        mWebView.setWebViewClient(new MyWebViewClient(mWebView));
        mWebView.setWebChromeClient(new WebChromeClientCompat(this, mCtrl, tv_center_title));
        iv_back.setOnClickListener(this);
        iv_close.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                if (mWebView.canGoBack()) {
                    mWebView.goBack();
                } else {
                    goBack();
                }
                break;
            case R.id.iv_close:
                goBack();
                break;

            default:
                break;
        }
    }

    /**
     * 处理返回，如果是广告，就跳首页
     */
    public void goBack() {
        if (getIntent().getExtras() != null && getIntent().getExtras().getString("goBack") != null) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else {
            this.finish();
        }
    }

    private class MyWebViewClient extends WVJBWebViewClient {

        MyWebViewClient(WebView webView) {
//             support js send
//            super(webView, new WVJBWebViewClient.WVJBHandler() {
//                @Override
//                public void request(Object data, WVJBResponseCallback callback) {
//                    CustomToast.show("Java Received message from JS:" + data, CustomToast.LENGTH_SHORT);
//                    callback.callback("Response for message from Java");
//                }
//            });
            super(webView);
            new RegisterHandler(this, HtmlActivity.this).init();
        }


        @Override
        public void onPageStarted(final WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            common_loading_widget.startLoading();
            try {
                // 拦截H5应用跳转网页版登录，使用app登录页面登录，登录成功加载loginUrl的链接
                if (url != null && (url.contains("rtnCode=900902") || url.contains("rtnCode=310001"))) {
                    if (loginUrl != null) {
                        goBack();
                    } else {
                        loginUrl = Uri.parse(url).getQueryParameter("loginUrl");
                        sendBroadcast(new Intent(BroadCastConst.UNLOGIN_ACTION));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
//            if (StringUtil.notEmpty(mTitle)) {
//                tv_center_title.setText(mTitle);
//            }
            mWebView.setEnabled(false);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

//            if (view.canGoBack()) {
//                iv_close.setVisibility(View.VISIBLE);
//            } else {
//                iv_close.setVisibility(View.GONE);
//            }
//            String title = view.getTitle();
//            if (StringUtil.notEmpty(title)
//                    && !title.startsWith(NetURL.getApi().replace("http://", ""))
//                    && !title.contains(".html")
//                    && !title.contains(".htm")
//                    && !title.contains(".jsp")
//                    && !title.contains(".aspx")
//                    && !title.contains(".do")
//                    && !AppConst.ISDEVELOP) {
//                mTitle = title;
//                tv_center_title.setText(mTitle);// 点击后退，设置标题
//            }

            common_loading_widget.closeLoading();
            if (!view.getSettings().getLoadsImagesAutomatically()) {
                view.getSettings().setLoadsImagesAutomatically(true);
            }
            mWebView.setEnabled(true);
        }

        public void onReceivedError(WebView webview, int errorCode, String description, String failingUrl) {
            // super.onReceivedError(webview, errorCode, description, failingUrl);
            try {
                webview.stopLoading();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (mWebView.canGoBack()) {
                mWebView.goBack();
            }
            if (errorCode == WebViewClient.ERROR_CONNECT || errorCode == WebViewClient.ERROR_TIMEOUT || errorCode == WebViewClient.ERROR_HOST_LOOKUP) {
                mWebView.loadDataWithBaseURL(null, CSS_STYLE + "哎呀，出错了，请检查网络并关闭重试！", "text/html", "utf-8", null);// 显示空白，屏蔽显示出错的网络地址url
            }
        }

        // 当load有ssl层的https页面时，如果这个网站的安全证书在Android无法得到认证，WebView就会变成一个空白页，而并不会像PC浏览器中那样跳出一个风险提示框
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            // 忽略证书的错误继续Load页面内容
            CustomToast.show("已忽略证书信息继续加载", 0);
            handler.proceed();// 忽略证书信息继续加载
            // handler.cancel(); // Android默认的处理方式
            // handleMessage(Message msg); // 进行其他处理
            // super.onReceivedSslError(view, handler, error);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void onDestroy() {
        super.onDestroy();
        mWebView.stopLoading();
        destroyView();
        if (common_loading_widget.isShown()) {
            common_loading_widget.closeLoading();
        }
    }

    /**
     * 销毁视图
     */
    public void destroyView() {
        mWebView.stopLoading();
        mWebView.removeAllViews();
        mWebView.clearAnimation();
        mWebView.clearFormData();
        mWebView.clearHistory();
        mWebView.clearMatches();
        mWebView.clearSslPreferences();
        mWebView.destroy();
        mCtrl.onDestroy();
    }

    /**
     * 增加JS调用接口 让html页面调用
     */
    // @SuppressLint("JavascriptInterface")
    // public void addJSInterfaces() {
    // mWebView.addJavascriptInterface(new HostJsDeal(mWebView, this), "DCJSBridge");
    // }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCtrl.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mWebView.canGoBack()) {
                mWebView.goBack();
            } else {
                goBack();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
