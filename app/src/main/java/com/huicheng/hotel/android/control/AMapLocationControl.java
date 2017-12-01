package com.huicheng.hotel.android.control;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.huicheng.hotel.android.PRJApplication;
import com.prj.sdk.util.LogUtil;

public class AMapLocationControl {
    private final String TAG = getClass().getSimpleName();

    private AMapLocationClientOption option = null;
    private AMapLocationClient mAmapLocationClient = null;
    private boolean isOnce = false;

    private static AMapLocationControl instance = null;

    public static AMapLocationControl getInstance() {
        if (instance == null) {
            instance = new AMapLocationControl();
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

    public void startLocationAlways(MyLocationListener myLocationListener) {
        startLocation(false, myLocationListener);
    }

    public void startLocationOnce(MyLocationListener myLocationListener) {
        startLocation(true, myLocationListener);
    }

    private void startLocation(boolean isOnce, MyLocationListener myLocationListener) {
        LogUtil.i(TAG, "startLocation()" + " isOnce = " + isOnce);
        this.isOnce = isOnce;
        this.myLocationListener = myLocationListener;
        mAmapLocationClient.startLocation();
    }

    public void stopLocation() {
        LogUtil.i(TAG, "stopLocation()");
        mAmapLocationClient.stopLocation();
        myLocationListener = null;
    }

    public boolean isStart() {
        return mAmapLocationClient.isStarted();
    }

    private AMapLocationListener listener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            LogUtil.i(TAG, "onLocationChanged()");
            boolean isSuccess = aMapLocation != null && aMapLocation.getErrorCode() == 0;
            if (null != aMapLocation && aMapLocation.getErrorCode() != 0) {
                LogUtil.e(TAG, "location Error, ErrCode:" + aMapLocation.getErrorCode() + ", errInfo:" + aMapLocation.getErrorInfo());
            }
            if (null != myLocationListener) {
                myLocationListener.onLocation(isSuccess, aMapLocation);
            }
            if (isOnce) {
                stopLocation();
            }
        }
    };

    public interface MyLocationListener {
        void onLocation(boolean isSuccess, AMapLocation aMapLocation);
    }

    private MyLocationListener myLocationListener = null;
}
