package com.huicheng.hotel.android.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.alibaba.fastjson.JSON;
import com.huicheng.hotel.android.BuildConfig;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.net.bean.AppInfoBean;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.huicheng.hotel.android.ui.dialog.CustomDialog;
import com.prj.sdk.util.ActivityTack;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.widget.CustomToast;
import com.umeng.analytics.MobclickAgent;

/**
 * @author kborid
 * @date 2017/5/24 0024
 */
public class GuideSwitchActivity extends BaseActivity {
    private AppInfoBean mAppInfoBean = null;
    private long exitTime = 0;
    private LinearLayout hotel_lay, plane_lay, train_lay, taxi_lay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        String appInfo = SharedPreferenceUtil.getInstance().getString(AppConst.APPINFO, "", false);
        if (StringUtil.notEmpty(appInfo)) {
            mAppInfoBean = JSON.parseObject(appInfo, AppInfoBean.class);
            new Handler().postDelayed(new Runnable() {
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
        if (!AppConst.ISDEVELOP && (1 == index || 3 == index)) {
            CustomDialog dialog = new CustomDialog(this);
            dialog.setMessage("机票预订正在测试中，即将与您见面");
            dialog.setNegativeButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
        } else {
            Intent intent = new Intent(this, MainFragmentActivity.class);
            intent.putExtra("index", index);
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean isNotFirstLaunch = SharedPreferenceUtil.getInstance().getBoolean(AppConst.NOT_FIRST_LAUNCH, false);
        if (!isNotFirstLaunch) {
            SharedPreferenceUtil.getInstance().setBoolean(AppConst.NOT_FIRST_LAUNCH, true);
//            sendBroadcast(new Intent(BroadCastConst.UNLOGIN_ACTION));
            startActivity(new Intent(this, RegisterActivity.class));
        } else {
            if (SessionContext.getWakeUpAppData() != null || SessionContext.getRecommandAppData() != null) {
                Intent intent = new Intent(this, MainFragmentActivity.class);
                intent.putExtra("index", 0);
                startActivity(intent);
            }
        }
    }

    private void showUpdateDialog(final AppInfoBean bean) {
        final CustomDialog dialog = new CustomDialog(this);
        dialog.setTitle("更新提示");
        dialog.setMessage(bean.updesc);
        dialog.setNegativeButton("暂不更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferenceUtil.getInstance().setString(AppConst.IGNORE_UPDATE_VERSION, bean.upid, false);
            }
        });
        dialog.setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                intent.setData(Uri.parse(bean.apkurls));
                startActivity(intent);
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
                CustomToast.show("再按一次 退出程序", CustomToast.LENGTH_SHORT);
                exitTime = System.currentTimeMillis();
            } else {
                SessionContext.destroy();
                MobclickAgent.onKillProcess(this);// 调用Process.kill或者System.exit之类的方法杀死进程前保存统计数据
                ActivityTack.getInstanse().exit();
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }
}
