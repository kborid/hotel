package com.huicheng.hotel.android.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.amap.api.location.AMapLocation;
import com.fm.openinstall.OpenInstall;
import com.fm.openinstall.listener.AppInstallListener;
import com.fm.openinstall.listener.AppWakeUpListener;
import com.fm.openinstall.model.AppData;
import com.fm.openinstall.model.Error;
import com.huicheng.hotel.android.BuildConfig;
import com.huicheng.hotel.android.PRJApplication;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.content.AppConst;
import com.huicheng.hotel.android.content.NetURL;
import com.huicheng.hotel.android.control.AMapLocationControl;
import com.huicheng.hotel.android.control.CityListDataManager;
import com.huicheng.hotel.android.control.DataCleanManager;
import com.huicheng.hotel.android.control.LocationInfo;
import com.huicheng.hotel.android.permission.PermissionsActivity;
import com.huicheng.hotel.android.permission.PermissionsDef;
import com.huicheng.hotel.android.requestbuilder.RequestBeanBuilder;
import com.huicheng.hotel.android.requestbuilder.bean.AirCompanyInfoBean;
import com.huicheng.hotel.android.requestbuilder.bean.AppInfoBean;
import com.huicheng.hotel.android.requestbuilder.bean.CityAirportInfoBean;
import com.huicheng.hotel.android.requestbuilder.bean.HomeBannerInfoBean;
import com.huicheng.hotel.android.tools.CityParseUtils;
import com.huicheng.hotel.android.ui.activity.hotel.HotelMainActivity;
import com.huicheng.hotel.android.ui.base.BaseAppActivity;
import com.huicheng.hotel.android.ui.dialog.CustomDialog;
import com.huicheng.hotel.android.ui.dialog.CustomToast;
import com.prj.sdk.algo.MD5Tool;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.net.data.ResponseData;
import com.prj.sdk.net.down.DownCallback;
import com.prj.sdk.net.down.DownLoaderTask;
import com.prj.sdk.util.ActivityTack;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 欢迎页面
 */
public class LauncherActivity extends BaseAppActivity implements AppInstallListener, AppWakeUpListener, AMapLocationControl.MyLocationListener {

    private int mTagCount = 0;
    private Map<Integer, Integer> mTag = new HashMap<>();
    private static HandlerThread mHandlerThread = null;
    private static Handler mHandler = null;

    private static long mStartTime = 0;
    private static final long AD_SHOWTIME = 1000;

    @Override
    protected void preOnCreate() {
        super.preOnCreate();
        initLaunchWindow();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStartTime = System.currentTimeMillis();
        // 避免从桌面启动程序后，会重新实例化入口类的activity
        if (!this.isTaskRoot()) {
            Intent intent = getIntent();
            if (intent != null) {
                String action = intent.getAction();
                if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(action)) {
                    finish();
                    return;
                }
            }
        }
        onNewIntent(getIntent());
    }

    @Override
    protected void setContentView() {
        setContentView(R.layout.act_launcher);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        OpenInstall.getWakeUp(intent, this);
    }

    @Override
    public void initViews() {
        super.initViews();
    }

    public void initParams() {
        super.initParams();
        //自OpenInstall SDK 2.0.0开始，SDK内部将会一直保存安装数据，每次调用getInstall方法都会返回值
        LogUtil.i(TAG, "IS_FIRST_LAUNCH = " + SharedPreferenceUtil.getInstance().getBoolean(AppConst.IS_FIRST_LAUNCH, true));
        if (SharedPreferenceUtil.getInstance().getBoolean(AppConst.IS_FIRST_LAUNCH, true)) {
            OpenInstall.getInstall(this);
        }
        Utils.initScreenSize(this);// 设置手机屏幕大小
        SessionContext.initUserInfo();
        mTagCount = 0; //初始化接口请求个数
    }

    private void requestGDTInterface() {
        LogUtil.i(TAG, "requestGDTInterface()");
        RequestBeanBuilder b = RequestBeanBuilder.create(false);
        b.addBody("muid", MD5Tool.getMD5(Utils.getIMEI()));
        ResponseData d = b.syncRequestGET(b);
        d.path = NetURL.AD_GDT_IF;
        d.flag = AppConst.AD_GDT_IF;
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void requestAppVersionInfo() {
        LogUtil.i(TAG, "requestAppVersionInfo()");
        RequestBeanBuilder b = RequestBeanBuilder.create(false);
        b.addBody("contype", "0"); // 0:android, 1:ios
        ResponseData d = b.syncRequest(b);
        d.path = NetURL.APP_INFO;
        d.flag = AppConst.APP_INFO;
        requestID = DataLoader.getInstance().loadData(this, d);
        mTagCount++;
    }

    private void requestHomeBannerInfo() {
        LogUtil.i(TAG, "requestHotelBanner()");
        RequestBeanBuilder b = RequestBeanBuilder.create(false);
        ResponseData d = b.syncRequest(b);
        d.path = NetURL.HOTEL_BANNER;
        d.flag = AppConst.HOTEL_BANNER;
        requestID = DataLoader.getInstance().loadData(this, d);
        mTagCount++;
    }

    private void requestAirportInfo() {
        LogUtil.i(TAG, "requestAirportInfo()");
        RequestBeanBuilder b = RequestBeanBuilder.create(false);
        ResponseData d = b.syncRequest(b);
        d.path = NetURL.PLANE_AIRPORT_LIST;
        d.flag = AppConst.PLANE_AIRPORT_LIST;
        requestID = DataLoader.getInstance().loadData(this, d);
        mTagCount++;
    }

    private void requestAirCompaniesInfo() {
        LogUtil.i(TAG, "requestAirCompaniesInfo()");
        RequestBeanBuilder b = RequestBeanBuilder.create(false);
        ResponseData d = b.syncRequest(b);
        d.path = NetURL.PLANE_COMPANY_LIST;
        d.flag = AppConst.PLANE_COMPANY_LIST;
        requestID = DataLoader.getInstance().loadData(this, d);
        mTagCount++;
    }

    public void doLastAction() {
        LogUtil.i(TAG, "doLastAction()");
        long exitTime = System.currentTimeMillis();
        if (exitTime - mStartTime < AD_SHOWTIME) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    goToNext();
                }
            }, AD_SHOWTIME - (exitTime - mStartTime));
        } else {
            goToNext();
        }
    }

    private void goToNext() {
        LogUtil.i(TAG, "goToNext()");
        Intent intent;
        if (SharedPreferenceUtil.getInstance().getBoolean(AppConst.IS_FIRST_LAUNCH, true)) {
            SharedPreferenceUtil.getInstance().setBoolean(AppConst.IS_FIRST_LAUNCH, false);
        }
//        intent = new Intent(this, MainSwitcherActivity.class);
        intent = new Intent(this, HotelMainActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.launch_in, R.anim.launch_out);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return event.getKeyCode() == KeyEvent.KEYCODE_BACK || super.dispatchKeyEvent(event);
    }

    @Override
    public void onInstallFinish(AppData appData, Error error) {
        LogUtil.i(TAG, "onInstallFinish() appData = " + appData + ", error = " + error + ", do nothing!!!");
        AppData tmp = null;
        if (null != appData) {
            LogUtil.i(TAG, "appData = " + appData.toString());
            if (StringUtil.notEmpty(appData.getChannel()) || StringUtil.notEmpty(appData.getData())) {
                tmp = appData;
            }
        }
        SessionContext.setOpenInstallAppData(tmp);
    }

    @Override
    public void onWakeUpFinish(AppData appData, Error error) {
        LogUtil.i(TAG, "onWakeUpFinish() appData = " + appData + ", error = " + error);
        AppData tmp = null;
        if (null != appData) {
            LogUtil.i(TAG, "appData = " + appData.toString());
            if (StringUtil.notEmpty(appData.getChannel()) || StringUtil.notEmpty(appData.getData())) {
                tmp = appData;
            }
        }
        SessionContext.setOpenInstallAppData(tmp);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 缺少权限时, 进入权限配置页面
        if (PRJApplication.getPermissionsChecker(this).lacksPermissions(PermissionsDef.LAUNCH_REQUIRE_PERMISSIONS)) {
            PermissionsActivity.startActivityForResult(this, PermissionsDef.PERMISSION_REQ_CODE, PermissionsDef.LAUNCH_REQUIRE_PERMISSIONS);
            return;
        }
        //用户第一次启动时，调用后台封装的广点通的接口，统计激活量
        if (SharedPreferenceUtil.getInstance().getBoolean(AppConst.IS_FIRST_LAUNCH, true)) {
            requestGDTInterface();
        }
//        AMapLocationControl.getInstance().startLocationOnce(this);
        if (null == mHandlerThread) {
            mHandlerThread = new HandlerThread("dealJsonReqThread");
            mHandlerThread.start();
            mHandler = new Handler(mHandlerThread.getLooper());
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    requestHomeBannerInfo();
                    requestAppVersionInfo();
                    //初始化城市列表
                    CityListDataManager.getInstance().initCityList(null);
                    //初始化机场信息
                    String airportJson = SharedPreferenceUtil.getInstance().getString(AppConst.CITY_PLANE_JSON, "", false);
                    if (StringUtil.isEmpty(airportJson)) {
                        requestAirportInfo();
                    } else {
                        List<CityAirportInfoBean> tmp = JSON.parseArray(airportJson, CityAirportInfoBean.class);
                        SessionContext.setAirportList(tmp);
                    }
                    //初始化航司信息
                    String airCompanyJson = SharedPreferenceUtil.getInstance().getString(AppConst.AIR_COMPANY_JSON, "", false);
                    if (StringUtil.isEmpty(airCompanyJson)) {
                        requestAirCompaniesInfo();
                    } else {
                        List<AirCompanyInfoBean> tmp = JSON.parseArray(airCompanyJson, AirCompanyInfoBean.class);
                        SessionContext.setAirCompanyMap(tmp);
                    }
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mHandlerThread) {
            mHandlerThread.quit();
            mHandlerThread = null;
        }

        if (null != mHandler) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onNotifyMessage(ResponseData request, ResponseData response) {
        if (response != null && response.body != null) {
            if (request.flag == AppConst.APP_INFO) {
                mTag.put(AppConst.APP_INFO, AppConst.APP_INFO);
                LogUtil.i(TAG, "json = " + response.body.toString());
                if (StringUtil.notEmpty(response.body.toString()) && !response.body.toString().equals("{}")) {
                    AppInfoBean bean = JSON.parseObject(response.body.toString(), AppInfoBean.class);
                    SharedPreferenceUtil.getInstance().setString(AppConst.APPINFO, response.body.toString(), false);
                    String versionSer = bean.upid.split("（")[0];
                    String versionLoc = BuildConfig.VERSION_NAME.split("（")[0];
                    if (bean.isforce == 0 && 1 <= SessionContext.VersionComparison(versionSer, versionLoc)) {
                        mTag.remove(AppConst.APP_INFO);
                        showFocusUpdateDialog(bean);
                    }
                }
            } else if (request.flag == AppConst.HOTEL_BANNER) {
                mTag.put(AppConst.HOTEL_BANNER, AppConst.HOTEL_BANNER);
                LogUtil.i(TAG, "json = " + response.body.toString());
                List<HomeBannerInfoBean> temp = JSON.parseArray(response.body.toString(), HomeBannerInfoBean.class);
                SessionContext.setBannerList(temp);
            } else if (request.flag == AppConst.AD_GDT_IF) {
                LogUtil.i(TAG, "json = " + response.body.toString());
            } else if (request.flag == AppConst.PLANE_AIRPORT_LIST) {
                mTag.put(AppConst.PLANE_AIRPORT_LIST, AppConst.PLANE_AIRPORT_LIST);
                LogUtil.i(TAG, "json = " + response.body.toString());
                SharedPreferenceUtil.getInstance().setString(AppConst.CITY_PLANE_JSON, response.body.toString(), false);
                List<CityAirportInfoBean> tmp = JSON.parseArray(response.body.toString(), CityAirportInfoBean.class);
                Collections.sort(tmp);
                SessionContext.setAirportList(tmp);
            } else if (request.flag == AppConst.PLANE_COMPANY_LIST) {
                mTag.put(AppConst.PLANE_COMPANY_LIST, AppConst.PLANE_COMPANY_LIST);
                LogUtil.i(TAG, "json = " + response.body.toString());
                SharedPreferenceUtil.getInstance().setString(AppConst.AIR_COMPANY_JSON, response.body.toString(), false);
                List<AirCompanyInfoBean> tmp = JSON.parseArray(response.body.toString(), AirCompanyInfoBean.class);
                SessionContext.setAirCompanyMap(tmp);
            }

            if (mTag.size() == mTagCount) {
                doLastAction();
            }
        }
    }

    @Override
    protected boolean isCheckException(ResponseData request, ResponseData response) {
        if (request.flag == AppConst.AD_GDT_IF) {
            return true;
        }
        return super.isCheckException(request, response);
    }

    @Override
    public void onNotifyError(ResponseData request, ResponseData response) {
        super.onNotifyError(request, response);
        mTag.put(request.flag, request.flag);
        if (mTag.size() == mTagCount) {
            doLastAction();
        }
    }

    private void showFocusUpdateDialog(final AppInfoBean bean) {
        final CustomDialog dialog = new CustomDialog(this);
        String title = getString(R.string.update_tips);
        if (StringUtil.notEmpty(bean.tip)) {
            title = bean.tip;
        }
        dialog.setTitle(title);
        dialog.setMessage(bean.updesc);
        dialog.setNegativeButton(getString(R.string.exit), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ActivityTack.getInstanse().exit();
            }
        });
        dialog.setPositiveButton(getString(R.string.update), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final CustomDialog pd = new CustomDialog(LauncherActivity.this);
                pd.setTitle(R.string.download_ing);
                View view = LayoutInflater.from(LauncherActivity.this).inflate(R.layout.progress_download_layout, null);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 拒绝时, 关闭页面, 缺少主要权限, 无法运行
        if (requestCode == PermissionsDef.PERMISSION_REQ_CODE) {
            if (resultCode == PermissionsDef.PERMISSIONS_DENIED) {
                ActivityTack.getInstanse().exit();
                SessionContext.destroy();
            }
            mStartTime = System.currentTimeMillis();
        }
    }

    //启动时设置一次定位信息
    @Override
    public void onLocation(boolean isSuccess, AMapLocation aMapLocation) {
        LogUtil.i(TAG, "onLocation()");
        if (isSuccess && null != aMapLocation) {
            LocationInfo.instance.setInfo(String.valueOf(aMapLocation.getLongitude()),
                    String.valueOf(aMapLocation.getLatitude()),
                    CityParseUtils.getProvinceString(aMapLocation.getProvince()),
                    CityParseUtils.getProvinceString(aMapLocation.getCity()),
                    String.valueOf(aMapLocation.getAdCode()));
        }
    }
}
