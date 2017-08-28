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
import android.view.View;
import android.view.WindowManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fm.openinstall.OpenInstall;
import com.fm.openinstall.listener.AppInstallListener;
import com.fm.openinstall.listener.AppWakeUpListener;
import com.fm.openinstall.model.AppData;
import com.fm.openinstall.model.Error;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.NetURL;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.control.BundleNavi;
import com.huicheng.hotel.android.net.RequestBeanBuilder;
import com.huicheng.hotel.android.net.bean.AppInfoBean;
import com.huicheng.hotel.android.net.bean.HomeBannerInfoBean;
import com.huicheng.hotel.android.tools.PinyinUtils;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.huicheng.hotel.android.ui.dialog.CustomDialog;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.ActivityTack;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;
import com.prj.sdk.widget.wheel.adapters.CityAreaInfoBean;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
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
        onNewIntent(getIntent());
        initViews();
        initParams();
        initListeners();
        new Thread() {
            @Override
            public void run() {
                super.run();
                initJsonData();
                requestHomeBannerInfo();
                requestActiveAboutInfo();
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

    //启动时解析area.json数据
    private void initJsonData() {
        LogUtil.i(TAG, "initJsonData() begin....");
        try {
            InputStreamReader inputReader = new InputStreamReader(getResources().getAssets().open("area.json"));
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line = "";
            StringBuilder result = new StringBuilder();
            while ((line = bufReader.readLine()) != null) {
                result.append(line);
            }
            inputReader.close();
            bufReader.close();
            JSONObject mJsonObject = JSONObject.parseObject(result.toString());
            if (mJsonObject != null) {
                if (mJsonObject.containsKey("citylist")) {
                    List<CityAreaInfoBean> temp = JSON.parseArray(mJsonObject.getString("citylist"), CityAreaInfoBean.class);
                    if (temp != null && temp.size() > 0) {
                        SessionContext.setCityAreaList(temp);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<String> cityIndexList = new ArrayList<>();
        List<CityAreaInfoBean> cityNameList = new ArrayList<>();

        for (int i = 0; i < SessionContext.getCityAreaList().size(); i++) {
            for (int j = 0; j < SessionContext.getCityAreaList().get(i).list.size(); j++) {
                cityNameList.add(SessionContext.getCityAreaList().get(i).list.get(j));
                String shortName = SessionContext.getCityAreaList().get(i).list.get(j).shortName;
                char c = PinyinUtils.getFirstSpell(shortName).charAt(0);
                String str = String.valueOf(c).toUpperCase();
                if (!cityIndexList.contains(str)) {
                    cityIndexList.add(str);
                }
            }
        }
        Collections.sort(cityIndexList);
        SessionContext.setCityIndexList(cityIndexList);

        Map<String, List<String>> nameMap = new HashMap<>();
        Map<String, List<CityAreaInfoBean>> areaMap = new HashMap<>();
        for (int i = 0; i < cityIndexList.size(); i++) {
            List<CityAreaInfoBean> tempArea = new ArrayList<>();
            List<String> tempStr = new ArrayList<>();
            for (int j = 0; j < cityNameList.size(); j++) {
                char c = ' ';
                String shortName = cityNameList.get(j).shortName;
                String str = SessionContext.getFirstSpellChat(shortName).toUpperCase();
                if (cityIndexList.get(i).equals(str)) {
                    tempArea.add(cityNameList.get(j));
                }
                tempStr.add(shortName);
            }
            areaMap.put(cityIndexList.get(i), tempArea);
            nameMap.put(cityIndexList.get(i), tempStr);
        }
        SessionContext.setCityAreaMap(areaMap);
        SessionContext.setCityNameMap(nameMap);
        LogUtil.i(TAG, "initJsonData() end....");
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

    private void requestActiveAboutInfo() {
        LogUtil.i(TAG, "requestActiveAboutInfo()");
        RequestBeanBuilder b = RequestBeanBuilder.create(false);
        ResponseData d = b.syncRequest(b);
        d.path = NetURL.ACTIVE_ABOUT;
        d.flag = AppConst.ACTIVE_ABOUT;
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
            if (SessionContext.getWakeUpAppData() != null) {
                intent = new Intent(this, MainFragmentActivity.class);
                String value = BundleNavi.getInstance().getString("path");
                if (value != null && !value.equals("")) {
                    BundleNavi.getInstance().putString("path", value);
                }
            } else {
                intent = new Intent(this, GuideSwitchActivity.class);
            }
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
                    if (bean.isforce == 0) {
                        mTag.remove(AppConst.APP_INFO);
                        showUpdateDialog(bean);
                    }
                }
            } else if (request.flag == AppConst.ACTIVE_ABOUT) {
                mTag.put(AppConst.ACTIVE_ABOUT, AppConst.ACTIVE_ABOUT);
                LogUtil.i(TAG, "json = " + response.body.toString());
                if (StringUtil.notEmpty(response.body.toString())) {
                    SessionContext.setHasActive(Boolean.parseBoolean(response.body.toString()));
                }
            } else if (request.flag == AppConst.HOTEL_BANNER) {
                mTag.put(AppConst.HOTEL_BANNER, AppConst.HOTEL_BANNER);
                LogUtil.i(TAG, "Hotel Main json = " + response.body.toString());
                List<HomeBannerInfoBean> temp = JSON.parseArray(response.body.toString(), HomeBannerInfoBean.class);
                SessionContext.setBannerList(temp);
            }

            if (mTag != null && mTag.size() == 3) {
                goToNextActivity();
            }
        }
    }

    @Override
    public void onNotifyError(ResponseData request) {
        mTag.put(request.flag, request.flag);
        if (mTag != null && mTag.size() == 3) {
            goToNextActivity();
        }
    }

    private void showUpdateDialog(final AppInfoBean bean) {
        final CustomDialog dialog = new CustomDialog(this);
        dialog.setTitle(bean.tip);
        dialog.setMessage(bean.updesc);
        dialog.setNegativeButton("退出", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DataLoader.getInstance().clearRequests();
                ActivityTack.getInstanse().exit();
            }
        });
        dialog.setPositiveButton("升级", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                intent.setData(Uri.parse(bean.apkurls));
                startActivity(intent);
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }
}
