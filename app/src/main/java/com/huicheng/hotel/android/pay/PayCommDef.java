package com.huicheng.hotel.android.pay;

/**
 * @auth kborid
 * @date 2017/8/15.
 */

public class PayCommDef {
    //支付渠道
    public static final String ALIPAY = "aliPay";
    public static final String WXPAY = "wechat";
    public static final String UNIONPAY = "unionPay";

    //支付结果
    public static final String P_SUCCESS = "pay_success";
    public static final String P_FAIL = "pay_fail";
    public static final String P_CANCEL = "pay_cancel";
    public static final String P_ERROR = "pay_error";

    //支付结果error code
    public static final int err_success = 0;
    public static final int err_fail = -1;
    public static final int err_cancel = -2;
    public static final int err_error = -3;
}
