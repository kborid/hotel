package com.huicheng.hotel.android.tools;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.requestbuilder.bean.CityAreaInfoBean;
import com.prj.sdk.app.AppConst;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;

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

    public static String getPlaneOffOnCity(String off, String on, String mid) {
        return getProvinceCityString(on, off, mid);
    }

    public static String getProvinceString(String province) {
        String tempStr = "";
        if (StringUtil.notEmpty(province)) {
            if (province.endsWith("省")) {
                province = province.replace("省", "");
            } else if (province.endsWith("市")) {
                province = province.replace("市", "");
            }
            tempStr += province;
        }
        return tempStr;
    }

    public static String getCityString(String city) {
        String tempStr = "";
        if (StringUtil.notEmpty(city)) {
            if (city.endsWith("市")) {
                city = city.replace("市", "");
            } else if (city.endsWith("区")) {
                city = city.replace("区", "");
            }
            tempStr += city;
        }
        return tempStr;
    }

    public static String getSiteIdString(String siteId) {
        StringBuilder sb = new StringBuilder(siteId);
        return sb.replace(4, 6, "00").toString();
    }

    private static String parseJsonFileAssets(Context context) {
        LogUtil.i(TAG, "parseJsonFileAssets()");
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
            String finalRet = "";
            JSONObject mJsonObject = JSONObject.parseObject(result.toString());
            if (mJsonObject != null && mJsonObject.containsKey("citylist")) {
                finalRet = mJsonObject.getString("citylist");
            }
            SharedPreferenceUtil.getInstance().setString(AppConst.CITY_HOTEL_JSON_FILE, finalRet, false);
            return finalRet;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void initAreaJsonData(Context context) {
        LogUtil.i(TAG, "initJsonData() begin....");
        String cityListJsonStr = SharedPreferenceUtil.getInstance().getString(AppConst.CITY_HOTEL_JSON_FILE, "", false);
        boolean isFocusParse = false;
        if (StringUtil.isEmpty(cityListJsonStr)) {
            isFocusParse = true;
            cityListJsonStr = parseJsonFileAssets(context);
        }
        LogUtil.i(TAG, "cityListJsonStr = " + cityListJsonStr);

        String cityMapJsonStr = SharedPreferenceUtil.getInstance().getString(AppConst.CITY_HOTEL_JSON, "", false);
        if (StringUtil.notEmpty(cityListJsonStr) && (isFocusParse || StringUtil.isEmpty(cityMapJsonStr))) {
            List<CityAreaInfoBean> cityAreaList = JSON.parseArray(cityListJsonStr, CityAreaInfoBean.class);
            if (cityAreaList.size() > 0) {
                Map<String, List<CityAreaInfoBean>> cityAreaInfoBeanMap = new HashMap<>();
                for (int i = 0; i < cityAreaList.size(); i++) {
                    for (int j = 0; j < cityAreaList.get(i).list.size(); j++) {
                        String shortName = cityAreaList.get(i).list.get(j).shortName;
                        String str = SessionContext.getFirstSpellChat(shortName).toUpperCase(); //转大写
                        List<CityAreaInfoBean> tmp;
                        if (!cityAreaInfoBeanMap.containsKey(str)) {
                            tmp = new ArrayList<>();
                        } else {
                            tmp = cityAreaInfoBeanMap.get(str);
                        }
                        tmp.add(cityAreaList.get(i).list.get(j));
                        cityAreaInfoBeanMap.put(str, tmp);
                    }
                }
                cityMapJsonStr = new Gson().toJson(cityAreaInfoBeanMap);
                SharedPreferenceUtil.getInstance().setString(AppConst.CITY_HOTEL_JSON, cityMapJsonStr, false);
            }
        }
        LogUtil.i(TAG, "cityMapJsonStr = " + cityMapJsonStr);
        LogUtil.i(TAG, "initJsonData() end....");
    }
}
