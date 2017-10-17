package com.huicheng.hotel.android.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.huicheng.hotel.android.BuildConfig;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.NetURL;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;

/**
 * @author kborid
 * @date 2017/6/5 0005
 */
public class DebugInfoActivity extends BaseActivity {
    private TextView tv_debugInfo;
    private TextView tv_changed, tv_js, tv_close;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_debuginfo_layout);
        initViews();
        initParams();
        initListeners();
    }

    @Override
    public void initViews() {
        super.initViews();
        tv_debugInfo = (TextView) findViewById(R.id.tv_debuginfo);
        tv_changed = (TextView) findViewById(R.id.tv_changed);
        tv_js = (TextView) findViewById(R.id.tv_js);
        tv_close = (TextView) findViewById(R.id.tv_close);
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

        tv_debugInfo.setText(sb.toString());
    }

    @Override
    public void initListeners() {
        super.initListeners();
        tv_changed.setOnClickListener(this);
        tv_js.setOnClickListener(this);
        tv_close.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        Intent mIntent = null;
        switch (v.getId()) {
            case R.id.tv_changed:
                mIntent = new Intent(this, DebugChangeEnvActivity.class);
                break;
            case R.id.tv_js:
                mIntent = new Intent(this, HtmlActivity.class);
                mIntent.putExtra("ISJS", AppConst.ISDEVELOP);
                mIntent.putExtra("title", "JS测试");
                break;
            case R.id.tv_close:
                SharedPreferenceUtil.getInstance().setBoolean("isShowDebug", false);
                finish();
                break;
        }

        if (null != mIntent) {
            startActivity(mIntent);
        }
    }
}
