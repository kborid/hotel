package com.huicheng.hotel.android.ui.custom;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.net.bean.WeatherInfoBean;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.SharedPreferenceUtil;

import java.util.Date;
import java.util.HashMap;

/**
 * @auth kborid
 * @date 2017/10/23 0023.
 */

public class CustomWeatherLayout extends RelativeLayout {
    private Context context;

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

    private ImageView iv_weather_bg;
    private RelativeLayout weather_info_lay;
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

    private void showWeatherInfoLayout(final View v, boolean showAnim) {
        ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(v, "alpha", 0f, 1f);
        alphaAnim.setDuration(1000);
        alphaAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                v.setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        if (showAnim) {
            alphaAnim.start();
        } else {
            v.setVisibility(VISIBLE);
        }
    }

    private void hideWeatherInfoLayout(final View v, boolean showAnim) {
        ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(v, "alpha", 1f, 0f);
        alphaAnim.setDuration(500);
        alphaAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                v.setVisibility(GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        if (showAnim) {
            alphaAnim.start();
        } else {
            v.setVisibility(GONE);
        }
    }


    private void init() {
        LayoutInflater.from(context).inflate(R.layout.layout_weather_banner, this);
        iv_weather_bg = (ImageView) findViewById(R.id.iv_weather_bg);
        weather_info_lay = (RelativeLayout) findViewById(R.id.weather_info_lay);
        tv_temp = (TextView) findViewById(R.id.tv_temp);
        tv_weather = (TextView) findViewById(R.id.tv_weather);
        tv_loc = (TextView) findViewById(R.id.tv_loc);
        iv_weather = (ImageView) findViewById(R.id.iv_weather);
        tv_date = (TextView) findViewById(R.id.tv_date);
    }

    public void refreshWeatherInfo(long timeStamp, WeatherInfoBean bean) {
        if (null != bean) {
            hideWeatherInfoLayout(weather_info_lay, false);
            tv_temp.setText(String.format(context.getString(R.string.homeTemperatureStr), bean.day_air_temperature) + "~" + String.format(context.getString(R.string.homeTemperatureStr), bean.night_air_temperature));
            int weatherBgId, weatherIconId;
            String weather;
            if (new Date(bean.systemTime).getHours() >= 18) {
                weather = bean.night_weather;
                weatherBgId = WEATHER_NIGHT_CODES.get(bean.night_weather_code)[0];
                weatherIconId = WEATHER_NIGHT_CODES.get(bean.night_weather_code)[1];
            } else {
                weather = bean.day_weather;
                weatherBgId = WEATHER_DAY_CODES.get(bean.day_weather_code)[0];
                weatherIconId = WEATHER_DAY_CODES.get(bean.day_weather_code)[1];
            }

            tv_weather.setText(weather);
            tv_loc.setText(SharedPreferenceUtil.getInstance().getString(AppConst.CITY, "", false));
            tv_date.setText(DateUtil.getDay("MM月dd日", timeStamp));
            iv_weather.setImageResource(weatherIconId);
            iv_weather_bg.setImageResource(weatherBgId);
            showWeatherInfoLayout(weather_info_lay, true);
        } else {
            iv_weather_bg.setImageResource(R.drawable.test);
            hideWeatherInfoLayout(weather_info_lay, true);
        }
    }
}
