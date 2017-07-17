package com.huicheng.hotel.android.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.View;

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
import com.huicheng.hotel.android.tools.PinyinUtils;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.huicheng.hotel.android.ui.dialog.CustomDialog;
import com.huicheng.hotel.android.ui.service.TestIntentservice;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.ActivityTack;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
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
public class WelcomeActivity extends BaseActivity implements AppInstallListener, AppWakeUpListener, DataCallback {

    private final String TAG = getClass().getSimpleName();
    private long start = 0;                                // 记录启动时间

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_welcome_layout);
        start = SystemClock.elapsedRealtime();
        onNewIntent(getIntent());
        initViews();
        initParams();
        initListeners();
//        initJsonData();
        requestAppVersionInfo();

        System.out.println("IntentService begin ......");
        TestIntentservice.startParse(this);
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

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            View decorView = getWindow().getDecorView();
//            状态栏透明
//            getWindow().setFlags(
//                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
//                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION //隐藏Navigation Bar
                            | View.SYSTEM_UI_FLAG_FULLSCREEN //隐藏Status Bar
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    public void initListeners() {
        super.initListeners();
    }

    @Override
    public void onClick(View v) {
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
        RequestBeanBuilder b = RequestBeanBuilder.create(false);
        b.addBody("contype", "0"); // 0:android, 1:ios
        ResponseData d = b.syncRequest(b);
        d.path = NetURL.APP_INFO;
        d.flag = AppConst.APP_INFO;

        requestID = DataLoader.getInstance().loadData(this, d);
    }

    public void intentActivity() {
        Intent intent;
        if (SessionContext.getWakeUpAppData() != null || SessionContext.getRecommandAppData() != null) {
            intent = new Intent(this, MainFragmentActivity.class);
            String value = BundleNavi.getInstance().getString("path");
            if (value != null && !value.equals("")) {
                BundleNavi.getInstance().putString("path", value);
            }
        } else {
            intent = new Intent(this, GuideSwitchActivity.class);
        }
        startActivity(intent);
        finish();
    }

    private void goToNextActivity() {
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
        LogUtil.d(TAG, "onInstallFinish()");
        if (error == null) {
            LogUtil.d(TAG, "OpenInstall install-data : " + appData.toString());
            SessionContext.setRecommandAppData(appData);
            finish();
        } else {
            LogUtil.d(TAG, "error : " + error.toString());
        }
    }

    @Override
    public void onWakeUpFinish(AppData appData, Error error) {
        LogUtil.d(TAG, "onWakeUpFinish()");
        if (error == null) {
            SessionContext.setWakeUpAppData(appData);
            finish();
            LogUtil.d("MainActivity", "OpenInstall wakeup-data : " + appData.toString());
        } else {
            LogUtil.d("MainActivity", "error : " + error.toString());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void preExecute(ResponseData request) {

    }

    @Override
    public void notifyMessage(ResponseData request, ResponseData response) throws Exception {
        if (response != null && response.body != null) {
            if (request.flag == AppConst.APP_INFO) {
                System.out.println("json = " + response.body.toString());
                AppInfoBean bean = JSON.parseObject(response.body.toString(), AppInfoBean.class);
                SharedPreferenceUtil.getInstance().setString(AppConst.APPINFO, response.body.toString(), false);
                if (bean.isforce == 0) {
                    showUpdateDialog(bean);
                    return;
                }
                goToNextActivity();
            }
        }
    }

    @Override
    protected void onNotifyError(ResponseData request) {
        super.onNotifyError(request);
        goToNextActivity();
    }

    private void showUpdateDialog(final AppInfoBean bean) {
        final CustomDialog dialog = new CustomDialog(this);
        dialog.setTitle(bean.tip);
        dialog.setMessage(bean.updesc);
        dialog.setNegativeButton("退出", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
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
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                DataLoader.getInstance().clearRequests();
                ActivityTack.getInstanse().exit();
            }
        });
        dialog.show();
    }
}