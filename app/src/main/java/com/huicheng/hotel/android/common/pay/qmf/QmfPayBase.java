package com.huicheng.hotel.android.common.pay.qmf;

/**
 * @auth kborid
 * @date 2017/11/2 0002.
 */

public abstract class QmfPayBase {
    protected IQmfPayStragety payStragety;

    public abstract void startPay(String str);
}
