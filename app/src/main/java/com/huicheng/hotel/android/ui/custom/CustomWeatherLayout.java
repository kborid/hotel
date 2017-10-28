package com.huicheng.hotel.android.ui.custom;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.huicheng.hotel.android.R;

/**
 * @auth kborid
 * @date 2017/10/23 0023.
 */

public class CustomWeatherLayout extends RelativeLayout {
    private Context context;

    private RelativeLayout weather_lay;
    private TextView tv_temp;
    private TextView tv_weather;
    private TextView tv_city;
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
        weather_lay = (RelativeLayout) findViewById(R.id.weather_lay);
        tv_temp = (TextView) findViewById(R.id.tv_temp);
        tv_weather = (TextView) findViewById(R.id.tv_weather);
        tv_city = (TextView) findViewById(R.id.tv_city);
        iv_weather = (ImageView) findViewById(R.id.iv_weather);
        tv_date = (TextView) findViewById(R.id.tv_date);
    }

    public void refreshWeatherInfo() {

    }
}
