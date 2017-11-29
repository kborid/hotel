package com.huicheng.hotel.android.ui.JSBridge;

import android.content.Context;

import com.huicheng.hotel.android.ui.JSBridge.functions.airPlaneChanged;
import com.huicheng.hotel.android.ui.JSBridge.functions.colseAndReloadList;
import com.huicheng.hotel.android.ui.JSBridge.functions.getDeviceId;
import com.huicheng.hotel.android.ui.JSBridge.functions.getPhone;
import com.huicheng.hotel.android.ui.JSBridge.functions.getServiceHost;
import com.huicheng.hotel.android.ui.JSBridge.functions.getUserId;
import com.huicheng.hotel.android.ui.JSBridge.functions.getUserSexy;
import com.huicheng.hotel.android.ui.JSBridge.functions.getUserTicket;
import com.huicheng.hotel.android.ui.JSBridge.functions.handleError;
import com.huicheng.hotel.android.ui.JSBridge.functions.openNativeOrderList;
import com.huicheng.hotel.android.ui.JSBridge.functions.openURL;
import com.huicheng.hotel.android.ui.JSBridge.functions.payPlaneTicket;
import com.huicheng.hotel.android.ui.JSBridge.functions.reBookPlane;
import com.huicheng.hotel.android.ui.JSBridge.functions.shareURL;
import com.huicheng.hotel.android.ui.JSBridge.functions.showException;

/**
 * 注册处理程序，使JavaScript可以调用
 *
 * @author LiaoBo
 */
public class RegisterHandler {

    private WVJBWebViewClient mWVJBWebViewClient;
    private Context mContext;

    /**
     * 构造函数
     *
     * @param mContext
     */
    public RegisterHandler(WVJBWebViewClient mWVJBWebViewClient, Context mContext) {
        this.mWVJBWebViewClient = mWVJBWebViewClient;
        this.mContext = mContext;
    }

    /**
     * 初始化，注册处理程序
     */
    public void init() {
        // mWVJBWebViewClient.registerHandler("showLoginModule", new showLoginModule());
        // mWVJBWebViewClient.registerHandler("loadRequest", new loadRequest());
        mWVJBWebViewClient.registerHandler("shareURL", new shareURL(mContext));
        mWVJBWebViewClient.registerHandler("openURL", new openURL(mContext));
        mWVJBWebViewClient.registerHandler("getUserTicket", new getUserTicket());
        mWVJBWebViewClient.registerHandler("getUserId", new getUserId());
        mWVJBWebViewClient.registerHandler("getUserSexy", new getUserSexy());
        mWVJBWebViewClient.registerHandler("getPhone", new getPhone());
        mWVJBWebViewClient.registerHandler("getDeviceId", new getDeviceId());
//        mWVJBWebViewClient.registerHandler("getCityInfo", new getCityInfo());
        mWVJBWebViewClient.registerHandler("showException", new showException(mContext));
        mWVJBWebViewClient.registerHandler("handleError", new handleError(mContext));

        mWVJBWebViewClient.registerHandler("payPlaneTicket", new payPlaneTicket(mContext));
        mWVJBWebViewClient.registerHandler("reBookPlane", new reBookPlane(mContext));
        mWVJBWebViewClient.registerHandler("getServiceHost", new getServiceHost());
        mWVJBWebViewClient.registerHandler("openNativeOrderList", new openNativeOrderList(mContext));
        mWVJBWebViewClient.registerHandler("colseAndReloadList", new colseAndReloadList(mContext));
        mWVJBWebViewClient.registerHandler("airPlaneChanged", new airPlaneChanged(mContext));
    }
}
