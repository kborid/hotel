package com.huicheng.hotel.android.common.pay.qmf;

/**
 * @auth kborid
 * @date 2017/11/2 0002.
 */

public class QmfPayHelper extends QmfPayBase {

    public void setPayStragety(IQmfPayStragety payStragety) {
        this.payStragety = payStragety;
    }

    @Override
    public void startPay(String str) {
        payStragety.startPay(str);
    }
}
