package com.huicheng.hotel.android.ui.activity;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.huicheng.hotel.android.BuildConfig;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.content.AppConst;
import com.huicheng.hotel.android.content.NetURL;
import com.huicheng.hotel.android.ui.base.BaseAppActivity;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;

/**
 * @author kborid
 * @date 2017/6/5 0005
 */
public class DebugInfoActivity extends BaseAppActivity {

    @Override
    protected void setContentView() {
        setContentView(R.layout.act_debuginfo_layout);
    }

    @Override
    public void initParams() {
        super.initParams();
        tv_center_title.setText(getString(R.string.debug_menu));
        StringBuilder sb = new StringBuilder();
        sb.append("PackageName : ").append(BuildConfig.APPLICATION_ID);
        sb.append("\nVersionName : ").append(BuildConfig.VERSION_NAME);
        sb.append("\nVersionCode : ").append(BuildConfig.VERSION_CODE);

        int type = SharedPreferenceUtil.getInstance().getInt(AppConst.APPTYPE, 0);
        String env = getResources().getStringArray(R.array.EnvItem)[type];
        sb.append("\nEnv : ").append(env);
        sb.append("\nEnv Domain : ").append(NetURL.getApi());
        sb.append("\nDebugSwitch : ").append(AppConst.ISDEVELOP);
        sb.append("\nBuildType : ").append(BuildConfig.BUILD_TYPE);
        sb.append("\nFlavor : ").append(StringUtil.notEmpty(BuildConfig.FLAVOR) ? BuildConfig.FLAVOR : "无");
        sb.append("\nUMENG Channel : ").append(SessionContext.getAppMetaData(this, "UMENG_CHANNEL"));

        ((TextView) findViewById(R.id.tv_debuginfo)).setText(sb.toString());
    }

    public void environmentChanged(View v) {
        startActivity(new Intent(this, DebugChangeEnvActivity.class));
    }

    public void jsTestMain(View v) {
        Intent intent = new Intent(this, HtmlActivity.class);
        intent.putExtra("ISJS", AppConst.ISDEVELOP);
        intent.putExtra("title", "JS测试");
        startActivity(intent);
    }

    public void closeDebugMenu(View v) {
        SharedPreferenceUtil.getInstance().setBoolean("isShowDebug", false);
        finish();
    }
}
