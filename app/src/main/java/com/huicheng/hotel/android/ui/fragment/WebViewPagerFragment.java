package com.huicheng.hotel.android.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.RelativeLayout;

import com.google.gson.Gson;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.requestbuilder.bean.ShenZhouConfigBean;
import com.huicheng.hotel.android.ui.base.BaseFragment;
import com.huicheng.hotel.android.ui.custom.MyCommWebViewClient;
import com.huicheng.hotel.android.content.AppConst;
import com.huicheng.hotel.android.content.NetURL;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;
import com.prj.sdk.webview.WebChromeClientCompat;

import java.util.HashMap;

/**
 * Fragment webView
 */
public class WebViewPagerFragment extends BaseFragment {
    public static final String TAB_PLANE = "tab_plane";
    public static final String TAB_TRAIN = "tab_train";
    public static final String TAB_TAXI = "tab_taxi";

    private static final String URL_BLANK = "about:blank";
    private boolean isFirstLoad = false;

    private boolean mIsReLoad = false;
    private String keyIndex;
    private WebView mWebView;
    private MyCommWebViewClient webViewClient;
    private RelativeLayout error_lay;
    private String mURL;

    public static Fragment newInstance(String key) {
        Fragment fragment = new WebViewPagerFragment();
        Bundle bundle = new Bundle();
        bundle.putString("key", key);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        keyIndex = getArguments().getString("key");
        isFirstLoad = true;
        View view = inflater.inflate(R.layout.fragment_pager_webview, container, false);
        initTypedArrayValue();
        initViews(view);
        initWebViewSettings();
        initParams();
        initListeners();
        return view;
    }

    protected void onVisible() {
        super.onVisible();
        LogUtil.d(TAG, ":onVisible()");
        dealUrlParams();
        if (isFirstLoad || mIsReLoad) {
            isFirstLoad = false;
            LogUtil.i(TAG, "mURL = " + mURL);
            mWebView.loadUrl(mURL);
        }
    }

    protected void onInvisible() {
        super.onInvisible();
        LogUtil.d(TAG, ":onInvisible()");
    }

    @Override
    protected void initViews(View view) {
        super.initViews(view);
        mWebView = (WebView) view.findViewById(R.id.webview);
        error_lay = (RelativeLayout) view.findViewById(R.id.error_lay);
        error_lay.setVisibility(View.GONE);
    }

    private void initWebViewSettings() {
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
        webViewClient = new MyCommWebViewClient(TAG, mWebView, getActivity());
        mWebView.setWebViewClient(webViewClient);
        mWebView.setWebChromeClient(new WebChromeClientCompat(getActivity(), null, null));
    }

    private void dealUrlParams() {
        switch (keyIndex) {
            case TAB_PLANE:
                mURL = NetURL.PLANE_HOME;

//                mIsReLoad = HotelMainActivity.getIsNeedReload();
//                String orderId = HotelMainActivity.getPlaneOrderId();
//                if (StringUtil.notEmpty(orderId)) {
//                    mURL = mURL + "?yiorderid=" + orderId;
//                }
                break;
            case TAB_TRAIN:
                mURL = NetURL.TRAIN_HOME;

                break;
            case TAB_TAXI:
                mURL = AppConst.ISDEVELOP ? NetURL.SZ_TAXI_HOME_DEBUG : NetURL.SZ_TAXI_HOME_RELEASE;

                String mainColorStr = Integer.toHexString(getResources().getColor(mMainColor));
                long timeStamp = System.currentTimeMillis();
                ShenZhouConfigBean bean = new ShenZhouConfigBean(mainColorStr.substring(2, mainColorStr.length()),
                        SessionContext.mUser.user.userid,
                        SessionContext.mUser.user.mobile,
                        String.valueOf(timeStamp),
                        "");
                bean.setSignature(bean.getSignatureSha1(getActivity()));
                LogUtil.i(TAG, "signature = " + bean.getSignature());
                String appKey = AppConst.ISDEVELOP ? getResources().getString(R.string.sz_appkey_debug) :
                        getResources().getString(R.string.sz_appkey_release);
                HashMap<String, String> params = new HashMap<>();
                params.put("key", appKey);
                params.put("mobile", SessionContext.mUser.user.mobile);
                params.put("q", new Gson().toJson(bean));

                if (StringUtil.notEmpty(mURL)) {
                    mURL = SessionContext.getUrl(mURL, params);
                }
                break;
        }
    }

    @Override
    protected void initParams() {
        super.initParams();
    }

    @Override
    public void initListeners() {
        super.initListeners();
        webViewClient.setLoadingResultListener(new MyCommWebViewClient.LoadingResultListener() {
            @Override
            public void onResult(int ret) {
                if (MyCommWebViewClient.RESULT_ERR == ret) {
                    mWebView.loadUrl(URL_BLANK);
                    mWebView.setVisibility(View.GONE);
                    error_lay.setVisibility(View.VISIBLE);
                }
            }
        });
        error_lay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dealUrlParams();
                mWebView.loadUrl(mURL);
                mWebView.setVisibility(View.VISIBLE);
                error_lay.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.i(TAG, "onActivityResult() " + requestCode + ", " + resultCode);
    }
}
