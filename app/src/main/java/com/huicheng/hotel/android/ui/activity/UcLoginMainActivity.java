package com.huicheng.hotel.android.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.ui.base.BaseActivity;

/**
 * @auth kborid
 * @date 2017/12/6 0006.
 */

public class UcLoginMainActivity extends BaseActivity {

    private TextView tv_right, tv_action, tv_action2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initMainWindow();
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.user_login_enter_in, R.anim.user_login_enter_out);
        setContentView(R.layout.act_uc_loginmain);
        initViews();
        initParams();
        initListeners();
    }

    @Override
    public void initViews() {
        super.initViews();
        tv_right = (TextView) findViewById(R.id.tv_right);
        tv_action = (TextView) findViewById(R.id.tv_action);
        tv_action2 = (TextView) findViewById(R.id.tv_action2);
    }

    @Override
    public void initParams() {
        super.initParams();
    }

    @Override
    public void initListeners() {
        super.initListeners();
        tv_right.setOnClickListener(this);
        tv_action.setOnClickListener(this);
        tv_action2.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.tv_right:
                finish();
                break;
            case R.id.tv_action: {
                Intent intent = new Intent(this, UcRegisterActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            }
            case R.id.tv_action2: {
                Intent intent = new Intent(this, UcLoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SessionContext.isLogin()) {
            finish();
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.user_login_exit_in, R.anim.user_login_exit_out);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
