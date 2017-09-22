package com.huicheng.hotel.android.ui.JSBridge.functions;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.HotelCommDef;
import com.huicheng.hotel.android.common.NetURL;
import com.huicheng.hotel.android.common.pay.alipay.AlipayUtil;
import com.huicheng.hotel.android.common.pay.unionpay.UnionPayActivity;
import com.huicheng.hotel.android.common.pay.unionpay.UnionPayUtil;
import com.huicheng.hotel.android.common.pay.wxpay.WXPayUtils;
import com.huicheng.hotel.android.net.RequestBeanBuilder;
import com.huicheng.hotel.android.ui.JSBridge.WVJBWebViewClient;
import com.huicheng.hotel.android.ui.dialog.ProgressDialog;
import com.prj.sdk.constants.BroadCastConst;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.LogUtil;
import com.huicheng.hotel.android.ui.dialog.CustomToast;

import java.net.ConnectException;

/**
 * @author kborid
 * @date 2017/3/17 0017
 */
public class payPlaneTicket implements WVJBWebViewClient.WVJBHandler {

    private final String TAG = getClass().getSimpleName();
    private Context mContext;
    private ProgressDialog mProgressDialog;
    private WVJBWebViewClient.WVJBResponseCallback mCallback;
    private BroadcastReceiver mBroadcastReceiver;

    public payPlaneTicket(Context context) {
        mContext = context;
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                LogUtil.i(TAG, "payPlaneTicket onReceive ACTION_PAY_STATUS callback");
                try {
                    String action = intent.getAction();
                    if (BroadCastConst.ACTION_PAY_STATUS.equals(action)) {
                        JSONObject mJson = new JSONObject();
                        String info = intent.getExtras().getString("info");
                        String type = intent.getExtras().getString("type");
                        mJson.put("info", info);
                        mJson.put("type", type);
                        LogUtil.i(TAG, "info = " + info + ", type = " + type);
                        mCallback.callback(mJson.toString());
                        unregisterReceiver();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    @Override
    public void request(Object data, WVJBWebViewClient.WVJBResponseCallback callback) {
        try {

            if (callback != null && data != null) {

                this.mCallback = callback;

                LogUtil.i(TAG, "data String = " + data.toString());
                JSONObject mJson = JSON.parseObject(data.toString());
                String payChannel = mJson.getString("payChannel");
                String orderNo = mJson.getString("orderNo");

                requestPayOrderInfo(orderNo, payChannel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void requestPayOrderInfo(String orderNo, String payChannel) {
        LogUtil.i(TAG, "requestPayOrderInfo() orderNo = " + orderNo + ", payChannel = " + payChannel);
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("orderNo", orderNo);
        b.addBody("tradeType", "02"); // 01 酒店业务，02 机票业务
        b.addBody("payChannel", payChannel);

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.PAY;
        d.flag = AppConst.PAY;

        if (!isProgressShowing()) {
            showProgressDialog(mContext);
        }

        DataLoader.getInstance().loadData(new DataCallback() {
            @Override
            public void preExecute(ResponseData request) {
            }

            @Override
            public void notifyMessage(ResponseData request, ResponseData response) throws Exception {
                if (response != null && response.body != null) {
                    if (request.flag == AppConst.PAY) {
                        LogUtil.i(TAG, "json = " + response.body.toString());
                        removeProgressDialog();
                        JSONObject mJson = JSON.parseObject(response.body.toString());
                        if (mJson.containsKey(HotelCommDef.WXPAY)) {
                            JSONObject mmJson = mJson.getJSONObject(HotelCommDef.WXPAY);
                            wechatPay(mmJson);
                        } else if (mJson.containsKey(HotelCommDef.ALIPAY)) {
                            aliPay(mJson.getString(HotelCommDef.ALIPAY));
                        } else if (mJson.containsKey(HotelCommDef.UNIONPAY)) {
                            JSONObject mmJson = mJson.getJSONObject(HotelCommDef.UNIONPAY);
                            unionPay(mmJson.getString("tn"));
                        } else {
                            CustomToast.show("支付失败", CustomToast.LENGTH_SHORT);
                        }
                    }
                }
            }

            @Override
            public void notifyError(ResponseData request, ResponseData response, Exception e) {
                LogUtil.i(TAG, "payPlaneTicket()");
                removeProgressDialog();
                String message;
                if (e != null && e instanceof ConnectException) {
                    message = mContext.getResources().getString(R.string.dialog_tip_net_error);
                } else {
                    message = response != null && response.data != null ? response.data.toString() : mContext.getResources().getString(R.string.dialog_tip_null_error);
                }
                LogUtil.i(TAG, "payPlaneTicket = " + message);
                CustomToast.show(message, CustomToast.LENGTH_SHORT);
            }
        }, d);
    }

    /**
     * 阿里支付
     */
    private void aliPay(String orderInfo) {
        AlipayUtil ali = new AlipayUtil((Activity) mContext);
        ali.pay(orderInfo);
        registerReceiver();
    }

    /**
     * 微信支付
     */
    private void wechatPay(JSONObject mmJson) {
        WXPayUtils wx = new WXPayUtils((Activity) mContext);
        if (wx.isSupport()) {
            wx.sendPayReq(mmJson.getString("package"),
                    mmJson.getString("appid"),
                    mmJson.getString("sign"),
                    mmJson.getString("partnerid"),
                    mmJson.getString("prepayid"),
                    mmJson.getString("noncestr"),
                    mmJson.getString("timestamp"));
            registerReceiver();
        }
    }

    private void unionPay(String tn) {
//        UnionPayUtil unionpay = new UnionPayUtil(mContext);
//        unionpay.setUnionPayServerMode(UnionPayUtil.RELEASE_MODE);
//        unionpay.unionStartPay(tn);
        Intent intent = new Intent(mContext, UnionPayActivity.class);
        intent.putExtra("ServerMode", UnionPayUtil.RELEASE_MODE);
        intent.putExtra("tn", tn);
        mContext.startActivity(intent);
        registerReceiver();
    }


    /**
     * 注册广播,获取支付结果
     */
    private void registerReceiver() {
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(BroadCastConst.ACTION_PAY_STATUS);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mBroadcastReceiver, mIntentFilter);
    }

    /**
     * 注销广播
     */
    private void unregisterReceiver() {
        try {
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mBroadcastReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showProgressDialog(Context context) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(context);
        }
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }


    private boolean isProgressShowing() {
        return mProgressDialog != null && mProgressDialog.isShowing();
    }

    private void removeProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }
}
