package com.huicheng.hotel.android.common.pay.unionpay;

import android.content.Context;

import com.unionpay.UPPayAssistEx;

/**
 * @auth kborid
 * @date 2017/8/9.
 */

public class UnionPayUtil {

    private Context context;
    private String serverMode = "01";

    public UnionPayUtil(Context context) {
        this.context = context;
    }

    /**
     * 检测是否安装银联apk
     */
    public boolean isUnionApkInstalled() {
        return UPPayAssistEx.checkInstalled(context);
    }
}
