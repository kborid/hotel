package com.huicheng.hotel.android.pay.qmf;

import com.chinaums.pppay.unify.UnifyPayPlugin;
import com.chinaums.pppay.unify.UnifyPayRequest;
import com.huicheng.hotel.android.PRJApplication;

/**
 * @auth kborid
 * @date 2017/11/2 0002.
 */

public class QmfAliPay implements IQmfPayStragety {
    private UnifyPayPlugin payPlugin = null;

    public QmfAliPay() {
        payPlugin = UnifyPayPlugin.getInstance(PRJApplication.getInstance());
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
