package com.huicheng.hotel.android.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huicheng.hotel.android.ui.activity.HotelSpaceDetailActivity;
import com.prj.sdk.util.LogUtil;

import cn.jpush.android.api.JPushInterface;

public class JPushReceiver extends BroadcastReceiver {
    private static final String TAG = "JPush";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (null == intent) {
            return;
        }
        String action = intent.getAction();
        LogUtil.d(TAG, "onReceive - " + action);
        Bundle bundle = intent.getExtras();
        LogUtil.d(TAG, "Extras: " + printBundle(bundle));

        if (action.equals(JPushInterface.ACTION_REGISTRATION_ID)) {
            String id = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
            LogUtil.d(TAG, "Regist Success! ID : " + id);
        } else if (action.equals(JPushInterface.ACTION_MESSAGE_RECEIVED)) {
            String msg = bundle.getString(JPushInterface.EXTRA_MESSAGE);
            LogUtil.d(TAG, "Received Message: " + msg);
        } else if (action.equals(JPushInterface.ACTION_NOTIFICATION_RECEIVED)) {
            String notify = bundle.getString(JPushInterface.EXTRA_ALERT);
            LogUtil.d(TAG, "Received Notification: " + notify);
        } else if (action.equals(JPushInterface.ACTION_NOTIFICATION_OPENED)) {
            LogUtil.d(TAG, "Open Notification");
            // 上报用户的通知栏被打开，或者用于上报用户自定义消息被展示等客户端需要统计的事件。
            JPushInterface.reportNotificationOpened(context, bundle.getString(JPushInterface.EXTRA_MSG_ID));
            openNotification(context, bundle);
//        } else if (action.equals(JPushInterface.ACTION_RICHPUSH_CALLBACK)) {
//            LogUtil.d(TAG,
//                    "Received RICHPUSH_CALLBACK: "
//                            + bundle.getString(JPushInterface.EXTRA_EXTRA));
//        } else if (action.equals(JPushInterface.ACTION_CONNECTION_CHANGE)) {
//            boolean connected = intent.getBooleanExtra(
//                    JPushInterface.EXTRA_CONNECTION_CHANGE, false);
//            Log.w(TAG, "Connect status changed : " + connected);
        } else {
            LogUtil.d(TAG, "Unhandled intent - " + intent.getAction());
        }
    }

    // 打印所有的 intent extra 数据
    private static String printBundle(Bundle bundle) {
        StringBuilder sb = new StringBuilder();
        for (String key : bundle.keySet()) {
            if (key.equals(JPushInterface.EXTRA_NOTIFICATION_ID)) {
                sb.append("\nkey:").append(key).append(", value:").append(bundle.getInt(key));
            } else if (key.equals(JPushInterface.EXTRA_CONNECTION_CHANGE)) {
                sb.append("\nkey:").append(key).append(", value:").append(bundle.getBoolean(key));
            } else {
                sb.append("\nkey:").append(key).append(", value:").append(bundle.getString(key));
            }
        }
        return sb.toString();
    }

    private void openNotification(Context context, Bundle bundle) {
        LogUtil.i(TAG, "openNotification()");
        String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
        LogUtil.i(TAG, "JPush:onReceiver extras = " + extras);

        Intent intent = null;
        JSONObject mJson = JSON.parseObject(extras);
        if (mJson.containsKey("type")) {
            if ("hotelArtical".equals(mJson.getString("type"))) {
//                if (ActivityTack.getInstanse().isRunActivity(context, AppContext.mAppContext.getPackageName())) {
                intent = new Intent(context, HotelSpaceDetailActivity.class);
//                } else {
//                    intent = context.getPackageManager().getLaunchIntentForPackage(AppContext.mAppContext.getPackageName());
//                }
                if (mJson.containsKey("hotelID")) {
                    intent.putExtra("hotelId", Integer.parseInt(mJson.getString("hotelID")));
                }
                if (mJson.containsKey("articalID")) {
                    intent.putExtra("articleId", Integer.parseInt(mJson.getString("articalID")));
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } else {
                LogUtil.i(TAG, "JPushReceiver: no action~~~(warning!!!)");
            }
        } else {
            LogUtil.i(TAG, "JPushReceiver: no action~~~");
        }
    }
}
