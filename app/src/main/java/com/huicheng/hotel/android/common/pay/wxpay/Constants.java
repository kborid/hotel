package com.huicheng.hotel.android.common.pay.wxpay;

import com.huicheng.hotel.android.PRJApplication;
import com.huicheng.hotel.android.R;

public class Constants {
    //appid
    public static final String APP_ID = PRJApplication.getInstance().getString(R.string.wx_appid);
    //appsecret
    public static final String API_KEY = PRJApplication.getInstance().getString(R.string.wx_appsecret);
}
