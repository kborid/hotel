package com.huicheng.hotel.android.ui.custom;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.WeatherCommDef;
import com.huicheng.hotel.android.control.LocationInfo;
import com.huicheng.hotel.android.requestbuilder.bean.WeatherInfoBean;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.Utils;

import java.util.Date;

/**
 * @auth kborid
 * @date 2017/10/23 0023.
 */

public class CustomWeatherLayout extends RelativeLayout {
    private Context context;

    private RelativeLayout weather_content_lay;
    private ImageView iv_weather_bg;

    private RelativeLayout weather_info_lay;
    private ImageView iv_xc;
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

    private void showLayoutAnim(final View v, boolean showAnim) {
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

    private void hideLayoutAnim(final View v, boolean showAnim) {
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
        View view = LayoutInflater.from(context).inflate(R.layout.layout_weather_banner, this);
        LinearLayout.LayoutParams rootRlp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rootRlp.width = Utils.mScreenWidth;
        rootRlp.height = (int) ((float) rootRlp.width / 750 * 400);
        view.setLayoutParams(rootRlp);
        weather_content_lay = (RelativeLayout) findViewById(R.id.weather_content_lay);
        weather_content_lay.setPadding(0, Utils.mStatusBarHeight, 0, 0);
        iv_weather_bg = (ImageView) findViewById(R.id.iv_weather_bg);
        iv_xc = (ImageView) findViewById(R.id.iv_xc);
        weather_info_lay = (RelativeLayout) findViewById(R.id.weather_info_lay);
        tv_temp = (TextView) findViewById(R.id.tv_temp);
        tv_weather = (TextView) findViewById(R.id.tv_weather);
        tv_loc = (TextView) findViewById(R.id.tv_loc);
        iv_weather = (ImageView) findViewById(R.id.iv_weather);
        tv_date = (TextView) findViewById(R.id.tv_date);
    }

    public void refreshWeatherInfo(long timeStamp, WeatherInfoBean bean) {
        if (null != bean) {
            hideLayoutAnim(weather_info_lay, false);
            hideLayoutAnim(iv_xc, false);
            tv_temp.setText(String.format(context.getString(R.string.homeTemperatureStr), bean.day_air_temperature) + "~" + String.format(context.getString(R.string.homeTemperatureStr), bean.night_air_temperature));
            int weatherBgId, weatherIconId;
            String weather;
            if (new Date(bean.systemTime).getHours() >= 18) {
                weather = bean.night_weather;
                weatherBgId = WeatherCommDef.WEATHER_NIGHT_CODES.get(bean.night_weather_code)[0];
                weatherIconId = WeatherCommDef.WEATHER_NIGHT_CODES.get(bean.night_weather_code)[1];
            } else {
                weather = bean.day_weather;
                weatherBgId = WeatherCommDef.WEATHER_DAY_CODES.get(bean.day_weather_code)[0];
                weatherIconId = WeatherCommDef.WEATHER_DAY_CODES.get(bean.day_weather_code)[1];
            }

            tv_weather.setText(weather);
            tv_loc.setText(LocationInfo.instance.getCity());
            tv_date.setText(DateUtil.getDay("MM月dd日", timeStamp));
            iv_weather.setImageResource(weatherIconId);
            iv_weather_bg.setImageResource(weatherBgId);
            showLayoutAnim(weather_info_lay, true);
        } else {
            iv_weather_bg.setImageResource(R.drawable.bg_weather_sun);
            hideLayoutAnim(weather_info_lay, false);
            if (!iv_xc.isShown()) {
                showLayoutAnim(iv_xc, true);
            }
        }
    }
}
