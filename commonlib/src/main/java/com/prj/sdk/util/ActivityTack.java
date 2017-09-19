package com.prj.sdk.util;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;

import com.prj.sdk.app.AppContext;
import com.prj.sdk.net.data.DataLoader;

import java.util.LinkedList;
import java.util.List;

/**
 * Activity栈管理类：包括退出管理
 *
 * @author LiaoBo
 */
public class ActivityTack {

    // Activity集合
    private List<Activity> mList = new LinkedList<>();
    private static ActivityTack ActivityTack = new ActivityTack();

    private ActivityTack() {

    }

    /**
     * 单例获取Activity管理实例
     *
     * @return
     */
    public static ActivityTack getInstanse() {
        return ActivityTack;
    }

    /**
     * 添加组件
     *
     * @param activity
     */
    public final void addActivity(Activity activity) {
        mList.add(activity);
    }

    /**
     * 去除组件
     *
     * @param activity
     */
    public final void removeActivity(Activity activity) {
        mList.remove(activity);
    }

    /*
    * 获取当前Activity*/
    public final Activity getCurrentActivity() {
        return mList.get(mList.size() - 1);
    }

    /**
     * 弹出activity
     *
     * @param activity
     */
    public void popActivity(Activity activity) {
        removeActivity(activity);
        activity.finish();
    }

    /**
     * 判断是否存在activity
     *
     * @param activity
     */
    public final boolean isExitActivity(Class<?> activity) {
        boolean isExit = false;
        for (Activity act : mList) {
            if (activity != null && (activity.getName().equals(act.getClass().getName()))) {
                isExit = true;
                break;
            }
        }
        return isExit;
    }

    /**
     * 弹出其他Activity
     *
     * @param mActivity
     */
    public final void finishOtherActivity(Class<?> mActivity) {
        for (Activity activity : mList) {
            if (activity != null && !(mActivity.getName().equals(activity.getClass().getName())))
                removeActivity(activity);
            activity.finish();
        }
    }

    /**
     * 根据class name获取activity
     *
     * @param name
     * @return
     */
    public Activity getActivityByClassName(String name) {
        for (Activity ac : mList) {
            if (ac.getClass().getName().indexOf(name) >= 0) {
                return ac;
            }
        }
        return null;
    }

    /**
     * 应用退出时调用
     */
    public final void exit() {
        DataLoader.getInstance().clearRequests();
        clearNotificaction();
        for (Activity activity : mList) {
            if (activity != null) {
                try {
                    activity.finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        System.exit(0);
    }

    public final void clearNotificaction() {
        NotificationManager manager = (NotificationManager) AppContext.mAppContext.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancelAll();
    }
}
