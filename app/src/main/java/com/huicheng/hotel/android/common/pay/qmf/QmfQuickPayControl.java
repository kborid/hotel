package com.huicheng.hotel.android.common.pay.qmf;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.chinaums.pppay.quickpay.service.QuickPayService;
import com.huicheng.hotel.android.PRJApplication;

/**
 * @auth kborid
 * @date 2017/11/3 0003.
 */

public class QmfQuickPayControl {

    private QuickPayService.LocalBinder mUmsQuickPayService = null;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mUmsQuickPayService = (QuickPayService.LocalBinder) (service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mUmsQuickPayService = null;
        }
    };


    private static QmfQuickPayControl instance = null;

    private QmfQuickPayControl() {
    }

    public static QmfQuickPayControl getInstance() {
        if (null == instance) {
            synchronized (QmfQuickPayControl.class) {
                if (null == instance) {
                    instance = new QmfQuickPayControl();
                }
            }
        }
        return instance;
    }

    public void bindQuickPayService() {
        // 绑定本地服务
        Intent intent = new Intent(PRJApplication.getInstance(), QuickPayService.class);
        PRJApplication.getInstance().bindService(intent, mConnection, Service.BIND_AUTO_CREATE);
    }

    public void unBindQuickPayService() {
        PRJApplication.getInstance().unbindService(mConnection);
    }

    public void startQuickPay(String str) {
        JSONObject json = JSON.parseObject(str);
        JSONObject payRequest = json.getJSONObject("appPayRequest");
        Bundle args = new Bundle();
        args.putString("merchantId", payRequest.getString("merchantId"));
        args.putString("merchantUserId", payRequest.getString("merchantUserId"));
        args.putString("merOrderId", json.getString("merOrderId"));
        args.putString("amount", json.getString("totalAmount"));// 将交易金额由元转成分
        args.putString("mobile", payRequest.getString("mobile"));
        args.putString("sign", payRequest.getString("sign"));
        args.putString("mode", "2");
        args.putString("notifyUrl", payRequest.getString("notifyUrl"));
        args.putBoolean("isProductEnv", false);
//        args.putBoolean(HomeActivity.ENV_KEY, isProEnv);

        try {
            bindQuickPayService();
            mUmsQuickPayService.getService().payOrder(args, new QmfQuickPayOrderResultListener());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void startScanPay(String str) {
        JSONObject json = JSON.parseObject(str);
        JSONObject payRequest = json.getJSONObject("appPayRequest");
        Bundle args = new Bundle();
        args.putString("scanCodeUrl", "https://xxxxx");
        args.putString("merchantUserId", payRequest.getString("merchantUserId"));
        args.putString("mobile", payRequest.getString("mobile"));
        args.putString("mode", "4");
        try {
            bindQuickPayService();
            mUmsQuickPayService.getService().scanCodePay(args, new QmfQuickPayOrderResultListener());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
