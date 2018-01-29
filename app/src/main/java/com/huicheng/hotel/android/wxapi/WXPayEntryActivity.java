package com.huicheng.hotel.android.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.chinaums.pppay.unify.UnifyPayPlugin;
import com.chinaums.pppay.unify.WXPayResultListener;
import com.huicheng.hotel.android.pay.wxpay.Constants;
import com.prj.sdk.constants.BroadCastConst;
import com.prj.sdk.util.LogUtil;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {

    private final String TAG = getClass().getSimpleName();

    private IWXAPI api;

    private WXPayResultListener listener = null;

    private void setListener(WXPayResultListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = WXAPIFactory.createWXAPI(this, Constants.APP_ID);
//        api = WXAPIFactory.createWXAPI(this, UnifyPayPlugin.getInstance(this).getAppId());
        api.handleIntent(getIntent(), this);
        setListener(UnifyPayPlugin.getInstance(this).getWXListener());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }


    @Override
    public void onReq(BaseReq baseReq) {
    }

    @Override
    public void onResp(BaseResp resp) {
        if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
            LogUtil.i(TAG, "wechat callback: resp = " + resp.errStr);
            if (listener != null) {
                listener.onResponse(this, resp);
            }
            Intent mIntent = new Intent(BroadCastConst.ACTION_PAY_STATUS);
            mIntent.putExtra("info", String.valueOf(resp.errCode));
            mIntent.putExtra("type", "wechat");
            LocalBroadcastManager.getInstance(this).sendBroadcast(mIntent);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        api.detach();
    }
}