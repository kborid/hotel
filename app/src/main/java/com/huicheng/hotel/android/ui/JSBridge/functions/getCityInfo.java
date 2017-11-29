package com.huicheng.hotel.android.ui.JSBridge.functions;

import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.ui.JSBridge.WVJBWebViewClient;
import com.prj.sdk.util.SharedPreferenceUtil;

import org.json.JSONObject;

/**
 * 3.5 获取城市信息
 *
 * @author LiaoBo
 */
public class getCityInfo implements WVJBWebViewClient.WVJBHandler {

    @Override
    public void request(Object data, WVJBWebViewClient.WVJBResponseCallback callback) {
        try {

            if (callback != null) {
                //{“cityCode”:”500000”,”cityName”:”重庆"}
                JSONObject mJson = new JSONObject();
                mJson.put("cityCode", SharedPreferenceUtil.getInstance().getString(AppConst.SITEID, "", false));
                mJson.put("cityName", SharedPreferenceUtil.getInstance().getString(AppConst.CITY, "", false));
                callback.callback(mJson.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
