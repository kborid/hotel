package com.huicheng.hotel.android.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huicheng.hotel.android.BuildConfig;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.HotelOrderManager;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.common.ShareTypeDef;
import com.huicheng.hotel.android.content.AppConst;
import com.huicheng.hotel.android.content.NetURL;
import com.huicheng.hotel.android.control.DataCleanManager;
import com.huicheng.hotel.android.control.LocationInfo;
import com.huicheng.hotel.android.permission.PermissionsDef;
import com.huicheng.hotel.android.requestbuilder.RequestBeanBuilder;
import com.huicheng.hotel.android.requestbuilder.bean.AppInfoBean;
import com.huicheng.hotel.android.requestbuilder.bean.HomeBannerInfoBean;
import com.huicheng.hotel.android.requestbuilder.bean.WeatherInfoBean;
import com.huicheng.hotel.android.ui.activity.hotel.Hotel0YuanHomeActivity;
import com.huicheng.hotel.android.ui.activity.hotel.HotelDetailActivity;
import com.huicheng.hotel.android.ui.activity.hotel.HotelRoomDetailActivity;
import com.huicheng.hotel.android.ui.activity.hotel.HotelSpaceDetailActivity;
import com.huicheng.hotel.android.ui.adapter.SwitcherContentAdapter;
import com.huicheng.hotel.android.ui.base.BaseAppActivity;
import com.huicheng.hotel.android.ui.custom.CommonBannerLayout;
import com.huicheng.hotel.android.ui.custom.LeftDrawerLayout;
import com.huicheng.hotel.android.ui.dialog.CustomDialog;
import com.huicheng.hotel.android.ui.dialog.CustomToast;
import com.huicheng.hotel.android.ui.listener.CustomOnPageChangeListener;
import com.huicheng.hotel.android.ui.listener.MainScreenCallback;
import com.huicheng.hotel.android.ui.listener.OnUpdateSwitcherListener;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author kborid
 * @date 2017/5/24 0024
 */
public class MainSwitcherActivity extends BaseAppActivity implements LeftDrawerLayout.OnLeftDrawerListener {
    private WeakReferenceHandler<MainSwitcherActivity> myHandler = new WeakReferenceHandler<>(this);
    private static final String[] tabTitle = {"酒店", "机票", "行程"};
    private static final int[] tabResId = {R.drawable.mainswitcher_hotel_sel, R.drawable.mainswitcher_plane_sel, R.drawable.mainswitcher_trace_sel};

    private DrawerLayout drawer_layout;
    private LeftDrawerLayout left_layout;
    private CommonBannerLayout banner_lay;
    private RelativeLayout user_lay;
    private ImageView iv_user;
    private LinearLayout tab_title_lay;
    private ViewPager tab_viewPager;

    private int viewPagerIndex = 0;
    private AppInfoBean mAppInfoBean = null;
    private boolean isFirstLaunch = false;
    private long exitTime = 0;
    private boolean isNeedCloseLeftDrawer = false;
    private long weatherTimestamp = 0;

    @Override
    protected void preOnCreate() {
        super.preOnCreate();
        initMainWindow();
    }

    @Override
    protected void setContentView() {
        setContentView(R.layout.act_main_switcher);
    }

    @Override
    protected void requestData() {
        super.requestData();
        if (SessionContext.getBannerList().size() == 0) {
            requestHomeBannerInfo();
        }
    }

    @Override
    public void initViews() {
        super.initViews();
        drawer_layout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer_layout.setScrimColor(getResources().getColor(R.color.transparent50));
        left_layout = (LeftDrawerLayout) findViewById(R.id.left_layout);
        banner_lay = (CommonBannerLayout) findViewById(R.id.banner_lay);
        user_lay = (RelativeLayout) findViewById(R.id.user_lay);
        iv_user = (ImageView) findViewById(R.id.iv_user);
        tab_title_lay = (LinearLayout) findViewById(R.id.tab_title_lay);
        tab_viewPager = (ViewPager) findViewById(R.id.tab_viewPager);
        tab_viewPager.setOffscreenPageLimit(3);
        tab_viewPager.setAdapter(new SwitcherContentAdapter(getSupportFragmentManager(), tabTitle, mainScreenCallback));

        //user relayout
        RelativeLayout.LayoutParams ucRlp = (RelativeLayout.LayoutParams) user_lay.getLayoutParams();
        ucRlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        user_lay.setPadding(Utils.dp2px(20), Utils.mStatusBarHeight + Utils.dp2px(10), Utils.dp2px(20), Utils.dp2px(10));
        user_lay.setLayoutParams(ucRlp);
        //banner & weather relayout
        RelativeLayout.LayoutParams weatherRlp = (RelativeLayout.LayoutParams) banner_lay.getLayoutParams();
        weatherRlp.width = Utils.mScreenWidth;
        weatherRlp.height = (int) ((float) weatherRlp.width / 750 * 400);
        banner_lay.setLayoutParams(weatherRlp);

        tab_title_lay.removeAllViews();
        for (int i = 0; i < tabTitle.length; i++) {
            TextView tv_tab = new TextView(this);
            tv_tab.setGravity(Gravity.CENTER);
            tv_tab.setText(tabTitle[i]);
            tv_tab.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            tv_tab.setTextColor(getResources().getColorStateList(R.color.main_switcher_color_sel));
            tv_tab.setCompoundDrawablePadding(Utils.dp2px(6));
            tv_tab.setCompoundDrawablesWithIntrinsicBounds(0, tabResId[i], 0, 0);
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            llp.weight = 1;
            tab_title_lay.addView(tv_tab, llp);
            final int finalI = i;
            tv_tab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (finalI == 2) {
                        if (!SessionContext.isLogin()) {
                            sendBroadcast(new Intent(BroadCastConst.UNLOGIN_ACTION));
                            return;
                        }
                    }
                    tab_title_lay.dispatchSetSelected(false);
                    v.setSelected(true);
                    tab_viewPager.setCurrentItem(finalI, true);
                }
            });
        }

        tab_title_lay.getChildAt(viewPagerIndex).setSelected(true);
        tab_viewPager.setCurrentItem(viewPagerIndex);
    }

    @Override
    public void dealIntent() {
        super.dealIntent();
        Bundle bundle = getIntent().getExtras();
        if (null != bundle) {
            isNeedCloseLeftDrawer = bundle.getBoolean("isClosed");
            viewPagerIndex = bundle.getInt("index");
        }
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
        isFirstLaunch = true;
        left_layout.updateUserInfo(); //更新用户中心
        banner_lay.setImageResource(SessionContext.getBannerList());
        banner_lay.setWeatherInfoLayoutMargin(Utils.dp2px(11), Utils.mStatusBarHeight + Utils.dp2px(10), 0, 0);
        banner_lay.setIndicatorLayoutMarginBottom(Utils.dp2px(8));

        //app更新提示
        if (SessionContext.getOpenInstallAppData() == null) {
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
                            showUpdateDialog(mAppInfoBean);
                        }
                    }
                }, 500);
            }
        }
    }

    @Override
    public void initListeners() {
        super.initListeners();
        left_layout.setOnLeftDrawerListener(this);
        user_lay.setOnClickListener(this);
        tab_viewPager.addOnPageChangeListener(new CustomOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == 2) {
                    if (!SessionContext.isLogin()) {
                        sendBroadcast(new Intent(BroadCastConst.UNLOGIN_ACTION));
                        tab_viewPager.setCurrentItem(1);
                        return;
                    }
                }
                tab_title_lay.dispatchSetSelected(false);
                tab_title_lay.getChildAt(position).setSelected(true);
            }
        });
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.user_lay:
                drawer_layout.openDrawer(left_layout);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        banner_lay.startBanner();
        boolean jump = false;
        if (isFirstLaunch) {
            isFirstLaunch = false;
            if (SessionContext.getOpenInstallAppData() != null) {
                myHandler.removeCallbacksAndMessages(null);
                //OpenInstall Event 分发
                jump = dispatchOpenInstallEvent();
            }
        }

        if (!jump) {
            if (SessionContext.isLogin()) {
                requestMessageCount();
            } else {
                //每次启动时，如果用户未登录，则显示侧滑
                if (SessionContext.isFirstLaunchDoAction(getClass().getSimpleName())) {
                    sendBroadcast(new Intent(BroadCastConst.UNLOGIN_ACTION));
                }
            }

            if (isNeedCloseLeftDrawer && drawer_layout.isDrawerOpen(left_layout)) {
                isNeedCloseLeftDrawer = false;
                drawer_layout.closeDrawers();
            }
        }
    }

    private boolean dispatchOpenInstallEvent() {
        LogUtil.i(TAG, "dispatchOpenInstallEvent()");
        if (SessionContext.getOpenInstallAppData() != null) {
            JSONObject mJson = JSON.parseObject(SessionContext.getOpenInstallAppData().getData());
            if (null != mJson && mJson.containsKey("channel")) {
                String channel = mJson.getString("channel");
                if (ShareTypeDef.SHARE_HOTEL.equals(channel)) {
//                    long beginDate = Long.valueOf(mJson.getString("beginDate"));
//                    long endDate = Long.valueOf(mJson.getString("endDate"));
                    Calendar calendar = Calendar.getInstance();
                    long beginDate = calendar.getTime().getTime();
                    calendar.add(Calendar.DAY_OF_MONTH, +1); //+1今天的时间加一天
                    long endDate = calendar.getTime().getTime();
                    HotelOrderManager.getInstance().setBeginTime(beginDate);
                    HotelOrderManager.getInstance().setEndTime(endDate);
                    Intent intent = new Intent(this, HotelDetailActivity.class);
                    intent.putExtra("hotelId", Integer.valueOf(mJson.getString("hotelID")));
                    startActivity(intent);
                    SessionContext.setOpenInstallAppData(null);
                    return true;
                } else if (ShareTypeDef.SHARE_ROOM.equals(channel)) {
//                    long beginDate = Long.valueOf(mJson.getString("beginDate"));
//                    long endDate = Long.valueOf(mJson.getString("endDate"));
                    Calendar calendar = Calendar.getInstance();
                    long beginDate = calendar.getTime().getTime();
                    calendar.add(Calendar.DAY_OF_MONTH, +1); //+1今天的时间加一天
                    long endDate = calendar.getTime().getTime();
                    HotelOrderManager.getInstance().setBeginTime(beginDate);
                    HotelOrderManager.getInstance().setEndTime(endDate);
                    Intent intent = new Intent(this, HotelRoomDetailActivity.class);
                    intent.putExtra("hotelId", Integer.valueOf(mJson.getString("hotelID")));
                    intent.putExtra("roomId", Integer.valueOf(mJson.getString("roomID")));
                    intent.putExtra("roomType", Integer.valueOf(mJson.getString("hotelType")));
                    startActivity(intent);
                    SessionContext.setOpenInstallAppData(null);
                    return true;
                } else if (ShareTypeDef.SHARE_FREE.equals(channel)) {
                    Intent intent = new Intent(this, Hotel0YuanHomeActivity.class);
                    startActivity(intent);
                    SessionContext.setOpenInstallAppData(null);
                    return true;
                } else if (ShareTypeDef.SHARE_TIE.equals(channel)) {
                    Intent intent = new Intent(this, HotelSpaceDetailActivity.class);
                    intent.putExtra("hotelId", Integer.valueOf(mJson.getString("hotelID")));
                    intent.putExtra("articleId", Integer.valueOf(mJson.getString("blogID")));
                    startActivity(intent);
                    SessionContext.setOpenInstallAppData(null);
                    return true;
                } else {
                    LogUtil.d("HotelMainActivity", "warning~~~");
                    return false;
                }
            }
            return false;
        }
        return false;
    }

    private void requestMessageCount() {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        ResponseData d = b.syncRequest(b);
        d.path = NetURL.MESSAGE_COUNT;
        d.flag = AppConst.MESSAGE_COUNT;
        DataLoader.getInstance().loadData(this, d);
    }

    private void requestHomeBannerInfo() {
        LogUtil.i(TAG, "requestHotelBanner()");
        RequestBeanBuilder b = RequestBeanBuilder.create(false);
        ResponseData d = b.syncRequest(b);
        d.path = NetURL.HOTEL_BANNER;
        d.flag = AppConst.HOTEL_BANNER;
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void requestWeatherInfo(long timeStamp) {
        RequestBeanBuilder b = RequestBeanBuilder.create(false);
        b.addBody("cityname", LocationInfo.instance.getCity());
        b.addBody("siteid", LocationInfo.instance.getCityCode());
        b.addBody("date", DateUtil.getDay("yyyyMMdd", timeStamp));
        ResponseData d = b.syncRequest(b);
        d.path = NetURL.WEATHER;
        d.flag = AppConst.WEATHER;
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
                final CustomDialog pd = new CustomDialog(MainSwitcherActivity.this);
                pd.setTitle(R.string.download_ing);
                View view = LayoutInflater.from(MainSwitcherActivity.this).inflate(R.layout.progress_download_layout, null);
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
    protected void onPause() {
        super.onPause();
        banner_lay.stopBanner();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        left_layout.unregisterBroadReceiver();
        LocationInfo.instance.reset();
        if (myHandler != null) {
            myHandler.removeCallbacksAndMessages(null);
            myHandler = null;
        }
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
    public void closeDrawer() {
        if (null != drawer_layout) {
            drawer_layout.closeDrawers();
        }
    }

    @Override
    public void doQmhAction() {
//////////全民化插件 platCode生产2003，测试8000///////////
//        Intent intent = new Intent(HotelMainActivity.this, IOUAppVerifyActivity.class);
//        intent.putExtra("appUserId", SessionContext.mUser.user.mobile);
//        intent.putExtra("token", SessionContext.getTicket());
//        intent.putExtra("platCode", "2003");
//        intent.putExtra("isShowGuide", "true");
//        startActivityForResult(intent, REQUEST_CODE_QMH);
    }

    @Override
    public void onNotifyMessage(ResponseData request, ResponseData response) {
        super.onNotifyMessage(request, response);
        if (null != response && response.body != null) {
            if (request.flag == AppConst.MESSAGE_COUNT) {
                JSONObject mJson = JSON.parseObject(response.body.toString());
                boolean hasMsg = left_layout.updateMsgCount(mJson.getString("count"));
                int userResId = hasMsg ? R.drawable.iv_home_user2 : R.drawable.iv_home_user;
                iv_user.setImageResource(userResId);
            } else if (request.flag == AppConst.HOTEL_BANNER) {
                LogUtil.i(TAG, "json = " + response.body.toString());
                List<HomeBannerInfoBean> temp = JSON.parseArray(response.body.toString(), HomeBannerInfoBean.class);
                SessionContext.setBannerList(temp);
                banner_lay.setImageResource(temp);
            } else if (request.flag == AppConst.WEATHER) {
                LogUtil.i(TAG, "json = " + response.body.toString());
                WeatherInfoBean bean = null;
                if (StringUtil.notEmpty(response.body.toString()) && !"{}".equals(response.body.toString())) {
                    bean = JSON.parseObject(response.body.toString(), WeatherInfoBean.class);
                }
                banner_lay.updateWeatherInfo(weatherTimestamp, bean);
            }
        }
    }

    private MainScreenCallback mainScreenCallback = new MainScreenCallback() {
        @Override
        public void requestWeather(long timestamp) {
            weatherTimestamp = timestamp;
            requestWeatherInfo(weatherTimestamp);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.i(TAG, "onActivityResult() " + requestCode + ", " + resultCode);
        if (requestCode == PermissionsDef.PERMISSION_REQ_CODE) {
            for (OnUpdateSwitcherListener listener : onUpdateSwitcherListeners) {
                listener.permissionResult(resultCode == PermissionsDef.PERMISSIONS_GRANTED);
            }
        }
    }

    private static List<OnUpdateSwitcherListener> onUpdateSwitcherListeners = new ArrayList<>();

    public static void registerPermissionListener(OnUpdateSwitcherListener listener) {
        if (!onUpdateSwitcherListeners.contains(listener)) {
            onUpdateSwitcherListeners.add(listener);
        }
    }

    public static void unRegisterPermissionListener(OnUpdateSwitcherListener listener) {
        if (onUpdateSwitcherListeners.contains(listener)) {
            onUpdateSwitcherListeners.remove(listener);
        }
    }
}
