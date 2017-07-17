package com.huicheng.hotel.android.ui.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.tools.PinyinUtils;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.widget.wheel.adapters.CityAreaInfoBean;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestIntentservice extends IntentService {
    private final String TAG = getClass().getSimpleName();

    private static final String PARSE_AREAJSON_ACTION = "com.huicheng.hotel.intentservice.action.PARSE_AREAJSON";

    public TestIntentservice() {
        super("IntentServiceDemo");
    }

    public static void startParse(Context context) {
        Intent intent = new Intent(context, TestIntentservice.class);
        intent.setAction(PARSE_AREAJSON_ACTION);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        System.out.println(TAG + ":" + "onHandleIntent()");
        final String action = intent.getAction();
        System.out.println("action = " + action);
        if (PARSE_AREAJSON_ACTION.equals(action)) {
            LogUtil.i(TAG, "outside-- initJsonData() start...");
            initJsonData();
            LogUtil.i(TAG, "outside-- initJsonData() enddd...");
            Intent broadcastIntent = new Intent("PARSE_JSONDATA_RESULT");
            broadcastIntent.putExtra("result", true);
            sendBroadcast(broadcastIntent);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println(TAG + ":" + "onCreate()");
    }

    @Override
    public void onStart(@Nullable Intent intent, int startId) {
        super.onStart(intent, startId);
        System.out.println(TAG + ":" + "onStart()");
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        System.out.println(TAG + ":" + "onStartCommand()");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println(TAG + ":" + "onDestroy()");
    }

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
}
