package com.huicheng.hotel.android.tools;

import com.prj.sdk.util.StringUtil;

/**
 * @auth kborid
 * @date 2017/8/22.
 */

public class CityStringUtils {
    public static String getProvinceCityString(String province, String city, String mid) {
        String tempStr = "";
        if (StringUtil.notEmpty(city)) {
            if (city.contains("市")) {
                city = city.replace("市", "");
            }
            tempStr += city;
        }
        if (StringUtil.notEmpty(province)) {
            if (!province.equals(city)) {
                if (province.contains("省")) {
                    province = province.replace("省", "");
                }
                tempStr += mid;
                tempStr += province;
            }
        }
        return tempStr;
    }
}
