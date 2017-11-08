package com.huicheng.hotel.android.ui.JSBridge.functions;

import android.content.Context;
import android.content.Intent;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huicheng.hotel.android.ui.JSBridge.WVJBWebViewClient;
import com.prj.sdk.app.AppContext;
import com.prj.sdk.constants.BroadCastConst;
import com.prj.sdk.util.StringUtil;

/**
 * 8. Private_处理错误信息
 *
 * @author LiaoBo
 */
public class handleError implements WVJBWebViewClient.WVJBHandler {
    private Context mContext;

    public handleError(Context context) {
        mContext = context;
    }

    @Override
    public void request(Object data, WVJBWebViewClient.WVJBResponseCallback callback) {
        try {

//			if (callback == null) {
//				return;
//			}
            if (StringUtil.isEmpty(data)) {
                return;
            }
            // 解析请求参数
            JSONObject mJson = JSON.parseObject(data.toString());
            String rtnCode = mJson.getString("rtnCode");
            String rtnMsg = mJson.getString("rtnMsg");
            if (rtnCode != null && (rtnCode.equals("900902") || rtnCode.equals("310001"))) {// 900902，310001//登录过期
                AppContext.mAppContext.sendBroadcast(new Intent(BroadCastConst.UNLOGIN_ACTION));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
