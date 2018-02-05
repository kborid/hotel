package com.huicheng.hotel.android.ui.JSBridge.functions;

import com.huicheng.hotel.android.control.LocationInfo;
import com.huicheng.hotel.android.ui.JSBridge.WVJBWebViewClient;

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
                mJson.put("cityCode", LocationInfo.instance.getCityCode());
                mJson.put("cityName", LocationInfo.instance.getCity());
                callback.callback(mJson.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
