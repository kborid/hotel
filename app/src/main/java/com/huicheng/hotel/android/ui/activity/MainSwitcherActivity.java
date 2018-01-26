package com.huicheng.hotel.android.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huicheng.hotel.android.BuildConfig;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.control.DataCleanManager;
import com.huicheng.hotel.android.requestbuilder.RequestBeanBuilder;
import com.huicheng.hotel.android.requestbuilder.bean.AppInfoBean;
import com.huicheng.hotel.android.ui.activity.hotel.HotelMainActivity;
import com.huicheng.hotel.android.ui.activity.plane.PlaneMainActivity;
import com.huicheng.hotel.android.ui.base.BaseAppActivity;
import com.huicheng.hotel.android.ui.custom.LeftDrawerLayout;
import com.huicheng.hotel.android.ui.dialog.CustomDialog;
import com.huicheng.hotel.android.ui.dialog.CustomToast;
import com.huicheng.hotel.android.content.AppConst;
import com.huicheng.hotel.android.content.NetURL;
import com.prj.sdk.constants.BroadCastConst;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.net.data.ResponseData;
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
public class MainSwitcherActivity extends BaseAppActivity implements LeftDrawerLayout.OnLeftDrawerListener {
    private AppInfoBean mAppInfoBean = null;
    private long exitTime = 0;
    private LinearLayout hotel_lay, plane_lay, train_lay, taxi_lay;
    private String[] tips = new String[4];
    private static Handler myHandler = new Handler(Looper.getMainLooper());
    private boolean isNeedCloseLeftDrawer = false;
    private boolean isFirstLaunch = false;

    private DrawerLayout drawer_layout;
    private LeftDrawerLayout left_layout;

    private RelativeLayout blur_lay;
    private ImageView iv_blur;
    private ImageView iv_logo_vertical;
    private ImageView iv_left;

    protected void preOnCreate() {
        super.preOnCreate();
        initMainWindow();
    }

    @Override
    protected void setContentView() {
        setContentView(R.layout.act_main_switcher);
    }

    @Override
    public void initViews() {
        super.initViews();
        hotel_lay = (LinearLayout) findViewById(R.id.hotel_lay);
        plane_lay = (LinearLayout) findViewById(R.id.plane_lay);
        train_lay = (LinearLayout) findViewById(R.id.train_lay);
        taxi_lay = (LinearLayout) findViewById(R.id.taxi_lay);

        drawer_layout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (null != drawer_layout) {
            drawer_layout.setScrimColor(getResources().getColor(R.color.transparent50));
        }
        left_layout = (LeftDrawerLayout) findViewById(R.id.left_layout);

        blur_lay = (RelativeLayout) findViewById(R.id.blur_lay);
        iv_blur = (ImageView) findViewById(R.id.iv_blur);
        iv_logo_vertical = (ImageView) findViewById(R.id.iv_logo_vertical);
        iv_left = (ImageView) findViewById(R.id.iv_left);
    }

    @Override
    public void dealIntent() {
        super.dealIntent();
        Bundle bundle = getIntent().getExtras();
        if (null != bundle) {
            isNeedCloseLeftDrawer = bundle.getBoolean("isClosed");
        }
    }

    @Override
    public void initParams() {
        super.initParams();
        isFirstLaunch = true;
        tips = getResources().getStringArray(R.array.MainTabTips);
        //app更新提示
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

        //更新用户中心
        left_layout.updateUserInfo();
    }

    @Override
    public void initListeners() {
        super.initListeners();
        hotel_lay.setOnClickListener(this);
        plane_lay.setOnClickListener(this);
        train_lay.setOnClickListener(this);
        taxi_lay.setOnClickListener(this);

        left_layout.setOnLeftDrawerListener(this);
        drawer_layout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                iv_blur.setAlpha(slideOffset * 1f);
                iv_logo_vertical.setAlpha(slideOffset);
                iv_left.setAlpha(slideOffset);
                if (slideOffset > 0) {
                    blur_lay.setVisibility(View.VISIBLE);
                } else {
                    blur_lay.setVisibility(View.GONE);
                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {
            }

            @Override
            public void onDrawerClosed(View drawerView) {
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int index = 0;
        Intent intent = null;
        switch (v.getId()) {
            case R.id.hotel_lay:
                index = 0;
                intent = new Intent(this, HotelMainActivity.class);
                intent.putExtra("index", index);
                break;
            case R.id.plane_lay:
                index = 1;
                intent = new Intent(this, PlaneMainActivity.class);
                intent.putExtra("index", index);
                break;
            case R.id.train_lay:
                index = 2;
                break;
            case R.id.taxi_lay:
                index = 3;
                break;
        }
        if (index == 2 || index == 3) {
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
            if (null != intent) {
                startActivity(intent);
            }
        }
    }

    private void requestMessageCount() {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        ResponseData d = b.syncRequest(b);
        d.path = NetURL.MESSAGE_COUNT;
        d.flag = AppConst.MESSAGE_COUNT;
        DataLoader.getInstance().loadData(this, d);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFirstLaunch) {
            isFirstLaunch = false;
            if (SessionContext.getOpenInstallAppData() != null) {
                myHandler.removeCallbacksAndMessages(null);
                Intent intent = new Intent(this, HotelMainActivity.class);
                intent.putExtra("index", 0);
                startActivity(intent);
            }
        }

        //每次启动时，如果用户未登录，则显示侧滑
        if (SessionContext.isFirstLaunchDoAction(getClass().getSimpleName())) {
            if (!SessionContext.isLogin()) {
//                myHandler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        drawer_layout.openDrawer(left_layout, true);
//                    }
//                }, 300);
                sendBroadcast(new Intent(BroadCastConst.UNLOGIN_ACTION));
            }
        }

        if (SessionContext.isLogin()) {
            requestMessageCount();
        }

        if (isNeedCloseLeftDrawer && drawer_layout.isDrawerOpen(left_layout)) {
            isNeedCloseLeftDrawer = false;
            drawer_layout.closeDrawers();
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
                final CustomDialog pd = new CustomDialog(MainSwitcherActivity.this);
                pd.setTitle(R.string.download_ing);
                View view = LayoutInflater.from(MainSwitcherActivity.this).inflate(R.layout.progress_download_layout, null);
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
    public void onNotifyMessage(ResponseData request, ResponseData response) {
        super.onNotifyMessage(request, response);
        if (null != response && response.body != null) {
            if (request.flag == AppConst.MESSAGE_COUNT) {
                JSONObject mJson = JSON.parseObject(response.body.toString());
                boolean hasMsg = left_layout.updateMsgCount(mJson.getString("count"));
//                if (hasMsg) {
//                    iv_user.setImageResource(R.drawable.iv_home_user2);
//                } else {
//                    iv_user.setImageResource(R.drawable.iv_home_user);
//                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SessionContext.cleanLocationInfo();
        left_layout.unregisterBroadReceiver();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (drawer_layout.isDrawerOpen(left_layout)) {
                drawer_layout.closeDrawers();
                return true;
            }
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                CustomToast.show(getString(R.string.exit_tip), CustomToast.LENGTH_SHORT);
                exitTime = System.currentTimeMillis();
            } else {
                MobclickAgent.onKillProcess(this);
                ActivityTack.getInstanse().exit();
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void closeDrawer() {
        if (null != drawer_layout) {
            drawer_layout.closeDrawers();
        }
    }

    @Override
    public void doQmhAction() {
        //全民化插件 platCode生产2003，测试8000
//        Intent intent = new Intent(HotelMainActivity.this, IOUAppVerifyActivity.class);
//        intent.putExtra("appUserId", SessionContext.mUser.user.mobile);
//        intent.putExtra("token", SessionContext.getTicket());
//        intent.putExtra("platCode", "2003");
//        intent.putExtra("isShowGuide", "true");
//        startActivityForResult(intent, REQUEST_CODE_QMH);
    }
}
