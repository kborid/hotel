package com.huicheng.hotel.android.control;

import android.content.Context;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.huicheng.hotel.android.common.AppConst;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;

/**
 * 高德定位
 */
public class AMapLocationControl {
    private static final String TAG = "AMapLocationControl";

    private AMapLocationClient mAmapLocationClient = null;
    private static AMapLocationControl instance = null;

    public static AMapLocationControl getInstance() {
        if (instance == null) {
            synchronized (AMapLocationControl.class) {
                if (instance == null) {
                    instance = new AMapLocationControl();
                }
            }
        }
        return instance;
    }

    private void initLocation(Context context, boolean isOnce) {
        if (mAmapLocationClient == null) {
            mAmapLocationClient = new AMapLocationClient(context);
        }
        AMapLocationClientOption option = new AMapLocationClientOption();
        option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        if (isOnce) {
            option.setOnceLocation(true);
        } else {
            option.setInterval(2000);
        }
        option.setNeedAddress(true);
        option.setMockEnable(true);
        mAmapLocationClient.setLocationOption(option);
    }

    public synchronized void startLocationOnce(Context context, boolean isOnce) {
        initLocation(context, isOnce);
        mAmapLocationClient.setLocationListener(listener);
        mAmapLocationClient.startLocation();
    }

    public synchronized void startLocationOnce(Context context, MyLocationListener myLocationListener, boolean isOnce) {
        initLocation(context, isOnce);
        mAmapLocationClient.setLocationListener(listener);
        this.myLocationListener = myLocationListener;
        mAmapLocationClient.startLocation();
    }

    public synchronized void startLocationAlways(Context context, AMapLocationListener listener) {
        initLocation(context, false);
        mAmapLocationClient.setLocationListener(listener);
        mAmapLocationClient.startLocation();
    }

    public void stopLocation() {
        if (mAmapLocationClient != null) {
            mAmapLocationClient.stopLocation();
            mAmapLocationClient.onDestroy();
            mAmapLocationClient = null;
        }
    }

    public boolean isStart() {
        boolean isStarted = false;
        if (mAmapLocationClient != null) {
            isStarted = mAmapLocationClient.isStarted();
        }
        return isStarted;
    }

    private AMapLocationListener listener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            LogUtil.i(TAG, "AMapLocationControl onLocationChanged()");
            stopLocation();
            if (null != aMapLocation) {
                if (aMapLocation.getErrorCode() == 0) {
                    //定位成功回调信息，设置相关消息
                    LogUtil.i(TAG, "=======location info======");
                    LogUtil.i(TAG, aMapLocation.toString().replace("#", "\n"));
                    try {
                        SharedPreferenceUtil.getInstance().setString(AppConst.LOCATION_LON, String.valueOf(aMapLocation.getLongitude()), false);
                        SharedPreferenceUtil.getInstance().setString(AppConst.LOCATION_LAT, String.valueOf(aMapLocation.getLatitude()), false);
                        String loc_province = aMapLocation.getProvince().replace("省", "");
                        String loc_city = aMapLocation.getCity().replace("市", "");
                        SharedPreferenceUtil.getInstance().setString(AppConst.LOCATION_PROVINCE, loc_province, false);
                        SharedPreferenceUtil.getInstance().setString(AppConst.LOCATION_CITY, loc_city, false);
                        SharedPreferenceUtil.getInstance().setString(AppConst.LOCATION_SITEID, String.valueOf(aMapLocation.getAdCode()), false);

//                        String last_province = SharedPreferenceUtil.getInstance().getString(AppConst.PROVINCE, "", false);
//                        String last_city = SharedPreferenceUtil.getInstance().getString(AppConst.CITY, "", false);
//                        String last_siteId = SharedPreferenceUtil.getInstance().getString(AppConst.SITEID, "", false);
//                        if (StringUtil.isEmpty(last_province) || StringUtil.isEmpty(last_city) || StringUtil.isEmpty(last_siteId)) {
//
                        SharedPreferenceUtil.getInstance().setString(AppConst.PROVINCE, loc_province, false);
                        SharedPreferenceUtil.getInstance().setString(AppConst.CITY, loc_city, false);
                        SharedPreferenceUtil.getInstance().setString(AppConst.SITEID, String.valueOf(aMapLocation.getAdCode()), false);
//                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (null != myLocationListener) {
                        myLocationListener.onLocation(aMapLocation);
                    }
                } else {
                    //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                    LogUtil.e(TAG, "location Error, ErrCode:"
                            + aMapLocation.getErrorCode() + ", errInfo:"
                            + aMapLocation.getErrorInfo());
                }
            }
        }
    };

    public interface MyLocationListener {
        void onLocation(AMapLocation aMapLocation);
    }

    private MyLocationListener myLocationListener = null;
}
