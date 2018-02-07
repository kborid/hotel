package com.huicheng.hotel.android.tools;

import com.prj.sdk.util.StringUtil;

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
//            } else if (city.endsWith("区")) {
//                city = city.replace("区", "");
            }
            tempStr += city;
        }
        return tempStr;
    }

    public static String getSiteIdString(String siteId) {
        StringBuilder sb = new StringBuilder(siteId);
        return sb.replace(4, 6, "00").toString();
    }
}
