package com.huicheng.hotel.android.pay.qmf;

import android.content.Context;

/**
 * @author kborid
 * @date 2017/11/2 0002.
 */

public class QmfPayHelper extends QmfPayBase {

    private Context context;

    public QmfPayHelper(Context context) {
        this.context = context;
    }

    public void setPayStrategy(IQmfPayStrategy payStrategy) {
        this.payStrategy = payStrategy;
    }

    public void setPayStrategy(int channel) {
        switch (channel) {
            case 0:
                this.payStrategy = new QmfAliPay(context);
                break;
            case 1:
                this.payStrategy = new QmfWxPay(context);
                break;
            case 2:
                this.payStrategy = new QmfPosterPay(context);
                break;
        }
    }

    @Override
    public void startPay(String str) {
        payStrategy.startPay(str);
    }

    @Override
    public void destroy() {
        if (null != context) {
            context = null;
        }
        if (null != payStrategy) {
            payStrategy = null;
        }
    }
}
