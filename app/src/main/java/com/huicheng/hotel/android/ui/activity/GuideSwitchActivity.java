package com.huicheng.hotel.android.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.huicheng.hotel.android.BuildConfig;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.control.DataCleanManager;
import com.huicheng.hotel.android.net.bean.AppInfoBean;
import com.huicheng.hotel.android.ui.activity.hotel.HotelMainActivity;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.huicheng.hotel.android.ui.dialog.CustomDialog;
import com.huicheng.hotel.android.ui.dialog.CustomToast;
import com.prj.sdk.net.down.DownCallback;
import com.prj.sdk.net.down.DownLoaderTask;
import com.prj.sdk.util.ActivityTack;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;
import com.umeng.analytics.MobclickAgent;

import java.io.File;

/**
 * @author kborid
 * @date 2017/5/24 0024
 */
public class GuideSwitchActivity extends BaseActivity {
    private AppInfoBean mAppInfoBean = null;
    private long exitTime = 0;
    private LinearLayout hotel_lay, plane_lay, train_lay, taxi_lay;
    private String[] tips = new String[4];
    private static Handler myHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initLaunchWindow();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_guideswitch_layout);
        initViews();
        initParams();
        initListeners();
    }

    @Override
    public void initViews() {
        super.initViews();
        hotel_lay = (LinearLayout) findViewById(R.id.hotel_lay);
        plane_lay = (LinearLayout) findViewById(R.id.plane_lay);
        train_lay = (LinearLayout) findViewById(R.id.train_lay);
        taxi_lay = (LinearLayout) findViewById(R.id.taxi_lay);
    }

    @Override
    public void initParams() {
        super.initParams();
        tips = getResources().getStringArray(R.array.MainTabTips);
        String appInfo = SharedPreferenceUtil.getInstance().getString(AppConst.APPINFO, "", false);
        if (StringUtil.notEmpty(appInfo)) {
            mAppInfoBean = JSON.parseObject(appInfo, AppInfoBean.class);
            myHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    String ignoreVersion = SharedPreferenceUtil.getInstance().getString(AppConst.IGNORE_UPDATE_VERSION, "", false);
                    String versionSer = mAppInfoBean.upid.split("（")[0];
                    String versionLoc = BuildConfig.VERSION_NAME.split("（")[0];
                    if ((1 <= SessionContext.VersionComparison(versionSer, versionLoc)) && !mAppInfoBean.upid.equals(ignoreVersion)) {
                        showUpdateDialog(mAppInfoBean);
                    }
                }
            }, 1000);
        }
    }

    @Override
    public void initListeners() {
        super.initListeners();
        hotel_lay.setOnClickListener(this);
        plane_lay.setOnClickListener(this);
        train_lay.setOnClickListener(this);
        taxi_lay.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int index = 0;
        switch (v.getId()) {
            case R.id.hotel_lay:
                index = 0;
                break;
            case R.id.plane_lay:
                index = 1;
                break;
            case R.id.train_lay:
                index = 2;
                break;
            case R.id.taxi_lay:
                index = 3;
                break;
        }
        if (index == 2) {
            CustomDialog dialog = new CustomDialog(this);
            dialog.setMessage(tips[index]);
            dialog.setNegativeButton(getString(R.string.iknown), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
        } else {
            Intent intent = new Intent(this, HotelMainActivity.class);
            intent.putExtra("index", index);
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SessionContext.getOpenInstallAppData() != null) {
            myHandler.removeCallbacksAndMessages(null);
            Intent intent = new Intent(this, HotelMainActivity.class);
            intent.putExtra("index", 0);
            startActivity(intent);
        }
    }

    private void showUpdateDialog(final AppInfoBean bean) {
        final CustomDialog dialog = new CustomDialog(this);
        String title = getString(R.string.update_tips);
        if (StringUtil.notEmpty(bean.tip)) {
            title = bean.tip;
        }
        dialog.setTitle(title);
        dialog.setMessage(bean.updesc);
        dialog.setNegativeButton(getString(R.string.update_not), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferenceUtil.getInstance().setString(AppConst.IGNORE_UPDATE_VERSION, bean.upid, false);
            }
        });
        dialog.setPositiveButton(getString(R.string.update_todo), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final CustomDialog pd = new CustomDialog(GuideSwitchActivity.this);
                pd.setTitle(R.string.download_ing);
                View view = LayoutInflater.from(GuideSwitchActivity.this).inflate(R.layout.progress_download_layout, null);
                final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
                final TextView tv_percent = (TextView) view.findViewById(R.id.tv_percent);
                final TextView tv_size = (TextView) view.findViewById(R.id.tv_size);
                pd.addView(view);
                pd.setCancelable(false);
                pd.setCanceledOnTouchOutside(false);
                pd.show();

                final String[] mFilePath = {""};
                DownLoaderTask task = new DownLoaderTask(bean.apkurls, "update.apk", true, new DownCallback() {
                    @Override
                    public void beginDownload(String url, String local, String fileName, int status) {
                        mFilePath[0] = local;
                        tv_percent.setText(String.format(getString(R.string.download_ing_percent), 0));
                        tv_size.setText(String.format(getString(R.string.download_ing_size), "0", "0"));
                        progressBar.setProgress(0);
                        progressBar.setMax(0);
                    }

                    @Override
                    public void downloading(int status, int progress, int maxLength) {
                        int percent = (int) ((float) progress * 100 / maxLength);
                        tv_percent.setText(String.format(getString(R.string.download_ing_percent), percent));
                        tv_size.setText(String.format(getString(R.string.download_ing_size), DataCleanManager.getFormatSize(progress), DataCleanManager.getFormatSize(maxLength)));
                        progressBar.setProgress(progress);
                        progressBar.setMax(maxLength);
                    }

                    @Override
                    public void finishDownload(int status) {
                        pd.dismiss();
                        if (status == DownLoaderTask.DOWNLOAD_SUCCESS) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(Uri.fromFile(new File(mFilePath[0])),
                                    "application/vnd.android.package-archive");
                            startActivity(intent);
                        } else {
                            CustomToast.show(getString(R.string.download_fail_tips), CustomToast.LENGTH_SHORT);
                        }
                    }
                });
                task.execute();
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                CustomToast.show(getString(R.string.exit_tip), CustomToast.LENGTH_SHORT);
                exitTime = System.currentTimeMillis();
            } else {
                SessionContext.destroy();
                MobclickAgent.onKillProcess(this);
                ActivityTack.getInstanse().exit();
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }
}
