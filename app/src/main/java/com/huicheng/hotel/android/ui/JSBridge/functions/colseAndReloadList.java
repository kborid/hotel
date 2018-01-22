package com.huicheng.hotel.android.ui.JSBridge.functions;

import android.content.Context;
import android.content.Intent;

import com.huicheng.hotel.android.ui.JSBridge.WVJBWebViewClient;
import com.huicheng.hotel.android.ui.activity.UcOrdersActivity;

/**
 * @author kborid
 * @date 2017/3/17 0017
 */
public class colseAndReloadList implements WVJBWebViewClient.WVJBHandler {
    private Context context;
    public colseAndReloadList(Context context) {
        this.context = context;
    }

    @Override
    public void request(Object data, WVJBWebViewClient.WVJBResponseCallback callback) {
        Intent intent = new Intent(context, UcOrdersActivity.class);
        context.startActivity(intent);
    }
}
