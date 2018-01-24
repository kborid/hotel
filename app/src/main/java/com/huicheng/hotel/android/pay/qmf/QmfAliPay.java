package com.huicheng.hotel.android.pay.qmf;

import android.content.Context;

import com.chinaums.pppay.unify.UnifyPayPlugin;
import com.chinaums.pppay.unify.UnifyPayRequest;

/**
 * @author kborid
 * @date 2017/11/2 0002.
 */

public class QmfAliPay implements IQmfPayStrategy {
    private UnifyPayPlugin payPlugin = null;

    public QmfAliPay(Context context) {
        payPlugin = UnifyPayPlugin.getInstance(context);
        payPlugin.setListener(new QmfUnifyPayListener());
    }

    @Override
    public void startPay(String str) {
        UnifyPayRequest request = new UnifyPayRequest();
        request.payChannel = UnifyPayRequest.CHANNEL_ALIPAY;
        request.payData = str;
        payPlugin.sendPayRequest(request);
    }
}
