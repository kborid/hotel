package com.huicheng.hotel.android.requestbuilder.bean;

import android.content.Context;

import com.huicheng.hotel.android.R;
import com.prj.sdk.algo.SHA1;
import com.prj.sdk.app.AppConst;

import java.io.Serializable;

/**
 * @auth kborid
 * @date 2017/8/9.
 */

public class ShenZhouConfigBean implements Serializable {
    private String headerBg;
    private String tpuid;
    private String mobile;
    private String timestamp;

    public String getHeaderBg() {
        return headerBg;
    }

    public void setHeaderBg(String headerBg) {
        this.headerBg = headerBg;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    private String signature;

    public ShenZhouConfigBean(String headerBg, String tpuid, String mobile, String timestamp, String signature) {
        this.headerBg = headerBg;
        this.tpuid = tpuid;
        this.mobile = mobile;
        this.timestamp = timestamp;
        this.signature = signature;
    }

    public String getSignatureSha1(Context context) {
        StringBuilder sb = new StringBuilder();
        String secret = AppConst.ISDEVELOP ? context.getResources().getString(R.string.sz_appsecret_debug) :
                context.getResources().getString(R.string.sz_appsecret_release);
        sb.append(secret)
                .append(mobile)
                .append(timestamp)
                .append(tpuid);
        SHA1 sha1 = new SHA1();
        return sha1.getDigestOfString(sb.toString().getBytes());
    }
}
