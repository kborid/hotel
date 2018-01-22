package com.huicheng.hotel.android.ui.JSBridge.functions;

import android.content.Context;
import android.content.Intent;

import com.huicheng.hotel.android.ui.JSBridge.WVJBWebViewClient;
import com.huicheng.hotel.android.ui.activity.hotel.HotelMainActivity;

/**
 * @author kborid
 * @date 2017/3/17 0017
 */
public class reBookPlane implements WVJBWebViewClient.WVJBHandler {
    private Context mContext;

    public reBookPlane(Context context) {
        this.mContext = context;
    }

    @Override
    public void request(Object data, WVJBWebViewClient.WVJBResponseCallback callback) {
        Intent intent = new Intent(mContext, HotelMainActivity.class);
        intent.putExtra("index", 1);
        intent.putExtra("isClosed", true);
        intent.putExtra("isReload", true);
        mContext.startActivity(intent);
    }
}
