package com.huicheng.hotel.android.ui.JSBridge.functions;

import com.huicheng.hotel.android.ui.JSBridge.WVJBWebViewClient;
import com.huicheng.hotel.android.content.NetURL;

import org.json.JSONObject;

/**
 * @author kborid
 * @date 2017/3/17 0017
 */
public class getServiceHost implements WVJBWebViewClient.WVJBHandler {

    @Override
    public void request(Object data, WVJBWebViewClient.WVJBResponseCallback callback) {
        try {

            if (callback != null) {
                JSONObject mJson = new JSONObject();
                mJson.put("serviceHost", NetURL.getApi());
                callback.callback(mJson.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
