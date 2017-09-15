package com.huicheng.hotel.android.ui.activity;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.fm.openinstall.OpenInstall;
import com.fm.openinstall.listener.AppInstallListener;
import com.fm.openinstall.listener.AppWakeUpListener;
import com.fm.openinstall.model.AppData;
import com.fm.openinstall.model.Error;
import com.huicheng.hotel.android.BuildConfig;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.NetURL;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.control.DataCleanManager;
import com.huicheng.hotel.android.net.RequestBeanBuilder;
import com.huicheng.hotel.android.net.bean.AppInfoBean;
import com.huicheng.hotel.android.net.bean.HomeBannerInfoBean;
import com.huicheng.hotel.android.tools.CityParseUtils;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.huicheng.hotel.android.ui.dialog.CustomDialog;
import com.prj.sdk.algo.MD5Tool;
import com.prj.sdk.constants.InfoType;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.net.down.DownCallback;
import com.prj.sdk.net.down.DownLoaderTask;
import com.prj.sdk.util.ActivityTack;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;
import com.prj.sdk.widget.CustomToast;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 欢迎页面
 */
public class WelcomeActivity extends BaseActivity implements AppInstallListener, AppWakeUpListener {

    private final String TAG = getClass().getSimpleName();
    private long start = 0; // 记录启动时间
    private Map<Integer, Integer> mTag = new HashMap<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getWindow().setBackgroundDrawable(null);
        initStatus();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_welcome_layout);
        start = SystemClock.elapsedRealtime();

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
        initViews();
        initParams();
        initListeners();

        //用户第一次启动时，调用后台封装的广点通的接口，统计激活量
        if (SharedPreferenceUtil.getInstance().getBoolean(AppConst.IS_FIRST_LAUNCH, true)) {
            requestGDTInterface();
        }

        new Thread() {
            @Override
            public void run() {
                super.run();
                CityParseUtils.initAreaJsonData(WelcomeActivity.this);
                requestHomeBannerInfo();
                requestAppVersionInfo();
            }
        }.start();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void initStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View decoderView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
            decoderView.setSystemUiVisibility(option);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        } else {
            WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        OpenInstall.getWakeUp(this, this);
    }

    @Override
    public void initViews() {
        super.initViews();
    }

    public void initParams() {
        super.initParams();
        OpenInstall.getInstall(this, this);
        Utils.initScreenSize(this);// 设置手机屏幕大小
        SessionContext.initUserInfo();
    }

    private void requestAppVersionInfo() {
        LogUtil.i(TAG, "requestAppVersionInfo()");
        RequestBeanBuilder b = RequestBeanBuilder.create(false);
        b.addBody("contype", "0"); // 0:android, 1:ios
        ResponseData d = b.syncRequest(b);
        d.path = NetURL.APP_INFO;
        d.flag = AppConst.APP_INFO;
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void requestGDTInterface() {
        LogUtil.i(TAG, "requestGDTInterface()");
        RequestBeanBuilder b = RequestBeanBuilder.create(false);
        b.addBody("muid", MD5Tool.getMD5(Utils.getIMEI()));
        ResponseData d = b.syncRequest(b);
        d.path = NetURL.AD_GDT_IF;
        d.flag = AppConst.AD_GDT_IF;
        d.type = InfoType.GET_REQUEST.toString();
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void requestHomeBannerInfo() {
        LogUtil.i(TAG, "requestHotelBanner()");
        RequestBeanBuilder b = RequestBeanBuilder.create(false);
        ResponseData d = b.syncRequest(b);
        d.path = NetURL.HOTEL_BANNER;
        d.flag = AppConst.HOTEL_BANNER;
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    public void intentActivity() {
        Intent intent;
        boolean isFirstLaunch = false;
        isFirstLaunch = SharedPreferenceUtil.getInstance().getBoolean(AppConst.IS_FIRST_LAUNCH, true);
        if (!isFirstLaunch) {
            intent = new Intent(this, GuideSwitchActivity.class);
        } else {
            intent = new Intent(this, GuideLauncherActivity.class);
        }
        startActivity(intent);
        finish();
    }

    private void goToNextActivity() {
        LogUtil.i(TAG, "goToNextActivity()");
        long end = SystemClock.elapsedRealtime();

        long LOADING_TIME = 1500;
        if (end - start < LOADING_TIME) {
            // 延迟加载
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    intentActivity();
                }
            }, LOADING_TIME - (end - start));

        } else {
            intentActivity();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            DataLoader.getInstance().clearRequests();
            ActivityTack.getInstanse().exit();
            SessionContext.destroy();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onInstallFinish(AppData appData, com.fm.openinstall.model.Error error) {
        LogUtil.i(TAG, "onInstallFinish()");
        if (error == null) {
            LogUtil.i(TAG, "OpenInstall install-data : " + appData.toString());
            SessionContext.setRecommandAppData(appData);
        } else {
            LogUtil.i(TAG, "error : " + error.toString());
        }
    }

    @Override
    public void onWakeUpFinish(AppData appData, Error error) {
        LogUtil.i(TAG, "onWakeUpFinish()");
        if (error == null) {
            SessionContext.setWakeUpAppData(appData);
            LogUtil.i(TAG, "OpenInstall wakeup-data : " + appData.toString());
        } else {
            LogUtil.i(TAG, "error : " + error.toString());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.i(TAG, "onResume()");
    }

    @Override
    public void preExecute(ResponseData request) {
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
                LogUtil.i(TAG, "Hotel Main json = " + response.body.toString());
                List<HomeBannerInfoBean> temp = JSON.parseArray(response.body.toString(), HomeBannerInfoBean.class);
                SessionContext.setBannerList(temp);
            }

            if (mTag != null && mTag.size() == 2) {
                goToNextActivity();
            }
        }
    }

    @Override
    public void onNotifyError(ResponseData request) {
        mTag.put(request.flag, request.flag);
        if (mTag != null && mTag.size() == 2) {
            goToNextActivity();
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
                DataLoader.getInstance().clearRequests();
                ActivityTack.getInstanse().exit();
            }
        });
        dialog.setPositiveButton(getString(R.string.update), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final CustomDialog pd = new CustomDialog(WelcomeActivity.this);
                pd.setTitle(R.string.download_ing);
                View view = LayoutInflater.from(WelcomeActivity.this).inflate(R.layout.progress_download_layout, null);
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
}
