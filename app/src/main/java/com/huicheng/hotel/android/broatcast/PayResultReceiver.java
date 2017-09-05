package com.huicheng.hotel.android.broatcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.HotelOrderManager;
import com.huicheng.hotel.android.common.pay.PayCommDef;
import com.huicheng.hotel.android.net.bean.OrderPayDetailInfoBean;
import com.huicheng.hotel.android.ui.activity.OrderPaySuccessActivity;
import com.prj.sdk.constants.BroadCastConst;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.widget.CustomToast;

/**
 * @auth kborid
 * @date 2017/8/15.
 */

public class PayResultReceiver extends BroadcastReceiver {

    private static final String TAG = "PayResultReceiver";

    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        String action = intent.getAction();
        if (!BroadCastConst.ACTION_PAY_STATUS.equals(action)) {
            return;
        }

        dealPayResult(intent);
    }

    private void dealPayResult(Intent intent) {
        Bundle bundle = intent.getExtras();
        if (null != bundle) {
            String info = intent.getExtras().getString("info");
            String type = intent.getExtras().getString("type");
            LogUtil.i(TAG, info);
            LogUtil.i(TAG, type);

            int ret = PayCommDef.err_fail;
            String msg = context.getResources().getString(R.string.pay_fail);

            switch (type) {
                case PayCommDef.ALIPAY:
                    JSONObject aliPayJson = JSON.parseObject(info);
                    String aliPayCode = aliPayJson.getString("resultStatus");
                    String aliPayMsg = aliPayJson.getString("memo");
                    LogUtil.i(TAG, "AliPay: Code=" + aliPayCode + ", Msg=" + aliPayMsg);
                    switch (aliPayCode) {
                        case "9000":
                            ret = PayCommDef.err_success;
                            break;
                        case "4000":
                            ret = PayCommDef.err_fail;
                            break;
                        case "6001":
                            ret = PayCommDef.err_cancel;
                            break;
                        case "8000":
                        case "6004":
                            ret = PayCommDef.err_unknown;
                            break;
                        default:
                            ret = PayCommDef.err_error;
                            break;
                    }
                    break;
                case PayCommDef.WXPAY:
                    LogUtil.i(TAG, "WXPay: Code=" + info);
                    switch (info) {
                        case "0":
                            ret = PayCommDef.err_success;
                            break;
                        case "-1":
                            ret = PayCommDef.err_fail;
                            break;
                        case "-2":
                            ret = PayCommDef.err_cancel;
                            break;
                        default:
                            ret = PayCommDef.err_unknown;
                            break;
                    }
                    break;
                case PayCommDef.UNIONPAY:
                    LogUtil.i(TAG, "UnionPay: Code=" + info);
                    switch (info) {
                        case "success":
                            ret = PayCommDef.err_success;
                            break;
                        case "fail":
                            ret = PayCommDef.err_fail;
                            break;
                        case "cancel":
                            ret = PayCommDef.err_cancel;
                            break;
                        default:
                            ret = PayCommDef.err_unknown;
                            break;
                    }
                    break;
            }


            switch (ret) {
                case PayCommDef.err_success:
                    msg = context.getResources().getString(R.string.pay_success);
                    break;
                case PayCommDef.err_fail:
                    msg = context.getResources().getString(R.string.pay_fail);
                    break;
                case PayCommDef.err_cancel:
                    msg = context.getResources().getString(R.string.pay_cancel);
                    break;
                case PayCommDef.err_error:
                case PayCommDef.err_unknown:
                    msg = context.getResources().getString(R.string.pay_error);
                    break;
            }

            if (PayCommDef.err_success == ret) {
                OrderPayDetailInfoBean orderPayDetailInfoBean = HotelOrderManager.getInstance().getOrderPayDetailInfoBean();
                if (null != orderPayDetailInfoBean) {
                    Intent intent1 = new Intent(context, OrderPaySuccessActivity.class);
                    intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent1.putExtra("hotelId", orderPayDetailInfoBean.hotelID);
                    intent1.putExtra("hotelName", orderPayDetailInfoBean.name);
                    intent1.putExtra("roomName", orderPayDetailInfoBean.roomName);
                    intent1.putExtra("checkRoomDate", orderPayDetailInfoBean.checkRoomDate);
                    intent1.putExtra("beginTime", orderPayDetailInfoBean.timeStart);
                    intent1.putExtra("endTime", orderPayDetailInfoBean.timeEnd);
                    intent1.putExtra("isPrePaySuccess", true);
                    context.startActivity(intent1);
                } else {
                    CustomToast.show(context.getResources().getString(R.string.pay_error), CustomToast.LENGTH_SHORT);
                }
            }
            CustomToast.show(msg, CustomToast.LENGTH_SHORT);
        }
    }
}
