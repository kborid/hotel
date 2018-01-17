package com.huicheng.hotel.android.pay.qmf;

import com.chinaums.pppay.unify.UnifyPayPlugin;
import com.chinaums.pppay.unify.UnifyPayRequest;
import com.huicheng.hotel.android.PRJApplication;
import com.huicheng.hotel.android.pay.wxpay.Constants;

/**
 * @auth kborid
 * @date 2017/11/2 0002.
 */

public class QmfWxPay implements IQmfPayStrategy {
    private UnifyPayPlugin payPlugin = null;

    public QmfWxPay() {
        payPlugin = UnifyPayPlugin.getInstance(PRJApplication.getInstance());
        payPlugin.initialize(Constants.APP_ID);
        payPlugin.setListener(new QmfUnifyPayListener());
    }

    @Override
    public void startPay(String str) {
        UnifyPayRequest request = new UnifyPayRequest();
        request.payChannel = UnifyPayRequest.CHANNEL_WEIXIN;
        request.payData = str;
        payPlugin.sendPayRequest(request);
    }
}
