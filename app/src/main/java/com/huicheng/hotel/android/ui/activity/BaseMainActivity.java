package com.huicheng.hotel.android.ui.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.alibaba.fastjson.JSON;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.control.LocationInfo;
import com.huicheng.hotel.android.requestbuilder.RequestBeanBuilder;
import com.huicheng.hotel.android.requestbuilder.bean.HomeBannerInfoBean;
import com.huicheng.hotel.android.requestbuilder.bean.WeatherInfoBean;
import com.huicheng.hotel.android.ui.base.BaseAppActivity;
import com.huicheng.hotel.android.ui.custom.CommonBannerLayout;
import com.huicheng.hotel.android.content.AppConst;
import com.huicheng.hotel.android.content.NetURL;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.net.data.ResponseData;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;

import java.util.Calendar;
import java.util.List;

/**
 * @auth kborid
 * @date 2017/11/20 0020.
 */

public class BaseMainActivity extends BaseAppActivity {
    protected static final int REQUEST_CODE_DATE = 0x01;
    protected static final int REQUEST_CODE_CITY = 0x02;
    protected static final int REQUEST_CODE_AIRPORT = 0x03;

    protected static Handler myHandler = new Handler(Looper.getMainLooper());
    protected long beginTime, endTime;
    private CommonBannerLayout banner_lay;
    private LinearLayout contentLay;
    protected boolean isSelectedDate = false;

//    private int oldSkinIndex = 0;

    @Override
    protected void preOnCreate() {
        super.preOnCreate();
        initMainWindow();
    }

    @Override
    protected void setContentView() {
        setContentView(R.layout.act_basemain_layout);
    }

    @Override
    protected void initViews() {
        super.initViews();
//        oldSkinIndex = SharedPreferenceUtil.getInstance().getInt(AppConst.SKIN_INDEX, 0);
        banner_lay = (CommonBannerLayout) findViewById(R.id.banner_lay);
        contentLay = (LinearLayout) findViewById(R.id.content_lay);
    }

    @Override
    protected void requestData() {
        super.requestData();
        if (SessionContext.getBannerList().size() == 0) {
            requestHomeBannerInfo();
        }
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
        LinearLayout.LayoutParams weatherRlp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        weatherRlp.width = Utils.mScreenWidth;
        weatherRlp.height = (int) ((float) weatherRlp.width / 750 * 400);
        banner_lay.setLayoutParams(weatherRlp);
        if (SessionContext.getBannerList().size() > 0) {
            banner_lay.setImageResource(SessionContext.getBannerList());
        }
        banner_lay.setWeatherInfoLayoutMargin(Utils.dp2px(11), Utils.mStatusBarHeight + Utils.dp2px(10), 0, 0);

        //初始化时间，如果没有手动选择，则默认显示今天到明天 1晚
        updateBeginTimeEndTime();
    }

    protected void updateBeginTimeEndTime() {
        if (!isSelectedDate) {
            Calendar calendar = Calendar.getInstance();
            beginTime = calendar.getTime().getTime();
            calendar.add(Calendar.DAY_OF_MONTH, +1); //+1今天的时间加一天
            endTime = calendar.getTime().getTime();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        banner_lay.startBanner();
        //男性女性界面初始化
//        int newSkinIndex = SharedPreferenceUtil.getInstance().getInt(AppConst.SKIN_INDEX, 0);
//        if (oldSkinIndex != newSkinIndex) {
//            oldSkinIndex = newSkinIndex;
//            isReStarted = true;
//            recreate();
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        banner_lay.stopBanner();
    }

    private void requestHomeBannerInfo() {
        LogUtil.i(TAG, "requestHotelBanner()");
        RequestBeanBuilder b = RequestBeanBuilder.create(false);
        ResponseData d = b.syncRequest(b);
        d.path = NetURL.HOTEL_BANNER;
        d.flag = AppConst.HOTEL_BANNER;
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    public void requestWeatherInfo(long timeStamp) {
        RequestBeanBuilder b = RequestBeanBuilder.create(false);
        b.addBody("cityname", LocationInfo.instance.getCity());
        b.addBody("siteid", LocationInfo.instance.getCityCode());
        b.addBody("date", DateUtil.getDay("yyyyMMdd", timeStamp));

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
                    banner_lay.updateWeatherInfo(beginTime, bean);
                } else {
                    banner_lay.updateWeatherInfo(beginTime, null);
                }
            } else if (request.flag == AppConst.HOTEL_BANNER) {
                LogUtil.i(TAG, "json = " + response.body.toString());
                List<HomeBannerInfoBean> temp = JSON.parseArray(response.body.toString(), HomeBannerInfoBean.class);
                SessionContext.setBannerList(temp);
                banner_lay.setImageResource(temp);
            }
        }
    }

    @Override
    public void onNotifyError(ResponseData request, ResponseData response) {
        super.onNotifyError(request, response);
        if (request.flag == AppConst.WEATHER) {
            banner_lay.updateWeatherInfo(beginTime, null);
        }
    }
}
