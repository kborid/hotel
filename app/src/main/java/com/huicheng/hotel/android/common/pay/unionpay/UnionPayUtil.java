package com.huicheng.hotel.android.common.pay.unionpay;

import android.content.Context;
import android.provider.Settings;

import com.huicheng.hotel.android.common.AppConst;
import com.unionpay.UPPayAssistEx;

/**
 * @auth kborid
 * @date 2017/8/9.
 */

public class UnionPayUtil {

    private static final String RELEASE_MODE = "02";
    private static final String DEBUG_MODE = "01";
    private Context context;
    private String serverMode = DEBUG_MODE;

    public UnionPayUtil(Context context) {
        this.context = context;
        serverMode = AppConst.ISDEVELOP ? DEBUG_MODE : RELEASE_MODE;
    }

    /**
     * 检测是否安装银联apk
     */
    public boolean isUnionApkInstalled() {
        return UPPayAssistEx.checkInstalled(context);
    }

    public void unionStartPay(String tn) {
        int ret = 0;
        ret = UPPayAssistEx.startPay(context, null, null, tn, serverMode);
    }
}
