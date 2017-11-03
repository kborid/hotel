package com.huicheng.hotel.android.common.pay.qmf;

import android.os.Bundle;

import com.chinaums.pppay.quickpay.service.UmsQuickPayResultListener;
import com.prj.sdk.util.LogUtil;

import cn.jpush.android.api.JPushInterface;

/**
 * @auth kborid
 * @date 2017/11/3 0003.
 */

public class QmfQuickPayOrderResultListener implements UmsQuickPayResultListener {

    // 打印所有的 intent extra 数据
    private static String printBundle(Bundle bundle) {
        StringBuilder sb = new StringBuilder();
        for (String key : bundle.keySet()) {
            if (key.equals(JPushInterface.EXTRA_NOTIFICATION_ID)) {
                sb.append("\nkey:").append(key).append(", value:").append(bundle.getInt(key));
            } else if (key.equals(JPushInterface.EXTRA_CONNECTION_CHANGE)) {
                sb.append("\nkey:").append(key).append(", value:").append(bundle.getBoolean(key));
            } else {
                sb.append("\nkey:").append(key).append(", value:").append(bundle.getString(key));
            }
        }
        return sb.toString();
    }

    @Override
    public void umsServiceResult(Bundle bundle) {
        String receive = printBundle(bundle);
        String errCode = bundle.getString("errCode");
        String errInfo = bundle.getString("errInfo");
        LogUtil.i("umsServiceResult", errCode + "=====-" + errInfo);
        LogUtil.i("umsServiceResult", "errCode:" + errCode + "errInfo:" + errInfo + ";所有信息:" + receive);
        LogUtil.i("umsServiceResult", "======" + receive);
        //TODO something
    }
}
