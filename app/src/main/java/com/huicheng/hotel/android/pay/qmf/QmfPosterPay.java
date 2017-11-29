package com.huicheng.hotel.android.pay.qmf;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.chinaums.pppay.WelcomeActivity;

/**
 * @auth kborid
 * @date 2017/11/2 0002.
 */

public class QmfPosterPay implements IQmfPayStragety {
    private Context context;

    public QmfPosterPay(Context context) {
        this.context = context;
    }

    @Override
    public void startPay(String str) {
        JSONObject json = JSON.parseObject(str);
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
    }
}
