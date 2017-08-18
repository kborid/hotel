package com.huicheng.hotel.android.ui.custom;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.huicheng.hotel.android.ui.JSBridge.RegisterHandler;
import com.huicheng.hotel.android.ui.JSBridge.WVJBWebViewClient;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.widget.CustomToast;

/**
 * @auth kborid
 * @date 2017/8/16.
 */

public class MyCommWebViewClient extends WVJBWebViewClient {
    public static final int RESULT_ERR = -1;
    public static final int RESULT_SUC = 0;

    private LoadingResultListener listener = null;
    private String TAG;

    public MyCommWebViewClient(String TAG, WebView webView, Context context) {
        super(webView);
        this.TAG = TAG;
        new RegisterHandler(this, context).init();
    }

    public void setLoadingResultListener(LoadingResultListener listener) {
        this.listener = listener;
    }


    @Override
    public void onPageStarted(final WebView webview, String url, Bitmap favicon) {
        super.onPageStarted(webview, url, favicon);
        LogUtil.i(TAG, "onPageStarted()");
    }

    @Override
    public void onPageFinished(WebView webview, String url) {
        super.onPageFinished(webview, url);
        LogUtil.i(TAG, "onPageFinished()");
    }

    public void onReceivedError(WebView webview, int errorCode, String description, String failingUrl) {
        super.onReceivedError(webview, errorCode, description, failingUrl);
        LogUtil.i(TAG, "onReceivedError()");
        webview.stopLoading();
        LogUtil.i(TAG, "errorCode = " + errorCode);
        if (errorCode == WebViewClient.ERROR_CONNECT || errorCode == WebViewClient.ERROR_TIMEOUT || errorCode == WebViewClient.ERROR_HOST_LOOKUP) {
            if (null != listener) {
                listener.onResult(RESULT_ERR);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onReceivedError(WebView webview, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(webview, request, error);
        LogUtil.i(TAG, "onReceivedError()");
        webview.stopLoading();
        int errorCode = error.getErrorCode();
        LogUtil.i(TAG, "errorCode = " + errorCode);
        if (errorCode == WebViewClient.ERROR_CONNECT || errorCode == WebViewClient.ERROR_TIMEOUT || errorCode == WebViewClient.ERROR_HOST_LOOKUP) {
            if (null != listener) {
                listener.onResult(RESULT_ERR);
            }
        }
    }

    @Override
    public void onReceivedSslError(WebView webview, SslErrorHandler handler, SslError error) {
//        super.onReceivedSslError(webview, handler, error);
        LogUtil.i(TAG, "onReceivedSslError()");
        CustomToast.show("已忽略证书信息继续加载", 0);
        handler.proceed();// 忽略证书信息继续加载
    }

//    @TargetApi(android.os.Build.VERSION_CODES.M)
//    @Override
//    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
//        super.onReceivedHttpError(view, request, errorResponse);
//        LogUtil.i(TAG, "onReceivedHttpError()");
//        // 这个方法在6.0才出现
//        int statusCode = errorResponse.getStatusCode();
//        LogUtil.i(TAG, "onReceivedHttpError code = " + statusCode);
//        if (404 == statusCode || 500 == statusCode) {
//            if (null != listener) {
//                listener.onResult(RESULT_ERR);
//            }
//        }
//    }

    public interface LoadingResultListener {
        void onResult(int ret);
    }
}