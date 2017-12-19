package com.huicheng.hotel.android.common;

import com.huicheng.hotel.android.R;

import java.util.HashMap;

/**
 * @auth kborid
 * @date 2017/12/19 0019.
 */

public class WeatherCommDef {
    public static final HashMap<String, Integer[]> WEATHER_DAY_CODES = new HashMap<String, Integer[]>() {
        {
            put("-1", new Integer[]{R.drawable.bg_weather_sun, R.drawable.iv_weather_icon_sun_day});
            //晴
            put("00", new Integer[]{R.drawable.bg_weather_sun, R.drawable.iv_weather_icon_sun_day});
            put("99", new Integer[]{R.drawable.bg_weather_sun, R.drawable.iv_weather_icon_sun_day});
            //多云
            put("01", new Integer[]{R.drawable.bg_weather_cloud, R.drawable.iv_weather_icon_cloud_01});
            put("02", new Integer[]{R.drawable.bg_weather_cloud, R.drawable.iv_weather_icon_cloud_01});
            put("18", new Integer[]{R.drawable.bg_weather_cloud, R.drawable.iv_weather_icon_cloud_04});
            put("29", new Integer[]{R.drawable.bg_weather_cloud, R.drawable.iv_weather_icon_cloud_02});
            put("53", new Integer[]{R.drawable.bg_weather_cloud, R.drawable.iv_weather_icon_cloud_03});
            //雨
            put("301", new Integer[]{R.drawable.bg_weather_rain, R.drawable.iv_weather_icon_rain_01});
            put("03", new Integer[]{R.drawable.bg_weather_rain, R.drawable.iv_weather_icon_rain_01});
            put("04", new Integer[]{R.drawable.bg_weather_rain, R.drawable.iv_weather_icon_rain_06});
            put("07", new Integer[]{R.drawable.bg_weather_rain, R.drawable.iv_weather_icon_rain_01});
            put("08", new Integer[]{R.drawable.bg_weather_rain, R.drawable.iv_weather_icon_rain_02});
            put("09", new Integer[]{R.drawable.bg_weather_rain, R.drawable.iv_weather_icon_rain_03});
            put("10", new Integer[]{R.drawable.bg_weather_rain, R.drawable.iv_weather_icon_rain_04});
            put("11", new Integer[]{R.drawable.bg_weather_rain, R.drawable.iv_weather_icon_rain_04});
            put("12", new Integer[]{R.drawable.bg_weather_rain, R.drawable.iv_weather_icon_rain_05});
            put("21", new Integer[]{R.drawable.bg_weather_rain, R.drawable.iv_weather_icon_rain_02});
            put("22", new Integer[]{R.drawable.bg_weather_rain, R.drawable.iv_weather_icon_rain_03});
            put("23", new Integer[]{R.drawable.bg_weather_rain, R.drawable.iv_weather_icon_rain_04});
            put("24", new Integer[]{R.drawable.bg_weather_rain, R.drawable.iv_weather_icon_rain_04});
            put("25", new Integer[]{R.drawable.bg_weather_rain, R.drawable.iv_weather_icon_rain_05});
            //雪
            put("302", new Integer[]{R.drawable.bg_weather_snow, R.drawable.iv_weather_icon_sun_day});
            put("05", new Integer[]{R.drawable.bg_weather_snow, R.drawable.iv_weather_icon_snow_05});
            put("06", new Integer[]{R.drawable.bg_weather_snow, R.drawable.iv_weather_icon_snow_01});
            put("13", new Integer[]{R.drawable.bg_weather_snow, R.drawable.iv_weather_icon_snow_02});
            put("14", new Integer[]{R.drawable.bg_weather_snow, R.drawable.iv_weather_icon_snow_02});
            put("15", new Integer[]{R.drawable.bg_weather_snow, R.drawable.iv_weather_icon_snow_03});
            put("16", new Integer[]{R.drawable.bg_weather_snow, R.drawable.iv_weather_icon_snow_04});
            put("17", new Integer[]{R.drawable.bg_weather_snow, R.drawable.iv_weather_icon_snow_04});
            put("19", new Integer[]{R.drawable.bg_weather_snow, R.drawable.iv_weather_icon_snow_06});
            put("26", new Integer[]{R.drawable.bg_weather_snow, R.drawable.iv_weather_icon_snow_03});
            put("27", new Integer[]{R.drawable.bg_weather_snow, R.drawable.iv_weather_icon_snow_04});
            put("28", new Integer[]{R.drawable.bg_weather_snow, R.drawable.iv_weather_icon_snow_04});
            //沙
            put("20", new Integer[]{R.drawable.bg_weather_wind, R.drawable.iv_weather_icon_sand});
            put("30", new Integer[]{R.drawable.bg_weather_wind, R.drawable.iv_weather_icon_sand});
            put("31", new Integer[]{R.drawable.bg_weather_wind, R.drawable.iv_weather_icon_sand});
        }
    };
    public static final HashMap<String, Integer[]> WEATHER_NIGHT_CODES = new HashMap<String, Integer[]>() {
        {
            put("-1", new Integer[]{R.drawable.bg_weather_sun, R.drawable.iv_weather_icon_sun_night});
            //晴
            put("00", new Integer[]{R.drawable.bg_weather_sun, R.drawable.iv_weather_icon_sun_night});
            put("99", new Integer[]{R.drawable.bg_weather_sun, R.drawable.iv_weather_icon_sun_night});
            //多云
            put("01", new Integer[]{R.drawable.bg_weather_cloud, R.drawable.iv_weather_icon_cloud_01});
            put("02", new Integer[]{R.drawable.bg_weather_cloud, R.drawable.iv_weather_icon_cloud_01});
            put("18", new Integer[]{R.drawable.bg_weather_cloud, R.drawable.iv_weather_icon_cloud_04});
            put("29", new Integer[]{R.drawable.bg_weather_cloud, R.drawable.iv_weather_icon_cloud_02});
            put("53", new Integer[]{R.drawable.bg_weather_cloud, R.drawable.iv_weather_icon_cloud_03});
            //雨
            put("301", new Integer[]{R.drawable.bg_weather_rain, R.drawable.iv_weather_icon_rain_01});
            put("03", new Integer[]{R.drawable.bg_weather_rain, R.drawable.iv_weather_icon_rain_01});
            put("04", new Integer[]{R.drawable.bg_weather_rain, R.drawable.iv_weather_icon_rain_06});
            put("07", new Integer[]{R.drawable.bg_weather_rain, R.drawable.iv_weather_icon_rain_01});
            put("08", new Integer[]{R.drawable.bg_weather_rain, R.drawable.iv_weather_icon_rain_02});
            put("09", new Integer[]{R.drawable.bg_weather_rain, R.drawable.iv_weather_icon_rain_03});
            put("10", new Integer[]{R.drawable.bg_weather_rain, R.drawable.iv_weather_icon_rain_04});
            put("11", new Integer[]{R.drawable.bg_weather_rain, R.drawable.iv_weather_icon_rain_04});
            put("12", new Integer[]{R.drawable.bg_weather_rain, R.drawable.iv_weather_icon_rain_05});
            put("21", new Integer[]{R.drawable.bg_weather_rain, R.drawable.iv_weather_icon_rain_02});
            put("22", new Integer[]{R.drawable.bg_weather_rain, R.drawable.iv_weather_icon_rain_03});
            put("23", new Integer[]{R.drawable.bg_weather_rain, R.drawable.iv_weather_icon_rain_04});
            put("24", new Integer[]{R.drawable.bg_weather_rain, R.drawable.iv_weather_icon_rain_04});
            put("25", new Integer[]{R.drawable.bg_weather_rain, R.drawable.iv_weather_icon_rain_05});
            //雪
            put("302", new Integer[]{R.drawable.bg_weather_snow, R.drawable.iv_weather_icon_sun_day});
            put("05", new Integer[]{R.drawable.bg_weather_snow, R.drawable.iv_weather_icon_snow_05});
            put("06", new Integer[]{R.drawable.bg_weather_snow, R.drawable.iv_weather_icon_snow_01});
            put("13", new Integer[]{R.drawable.bg_weather_snow, R.drawable.iv_weather_icon_snow_02});
            put("14", new Integer[]{R.drawable.bg_weather_snow, R.drawable.iv_weather_icon_snow_02});
            put("15", new Integer[]{R.drawable.bg_weather_snow, R.drawable.iv_weather_icon_snow_03});
            put("16", new Integer[]{R.drawable.bg_weather_snow, R.drawable.iv_weather_icon_snow_04});
            put("17", new Integer[]{R.drawable.bg_weather_snow, R.drawable.iv_weather_icon_snow_04});
            put("19", new Integer[]{R.drawable.bg_weather_snow, R.drawable.iv_weather_icon_snow_06});
            put("26", new Integer[]{R.drawable.bg_weather_snow, R.drawable.iv_weather_icon_snow_03});
            put("27", new Integer[]{R.drawable.bg_weather_snow, R.drawable.iv_weather_icon_snow_04});
            put("28", new Integer[]{R.drawable.bg_weather_snow, R.drawable.iv_weather_icon_snow_04});
            //沙
            put("20", new Integer[]{R.drawable.bg_weather_wind, R.drawable.iv_weather_icon_sand});
            put("30", new Integer[]{R.drawable.bg_weather_wind, R.drawable.iv_weather_icon_sand});
            put("31", new Integer[]{R.drawable.bg_weather_wind, R.drawable.iv_weather_icon_sand});
        }
    };
}
