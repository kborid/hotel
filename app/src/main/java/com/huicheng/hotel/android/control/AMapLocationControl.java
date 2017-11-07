package com.huicheng.hotel.android.control;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.huicheng.hotel.android.PRJApplication;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.tools.CityParseUtils;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;

import java.util.ArrayList;

public class AMapLocationControl {
    private final String TAG = getClass().getSimpleName();

    private AMapLocationClientOption option = null;
    private AMapLocationClient mAmapLocationClient = null;
    private boolean isAlways = false;

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

    private AMapLocationControl() {
        mAmapLocationClient = new AMapLocationClient(PRJApplication.getInstance());
        option = new AMapLocationClientOption();
        initLocation();
    }

    private void initLocation() {
        option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        option.setInterval(2000);
        option.setNeedAddress(true);
        option.setMockEnable(true);
        mAmapLocationClient.setLocationListener(listener);
        mAmapLocationClient.setLocationOption(option);
    }

    public void startLocation() {
        startLocation(false);
    }

    public void startLocation(boolean isAlways) {
        this.isAlways = isAlways;
        mAmapLocationClient.startLocation();
    }

    public void stopLocation() {
        mAmapLocationClient.stopLocation();
        listeners.clear();
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
                    notifyLocationChanged(aMapLocation);
                    if (!isAlways) {
                        stopLocation();
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

    private ArrayList<MyLocationListener> listeners = new ArrayList<>();

    public void registerLocationListener(MyLocationListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void unRegisterLocationListener(MyLocationListener listener) {
        if (listeners.contains(listener)) {
            listeners.remove(listener);
        }
    }

    private void notifyLocationChanged(AMapLocation aMapLocation) {
        for (MyLocationListener listener : listeners) {
            listener.onLocation(aMapLocation);
        }
    }
}
