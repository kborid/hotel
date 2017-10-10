package com.huicheng.hotel.android.common.pay.qmfpay;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.chinaums.pppay.WelcomeActivity;
import com.chinaums.pppay.unify.UnifyPayListener;
import com.chinaums.pppay.unify.UnifyPayPlugin;
import com.chinaums.pppay.unify.UnifyPayRequest;
import com.huicheng.hotel.android.PRJApplication;

/**
 * @auth kborid
 * @date 2017/9/28.
 */

public class QMFPayUtil {

    public static final String CHANNEL_WXPAY = UnifyPayRequest.CHANNEL_WEIXIN;
    public static final String CHANNEL_ALIPAY = UnifyPayRequest.CHANNEL_ALIPAY;
    public static final String CHANNEL_UNIONPAY = "qmf_unionpay";

    private UnifyPayPlugin payPlugin = null;
    private static QMFPayUtil instance = null;

    private QMFPayUtil() {
        payPlugin = UnifyPayPlugin.getInstance(PRJApplication.getInstance());
        payPlugin.setListener(new UnifyPayListener() {
            @Override
            public void onResult(int i, String s, String s1) {
//                System.out.println("onResult() i = " + i + ", s = " + s + ", s1 = " + s1);
            }
        });
    }

    public static QMFPayUtil getInstance() {
        if (null == instance) {
            synchronized (QMFPayUtil.class) {
                if (null == instance) {
                    instance = new QMFPayUtil();
                }
            }
        }
        return instance;
    }

    public void pay(Context context, String channel, String params) {
        if (CHANNEL_UNIONPAY.equals("qmf_unionpay")) {
            JSONObject json = JSON.parseObject(params);
            JSONObject payRequest = json.getJSONObject("appPayRequest");
            Bundle args = new Bundle();
            args.putString("merchantId", payRequest.getString("merchantId"));
            args.putString("merchantUserId", payRequest.getString("merchantUserId"));
            args.putString("merOrderId", json.getString("merOrderId"));
            args.putString("amount", json.getString("totalAmount"));
            args.putString("mobile", payRequest.getString("mobile"));
            args.putString("sign", payRequest.getString("sign"));
            args.putString("mode", payRequest.getString("mode"));
            args.putString("notifyUrl", payRequest.getString("notifyUrl"));
            args.putBoolean("isProductEnv", false);
            args.putString("payChannel", "2");
            args.putString("orderId", payRequest.getString("orderId"));

            Intent intent = new Intent(context, WelcomeActivity.class);
            intent.putExtra("extra_args", args);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else {
            UnifyPayRequest request = new UnifyPayRequest();
            request.CHANNEL = channel;
            request.payData = params;
            payPlugin.sendPayRequest(request);
        }
    }
}
