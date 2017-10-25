package com.huicheng.hotel.android.ui.custom;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.huicheng.hotel.android.R;

/**
 * @auth kborid
 * @date 2017/10/23 0023.
 */

public class CustomWeatherLayout extends LinearLayout {
    private Context context;

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
    }
}
