package com.huicheng.hotel.android.ui.custom;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.net.bean.WeatherInfoBean;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.SharedPreferenceUtil;

import java.util.HashMap;

/**
 * @auth kborid
 * @date 2017/10/23 0023.
 */

public class CustomWeatherLayout extends RelativeLayout {
    private Context context;

    public static final HashMap<String, Integer> WEATHER_CODES = new HashMap<String, Integer>() {
        {
            put("-1", -1);
            put("00", 0);
            put("99", 0);
            put("01", 1);
            put("02", 1);
            put("29", 1);
            put("30", 1);
            put("31", 1);
            put("53", 1);
            put("18", 1);
            put("03", 2);
            put("04", 2);
            put("05", 2);
            put("06", 2);
            put("07", 2);
            put("08", 2);
            put("09", 2);
            put("10", 2);
            put("11", 2);
            put("12", 2);
            put("19", 2);
            put("21", 2);
            put("22", 2);
            put("23", 2);
            put("24", 2);
            put("25", 2);
            put("301", 2);
            put("13", 3);
            put("14", 3);
            put("15", 3);
            put("16", 3);
            put("17", 3);
            put("20", 3);
            put("26", 3);
            put("27", 3);
            put("28", 3);
            put("302", 3);
        }
    };

    private ImageView iv_weather_bg;
    private TextView tv_temp;
    private TextView tv_weather;
    private TextView tv_loc;
    private ImageView iv_weather;
    private TextView tv_date;

    public CustomWeatherLayout(Context context) {
        this(context, null);
    }

    public CustomWeatherLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomWeatherLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        LayoutInflater.from(context).inflate(R.layout.layout_weather_banner, this);
        iv_weather_bg = (ImageView) findViewById(R.id.iv_weather_bg);
        tv_temp = (TextView) findViewById(R.id.tv_temp);
        tv_weather = (TextView) findViewById(R.id.tv_weather);
        tv_loc = (TextView) findViewById(R.id.tv_loc);
        iv_weather = (ImageView) findViewById(R.id.iv_weather);
        tv_date = (TextView) findViewById(R.id.tv_date);
    }

    public void refreshWeatherInfo(long timeStamp, WeatherInfoBean bean) {
        if (null != bean) {
            tv_temp.setText(String.format(context.getString(R.string.homeTemperatureStr), bean.day_air_temperature) + "-" + String.format(context.getString(R.string.homeTemperatureStr), bean.night_air_temperature));
            tv_weather.setText(bean.day_weather + "-" + bean.night_weather);
            tv_loc.setText(SharedPreferenceUtil.getInstance().getString(AppConst.CITY, "", false));
            tv_date.setText(DateUtil.getDay("MM月dd日", timeStamp));
            iv_weather.setImageResource(convertWeatherImage(bean.day_weather_code));
            iv_weather_bg.setImageResource(convertWeatherBg(bean.day_weather_code));
        } else {
            showDefaultWeather(timeStamp);
        }
    }

    public void showDefaultWeather(long timeStamp) {
        tv_temp.setText(String.format(context.getString(R.string.homeTemperatureStr), "--"));
        tv_weather.setText("晴");
        tv_loc.setText(SharedPreferenceUtil.getInstance().getString(AppConst.CITY, "", false));
        tv_date.setText(DateUtil.getDay("MM月dd日", timeStamp));
        iv_weather.setImageResource(convertWeatherImage("-1"));
        iv_weather_bg.setImageResource(convertWeatherBg("-1"));
    }

    private int convertWeatherBg(String code) {
        int bgId;
        switch (WEATHER_CODES.get(code)) {
            case 0:
                bgId = R.drawable.bg_sun;
                break;
            case 1:
                bgId = R.drawable.bg_cloud;
                break;
            case 2:
                bgId = R.drawable.bg_rain;
                break;
            case 3:
                bgId = R.drawable.bg_snow;
                break;
            default:
                bgId = R.drawable.bg_sun;
                break;
        }
        return bgId;
    }

    private int convertWeatherImage(String code) {
        int imageId;
        switch (WEATHER_CODES.get(code)) {
            case 0:
                imageId = R.drawable.iv_weather_sun;
                break;
            case 1:
                imageId = R.drawable.iv_weather_cloud;
                break;
            case 2:
                imageId = R.drawable.iv_weather_rain;
                break;
            case 3:
                imageId = R.drawable.iv_weather_snow;
                break;
            default:
                imageId = R.drawable.iv_weather_sun;
                break;
        }
        return imageId;
    }
}
