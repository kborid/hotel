package com.huicheng.hotel.android.ui.activity.hotel;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.widget.DrawerLayout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.amap.api.location.AMapLocation;
import com.huicheng.hotel.android.BuildConfig;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.HotelOrderManager;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.common.ShareTypeDef;
import com.huicheng.hotel.android.content.AppConst;
import com.huicheng.hotel.android.content.NetURL;
import com.huicheng.hotel.android.control.AMapLocationControl;
import com.huicheng.hotel.android.control.DataCleanManager;
import com.huicheng.hotel.android.requestbuilder.RequestBeanBuilder;
import com.huicheng.hotel.android.requestbuilder.bean.AppInfoBean;
import com.huicheng.hotel.android.requestbuilder.bean.HomeBannerInfoBean;
import com.huicheng.hotel.android.requestbuilder.bean.WeatherInfoBean;
import com.huicheng.hotel.android.tools.CityParseUtils;
import com.huicheng.hotel.android.ui.activity.UcBountyMainActivity;
import com.huicheng.hotel.android.ui.activity.UcCouponsActivity;
import com.huicheng.hotel.android.ui.activity.UcOrdersActivity;
import com.huicheng.hotel.android.ui.base.BaseAppActivity;
import com.huicheng.hotel.android.ui.custom.CommonBannerLayout;
import com.huicheng.hotel.android.ui.custom.CustomConsiderLayoutForHome;
import com.huicheng.hotel.android.ui.custom.LeftDrawerLayout;
import com.huicheng.hotel.android.ui.dialog.CustomDialog;
import com.huicheng.hotel.android.ui.dialog.CustomToast;
import com.prj.sdk.constants.BroadCastConst;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.net.data.ResponseData;
import com.prj.sdk.net.down.DownCallback;
import com.prj.sdk.net.down.DownLoaderTask;
import com.prj.sdk.util.ActivityTack;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class HotelMainActivity extends BaseAppActivity implements LeftDrawerLayout.OnLeftDrawerListener, AMapLocationControl.MyLocationListener {

    private static final int REQUEST_CODE_CITY = 0x01;
    private static final int REQUEST_CODE_DATE = 0x02;
    private static final int REQUEST_CODE_QMH = 0x03;

    private String mProvince, mCity, mSiteId;

    private static Handler myHandler = new Handler(Looper.getMainLooper());
    private AppInfoBean mAppInfoBean = null;

    private DrawerLayout drawer_layout;
    private LeftDrawerLayout left_layout;
    private CommonBannerLayout banner_lay;

    private RelativeLayout blur_lay;
    private ImageView iv_blur;
    private ImageView iv_logo_vertical;
    private ImageView iv_left;

    private RelativeLayout user_lay;
    private ImageView iv_user;
    private LinearLayout coupon_lay;
    private LinearLayout order_lay;
    private LinearLayout bounty_lay;
    private TextView tv_city;
    private TextView tv_next_search;
    private TextView tv_in_date, tv_days, tv_out_date;
    private TextView tv_search;
    private TextView tv_consider;

    private long exitTime = 0;
    private boolean isSelectedDate = false;
    private long beginTime, endTime;
    private boolean isNeedCloseLeftDrawer = false;
    private int oldSkinIndex = 0;

    private CustomConsiderLayoutForHome mConsiderLayout = null;
    private PopupWindow mConsiderPopupWindow = null;
    private int typeIndex = -1, gradeIndex = -1, priceIndex = -1;

    // 海南诚信广告Popup
    private boolean isAdShowed = false;
    private PopupWindow mAdHaiNanPopupWindow = null;

    private boolean isMyLoc = false;
    private TextView tv_curr_position;
    private AMapLocation aMapLocation = null;

    @Override
    protected void preOnCreate() {
        super.preOnCreate();
        initMainWindow();
    }

    @Override
    protected void requestData() {
        super.requestData();
        if (SessionContext.getBannerList().size() == 0) {
            requestMainBannerInfo();
        }
    }

    @Override
    protected void setContentView() {
        setContentView(R.layout.act_hotel_main);
    }

    public void initViews() {
        super.initViews();
        drawer_layout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (null != drawer_layout) {
            drawer_layout.setScrimColor(getResources().getColor(R.color.transparent50));
        }
        left_layout = (LeftDrawerLayout) findViewById(R.id.left_layout);
        banner_lay = (CommonBannerLayout) findViewById(R.id.banner_lay);

        blur_lay = (RelativeLayout) findViewById(R.id.blur_lay);
        iv_blur = (ImageView) findViewById(R.id.iv_blur);
        iv_logo_vertical = (ImageView) findViewById(R.id.iv_logo_vertical);
        iv_left = (ImageView) findViewById(R.id.iv_left);

        user_lay = (RelativeLayout) findViewById(R.id.user_lay);
        iv_user = (ImageView) findViewById(R.id.iv_user);
        tv_city = (TextView) findViewById(R.id.tv_city);
        tv_next_search = (TextView) findViewById(R.id.tv_next_search);

        coupon_lay = (LinearLayout) findViewById(R.id.coupon_lay);
        order_lay = (LinearLayout) findViewById(R.id.order_lay);
        bounty_lay = (LinearLayout) findViewById(R.id.bounty_lay);

        tv_in_date = (TextView) findViewById(R.id.tv_in_date);
        tv_days = (TextView) findViewById(R.id.tv_days);
        tv_out_date = (TextView) findViewById(R.id.tv_out_date);

        tv_search = (TextView) findViewById(R.id.tv_search);
        tv_consider = (TextView) findViewById(R.id.tv_consider);

        //首页筛选菜单
        mConsiderLayout = new CustomConsiderLayoutForHome(this);
        mConsiderLayout.initConsiderConfig();
        //评分筛选条件初始化
        SharedPreferenceUtil.getInstance().setInt(AppConst.CONSIDER_POINT, -1);
        mConsiderLayout.setOnConsiderLayoutListener(new CustomConsiderLayoutForHome.OnConsiderLayoutListener() {
            @Override
            public void onDismiss() {
                if (null != mConsiderPopupWindow) {
                    mConsiderPopupWindow.dismiss();
                }
            }

            @Override
            public void onResult(String str, int type, int grade, int price) {
                tv_consider.setText(str);
                typeIndex = type;
                gradeIndex = grade;
                priceIndex = price;
            }
        });
        mConsiderPopupWindow = new PopupWindow(mConsiderLayout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        mConsiderPopupWindow.setAnimationStyle(R.style.share_anim);
        mConsiderPopupWindow.setFocusable(true);
        mConsiderPopupWindow.setOutsideTouchable(true);
        mConsiderPopupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        mConsiderPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                //重置consider
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1f;
                getWindow().setAttributes(lp);
            }
        });

        //海南诚信认证广告
        View adView = LayoutInflater.from(this).inflate(R.layout.pw_ad_hainan_layout, null);
        mAdHaiNanPopupWindow = new PopupWindow(adView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        mAdHaiNanPopupWindow.getContentView().findViewById(R.id.iv_ad_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdHaiNanPopupWindow.dismiss();
            }
        });
        mAdHaiNanPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                //重置consider
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1f;
                getWindow().setAttributes(lp);
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int option = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
            mAdHaiNanPopupWindow.setSoftInputMode(option);
            try {
                Field mLayoutInScreen = PopupWindow.class.getDeclaredField("mLayoutInScreen");
                mLayoutInScreen.setAccessible(true);
                mLayoutInScreen.set(mAdHaiNanPopupWindow, true);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        tv_curr_position = (TextView) findViewById(R.id.tv_curr_position);
    }

    @Override
    public void initParams() {
        super.initParams();
        RelativeLayout.LayoutParams ucRlp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ucRlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        user_lay.setPadding(Utils.dp2px(20), Utils.mStatusBarHeight + Utils.dp2px(10), Utils.dp2px(20), Utils.dp2px(10));
        user_lay.setLayoutParams(ucRlp);

        RelativeLayout.LayoutParams weatherRlp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        weatherRlp.width = Utils.mScreenWidth;
        weatherRlp.height = (int) ((float) weatherRlp.width / 750 * 400);
        banner_lay.setLayoutParams(weatherRlp);
        if (SessionContext.getBannerList().size() > 0) {
            banner_lay.setImageResource(SessionContext.getBannerList());
        }
        banner_lay.setWeatherInfoLayoutMargin(Utils.dp2px(11), Utils.mStatusBarHeight + Utils.dp2px(5), 0, 0);

        //初始化时间，今天到明天 1晚
        initCurrentTodayTime();
        tv_in_date.setText(formatDateForBigDay(DateUtil.getDay("M月d日 ", beginTime) + DateUtil.dateToWeek2(new Date(beginTime))));
        tv_out_date.setText(formatDateForBigDay(DateUtil.getDay("M月d日 ", endTime) + DateUtil.dateToWeek2(new Date(endTime))));
        tv_days.setText(String.format(getString(R.string.duringNightStr), DateUtil.getGapCount(new Date(beginTime), new Date(endTime))));

        //地点信息
        tv_city.setHint("正在定位当前城市");
        AMapLocationControl.getInstance().startLocationOnce(this);

        //更新用户中心
        left_layout.updateUserInfo();
        oldSkinIndex = SharedPreferenceUtil.getInstance().getInt(AppConst.SKIN_INDEX, 0);

        //app更新提示
        String appInfo = SharedPreferenceUtil.getInstance().getString(AppConst.APPINFO, "", false);
        if (StringUtil.notEmpty(appInfo)) {
            mAppInfoBean = JSON.parseObject(appInfo, AppInfoBean.class);
            myHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    String ignoreVersion = SharedPreferenceUtil.getInstance().getString(AppConst.IGNORE_UPDATE_VERSION, "", false);
                    String versionSer = mAppInfoBean.upid.split("（")[0];
                    String versionLoc = BuildConfig.VERSION_NAME.split("（")[0];
                    if ((1 <= SessionContext.VersionComparison(versionSer, versionLoc)) && !mAppInfoBean.upid.equals(ignoreVersion)) {
                        if (SessionContext.getOpenInstallAppData() == null) {
                            showUpdateDialog(mAppInfoBean);
                        }
                    }
                }
            }, 500);
        }
    }

    private void requestWeatherInfo(long timeStamp) {
        LogUtil.i(TAG, "requestWeatherInfo() timeStamp = " + timeStamp);
        RequestBeanBuilder b = RequestBeanBuilder.create(false);
        b.addBody("cityname", mCity);
        b.addBody("date", DateUtil.getDay("yyyyMMdd", timeStamp));
        b.addBody("siteid", mSiteId);
        ResponseData d = b.syncRequest(b);
        d.path = NetURL.WEATHER;
        d.flag = AppConst.WEATHER;
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void requestMainBannerInfo() {
        LogUtil.i(TAG, "requestHotelBanner()");
        RequestBeanBuilder b = RequestBeanBuilder.create(false);
        ResponseData d = b.syncRequest(b);
        d.path = NetURL.HOTEL_BANNER;
        d.flag = AppConst.HOTEL_BANNER;
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void showUpdateDialog(final AppInfoBean bean) {
        final CustomDialog dialog = new CustomDialog(this);
        String title = getString(R.string.update_tips);
        if (StringUtil.notEmpty(bean.tip)) {
            title = bean.tip;
        }
        dialog.setTitle(title);
        dialog.setMessage(bean.updesc);
        dialog.setNegativeButton(getString(R.string.update_not), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferenceUtil.getInstance().setString(AppConst.IGNORE_UPDATE_VERSION, bean.upid, false);
            }
        });
        dialog.setPositiveButton(getString(R.string.update_todo), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final CustomDialog pd = new CustomDialog(HotelMainActivity.this);
                pd.setTitle(R.string.download_ing);
                View view = LayoutInflater.from(HotelMainActivity.this).inflate(R.layout.progress_download_layout, null);
                final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
                final TextView tv_percent = (TextView) view.findViewById(R.id.tv_percent);
                final TextView tv_size = (TextView) view.findViewById(R.id.tv_size);
                pd.addView(view);
                pd.setCancelable(false);
                pd.setCanceledOnTouchOutside(false);
                pd.show();

                final String[] mFilePath = {""};
                DownLoaderTask task = new DownLoaderTask(bean.apkurls, "update.apk", true, new DownCallback() {
                    @Override
                    public void beginDownload(String url, String local, String fileName, int status) {
                        mFilePath[0] = local;
                        tv_percent.setText(String.format(getString(R.string.download_ing_percent), 0));
                        tv_size.setText(String.format(getString(R.string.download_ing_size), "0", "0"));
                        progressBar.setProgress(0);
                        progressBar.setMax(0);
                    }

                    @Override
                    public void downloading(int status, int progress, int maxLength) {
                        int percent = (int) ((float) progress * 100 / maxLength);
                        tv_percent.setText(String.format(getString(R.string.download_ing_percent), percent));
                        tv_size.setText(String.format(getString(R.string.download_ing_size), DataCleanManager.getFormatSize(progress), DataCleanManager.getFormatSize(maxLength)));
                        progressBar.setProgress(progress);
                        progressBar.setMax(maxLength);
                    }

                    @Override
                    public void finishDownload(int status) {
                        pd.dismiss();
                        if (status == DownLoaderTask.DOWNLOAD_SUCCESS) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(Uri.fromFile(new File(mFilePath[0])),
                                    "application/vnd.android.package-archive");
                            startActivity(intent);
                        } else {
                            CustomToast.show(getString(R.string.download_fail_tips), CustomToast.LENGTH_SHORT);
                        }
                    }
                });
                task.execute();
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        mProvince = SharedPreferenceUtil.getInstance().getString(AppConst.PROVINCE, "", false);
        mCity = SharedPreferenceUtil.getInstance().getString(AppConst.CITY, "", false);
        mSiteId = SharedPreferenceUtil.getInstance().getString(AppConst.SITEID, "", false);

        banner_lay.startBanner();
        //重置consider
        mConsiderLayout.reloadConsiderConfig(typeIndex, gradeIndex, priceIndex);
        //男性女性界面初始化
//        int newSkinIndex = SharedPreferenceUtil.getInstance().getInt(AppConst.SKIN_INDEX, 0);
//        if (oldSkinIndex != newSkinIndex) {
//            oldSkinIndex = newSkinIndex;
//            isReStarted = true;
//            recreate();
//        }

        //第每次启动时，如果用户未登录，则显示侧滑
        if (SessionContext.isFirstLaunchDoAction(getClass().getSimpleName()) &&
                !SessionContext.isLogin()) {
//            myHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    drawer_layout.openDrawer(left_layout, true);
//                    sendBroadcast(new Intent(BroadCastConst.UNLOGIN_ACTION));
//                }
//            }, 450);
            sendBroadcast(new Intent(BroadCastConst.UNLOGIN_ACTION));
        }

        if (SessionContext.isLogin()) {
            requestMessageCount();
            requestUserMenusStatus();
        }

        if (isNeedCloseLeftDrawer && drawer_layout.isDrawerOpen(left_layout)) {
            isNeedCloseLeftDrawer = false;
            drawer_layout.closeDrawers();
        }
        //OpenInstall Event 分发
        dispatchOpenInstallEvent();
    }

    private void dispatchOpenInstallEvent() {
        LogUtil.i(TAG, "dispatchOpenInstallEvent()");
        if (SessionContext.getOpenInstallAppData() != null) {
            JSONObject mJson = JSON.parseObject(SessionContext.getOpenInstallAppData().getData());
            if (null != mJson && mJson.containsKey("channel")) {
                String channel = mJson.getString("channel");
                if (ShareTypeDef.SHARE_HOTEL.equals(channel)) {
                    long beginDate = Long.valueOf(mJson.getString("beginDate"));
                    long endDate = Long.valueOf(mJson.getString("endDate"));
                    HotelOrderManager.getInstance().setBeginTime(beginDate);
                    HotelOrderManager.getInstance().setEndTime(endDate);
                    Intent intent = new Intent(this, HotelDetailActivity.class);
                    intent.putExtra("hotelId", Integer.valueOf(mJson.getString("hotelID")));
                    startActivity(intent);
                    SessionContext.setOpenInstallAppData(null);
                } else if (ShareTypeDef.SHARE_ROOM.equals(channel)) {
                    long beginDate = Long.valueOf(mJson.getString("beginDate"));
                    long endDate = Long.valueOf(mJson.getString("endDate"));
                    HotelOrderManager.getInstance().setBeginTime(beginDate);
                    HotelOrderManager.getInstance().setEndTime(endDate);
                    Intent intent = new Intent(this, HotelRoomDetailActivity.class);
                    intent.putExtra("hotelId", Integer.valueOf(mJson.getString("hotelID")));
                    intent.putExtra("roomId", Integer.valueOf(mJson.getString("roomID")));
                    intent.putExtra("roomType", Integer.valueOf(mJson.getString("hotelType")));
                    startActivity(intent);
                    SessionContext.setOpenInstallAppData(null);
                } else if (ShareTypeDef.SHARE_FREE.equals(channel)) {
                    Intent intent = new Intent(this, Hotel0YuanHomeActivity.class);
                    startActivity(intent);
                    SessionContext.setOpenInstallAppData(null);
                } else if (ShareTypeDef.SHARE_TIE.equals(channel)) {
                    Intent intent = new Intent(this, HotelSpaceDetailActivity.class);
                    intent.putExtra("hotelId", Integer.valueOf(mJson.getString("hotelID")));
                    intent.putExtra("articleId", Integer.valueOf(mJson.getString("blogID")));
                    startActivity(intent);
                    SessionContext.setOpenInstallAppData(null);
                } else {
                    LogUtil.d("HotelMainActivity", "warning~~~");
                }
            }
        }
    }

    private void showHaiNanAdPopupWindow() {
        // 设置背景颜色变暗
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.35f;
        getWindow().setAttributes(lp);
        mAdHaiNanPopupWindow.showAtLocation(getWindow().getDecorView(), Gravity.CENTER, 0, 0);
    }

    private void showHaiNanAd(String province) {
        if (province.contains("海南")) {
            myHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isAdShowed = true;
                    showHaiNanAdPopupWindow();
                }
            }, 500);
        }
    }

    private void showConsiderPopupWindow() {
        mConsiderLayout.reloadConsiderConfig(typeIndex, gradeIndex, priceIndex);
        // 设置背景颜色变暗
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;
        getWindow().setAttributes(lp);
        mConsiderPopupWindow.showAtLocation(getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
    }

    @Override
    public void dealIntent() {
        super.dealIntent();
        Bundle bundle = getIntent().getExtras();
        if (null != bundle) {
            isNeedCloseLeftDrawer = bundle.getBoolean("isClosed");
        }
    }

    private void requestMessageCount() {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        ResponseData d = b.syncRequest(b);
        d.path = NetURL.MESSAGE_COUNT;
        d.flag = AppConst.MESSAGE_COUNT;
        DataLoader.getInstance().loadData(this, d);
    }

    private void requestUserMenusStatus() {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        ResponseData d = b.syncRequest(b);
        d.path = NetURL.MENUS_STATUS;
        d.flag = AppConst.MENUS_STATUS;
        DataLoader.getInstance().loadData(this, d);
    }

    private void updateUserMenuStatus(String str) {
        JSONObject mJson = JSON.parseObject(str);
        boolean couponflag = mJson.containsKey("couponflag") ? mJson.getBoolean("couponflag") : false;
        int couponResId = couponflag ? R.drawable.iv_my_coupon_red : R.drawable.iv_my_coupon_normal;
        ((ImageView) findViewById(R.id.iv_coupon_icon)).setImageResource(couponResId);

        boolean orderflag = mJson.containsKey("orderflag") ? mJson.getBoolean("orderflag") : false;
        int orderResId = orderflag ? R.drawable.iv_my_order_red : R.drawable.iv_my_order_normal;
        ((ImageView) findViewById(R.id.iv_order_icon)).setImageResource(orderResId);

        boolean bountyflag = mJson.containsKey("bountyflag") ? mJson.getBoolean("bountyflag") : false;
        int bountyResId = bountyflag ? R.drawable.iv_my_bounty_red : R.drawable.iv_my_bounty_normal;
        ((ImageView) findViewById(R.id.iv_bounty_icon)).setImageResource(bountyResId);
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
    public void initListeners() {
        super.initListeners();
        left_layout.setOnLeftDrawerListener(this);
        drawer_layout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                iv_blur.setAlpha(slideOffset * 1f);
                iv_logo_vertical.setAlpha(slideOffset);
                iv_left.setAlpha(slideOffset);
                if (slideOffset > 0) {
                    blur_lay.setVisibility(View.VISIBLE);
                } else {
                    blur_lay.setVisibility(View.GONE);
                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {
            }

            @Override
            public void onDrawerClosed(View drawerView) {
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });
        user_lay.setOnClickListener(this);
        tv_city.setOnClickListener(this);
        tv_in_date.setOnClickListener(this);
        tv_out_date.setOnClickListener(this);
        tv_next_search.setOnClickListener(this);
        coupon_lay.setOnClickListener(this);
        order_lay.setOnClickListener(this);
        bounty_lay.setOnClickListener(this);
        tv_search.setOnClickListener(this);
        tv_consider.setOnClickListener(this);
        tv_curr_position.setOnClickListener(this);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (drawer_layout.isDrawerOpen(left_layout)) {
                drawer_layout.closeDrawers();
                return true;
            }
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                CustomToast.show(getString(R.string.exit_tip), CustomToast.LENGTH_SHORT);
                exitTime = System.currentTimeMillis();
            } else {
                MobclickAgent.onKillProcess(this);
                ActivityTack.getInstanse().exit();
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onPause() {
        super.onPause();
        banner_lay.stopBanner();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SessionContext.cleanLocationInfo();
        left_layout.unregisterBroadReceiver();
        SharedPreferenceUtil.getInstance().setString(AppConst.PROVINCE, mProvince, false);
        SharedPreferenceUtil.getInstance().setString(AppConst.CITY, mCity, false);
        SharedPreferenceUtil.getInstance().setString(AppConst.SITEID, mSiteId, false);
    }

    private void initCurrentTodayTime() {
        Calendar calendar = Calendar.getInstance();
        beginTime = calendar.getTime().getTime();
        calendar.add(Calendar.DAY_OF_MONTH, +1); //+1今天的时间加一天
        endTime = calendar.getTime().getTime();
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.user_lay:
                drawer_layout.openDrawer(left_layout);
                break;
            case R.id.tv_city: {
                if (!isSelectedDate) {
                    initCurrentTodayTime();
                }
                HotelOrderManager.getInstance().setBeginTime(beginTime);
                HotelOrderManager.getInstance().setEndTime(endTime);
                Intent resIntent = new Intent(this, HotelCityChooseActivity.class);
                startActivityForResult(resIntent, REQUEST_CODE_CITY);
                break;
            }
            case R.id.coupon_lay:
                if (!SessionContext.isLogin()) {
                    sendBroadcast(new Intent(BroadCastConst.UNLOGIN_ACTION));
                    return;
                }
                ((ImageView) findViewById(R.id.iv_coupon_icon)).setImageResource(R.drawable.iv_my_coupon_normal);
                intent = new Intent(this, UcCouponsActivity.class);
                break;
            case R.id.order_lay:
                if (!SessionContext.isLogin()) {
                    sendBroadcast(new Intent(BroadCastConst.UNLOGIN_ACTION));
                    return;
                }
                ((ImageView) findViewById(R.id.iv_order_icon)).setImageResource(R.drawable.iv_my_order_normal);
                intent = new Intent(this, UcOrdersActivity.class);
                break;
            case R.id.bounty_lay:
                if (!SessionContext.isLogin()) {
                    sendBroadcast(new Intent(BroadCastConst.UNLOGIN_ACTION));
                    return;
                }
                ((ImageView) findViewById(R.id.iv_bounty_icon)).setImageResource(R.drawable.iv_my_bounty_normal);
                intent = new Intent(this, UcBountyMainActivity.class);
                break;
            case R.id.tv_next_search:
                if (StringUtil.isEmpty(tv_city.getText().toString())) {
                    CustomToast.show("城市定位失败，请打开GPS或手动选择城市", CustomToast.LENGTH_SHORT);
                    return;
                }
                HotelOrderManager.getInstance().reset();
                if (!isSelectedDate) {
                    initCurrentTodayTime();
                }
                HotelOrderManager.getInstance().setBeginTime(beginTime);
                HotelOrderManager.getInstance().setEndTime(endTime);
                HotelOrderManager.getInstance().setDateStr(DateUtil.getDay("M.d", beginTime) + " - " + DateUtil.getDay("M.d", endTime));
                intent = new Intent(this, HotelListActivity.class);
                intent.putExtra("index", 0);
                intent.putExtra("keyword", "");
                if (isMyLoc && null != aMapLocation && aMapLocation.getLatitude() > 0 && aMapLocation.getLongitude() > 0) {
                    intent.putExtra("landmark", tv_city.getText().toString());
                    intent.putExtra("isLandMark", true);
                    intent.putExtra("lonLat", String.format("%1$f|%2$f", aMapLocation.getLatitude(), aMapLocation.getLongitude()));
                    intent.putExtra("siteId", aMapLocation.getAdCode());
                }

                break;
            case R.id.tv_in_date:
            case R.id.tv_out_date: {
                Intent resIntent = new Intent(this, HotelCalendarChooseActivity.class);
                resIntent.putExtra("isTitleCanClick", true);
//                resIntent.putExtra("beginTime", beginTime);
//                resIntent.putExtra("endTime", endTime);
                startActivityForResult(resIntent, REQUEST_CODE_DATE);
                break;
            }
            case R.id.tv_search:
                if (!isSelectedDate) {
                    initCurrentTodayTime();
                }
                HotelOrderManager.getInstance().setBeginTime(beginTime);
                HotelOrderManager.getInstance().setEndTime(endTime);
                HotelOrderManager.getInstance().setDateStr(DateUtil.getDay("M.d", beginTime) + " - " + DateUtil.getDay("M.d", endTime));
                intent = new Intent(this, HotelAllSearchActivity.class);
                break;
            case R.id.tv_consider:
                showConsiderPopupWindow();
                break;
            case R.id.tv_curr_position:
                tv_city.setHint("正在获取当前位置");
                tv_city.setText("");
                isMyLoc = true;
                AMapLocationControl.getInstance().startLocationOnce(this);
                break;
        }
        if (null != intent) {
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.i(TAG, "onActivityResult() " + requestCode + ", " + resultCode);
        if (Activity.RESULT_OK != resultCode) {
            return;
        }

        if (requestCode == REQUEST_CODE_CITY) {
            tv_city.setHint("正在定位当前城市");
            isMyLoc = false;
            aMapLocation = null;
            mProvince = data.getStringExtra(AppConst.PROVINCE);
            mCity = data.getStringExtra(AppConst.CITY);
            mSiteId = data.getStringExtra(AppConst.SITEID);
            if (AMapLocationControl.getInstance().isStart()) {
                AMapLocationControl.getInstance().stopLocation();
            }
        } else if (requestCode == REQUEST_CODE_DATE) {
            if (null != data) {
                isSelectedDate = true;
                beginTime = data.getLongExtra("beginTime", beginTime);
                endTime = data.getLongExtra("endTime", endTime);
                HotelOrderManager.getInstance().setBeginTime(beginTime);
                HotelOrderManager.getInstance().setEndTime(endTime);
                HotelOrderManager.getInstance().setDateStr(DateUtil.getDay("M.d", beginTime) + " - " + DateUtil.getDay("M.d", endTime));
                tv_in_date.setText(formatDateForBigDay(DateUtil.getDay("M月d日 ", beginTime) + DateUtil.dateToWeek2(new Date(beginTime))));
                tv_out_date.setText(formatDateForBigDay(DateUtil.getDay("M月d日 ", endTime) + DateUtil.dateToWeek2(new Date(endTime))));
                tv_days.setText(String.format(getString(R.string.duringNightStr), DateUtil.getGapCount(new Date(beginTime), new Date(endTime))));
            }
            mProvince = SharedPreferenceUtil.getInstance().getString(AppConst.PROVINCE, "", false);
            mCity = SharedPreferenceUtil.getInstance().getString(AppConst.CITY, "", false);
            mSiteId = SharedPreferenceUtil.getInstance().getString(AppConst.SITEID, "", false);
        }
        HotelOrderManager.getInstance().setCityStr(CityParseUtils.getProvinceCityString(mProvince, mCity, "-"));
        tv_city.setText(CityParseUtils.getCityString(mCity));
        if (!isAdShowed) {
            showHaiNanAd(mProvince);
        }

        requestWeatherInfo(beginTime);
    }

    private SpannableString formatDateForBigDay(String date) {
        if (StringUtil.notEmpty(date)) {
//            int startIndex = date.indexOf("月");
//            int endIndex = date.indexOf("日");
            int endIndex = date.indexOf("周");
            SpannableString ss = new SpannableString(date);
            ss.setSpan(new AbsoluteSizeSpan(21, true), 0, endIndex,
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            return ss;
        }
        return new SpannableString("");
    }

    @Override
    public void onNotifyMessage(ResponseData request, ResponseData response) {
        super.onNotifyMessage(request, response);
        removeProgressDialog();
        if (response != null && response.body != null) {
            if (request.flag == AppConst.MESSAGE_COUNT) {
                JSONObject mJson = JSON.parseObject(response.body.toString());
                boolean hasMsg = left_layout.updateMsgCount(mJson.getString("count"));
                if (hasMsg) {
                    iv_user.setImageResource(R.drawable.iv_home_user2);
                } else {
                    iv_user.setImageResource(R.drawable.iv_home_user);
                }
            } else if (request.flag == AppConst.WEATHER) {
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
                if (null != banner_lay) {
                    banner_lay.setImageResource(temp);
                }
            } else if (request.flag == AppConst.MENUS_STATUS) {
                LogUtil.i(TAG, "json = " + response.body.toString());
                if (StringUtil.notEmpty(response.body.toString()) && !"{}".equals(response.body.toString())) {
                    updateUserMenuStatus(response.body.toString());
                }
            }
        }
    }

    @Override
    protected boolean isCheckException(ResponseData request, ResponseData response) {
        if (response != null && response.data != null) {
            if (request.flag == AppConst.WEATHER) {
                banner_lay.updateWeatherInfo(beginTime, null);
                return true;
            }
        }
        return super.isCheckException(request, response);
    }

    @Override
    public void onLocation(boolean isSuccess, AMapLocation aMapLocation) {
        LogUtil.i(TAG, "onLocation()");
        if (isSuccess && aMapLocation != null) {
            SharedPreferenceUtil.getInstance().setString(AppConst.LOCATION_LON, String.valueOf(aMapLocation.getLongitude()), false);
            SharedPreferenceUtil.getInstance().setString(AppConst.LOCATION_LAT, String.valueOf(aMapLocation.getLatitude()), false);
            String province = CityParseUtils.getProvinceString(aMapLocation.getProvince());
            String city = CityParseUtils.getProvinceString(aMapLocation.getCity());
            String siteId = String.valueOf(aMapLocation.getAdCode());
            SharedPreferenceUtil.getInstance().setString(AppConst.PROVINCE, province, false);
            SharedPreferenceUtil.getInstance().setString(AppConst.CITY, city, false);
            SharedPreferenceUtil.getInstance().setString(AppConst.SITEID, siteId, false);
            String tmp = CityParseUtils.getCityString(city);
            LogUtil.i("\n" + aMapLocation.toString().replace("#", "\n"));
            if (isMyLoc) {
                this.aMapLocation = aMapLocation;
                tmp = aMapLocation.getPoiName();
                if (StringUtil.isEmpty(tmp)) {
                    tmp = aMapLocation.getStreet() + aMapLocation.getStreetNum();
                }
            }
            tv_city.setText(tmp);
            HotelOrderManager.getInstance().setCityStr(CityParseUtils.getProvinceCityString(province, city, "-"));
            requestWeatherInfo(beginTime);
            showHaiNanAd(province);
        } else {
            tv_city.setHint("城市定位失败");
        }
    }

    @Override
    public void closeDrawer() {
        if (null != drawer_layout) {
            drawer_layout.closeDrawers();
        }
    }

    @Override
    public void doQmhAction() {
        //全民化插件
//        Intent intent = new Intent(HotelMainActivity.this, IOUAppVerifyActivity.class);
//        intent.putExtra("appUserId", SessionContext.mUser.user.mobile);
//        intent.putExtra("token", SessionContext.getTicket());
//        intent.putExtra("platCode", "2003");
//        intent.putExtra("isShowGuide", "true");
//        startActivityForResult(intent, REQUEST_CODE_QMH);
    }
}
