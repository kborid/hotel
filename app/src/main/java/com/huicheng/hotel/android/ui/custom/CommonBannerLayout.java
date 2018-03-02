package com.huicheng.hotel.android.ui.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.support.v4.view.ViewPager;
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
import com.huicheng.hotel.android.requestbuilder.bean.HomeBannerInfoBean;
import com.huicheng.hotel.android.requestbuilder.bean.WeatherInfoBean;
import com.huicheng.hotel.android.ui.adapter.BannerImageAdapter;
import com.huicheng.hotel.android.ui.listener.CustomOnPageChangeListener;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author kborid
 * @date 2016/8/15
 */
public class CommonBannerLayout extends RelativeLayout {

    private final String TAG = getClass().getSimpleName();

    private static final int DELAY_TIME = 5000;
    private Context context;
    private ViewPager viewpager;
    private LinearLayout indicator_lay;
    private Handler myHandler = new Handler();
    private int positionIndex = 0;
    private CustomViewPagerScroller customViewPagerScroller;
    private List<HomeBannerInfoBean> mList = new ArrayList<>();
    private LinearLayout weather_layout;

    private int mIndicatorResId;

    public CommonBannerLayout(Context context) {
        this(context, null);
    }

    public CommonBannerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CommonBannerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.comm_banner_layout, this);
        TypedArray ta = context.obtainStyledAttributes(R.styleable.MyTheme);
        mIndicatorResId = ta.getResourceId(R.styleable.MyTheme_indicatorSel, R.drawable.indicator_sel);
        ta.recycle();

        findViews();
        initListener();
    }

    private void initListener() {
        viewpager.addOnPageChangeListener(new CustomOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                int newPosition = position % mList.size();
                if (null != indicator_lay && indicator_lay.getChildCount() > 1) {
                    indicator_lay.getChildAt(newPosition).setEnabled(true);
                    if (positionIndex != newPosition) {
                        indicator_lay.getChildAt(positionIndex).setEnabled(false);
                        positionIndex = newPosition;
                    }
                }
            }
        });
    }

    private void findViews() {
        viewpager = (ViewPager) findViewById(R.id.viewpager);
        customViewPagerScroller = new CustomViewPagerScroller(context);
        customViewPagerScroller.setScrollDuration(1500);
        customViewPagerScroller.initViewPagerScroll(viewpager);
        indicator_lay = (LinearLayout) findViewById(R.id.indicator_lay);
        weather_layout = (LinearLayout) findViewById(R.id.weather_layout);
    }

    public void setImageResource(List<HomeBannerInfoBean> list) {
        if (list != null && list.size() > 0) {
            mList.clear();
            mList.addAll(list);
            viewpager.setAdapter(new BannerImageAdapter(context, mList));
            initIndicatorLay(mList.size());
            //viewPager一个假的无限循环，初始位置是viewPager count的100倍
            viewpager.setCurrentItem(mList.size() * 100);
            viewpager.setOffscreenPageLimit(mList.size());
            startBanner();
        }
    }

    /**
     * 图片滚动线程
     */

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            viewpager.setCurrentItem(viewpager.getCurrentItem() + 1);
            myHandler.postDelayed(runnable, DELAY_TIME);
        }
    };

    public void startBanner() {
        myHandler.removeCallbacks(runnable);
        if (mList.size() > 1) {
            myHandler.postDelayed(runnable, DELAY_TIME);
        }
    }

    public void stopBanner() {
        myHandler.removeCallbacks(runnable);
    }

    private void initIndicatorLay(int count) {
        indicator_lay.removeAllViews();
        if (count > 1) {
            for (int i = 0; i < count; i++) {
                View view = new View(context);
                view.setBackgroundResource(mIndicatorResId);
                view.setEnabled(false);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Utils.dp2px(7), Utils.dp2px(7));
                if (i > 0) {
                    lp.leftMargin = Utils.dp2px(7);
                }
                view.setLayoutParams(lp);
                indicator_lay.addView(view);
            }
            indicator_lay.getChildAt(positionIndex).setEnabled(true);
        }
    }

    public void setWeatherInfoLayoutMargin(int left, int top, int right, int bottom) {
        if (null != weather_layout) {
            RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rlp.setMargins(left, top, right, bottom);
            weather_layout.setLayoutParams(rlp);
        }
    }

    public void setIndicatorLayoutMarginBottom(int bottom) {
        if (null != indicator_lay) {
            LinearLayout.LayoutParams llp = (LinearLayout.LayoutParams) indicator_lay.getLayoutParams();
            llp.bottomMargin = bottom;
            indicator_lay.setLayoutParams(llp);
        }
    }

    public void updateWeatherInfo(long timeStamp, WeatherInfoBean bean) {
        if (null != bean) {
            weather_layout.setVisibility(VISIBLE);
            ImageView iv_weather_icon = (ImageView) weather_layout.findViewById(R.id.iv_weather_icon);
            TextView tv_weather_info = (TextView) weather_layout.findViewById(R.id.tv_weather_info);
            TextView tv_weather_temp = (TextView) weather_layout.findViewById(R.id.tv_weather_temp);
            TextView tv_weather_date = (TextView) weather_layout.findViewById(R.id.tv_weather_date);
            int weatherIconId, weatherBgId;
            String weatherTemp, weatherInfo;
            if (new Date(bean.systemTime).getHours() >= 18) {
                weatherInfo = bean.night_weather;
                weatherTemp = bean.night_air_temperature;
                weatherBgId = WeatherCommDef.WEATHER_NIGHT_CODES.get(bean.night_weather_code)[0];
                weatherIconId = WeatherCommDef.WEATHER_NIGHT_CODES.get(bean.night_weather_code)[1];
            } else {
                weatherInfo = bean.day_weather;
                weatherTemp = bean.day_air_temperature;
                weatherBgId = WeatherCommDef.WEATHER_DAY_CODES.get(bean.day_weather_code)[0];
                weatherIconId = WeatherCommDef.WEATHER_DAY_CODES.get(bean.day_weather_code)[1];
            }
            tv_weather_temp.setText(String.format(context.getString(R.string.homeTemperatureStr), weatherTemp));
            tv_weather_info.setText(weatherInfo);
//            tv_loc.setText(SharedPreferenceUtil.getInstance().getString(AppConst.CITY, "", false));
            tv_weather_date.setText(DateUtil.getDay("yyyy.MM.dd", timeStamp));
            iv_weather_icon.setImageResource(weatherIconId);
//            iv_weather_bg.setImageResource(weatherBgId);
        } else {
            weather_layout.setVisibility(GONE);
        }
    }
}
