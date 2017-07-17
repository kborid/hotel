package com.huicheng.hotel.android.ui.JSBridge.functions;

import android.content.Intent;

import com.huicheng.hotel.android.ui.JSBridge.WVJBWebViewClient;
import com.prj.sdk.app.AppContext;
import com.prj.sdk.constants.BroadCastConst;

/**
 * 打开登录界面
 *
 * @author LiaoBo
 */
public class showLoginModule implements WVJBWebViewClient.WVJBHandler {
    @Override
    public void request(Object data, WVJBWebViewClient.WVJBResponseCallback callback) {
        AppContext.mAppContext.sendBroadcast(new Intent(BroadCastConst.UNLOGIN_ACTION));
    }

}
