package com.huicheng.hotel.android.common.pay.qmf;

import android.os.Bundle;

import com.chinaums.pppay.quickpay.service.UmsQuickPayResultListener;
import com.prj.sdk.util.LogUtil;

/**
 * @auth kborid
 * @date 2017/11/3 0003.
 */

public class QmfQuickPayOrderResultListener implements UmsQuickPayResultListener {

    // 打印所有的 intent extra 数据
    private static String printBundle(Bundle bundle) {
        StringBuilder sb = new StringBuilder();
        for (String key : bundle.keySet()) {
            sb.append("\n").append(key).append(":").append(bundle.get(key));
        }
        return sb.toString();
    }

    @Override
    public void umsServiceResult(Bundle bundle) {
        String receive = printBundle(bundle);
        String errCode = bundle.getString("errCode");
        String errInfo = bundle.getString("errInfo");
        LogUtil.i("umsServiceResult", "errCode:" + errCode + ", errInfo:" + errInfo);
        LogUtil.i("umsServiceResult", "======" + receive);
        //TODO something
    }
}
