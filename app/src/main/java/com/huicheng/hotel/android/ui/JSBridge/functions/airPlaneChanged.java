package com.huicheng.hotel.android.ui.JSBridge.functions;

import android.content.Context;
import android.content.Intent;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huicheng.hotel.android.ui.JSBridge.WVJBWebViewClient;
import com.huicheng.hotel.android.ui.activity.MainSwitcherActivity;

/**
 * @author kborid
 * @date 2017/3/17 0017
 */
public class airPlaneChanged implements WVJBWebViewClient.WVJBHandler {
    private Context mContext;

    public airPlaneChanged(Context context) {
        this.mContext = context;
    }

    @Override
    public void request(Object data, WVJBWebViewClient.WVJBResponseCallback callback) {
        if (data != null) {
            JSONObject mJson = JSON.parseObject(data.toString());
            String orderId = mJson.getString("yiorderid");
            Intent intent = new Intent(mContext, MainSwitcherActivity.class);
            intent.putExtra("orderId", orderId);
            intent.putExtra("isClosed", true);
            intent.putExtra("isReload", true);
            mContext.startActivity(intent);
        }
    }
}
