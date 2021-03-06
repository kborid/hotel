package com.huicheng.hotel.android.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.HotelOrderManager;
import com.huicheng.hotel.android.pay.PayCommDef;
import com.huicheng.hotel.android.requestbuilder.bean.OrderPayDetailInfoBean;
import com.huicheng.hotel.android.ui.activity.hotel.HotelOrderPaySuccessActivity;
import com.huicheng.hotel.android.ui.activity.plane.PlaneOrderPaySuccessActivity;
import com.huicheng.hotel.android.ui.dialog.CustomToast;
import com.prj.sdk.constants.BroadCastConst;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.StringUtil;

/**
 * @auth kborid
 * @date 2017/8/15.
 */

public class PayResultReceiver extends BroadcastReceiver {

    public static final String MODULE_HOTEL = "module_hotel";
    public static final String MODULE_PLANE = "module_plane";

    private final String TAG = getClass().getSimpleName();

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
            String module = (null != bundle.getString("module")) ? bundle.getString("module") : "";
            String type = (null != bundle.getString("type")) ? bundle.getString("type") : "";
            String info = (null != bundle.getString("info")) ? bundle.getString("info") : "";
            LogUtil.i(TAG, module);
            LogUtil.i(TAG, info);
            LogUtil.i(TAG, type);

            int ret = PayCommDef.err_error;
            if (StringUtil.notEmpty(type) && StringUtil.notEmpty(info)) {
                String retCode = "";
                switch (type) {
                    case PayCommDef.ALIPAY:
                        JSONObject aliPayJson = JSON.parseObject(info);
                        String aliPayCode = aliPayJson.getString("resultStatus");
                        String aliPayMsg = aliPayJson.getString("memo");
                        LogUtil.i(TAG, "AliPay:Code=" + aliPayCode + ", Msg=" + aliPayMsg);
                        retCode = aliPayCode;
                        break;
                    case PayCommDef.WXPAY:
                    case PayCommDef.UNIONPAY:
                    case PayCommDef.CUSTOMPAY:
                        retCode = info;
                        LogUtil.i(TAG, type + ":Code=" + info);
                        break;
                    case PayCommDef.NOTPAY:
                        retCode = PayCommDef.NOTPAY;
                        break;
                }

                //整合支付宝、微信、银联支付结果码
                if ("9000".equals(retCode) || "0".equals(retCode) || "success".equals(retCode) || PayCommDef.NOTPAY.equals(retCode) || "SUCCESS".equals(retCode)) {
                    ret = PayCommDef.err_success;
                } else if ("4000".equals(retCode) || "-1".equals(retCode) || "fail".equals(retCode) || "ERROR".equals(retCode)) {
                    ret = PayCommDef.err_fail;
                } else if ("6001".equals(retCode) || "-2".equals(retCode) || "cancel".equals(retCode)) {
                    ret = PayCommDef.err_cancel;
                } else {
                    ret = PayCommDef.err_error;
                }
            }
            LogUtil.i(TAG, "pay result ret = " + ret);
            if (PayCommDef.err_success == ret) {
                switch (module) {
                    case MODULE_PLANE:
                        Intent planeIntent = new Intent(context, PlaneOrderPaySuccessActivity.class);
                        context.startActivity(planeIntent);
                        break;
                    case MODULE_HOTEL:
                    default:
                        OrderPayDetailInfoBean orderPayDetailInfoBean = HotelOrderManager.getInstance().getOrderPayDetailInfoBean();
                        LogUtil.i(TAG, "orderPayDetailInfoBean = " + orderPayDetailInfoBean);
                        if (null != orderPayDetailInfoBean) {
                            Intent hotelIntent = new Intent(context, HotelOrderPaySuccessActivity.class);
                            hotelIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            hotelIntent.putExtra("hotelId", orderPayDetailInfoBean.hotelID);
                            hotelIntent.putExtra("roomName", orderPayDetailInfoBean.roomName);
                            hotelIntent.putExtra("checkRoomDate", orderPayDetailInfoBean.checkRoomDate);
                            hotelIntent.putExtra("beginTime", orderPayDetailInfoBean.timeStart);
                            hotelIntent.putExtra("endTime", orderPayDetailInfoBean.timeEnd);
                            hotelIntent.putExtra("isPrePaySuccess", true);
                            hotelIntent.putExtra("showTipsOrNot", orderPayDetailInfoBean.showTipsOrNot);
                            context.startActivity(hotelIntent);
                        } else {
                            CustomToast.show(context.getResources().getString(R.string.pay_error), CustomToast.LENGTH_SHORT);
                        }
                        break;
                }
            }

            String msg = context.getResources().getString(R.string.pay_fail);
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
                    msg = context.getResources().getString(R.string.pay_error);
                    break;
            }
            LogUtil.i(TAG, "pay result msg = " + msg);
            CustomToast.show(msg, CustomToast.LENGTH_SHORT);
        }
    }
}
