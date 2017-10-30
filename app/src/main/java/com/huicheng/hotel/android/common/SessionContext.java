package com.huicheng.hotel.android.common;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.fm.openinstall.model.AppData;
import com.huicheng.hotel.android.net.bean.HomeBannerInfoBean;
import com.huicheng.hotel.android.net.bean.HotelMapInfoBean;
import com.huicheng.hotel.android.net.bean.UserInfo;
import com.huicheng.hotel.android.net.bean.WeatherInfoBean;
import com.huicheng.hotel.android.tools.PinyinUtils;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.widget.wheel.adapters.CityAreaInfoBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 缓存全局数据
 */
public class SessionContext {

    private static final String TAG = "SessionContext";

    public static UserInfo mUser;                // 用户信息
    private static String mTicket;               // 票据信息
    private static AppData mAppData;          // OpenInstall数据
    private static WeatherInfoBean mWeatherInfo; //天气预报信息

    private static List<HomeBannerInfoBean> bannerList = new ArrayList<>();

    private static List<CityAreaInfoBean> cityAreaList = new ArrayList<>();
    private static Map<String, List<CityAreaInfoBean>> cityAreaMap = new HashMap<>();

    private static List<HotelMapInfoBean> allDayList = new ArrayList<>();
    private static List<HotelMapInfoBean> clockList = new ArrayList<>();
    private static List<HotelMapInfoBean> ygrList = new ArrayList<>();
    private static List<HotelMapInfoBean> hhyList = new ArrayList<>();

    /**
     * 是否登录
     */
    public static boolean isLogin() {
        boolean ret = false;
        if (StringUtil.notEmpty(getTicket()) && mUser != null) {
            ret = true;
        }
        return ret;
    }

    /**
     * 获取访问票据
     */
    public static String getTicket() {
        return mTicket;
    }

    public static void setTicket(String ticket) {
        mTicket = ticket;
    }

    public static AppData getOpenInstallAppData() {
        return mAppData;
    }

    public static void setOpenInstallAppData(AppData appData) {
        mAppData = appData;
    }

    /**
     * 初始化用户数据
     */
    public static void initUserInfo() {
        String json = SharedPreferenceUtil.getInstance().getString(AppConst.USER_INFO, "", true);
        String ticket = SharedPreferenceUtil.getInstance().getString(AppConst.ACCESS_TICKET, "", true);
        if (StringUtil.notEmpty(json) && StringUtil.notEmpty(ticket)) {
            mUser = JSON.parseObject(json, UserInfo.class);
            setTicket(ticket);
        }
    }

    public static List<HomeBannerInfoBean> getBannerList() {
        return bannerList;
    }

    public static void setBannerList(List<HomeBannerInfoBean> list) {
        if (null != list) {
            bannerList = list;
        } else {
            bannerList = new ArrayList<>();
        }
    }

    public static List<CityAreaInfoBean> getCityAreaList() {
        return cityAreaList;
    }

    public static void setCityAreaList(List<CityAreaInfoBean> list) {
        if (null != list) {
            cityAreaList = list;
        } else {
            cityAreaList = new ArrayList<>();
        }
    }

    public static Map<String, List<CityAreaInfoBean>> getCityAreaMap() {
        return cityAreaMap;
    }

    public static void setCityAreaMap(Map<String, List<CityAreaInfoBean>> map) {
        if (null != map) {
            cityAreaMap = map;
        } else {
            cityAreaMap = new HashMap<>();
        }
    }

    public static List<HotelMapInfoBean> getAllDayList() {
        return allDayList;
    }

    public static void setAllDayList(List<HotelMapInfoBean> list) {
        if (null != list) {
            allDayList = list;
        } else {
            allDayList = new ArrayList<>();
        }
    }

    public static List<HotelMapInfoBean> getClockList() {
        return clockList;
    }

    public static void setClockList(List<HotelMapInfoBean> list) {
        if (null != list) {
            clockList = list;
        } else {
            clockList = new ArrayList<>();
        }
    }

    public static List<HotelMapInfoBean> getYgrList() {
        return ygrList;
    }

    public static void setYgrList(List<HotelMapInfoBean> list) {
        if (null != list) {
            ygrList = list;
        } else {
            ygrList = new ArrayList<>();
        }
    }

    public static List<HotelMapInfoBean> getHhyList() {
        return hhyList;
    }

    public static void setHhyList(List<HotelMapInfoBean> list) {
        if (null != list) {
            hhyList = list;
        } else {
            hhyList = new ArrayList<>();
        }
    }

    public static WeatherInfoBean getWeatherInfo() {
        return mWeatherInfo;
    }
    public static void setWeatherInfo(WeatherInfoBean bean) {
        if (null != bean) {
            mWeatherInfo = bean;
        } else {
            mWeatherInfo = new WeatherInfoBean();
        }
    }

    /**
     * 清除用户数据和状态
     */
    public static void cleanUserInfo() {
        mUser = null;
        mTicket = null;
        SharedPreferenceUtil.getInstance().setString(AppConst.LAST_LOGIN_DATE, "", false);// 置空登录时间
        SharedPreferenceUtil.getInstance().setString(AppConst.USER_INFO, "", true);
        SharedPreferenceUtil.getInstance().setString(AppConst.ACCESS_TICKET, "", true);
//        SharedPreferenceUtil.getInstance().setInt(AppConst.SKIN_INDEX, 0);
    }

    /**
     * 销毁数据
     */
    public static void destroy() {
        mUser = null;
    }

    /**
     * 获取application中指定的meta-data
     *
     * @return String
     */
    public static String getAppMetaData(Context ctx, String key) {
        if (ctx == null || TextUtils.isEmpty(key)) {
            return null;
        }
        String resultData = null;
        try {
            PackageManager packageManager = ctx.getPackageManager();
            if (packageManager != null) {
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(ctx.getPackageName(), PackageManager.GET_META_DATA);
                if (applicationInfo != null) {
                    if (applicationInfo.metaData != null) {
                        resultData = applicationInfo.metaData.getString(key);
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return resultData;
    }

    public static String getUrl(String url, HashMap<String, String> params) {
        if (params != null) {
            Iterator<String> it = params.keySet().iterator();
            StringBuffer sb = null;
            while (it.hasNext()) {
                String key = it.next();
                String value = params.get(key);
                if (sb == null) {
                    sb = new StringBuffer();
                    sb.append("?");
                } else {
                    sb.append("&");
                }
                sb.append(key);
                sb.append("=");
                sb.append(value);
            }
            if (sb != null) {
                url += sb.toString();
            }
        }
        return url;
    }

    public static boolean isRunningApp(Context context, String packageName) {
        ActivityManager __am = (ActivityManager) context
                .getApplicationContext().getSystemService(
                        Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> __list = __am.getRunningTasks(100);
        if (__list.size() == 0)
            return false;
        for (ActivityManager.RunningTaskInfo task : __list) {
            if (task.topActivity.getPackageName().equals(packageName)) {

                Intent activityIntent = new Intent();
                activityIntent.setComponent(task.topActivity);
                activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                context.startActivity(activityIntent);
                return true;
            }
        }
        return false;
    }

    public static String getFirstSpellChat(String cityStr) {
        String firstSpellChar = "A";
        if ("长沙".equals(cityStr) || "重庆".equals(cityStr) || "长春".equals(cityStr) || "长治".equals(cityStr) || cityStr.startsWith("重")) {
            firstSpellChar = "C";
        } else if (cityStr.startsWith("厦")) {
            firstSpellChar = "X";
        } else {
            firstSpellChar = String.valueOf(PinyinUtils.getFirstSpell(cityStr).charAt(0)).toUpperCase();
        }

        return firstSpellChar;
    }

    /**
     * @param versionServer
     * @param versionLocal
     * @return if versionServer > versionLocal, return 1, if equal, return 0, else return -1
     */
    public static int VersionComparison(String versionServer, String versionLocal) {
        if (versionServer == null || versionServer.length() == 0 || versionLocal == null || versionLocal.length() == 0)
            throw new IllegalArgumentException("Invalid parameter!");

        int index1 = 0;
        int index2 = 0;
        while (index1 < versionServer.length() && index2 < versionLocal.length()) {
            int[] number1 = getValue(versionServer, index1);
            int[] number2 = getValue(versionLocal, index2);

            if (number1[0] < number2[0]) {
                return -1;
            } else if (number1[0] > number2[0]) {
                return 1;
            } else {
                index1 = number1[1] + 1;
                index2 = number2[1] + 1;
            }
        }
        if (index1 == versionServer.length() && index2 == versionLocal.length())
            return 0;
        if (index1 < versionServer.length())
            return 1;
        else
            return -1;
    }

    /**
     * @param version
     * @param index   the starting point
     * @return the number between two dots, and the index of the dot
     */
    private static int[] getValue(String version, int index) {
        int[] value_index = new int[2];
        StringBuilder sb = new StringBuilder();
        while (index < version.length() && version.charAt(index) != '.') {
            sb.append(version.charAt(index));
            index++;
        }
        try {
            value_index[0] = Integer.parseInt(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
            value_index[0] = 0;
        }
        value_index[1] = index;

        return value_index;
    }
}
