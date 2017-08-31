package com.huicheng.hotel.android.common.pay.unionpay;

import android.content.Context;

import com.huicheng.hotel.android.common.AppConst;
import com.unionpay.UPPayAssistEx;

/**
 * @auth kborid
 * @date 2017/8/9.
 */

public class UnionPayUtil {

    public static final String RELEASE_MODE = "00";
    public static final String DEBUG_MODE = "01";
    private Context context;
    private String serverMode = DEBUG_MODE;

    public UnionPayUtil(Context context) {
        this.context = context;
        serverMode = AppConst.ISDEVELOP ? DEBUG_MODE : RELEASE_MODE;
    }

    public void setUnionPayServerMode(String mode) {
        this.serverMode = mode;
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
