package com.huicheng.hotel.android;

import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Build;
import android.support.multidex.MultiDex;
import android.webkit.WebView;

import com.fm.openinstall.OpenInstall;
import com.huicheng.hotel.android.broadcast.UnLoginBroadcastReceiver;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.CrashHandler;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.permission.PermissionsChecker;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.prj.sdk.app.AppContext;
import com.prj.sdk.constants.BroadCastConst;
import com.prj.sdk.util.LogUtil;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.umeng.analytics.MobclickAgent;
import com.umeng.socialize.Config;
import com.umeng.socialize.PlatformConfig;

import cn.jpush.android.api.JPushInterface;

public class PRJApplication extends Application {

    private final static String TAG = "PRJApplication";
    private static PRJApplication instance;

    public static PRJApplication getInstance() {
        return instance;
    }

    public PRJApplication() {
        instance = this;
    }

    private RefWatcher refWatcher;
    private PermissionsChecker mPermissionsChecker;

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.i(TAG, "The App Start--->>> onCreate()");
        AppContext.init(this);
//        Collections.addAll(DataLoader.getInstance().mCacheUrls, NetURL.CACHE_URL);
//        BDLocationControl.getInstance().startLocationOnce();
        CrashHandler.getInstance().init();
        refWatcher = LeakCanary.install(this);
        mPermissionsChecker = new PermissionsChecker(this);

        //友盟统计、分享、第三方登录
        PlatformConfig.setWeixin(getResources().getString(R.string.wx_appid), getResources().getString(R.string.wx_appsecret));
        PlatformConfig.setQQZone(getResources().getString(R.string.qq_appid), getResources().getString(R.string.qq_appkey));
        Config.DEBUG = AppConst.ISDEVELOP;
        MobclickAgent.setScenarioType(this, MobclickAgent.EScenarioType.E_UM_NORMAL);
        MobclickAgent.startWithConfigure(new MobclickAgent.UMAnalyticsConfig(this, getResources().getString(R.string.umeng_appkey), SessionContext.getAppMetaData(this, "UMENG_CHANNEL")));
        MobclickAgent.setDebugMode(AppConst.ISDEVELOP);// 普通测试流程，打开调试模式
        MobclickAgent.openActivityDurationTrack(false); // 禁止默认的页面统计方式

        // JPush
        JPushInterface.init(this); // 初始化 JPush
        JPushInterface.setDebugMode(AppConst.ISDEVELOP); // 设置开启日志,发布时请关闭日志

        // openInstall
        OpenInstall.init(this);
        OpenInstall.setDebug(AppConst.ISDEVELOP);

        //科大讯飞语音识别初始化
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=" + getResources().getString(R.string.iflytek_appid));

        //动态注册未登录广播(静态注册，部分OPPO手机无法接收广播)
        UnLoginBroadcastReceiver unLoginBroadcastReceiver = new UnLoginBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadCastConst.UNLOGIN_ACTION);
        registerReceiver(unLoginBroadcastReceiver, intentFilter);

        // Debug模式下打开webView debug开关
        if (AppConst.ISDEVELOP) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                WebView.setWebContentsDebuggingEnabled(true);
            }
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        LogUtil.i(TAG, "The App attach base context--->>> attachBaseContext()");
        MultiDex.install(this);
    }

    public static RefWatcher getRefWatcher(Context context) {
        PRJApplication application = (PRJApplication) context.getApplicationContext();
        return application.refWatcher;
    }

    public static PermissionsChecker getPermissionsChecker(Context context) {
        PRJApplication app = (PRJApplication) context.getApplicationContext();
        return app.mPermissionsChecker;
    }
}
