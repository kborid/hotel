package com.huicheng.hotel.android.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.huicheng.hotel.android.BuildConfig;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.NetURL;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;

/**
 * @author kborid
 * @date 2017/6/5 0005
 */
public class DebugInfoActivity extends BaseActivity {
    private TextView tv_debugInfo;
    private Button btn_changed, btn_js, btn_close;

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
        btn_changed = (Button) findViewById(R.id.btn_changed);
        btn_js = (Button) findViewById(R.id.btn_JS);
        btn_close = (Button) findViewById(R.id.btn_close);
    }

    @Override
    public void initParams() {
        super.initParams();
        tv_center_title.setText("Debug Info");

        StringBuilder sb = new StringBuilder();
        sb.append("\nPackageName : ").append(BuildConfig.APPLICATION_ID);
        sb.append("\nVersionName : ").append(BuildConfig.VERSION_NAME);
        sb.append("\nVersionCode : ").append(BuildConfig.VERSION_CODE);
        String env = "";
        int type = SharedPreferenceUtil.getInstance().getInt(AppConst.APPTYPE, 0);
        switch (type){
            case 0:
                env = "测试环境";
                break;
            case 1:
                env = "正式环境";
                break;
            case 2:
                env = "开发环境";
                break;
            default:
                env = "测试环境";
                break;
        }
        sb.append("\nEnvironment : ")
                .append(env)
                .append("(").append(NetURL.getApi()).append(")");
        sb.append("\nBuildType : ").append(BuildConfig.BUILD_TYPE);
        sb.append("\nDebugSwitch : ").append(LogUtil.isDebug());
        sb.append("\nFlavor : ").append(StringUtil.notEmpty(BuildConfig.FLAVOR) ? BuildConfig.FLAVOR : "无");
        sb.append("\nUMchannel : ").append(SessionContext.getAppMetaData(this, "UMENG_CHANNEL"));

        tv_debugInfo.setText(sb.toString());
    }

    @Override
    public void initListeners() {
        super.initListeners();
        btn_changed.setOnClickListener(this);
        btn_js.setOnClickListener(this);
        btn_close.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        Intent mIntent = null;
        switch (v.getId()) {
            case R.id.btn_changed:
                mIntent = new Intent(this, HtmlActivity.class);
                mIntent.putExtra("ISDEVELOP", AppConst.ISDEVELOP);
                mIntent.putExtra("title", "环境切换");
                break;
            case R.id.btn_JS:
                mIntent = new Intent(this, HtmlActivity.class);
                mIntent.putExtra("ISJS", AppConst.ISDEVELOP);
                mIntent.putExtra("title", "JS测试");
                break;
            case R.id.btn_close:
                SharedPreferenceUtil.getInstance().setBoolean("isShowDebug", false);
                finish();
                break;
        }

        if (null != mIntent) {
            startActivity(mIntent);
        }
    }
}
