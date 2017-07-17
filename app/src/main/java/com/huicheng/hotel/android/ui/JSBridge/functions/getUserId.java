package com.huicheng.hotel.android.ui.JSBridge.functions;

import android.content.Intent;

import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.ui.JSBridge.WVJBWebViewClient;
import com.huicheng.hotel.android.ui.JSBridge.WVJBWebViewClient.WVJBResponseCallback;
import com.prj.sdk.app.AppContext;
import com.prj.sdk.constants.BroadCastConst;

import org.json.JSONObject;

/**
 * 获取用户ID,用户未登录时调用此接口框架会要求用户去登录
 */
public class getUserId implements WVJBWebViewClient.WVJBHandler {

    @Override
    public void request(Object data, WVJBResponseCallback callback) {
        try {

            if (callback != null) {
                if (SessionContext.isLogin()) {
                    JSONObject mJson = new JSONObject();
                    mJson.put("userId", SessionContext.mUser.user.userid);
                    callback.callback(mJson.toString());
                } else {
                    AppContext.mAppContext.sendBroadcast(new Intent(BroadCastConst.UNLOGIN_ACTION));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
