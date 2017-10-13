package com.huicheng.hotel.android.tools;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huicheng.hotel.android.common.SessionContext;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.widget.wheel.adapters.CityAreaInfoBean;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @auth kborid
 * @date 2017/8/22.
 */

public class CityParseUtils {

    private static final String TAG = "CityParseUtils";

    public static String getProvinceCityString(String province, String city, String mid) {
        String tempStr = "";
        if (StringUtil.notEmpty(city)) {
            if (city.endsWith("市")) {
                city = city.replace("市", "");
            } else if (city.endsWith("区")) {
                city = city.replace("区", "");
            }
            tempStr += city;
        }
        if (StringUtil.notEmpty(province)) {
            if (!province.contains(city)) {
                if (province.endsWith("省")) {
                    province = province.replace("省", "");
                } else if (province.endsWith("市")) {
                    province = province.replace("市", "");
                }
                tempStr += mid;
                tempStr += province;
            }
        }
        return tempStr;
    }

    public static void initAreaJsonData(Context context) {
        LogUtil.i(TAG, "initJsonData() begin....");
        try {
            InputStreamReader inputReader = new InputStreamReader(context.getResources().getAssets().open("area.json"));
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line = "";
            StringBuilder result = new StringBuilder();
            while ((line = bufReader.readLine()) != null) {
                result.append(line);
            }
            inputReader.close();
            bufReader.close();
            JSONObject mJsonObject = JSONObject.parseObject(result.toString());
            if (mJsonObject != null && mJsonObject.containsKey("citylist")) {
                List<CityAreaInfoBean> temp = JSON.parseArray(mJsonObject.getString("citylist"), CityAreaInfoBean.class);
                if (temp != null && temp.size() > 0) {
                    SessionContext.setCityAreaList(temp);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, List<CityAreaInfoBean>> cityAreaInfoBeanMap = new HashMap<>();

        for (int i = 0; i < SessionContext.getCityAreaList().size(); i++) {
            for (int j = 0; j < SessionContext.getCityAreaList().get(i).list.size(); j++) {
                String shortName = SessionContext.getCityAreaList().get(i).list.get(j).shortName;
                String str = SessionContext.getFirstSpellChat(shortName).toUpperCase(); //转大写
                List<CityAreaInfoBean> tmp;
                if (!cityAreaInfoBeanMap.containsKey(str)) {
                    tmp = new ArrayList<>();
                } else {
                    tmp = cityAreaInfoBeanMap.get(str);
                }
                tmp.add(SessionContext.getCityAreaList().get(i).list.get(j));
                cityAreaInfoBeanMap.put(str, tmp);
            }
        }
        SessionContext.setCityAreaMap(cityAreaInfoBeanMap);
        LogUtil.i(TAG, "initJsonData() end....");
    }
}
