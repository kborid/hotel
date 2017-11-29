package com.huicheng.hotel.android.ui.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.huicheng.hotel.android.PRJApplication;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.huicheng.hotel.android.ui.dialog.CustomToast;
import com.prj.sdk.app.AppConst;
import com.prj.sdk.constants.BroadCastConst;
import com.prj.sdk.util.ActivityTack;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;

/**
 * @author kborid
 * @date 2017/9/19.
 */

public class DebugChangeEnvActivity extends BaseActivity {

    private int oldIndex = 0;
    private String oldUrl = "";
    private int currIndex = 0;
    private String[] envs;
    private LinearLayout env_lay;
    private EditText et_url;
    private Button btn_restart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_change_env_layout);
        initViews();
        initParams();
        initListeners();
    }

    @Override
    public void initViews() {
        super.initViews();
        env_lay = (LinearLayout) findViewById(R.id.env_lay);
        et_url = (EditText) findViewById(R.id.et_url);
        btn_restart = (Button) findViewById(R.id.btn_restart);
    }

    @Override
    public void initParams() {
        super.initParams();
        tv_center_title.setText(getString(R.string.debug_change));
        oldIndex = SharedPreferenceUtil.getInstance().getInt(AppConst.APPTYPE, 0);
        oldUrl = SharedPreferenceUtil.getInstance().getString(AppConst.DEV_URL, "", false);
        et_url.setText(oldUrl);
        et_url.setSelection(oldUrl.length());
        envs = getResources().getStringArray(R.array.EnvItem);
        env_lay.removeAllViews();
        for (int i = 0; i < envs.length; i++) {
            View view = LayoutInflater.from(this).inflate(R.layout.lv_changeenv_item, null);
            TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
            final ImageView iv_flag = (ImageView) view.findViewById(R.id.iv_flag);
            tv_title.setText(envs[i]);
            if (i == currIndex) {
                iv_flag.setVisibility(View.VISIBLE);
            } else {
                iv_flag.setVisibility(View.INVISIBLE);
            }

            final int finalI = i;
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currIndex != finalI) {
                        refreshStatus(finalI);
                    }
                }
            });

            env_lay.addView(view);
        }
        refreshStatus(oldIndex);
    }

    private void refreshStatus(int index) {
        for (int i = 0; i < env_lay.getChildCount(); i++) {
            if (i == index) {
                env_lay.getChildAt(i).findViewById(R.id.iv_flag).setVisibility(View.VISIBLE);
            } else {
                env_lay.getChildAt(i).findViewById(R.id.iv_flag).setVisibility(View.INVISIBLE);
            }
        }
        currIndex = index;
        if (currIndex == 4 && getString(R.string.env_local).equals(envs[currIndex])) {
            et_url.setVisibility(View.VISIBLE);
        } else {
            et_url.setVisibility(View.GONE);
        }
    }

    @Override
    public void initListeners() {
        super.initListeners();
        btn_restart.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.btn_restart:
//                if (currIndex == oldIndex && oldUrl.equals(et_url.getText().toString())) {
//                    CustomToast.show(getString(R.string.change_no), CustomToast.LENGTH_SHORT);
//                    return;
//                }
                if (currIndex == 4 && et_url.isShown()) {
                    String url = et_url.getText().toString().trim();
                    if (StringUtil.isEmpty(url)) {
                        CustomToast.show("输入联调地址", CustomToast.LENGTH_SHORT);
                        return;
                    } else {
                        if (!url.startsWith("http") && !url.endsWith("/")) {
                            url = "http://" + url + "/";
                        } else if (!url.startsWith("http")) {
                            url = "http://" + url;
                        } else if (!url.endsWith("/")) {
                            url = url + "/";
                        }
                        SharedPreferenceUtil.getInstance().setString(AppConst.DEV_URL, url, false);
                    }
                }
                SharedPreferenceUtil.getInstance().setInt(AppConst.APPTYPE, currIndex);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        SessionContext.cleanUserInfo();
                        SessionContext.destroy();
                        sendBroadcast(new Intent(BroadCastConst.UPDATE_USERINFO));
                        ActivityTack.getInstanse().exit();
                        Process.killProcess(Process.myPid());
                    }
                }, 1000);
                CustomToast.show(getString(R.string.change_success), CustomToast.LENGTH_SHORT);

                Intent intent = new Intent(PRJApplication.getInstance(), LauncherActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                PendingIntent restartIntent = PendingIntent.getActivity(
                        PRJApplication.getInstance(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                //退出并重启程序
                AlarmManager mgr = (AlarmManager) PRJApplication.getInstance().getSystemService(Context.ALARM_SERVICE);
                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1500, restartIntent);

                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (env_lay != null) {
            env_lay.removeAllViews();
        }
    }
}
