package com.huicheng.hotel.android.tools;

import android.content.Context;
import android.content.res.AssetManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.content.AppConst;
import com.huicheng.hotel.android.requestbuilder.bean.CityAreaInfoBean;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
        StringBuilder stringBuilder = new StringBuilder();
        try {
            AssetManager assetManager = context.getAssets();
            BufferedReader bf = new BufferedReader(new InputStreamReader(assetManager.open("area.json")));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        String finalRet = "";
        JSONObject mJsonObject = JSONObject.parseObject(stringBuilder.toString());
        if (mJsonObject != null && mJsonObject.containsKey("citylist")) {
            finalRet = mJsonObject.getString("citylist");
        }
        SharedPreferenceUtil.getInstance().setString(AppConst.CITY_HOTEL_JSON_FILE, finalRet, false);
        return finalRet;
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
        List<CityAreaInfoBean> tmpCityAreaList = JSON.parseArray(cityListJsonStr, CityAreaInfoBean.class);
        SessionContext.setCityAreaList(tmpCityAreaList);

        String cityMapJsonStr = SharedPreferenceUtil.getInstance().getString(AppConst.CITY_HOTEL_JSON, "", false);
        if (StringUtil.notEmpty(cityListJsonStr) && (isFocusParse || StringUtil.isEmpty(cityMapJsonStr))) {
            if (tmpCityAreaList.size() > 0) {
                HashMap<String, List<CityAreaInfoBean>> cityAreaMap = new HashMap<>();
                for (int i = 0; i < tmpCityAreaList.size(); i++) {
                    for (int j = 0; j < tmpCityAreaList.get(i).list.size(); j++) {
                        String shortName = tmpCityAreaList.get(i).list.get(j).shortName;
                        String str = SessionContext.getFirstSpellChat(shortName).toUpperCase(); //转大写
                        List<CityAreaInfoBean> tmp;
                        if (!cityAreaMap.containsKey(str)) {
                            tmp = new ArrayList<>();
                        } else {
                            tmp = cityAreaMap.get(str);
                        }
                        tmp.add(tmpCityAreaList.get(i).list.get(j));
                        cityAreaMap.put(str, tmp);
                    }
                }
                cityMapJsonStr = new Gson().toJson(cityAreaMap);
                SharedPreferenceUtil.getInstance().setString(AppConst.CITY_HOTEL_JSON, cityMapJsonStr, false);
            }
        }
        Type type = new TypeToken<HashMap<String, List<CityAreaInfoBean>>>() {
        }.getType();
        HashMap<String, List<CityAreaInfoBean>> cityAreaMap = JSON.parseObject(cityMapJsonStr, type);
        SessionContext.setCityAreaMap(cityAreaMap);
        LogUtil.i(TAG, "cityMapJsonStr = " + cityMapJsonStr);
        LogUtil.i(TAG, "initJsonData() end....");
    }
}
