package com.huicheng.hotel.android.app;

import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Build;
import android.support.multidex.MultiDex;
import android.webkit.WebView;

import com.fm.openinstall.OpenInstall;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.broatcast.UnLoginBroadcastReceiver;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.CrashHandler;
import com.huicheng.hotel.android.common.NetURL;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.control.AMapLocationControl;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.prj.sdk.app.AppContext;
import com.prj.sdk.constants.BroadCastConst;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.ActivityTack;
import com.squareup.leakcanary.LeakCanary;
import com.umeng.analytics.MobclickAgent;
import com.umeng.socialize.Config;
import com.umeng.socialize.PlatformConfig;

import java.util.Collections;

import cn.jpush.android.api.JPushInterface;

import static com.umeng.socialize.utils.DeviceConfig.context;

public class PRJApplication extends Application {
    UnLoginBroadcastReceiver mReceiver = new UnLoginBroadcastReceiver();

    @Override
    public void onCreate() {
        super.onCreate();
        AppContext.init(this);
        CrashHandler.getInstance().init(this);
        AMapLocationControl.getInstance().startLocationOnce(this);
        PlatformConfig.setWeixin(getResources().getString(R.string.wx_appid), getResources().getString(R.string.wx_appsecret));
        PlatformConfig.setQQZone(getResources().getString(R.string.qq_appid), getResources().getString(R.string.qq_appkey));
        Config.DEBUG = AppConst.ISDEVELOP;
        MobclickAgent.setScenarioType(this, MobclickAgent.EScenarioType.E_UM_NORMAL);
        MobclickAgent.startWithConfigure(new MobclickAgent.UMAnalyticsConfig(this, getResources().getString(R.string.umeng_appkey), SessionContext.getAppMetaData(this, "UMENG_CHANNEL")));
        MobclickAgent.setDebugMode(AppConst.ISDEVELOP);// 普通测试流程，打开调试模式
        MobclickAgent.openActivityDurationTrack(false); // 禁止默认的页面统计方式
        Collections.addAll(DataLoader.getInstance().mCacheUrls, NetURL.CACHE_URL);

        // JPush
        JPushInterface.setDebugMode(AppConst.ISDEVELOP); // 设置开启日志,发布时请关闭日志
        JPushInterface.init(this); // 初始化 JPush

        // 动态注册登录广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadCastConst.UNLOGIN_ACTION);
        registerReceiver(mReceiver, intentFilter);

        // openInstalled
        OpenInstall.init(this);
        OpenInstall.setDebug(AppConst.ISDEVELOP);

        //科大讯飞语音识别初始化
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=" + getResources().getString(R.string.iflytek_appid));

        // Debug模式下打开webview debug开关
        if (AppConst.ISDEVELOP) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                WebView.setWebContentsDebuggingEnabled(true);
            }
//            LeakCanary.install(this);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        ActivityTack.getInstanse().exit();
        DataLoader.getInstance().clearRequests();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
