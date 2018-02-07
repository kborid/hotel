package com.huicheng.hotel.android.control;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.content.AppConst;
import com.huicheng.hotel.android.content.NetURL;
import com.huicheng.hotel.android.requestbuilder.RequestBeanBuilder;
import com.huicheng.hotel.android.requestbuilder.bean.CityAreaInfoBean;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.net.data.ResponseData;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author kborid
 * @date 2018/2/6 0006.
 */

public class CityListDataManager {
    private static final String TAG = "CityListDataManager";
    private static CityListDataManager instance = new CityListDataManager();
    public static List<CityAreaInfoBean> mCityAreaList = new ArrayList<>();
    public static HashMap<String, List<CityAreaInfoBean>> mCityAreaMap = new HashMap<>();

    public static CityListDataManager getInstance() {
        return instance;
    }

    private CityListDataManager() {
    }

    public interface OnCityParseCompleteListener {
        void onComplete(boolean isSuccess);
    }

    private OnCityParseCompleteListener listener = null;

    public void initCityList(OnCityParseCompleteListener listener) {
        this.listener = listener;
        requestCityListInfo();
    }

    private void initAreaJsonFromNet(String jsonStr) {
        LogUtil.i(TAG, "initAreaJsonFromNet() begin....");
        boolean isSuccess = false;
        LogUtil.i(TAG, "jsonStr = " + jsonStr);
        if (StringUtil.notEmpty(jsonStr)) {
            mCityAreaList = JSON.parseArray(jsonStr, CityAreaInfoBean.class);

            String cityMapJsonStr = SharedPreferenceUtil.getInstance().getString(AppConst.CITY_HOTEL_JSON, "", false);
            if (StringUtil.isEmpty(cityMapJsonStr)) {
                if (mCityAreaList.size() > 0) {
                    HashMap<String, List<CityAreaInfoBean>> cityAreaMap = new HashMap<>();
                    for (int i = 0; i < mCityAreaList.size(); i++) {
                        for (int j = 0; j < mCityAreaList.get(i).list.size(); j++) {
                            String shortName = mCityAreaList.get(i).list.get(j).shortName;
                            String str = SessionContext.getFirstSpellChat(shortName).toUpperCase(); //转大写
                            List<CityAreaInfoBean> tmp;
                            if (!cityAreaMap.containsKey(str)) {
                                tmp = new ArrayList<>();
                            } else {
                                tmp = cityAreaMap.get(str);
                            }
                            tmp.add(mCityAreaList.get(i).list.get(j));
                            cityAreaMap.put(str, tmp);
                        }
                    }
                    cityMapJsonStr = new Gson().toJson(cityAreaMap);
                    SharedPreferenceUtil.getInstance().setString(AppConst.CITY_HOTEL_JSON, cityMapJsonStr, false);
                }
            }
            Type type = new TypeToken<HashMap<String, List<CityAreaInfoBean>>>() {
            }.getType();
            mCityAreaMap = JSON.parseObject(cityMapJsonStr, type);
            LogUtil.i(TAG, "cityMapJsonStr = " + cityMapJsonStr);
            isSuccess = true;
        } else {
            isSuccess = false;
        }
        if (null != listener) {
            listener.onComplete(isSuccess);
        }
        LogUtil.i(TAG, "initAreaJsonFromNet() end....");
    }

    private void requestCityListInfo() {
        LogUtil.i(TAG, "requestCityListInfo()");
        RequestBeanBuilder b = RequestBeanBuilder.create(false);
        ResponseData d = b.syncRequest(b);
        d.path = NetURL.COMMON_CITY_LIST;
        d.flag = AppConst.COMMON_CITY_LIST;
        DataLoader.getInstance().loadData(callback, d);
    }

    private DataCallback callback = new DataCallback() {
        @Override
        public void preExecute(ResponseData request) {
        }

        @Override
        public void notifyMessage(ResponseData request, ResponseData response) throws Exception {
            if (response != null && response.body != null) {
                if (request.flag == AppConst.COMMON_CITY_LIST) {
                    LogUtil.i(TAG, "json = " + response.body.toString());
                    String jsonStr = SharedPreferenceUtil.getInstance().getString(AppConst.CITY_HOTEL_JSON_FILE, "", false);
                    JSONObject mJson = JSON.parseObject(response.body.toString());
                    if (null != mJson) {
                        String localVersion = SharedPreferenceUtil.getInstance().getString(AppConst.CITY_LIST_JSON_VERSION, "", false);
                        String serverVersion = mJson.containsKey("version") ? mJson.getString("version") : "";
                        LogUtil.i(TAG, "cityList version：local = " + localVersion + ", server = " + serverVersion);
                        if (localVersion.compareTo(serverVersion) < 0) {
                            SharedPreferenceUtil.getInstance().setString(AppConst.CITY_LIST_JSON_VERSION, serverVersion, false);
                            if (mJson.containsKey("citylist")) {
                                SharedPreferenceUtil.getInstance().setString(AppConst.CITY_HOTEL_JSON_FILE, mJson.getString("citylist"), false);
                                jsonStr = mJson.getString("citylist");
                            }
                        }
                    }
                    initAreaJsonFromNet(jsonStr);
                }
            }
        }

        @Override
        public void notifyError(ResponseData request, ResponseData response, Exception e) {
            String localCityJsonStr = SharedPreferenceUtil.getInstance().getString(AppConst.CITY_HOTEL_JSON_FILE, "", false);
            initAreaJsonFromNet(localCityJsonStr);
        }
    };


    //    private static String parseJsonFileAssets(Context context) {
//        LogUtil.i(TAG, "parseJsonFileAssets()");
//        StringBuilder stringBuilder = new StringBuilder();
//        try {
//            AssetManager assetManager = context.getAssets();
//            BufferedReader bf = new BufferedReader(new InputStreamReader(assetManager.open("area.json")));
//            String line;
//            while ((line = bf.readLine()) != null) {
//                stringBuilder.append(line);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        String finalRet = "";
//        JSONObject mJsonObject = JSONObject.parseObject(stringBuilder.toString());
//        if (mJsonObject != null && mJsonObject.containsKey("citylist")) {
//            finalRet = mJsonObject.getString("citylist");
//        }
//        SharedPreferenceUtil.getInstance().setString(AppConst.CITY_HOTEL_JSON_FILE, finalRet, false);
//        return finalRet;
//    }
//
//    public static void initAreaJsonFromAssets(Context context) {
//        LogUtil.i(TAG, "initAreaJsonFromAssets() begin....");
//        String cityListJsonStr = SharedPreferenceUtil.getInstance().getString(AppConst.CITY_HOTEL_JSON_FILE, "", false);
//        boolean isFocusParse = false;
//        if (StringUtil.isEmpty(cityListJsonStr)) {
//            isFocusParse = true;
//            cityListJsonStr = parseJsonFileAssets(context);
//        }
//        LogUtil.i(TAG, "cityListJsonStr = " + cityListJsonStr);
//        List<CityAreaInfoBean> mCityAreaList = JSON.parseArray(cityListJsonStr, CityAreaInfoBean.class);
//        SessionContext.setCityAreaList(mCityAreaList);
//
//        String cityMapJsonStr = SharedPreferenceUtil.getInstance().getString(AppConst.CITY_HOTEL_JSON, "", false);
//        if (StringUtil.notEmpty(cityListJsonStr) && (isFocusParse || StringUtil.isEmpty(cityMapJsonStr))) {
//            if (mCityAreaList.size() > 0) {
//                HashMap<String, List<CityAreaInfoBean>> cityAreaMap = new HashMap<>();
//                for (int i = 0; i < mCityAreaList.size(); i++) {
//                    for (int j = 0; j < mCityAreaList.get(i).list.size(); j++) {
//                        String shortName = mCityAreaList.get(i).list.get(j).shortName;
//                        String str = SessionContext.getFirstSpellChat(shortName).toUpperCase(); //转大写
//                        List<CityAreaInfoBean> tmp;
//                        if (!cityAreaMap.containsKey(str)) {
//                            tmp = new ArrayList<>();
//                        } else {
//                            tmp = cityAreaMap.get(str);
//                        }
//                        tmp.add(mCityAreaList.get(i).list.get(j));
//                        cityAreaMap.put(str, tmp);
//                    }
//                }
//                cityMapJsonStr = new Gson().toJson(cityAreaMap);
//                SharedPreferenceUtil.getInstance().setString(AppConst.CITY_HOTEL_JSON, cityMapJsonStr, false);
//            }
//        }
//        Type type = new TypeToken<HashMap<String, List<CityAreaInfoBean>>>() {
//        }.getType();
//        HashMap<String, List<CityAreaInfoBean>> cityAreaMap = JSON.parseObject(cityMapJsonStr, type);
//        SessionContext.setCityAreaMap(cityAreaMap);
//        LogUtil.i(TAG, "cityMapJsonStr = " + cityMapJsonStr);
//        LogUtil.i(TAG, "initAreaJsonFromAssets() end....");
//    }
}
