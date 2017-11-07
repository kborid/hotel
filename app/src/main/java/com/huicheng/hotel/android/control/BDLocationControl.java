package com.huicheng.hotel.android.control;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.huicheng.hotel.android.PRJApplication;
import com.prj.sdk.util.LogUtil;

/**
 * 百度定位
 */
public class BDLocationControl {
    private final String TAG = getClass().getSimpleName();
    private LocationClient mLocationClient = null;
    private LocationClientOption option = null;

    private BDLocationControl() {
        mLocationClient = new LocationClient(PRJApplication.getInstance());
        option = new LocationClientOption();
    }

    private static BDLocationControl instance = null;

    public static BDLocationControl getInstance() {
        if (instance == null) {
            synchronized (BDLocationControl.class) {
                if (instance == null) {
                    instance = new BDLocationControl();
                }
            }
        }
        return instance;
    }

    public void startLocationOnce() {
        mLocationClient.registerLocationListener(listener);
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");
        option.setScanSpan(1000);
        option.setIsNeedAddress(true);
        option.setOpenGps(true);
        mLocationClient.setLocOption(option);
        mLocationClient.start();
    }


    private BDLocationListener listener = new BDLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            LogUtil.i(TAG, "onLocationChanged()");
            if (null != bdLocation) {
                //定位成功回调信息，设置相关消息
                mLocationClient.stop();
//                System.out.println("bdLocation = " + bdLocation.getLocType());
//                System.out.println("bdLocation = " + bdLocation.getLocTypeDescription());
//                System.out.println("bdLocation = " + bdLocation.getLongitude());
//                System.out.println("bdLocation = " + bdLocation.getLatitude());
//                System.out.println("bdLocation = " + bdLocation.getProvince());
//                System.out.println("bdLocation = " + bdLocation.getCity());
//                System.out.println("bdLocation = " + bdLocation.getAddrStr());
//                System.out.println("bdLocation = " + bdLocation.getCityCode());
            } else {
                //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                LogUtil.e(TAG, "location Error");
            }
        }
    };
}
