package com.huicheng.hotel.android.ui.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.gson.Gson;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.NetURL;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.net.bean.ShenZhouConfigBean;
import com.huicheng.hotel.android.ui.JSBridge.RegisterHandler;
import com.huicheng.hotel.android.ui.JSBridge.WVJBWebViewClient;
import com.huicheng.hotel.android.ui.base.BaseFragment;
import com.huicheng.hotel.android.ui.custom.CommonLoadingWidget;
import com.prj.sdk.constants.BroadCastConst;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;
import com.prj.sdk.widget.CustomToast;
import com.prj.sdk.widget.webview.WebChromeClientCompat;

import java.util.HashMap;

/**
 * Fragment car
 */
public class TaxiPagerFragment extends BaseFragment {

    private static final String TAG = "TaxiPagerFragment";
    private static boolean isFirstLoad = false;

    private HashMap<String, String> params = new HashMap<>();
    private ShenZhouConfigBean bean = null;

    private WebView mWebView;
    private CommonLoadingWidget common_loading_widget;

    public static Fragment newInstance(String key) {
        isFirstLoad = true;
        Fragment fragment = new TaxiPagerFragment();
        Bundle bundle = new Bundle();
        bundle.putString("key", key);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        getArguments().getString("key");
        View view = inflater.inflate(R.layout.fragment_pager_car, container, false);
        initTypedArrayValue();
        initViews(view);
        initParams();
        initListeners();
        return view;
    }

    protected void onVisible() {
        super.onVisible();

        if (isFirstLoad) {
            isFirstLoad = false;
            String url = NetURL.SZ_TAXI_HOME;
            String mainColorStr = Integer.toHexString(getResources().getColor(mMainColor));
            if (SessionContext.isLogin()) {
                long timeStamp = System.currentTimeMillis();
                if (null == bean) {
                    bean = new ShenZhouConfigBean(mainColorStr.substring(2, mainColorStr.length()),
                            SessionContext.mUser.user.userid,
                            SessionContext.mUser.user.mobile,
                            String.valueOf(timeStamp),
                            "");
                }
                bean.setSignature(bean.getSignatureSha1(getActivity()));
                LogUtil.i(TAG,"signature = " + bean.getSignature());
                String jsonStr = new Gson().toJson(bean);
                params.put("key", getResources().getString(R.string.sz_appkey));
                params.put("mobile", SessionContext.mUser.user.mobile);
                params.put("q", jsonStr);

                if (StringUtil.notEmpty(url)) {
                    url += "?";
                    for (String key : params.keySet()) {
                        url = url + key + "=" + params.get(key) + "&";
                    }
                }

            } else {
                getActivity().sendBroadcast(new Intent(BroadCastConst.UNLOGIN_ACTION));
                return;
            }


            LogUtil.i(TAG,"url = " + url);
            mWebView.loadUrl(url);
        }
    }

    protected void onInvisible() {
        super.onInvisible();
        common_loading_widget.closeLoading();
    }

    @Override
    protected void initViews(View view) {
        super.initViews(view);
        mWebView = (WebView) view.findViewById(R.id.webview);
        common_loading_widget = (CommonLoadingWidget) view.findViewById(R.id.common_loading_widget);
    }

    @Override
    protected void initParams() {
        super.initParams();

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
            String pkName = getActivity().getPackageName();
            String versionName = getActivity().getPackageManager().getPackageInfo(pkName, 0).versionName;
            sb.append(webSetting.getUserAgentString()).append(" Android/").append("Hotel").append("/").append(versionName);
            webSetting.setUserAgentString(sb.toString());// 追加修改ua特征标识（名字+包名+版本号）使得web端正确判断
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initListeners() {
        super.initListeners();
        mWebView.setWebViewClient(new MyWebViewClient(mWebView));
        mWebView.setWebChromeClient(new WebChromeClientCompat(getActivity(), null, null));
    }



    class MyWebViewClient extends WVJBWebViewClient {

        public MyWebViewClient(WebView webView) {
            super(webView);
            new RegisterHandler(this, getActivity()).init();
        }


        @Override
        public void onPageStarted(final WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            common_loading_widget.startLoading();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            common_loading_widget.closeLoading();
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
                mWebView.loadDataWithBaseURL(null, "<style>* {font-size:40px;padding:10px;}</style>" + "哎呀，出错了，请检查网络并关闭重试！", "text/html", "utf-8", null);// 显示空白，屏蔽显示出错的网络地址url
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
}
