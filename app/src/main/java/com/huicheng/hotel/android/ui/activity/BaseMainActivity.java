package com.huicheng.hotel.android.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.alibaba.fastjson.JSON;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.NetURL;
import com.huicheng.hotel.android.net.RequestBeanBuilder;
import com.huicheng.hotel.android.net.bean.WeatherInfoBean;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.huicheng.hotel.android.ui.custom.CustomWeatherLayout;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;

import java.util.Calendar;

/**
 * @auth kborid
 * @date 2017/11/20 0020.
 */

public class BaseMainActivity extends BaseActivity {
    protected static Handler myHandler = new Handler(Looper.getMainLooper());
    protected long beginTime, endTime;
    protected View rootView;
    private CustomWeatherLayout customWeatherLay;
    private LinearLayout contentLay;

//    private int oldSkinIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initMainWindow();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_basemain_layout);
//        oldSkinIndex = SharedPreferenceUtil.getInstance().getInt(AppConst.SKIN_INDEX, 0);
        customWeatherLay = (CustomWeatherLayout) findViewById(R.id.weather_lay);
        contentLay = (LinearLayout) findViewById(R.id.content_lay);
    }

    protected void initContentLayout(View view) {
        contentLay.removeAllViews();
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        contentLay.addView(view, llp);
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        LogUtil.i(TAG, "onNewIntent()");
        // Note that getIntent() still returns the original Intent. You can use setIntent(Intent) to update it to this new Intent.
        setIntent(intent);
        dealIntent();
        isReStarted = true;
    }

    @Override
    public void initParams() {
        super.initParams();
        findViewById(R.id.comm_title_rl).setPadding(0, Utils.mStatusBarHeight, 0, 0);
        setBackButtonResource(R.drawable.iv_back_white);
        //初始化时间，今天到明天 1晚
        Calendar calendar = Calendar.getInstance();
        beginTime = calendar.getTime().getTime();
        calendar.add(Calendar.DAY_OF_MONTH, +1); //+1今天的时间加一天
        endTime = calendar.getTime().getTime();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //男性女性界面初始化
//        int newSkinIndex = SharedPreferenceUtil.getInstance().getInt(AppConst.SKIN_INDEX, 0);
//        if (oldSkinIndex != newSkinIndex) {
//            oldSkinIndex = newSkinIndex;
//            isReStarted = true;
//            recreate();
//        }
    }

    public void requestWeatherInfo(long timeStamp) {
        RequestBeanBuilder b = RequestBeanBuilder.create(false);
        b.addBody("cityname", SharedPreferenceUtil.getInstance().getString(AppConst.CITY, "", false));
        b.addBody("date", DateUtil.getDay("yyyyMMdd", timeStamp));
        b.addBody("siteid", SharedPreferenceUtil.getInstance().getString(AppConst.SITEID, "", false));

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.WEATHER;
        d.flag = AppConst.WEATHER;

        requestID = DataLoader.getInstance().loadData(this, d);
    }

    @Override
    public void onNotifyMessage(ResponseData request, ResponseData response) {
        super.onNotifyMessage(request, response);
        removeProgressDialog();
        if (response != null && response.body != null) {
            if (request.flag == AppConst.WEATHER) {
                LogUtil.i(TAG, "json = " + response.body.toString());
                if (StringUtil.notEmpty(response.body.toString()) && !"{}".equals(response.body.toString())) {
                    WeatherInfoBean bean = JSON.parseObject(response.body.toString(), WeatherInfoBean.class);
                    customWeatherLay.refreshWeatherInfo(beginTime, bean);
                } else {
                    customWeatherLay.refreshWeatherInfo(beginTime, null);
                }
            }
        }
    }

    @Override
    public void onNotifyError(ResponseData request) {
        super.onNotifyError(request);
        removeProgressDialog();
        if (request.flag == AppConst.WEATHER) {
            customWeatherLay.refreshWeatherInfo(beginTime, null);
        }
    }
}
