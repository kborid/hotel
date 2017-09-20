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
            String notifyMsg = bundle.getString(JPushInterface.EXTRA_ALERT);
            LogUtil.d(TAG, "Received Notification: " + notifyMsg);
        } else if (action.equals(JPushInterface.ACTION_NOTIFICATION_OPENED)) {
            LogUtil.d(TAG, "Open Notification");
            // 上报用户的通知栏被打开，或者用于上报用户自定义消息被展示等客户端需要统计的事件。
            JPushInterface.reportNotificationOpened(context, bundle.getString(JPushInterface.EXTRA_MSG_ID));
            openNotification(context, bundle);
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

//    private void showNotification(String notifyMsg) {
//        BasicPushNotificationBuilder builder = new BasicPushNotificationBuilder(mContext);
//        builder.statusBarDrawable = R.drawable.amap_car;
//        builder.notificationFlags = Notification.FLAG_AUTO_CANCEL
//                | Notification.FLAG_SHOW_LIGHTS;  //设置为自动消失和呼吸灯闪烁
//        builder.notificationDefaults = Notification.DEFAULT_SOUND
//                | Notification.DEFAULT_VIBRATE
//                | Notification.DEFAULT_LIGHTS;  // 设置为铃声、震动、呼吸灯闪烁都要
//        builder.developerArg0 = notifyMsg;
//        JPushInterface.setPushNotificationBuilder(0, builder);
//    }

    private void openNotification(Context context, Bundle bundle) {
        LogUtil.i(TAG, "openNotification()");
        String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
        LogUtil.i(TAG, "JPush:onReceiver extras = " + extras);

        JSONObject mJson = JSON.parseObject(extras);
        if (mJson.containsKey("type")) {
            if ("hotelArtical".equals(mJson.getString("type"))) {
                Intent intent = new Intent(context, HotelSpaceDetailActivity.class);
                if (mJson.containsKey("hotelID")) {
                    intent.putExtra("hotelId", Integer.parseInt(mJson.getString("hotelID")));
                }
                if (mJson.containsKey("articalID")) {
                    intent.putExtra("articleId", Integer.parseInt(mJson.getString("articalID")));
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        }
    }
}
