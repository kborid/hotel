package com.prj.sdk.util;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;

import com.prj.sdk.app.AppContext;

/**
 * Activity栈管理类：包括退出管理
 * 
 * @author LiaoBo
 * 
 */
public class ActivityTack {

	// Activity集合
	private List<Activity>		mList			= new LinkedList<>();
	private static ActivityTack	ActivityTack	= new ActivityTack();

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
	public final void finishOtherActity(Class<?> mActivity) {
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
		AppContext.destory();
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

	public boolean isRunActivity(Context context, String packageName) {
		ActivityManager __am = (ActivityManager) context
				.getApplicationContext().getSystemService(
						Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> __list = __am.getRunningTasks(100);
		if (__list.size() == 0)
			return false;
		for (ActivityManager.RunningTaskInfo task : __list) {
			if (task.topActivity.getPackageName().equals(packageName)) {

				Intent activityIntent = new Intent();
				activityIntent.setComponent(task.topActivity);
				activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_SINGLE_TOP);
				context.startActivity(activityIntent);
				return true;
			}
		}
		return false;
	}
}
