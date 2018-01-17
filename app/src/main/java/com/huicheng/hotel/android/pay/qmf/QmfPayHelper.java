package com.huicheng.hotel.android.pay.qmf;

import com.huicheng.hotel.android.PRJApplication;

/**
 * @auth kborid
 * @date 2017/11/2 0002.
 */

public class QmfPayHelper extends QmfPayBase {

    public void setPayStrategy(IQmfPayStrategy payStrategy) {
        this.payStrategy = payStrategy;
    }

    public void setPayStrategy(int channel){
        switch (channel){
            case 0:
                this.payStrategy = new QmfAliPay();
                break;
            case 1:
                this.payStrategy = new QmfWxPay();
                break;
            case 2:
                this.payStrategy = new QmfPosterPay(PRJApplication.getInstance());
                break;
        }
    }

    @Override
    public void startPay(String str) {
        payStrategy.startPay(str);
    }
}
