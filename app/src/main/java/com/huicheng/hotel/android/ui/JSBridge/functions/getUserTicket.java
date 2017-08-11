package com.huicheng.hotel.android.ui.JSBridge.functions;

import android.content.Intent;

import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.ui.JSBridge.WVJBWebViewClient;
import com.huicheng.hotel.android.ui.JSBridge.WVJBWebViewClient.WVJBResponseCallback;
import com.prj.sdk.app.AppContext;
import com.prj.sdk.constants.BroadCastConst;
import com.prj.sdk.util.StringUtil;

import org.json.JSONObject;

/**
 * 2.5. 获取访问凭据，Null时为未登录
 *
 * @author LiaoBo
 */
public class getUserTicket implements WVJBWebViewClient.WVJBHandler {
    @Override
    public void request(Object data, WVJBResponseCallback callback) {
        try {
            if (callback != null) {
                if (StringUtil.notEmpty(SessionContext.getTicket())) {
                    JSONObject mJson = new JSONObject();
                    mJson.put("userTicket", SessionContext.getTicket());
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
