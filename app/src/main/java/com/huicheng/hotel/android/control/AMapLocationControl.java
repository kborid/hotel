package com.huicheng.hotel.android.control;

import android.content.Context;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.tools.CityParseUtils;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;

/**
 * 高德定位
 */
public class AMapLocationControl {
    private final String TAG = getClass().getSimpleName();

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
            LogUtil.i(TAG, "onLocationChanged()");
            stopLocation();
            if (null != aMapLocation) {
                if (aMapLocation.getErrorCode() == 0) {
                    //定位成功回调信息，设置相关消息
                    SharedPreferenceUtil.getInstance().setString(AppConst.LOCATION_LON, String.valueOf(aMapLocation.getLongitude()), false);
                    SharedPreferenceUtil.getInstance().setString(AppConst.LOCATION_LAT, String.valueOf(aMapLocation.getLatitude()), false);
                    String province = CityParseUtils.getProvinceString(aMapLocation.getProvince());
                    String city = CityParseUtils.getProvinceString(aMapLocation.getCity());
                    String siteId = String.valueOf(aMapLocation.getAdCode());
                    SharedPreferenceUtil.getInstance().setString(AppConst.PROVINCE, province, false);
                    SharedPreferenceUtil.getInstance().setString(AppConst.CITY, city, false);
                    SharedPreferenceUtil.getInstance().setString(AppConst.SITEID, siteId, false);
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
