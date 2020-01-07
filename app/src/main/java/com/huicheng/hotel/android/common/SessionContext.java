package com.huicheng.hotel.android.common;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.ArrayMap;

import com.alibaba.fastjson.JSON;
import com.huicheng.hotel.android.content.AppConst;
import com.huicheng.hotel.android.requestbuilder.bean.AirCompanyInfoBean;
import com.huicheng.hotel.android.requestbuilder.bean.CityAirportInfoBean;
import com.huicheng.hotel.android.requestbuilder.bean.HomeBannerInfoBean;
import com.huicheng.hotel.android.requestbuilder.bean.HotelMapInfoBean;
import com.huicheng.hotel.android.requestbuilder.bean.UserInfo;
import com.huicheng.hotel.android.tools.PinyinUtils;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * 缓存全局数据
 */
public class SessionContext {

    private static final String TAG = "SessionContext";

    public static UserInfo mUser;                   // 用户信息
    private static String mTicket;                  // 票据信息

    private static List<HomeBannerInfoBean> bannerList = new ArrayList<>();
    private static List<CityAirportInfoBean> airportList = new ArrayList<>(); //机场列表
    private static ArrayMap<String, AirCompanyInfoBean> airCompanyMap = new ArrayMap<>(); //航司ArrayMap

    private static List<HotelMapInfoBean> allDayList = new ArrayList<>();
    private static List<HotelMapInfoBean> clockList = new ArrayList<>();
    private static List<HotelMapInfoBean> ygrList = new ArrayList<>();
    private static List<HotelMapInfoBean> hhyList = new ArrayList<>();

    /**
     * 是否登录
     */
    public static boolean isLogin() {
        return StringUtil.notEmpty(getTicket()) && mUser != null;
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
        initData();
    }

    private static void initData() {
        if (null != mFirstLaunchDoActionFlag) {
            mFirstLaunchDoActionFlag.clear();
        }
    }

    public static List<HomeBannerInfoBean> getBannerList() {
        return bannerList;
    }

    public static void setBannerList(List<HomeBannerInfoBean> list) {
        if (null != list) {
            bannerList = list;
        }
    }

    public static List<HotelMapInfoBean> getAllDayList() {
        return allDayList;
    }

    public static void setAllDayList(List<HotelMapInfoBean> list) {
        if (null != list) {
            allDayList = list;
        }
    }

    public static List<HotelMapInfoBean> getClockList() {
        return clockList;
    }

    public static void setClockList(List<HotelMapInfoBean> list) {
        if (null != list) {
            clockList = list;
        }
    }

    public static List<HotelMapInfoBean> getYgrList() {
        return ygrList;
    }

    public static void setYgrList(List<HotelMapInfoBean> list) {
        if (null != list) {
            ygrList = list;
        }
    }

    public static List<HotelMapInfoBean> getHhyList() {
        return hhyList;
    }

    public static void setHhyList(List<HotelMapInfoBean> list) {
        if (null != list) {
            hhyList = list;
        }
    }

    public static void setAirCompanyMap(List<AirCompanyInfoBean> list) {
        if (list != null && list.size() > 0) {
            airCompanyMap.clear();
            for (AirCompanyInfoBean bean : list) {
                airCompanyMap.put(bean.code, bean);
            }
        }
    }

    public static ArrayMap<String, AirCompanyInfoBean> getAirCompanyMap() {
        return airCompanyMap;
    }

    public static void setAirportList(List<CityAirportInfoBean> list) {
        if (null != list) {
            airportList = list;
        }
    }

    public static List<CityAirportInfoBean> getAirportList() {
        return airportList;
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
//        清空极光推送的别名设置
//        JPushInterface.setAliasAndTags(PRJApplication.getInstance(), "", null, new TagAliasCallback() {
//            @Override
//            public void gotResult(int i, String s, Set<String> set) {
//                LogUtil.i(TAG, ((i == 0) ? "设置成功" : "设置失败") + ", Alias = " + s + ", Tag = " + set);
//            }
//        });
    }

    /**
     * 销毁数据
     */
    public static void destroy() {
        cleanUserInfo();
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
        LogUtil.i(TAG, "VersionComparison():\nserVersion " + versionServer + "\nlocVersion " + versionLocal);
        if (versionServer == null || versionServer.length() == 0 || versionLocal == null || versionLocal.length() == 0)
            throw new IllegalArgumentException("Invalid parameter!");

        int index1 = 0;
        int index2 = 0;
        while (index1 < versionServer.length() && index2 < versionLocal.length()) {
            LogUtil.i(TAG, "VersionComparison() :index======" + index1 + "====" + index2 + "=====");
            int[] number1 = getValue(versionServer, index1);
            int[] number2 = getValue(versionLocal, index2);
            LogUtil.i(TAG, "VersionComparison() :number=====" + number1[0] + "====" + number2[0] + "=====");

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

    private static HashMap<String, Boolean> mFirstLaunchDoActionFlag = new HashMap<>();

    public static boolean isFirstLaunchDoAction(String key) {
        boolean isFirst = true;
        if (!mFirstLaunchDoActionFlag.containsKey(key)) {
            mFirstLaunchDoActionFlag.put(key, false);
        } else {
            isFirst = mFirstLaunchDoActionFlag.get(key);
        }
        return isFirst;
    }
}
