package com.huicheng.hotel.android.ui.activity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.NetURL;
import com.huicheng.hotel.android.common.pay.wxpay.MD5;
import com.huicheng.hotel.android.net.RequestBeanBuilder;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.huicheng.hotel.android.ui.dialog.CustomToast;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;

/**
 * 找回密码
 */
public class UserForgetPwdActivity extends BaseActivity {

    private EditText et_phone, et_yzm, et_pwd;
    private TextView tv_yzm;
    private TextView tv_action;
    private CountDownTimer mCountDownTimer;
    private boolean isValid = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initMainWindow();
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.user_login_enter_in, R.anim.user_login_enter_out);
        setContentView(R.layout.act_forget_pwd_layout);
        initViews();
        initParams();
        initListeners();
    }

    @Override
    public void initViews() {
        super.initViews();
        et_phone = (EditText) findViewById(R.id.et_phone);
        et_yzm = (EditText) findViewById(R.id.et_yzm);
        et_pwd = (EditText) findViewById(R.id.et_pwd);
        tv_yzm = (TextView) findViewById(R.id.tv_yzm);
        tv_action = (TextView) findViewById(R.id.tv_action);
    }

    @Override
    public void initParams() {
        super.initParams();
        setCountDownTimer(60 * 1000, 1000);
        String name = SharedPreferenceUtil.getInstance().getString(AppConst.USERNAME, "", true);
        if (StringUtil.notEmpty(name)) {
            et_phone.setText(name);// 设置默认用户名
        }
    }

    @Override
    public void initListeners() {
        super.initListeners();
        tv_action.setOnClickListener(this);
        tv_yzm.setOnClickListener(this);
        et_pwd.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    tv_action.performClick();
                    return true;
                }
                return false;
            }
        });
        et_phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 11) {
                    tv_yzm.performClick();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.tv_yzm:
                if (StringUtil.notEmpty(et_phone.getText().toString())) {
                    if (Utils.isMobile(et_phone.getText().toString())) {
                        requestYZM();
                    } else {
                        CustomToast.show(getString(R.string.tips_user_phone_confirm), CustomToast.LENGTH_SHORT);
                    }
                } else {
                    CustomToast.show(getString(R.string.tips_user_phone), CustomToast.LENGTH_SHORT);
                }
                break;
            case R.id.tv_action:
                String phoneNumber = et_phone.getText().toString();
                String checkCode = et_yzm.getText().toString();
                String pwd = et_pwd.getText().toString();

                if (StringUtil.isEmpty(phoneNumber)) {
                    CustomToast.show(getString(R.string.tips_user_phone), CustomToast.LENGTH_SHORT);
                    return;
                }
                if (!Utils.isMobile(phoneNumber)) {
                    CustomToast.show(getString(R.string.tips_user_phone_confirm), CustomToast.LENGTH_SHORT);
                    return;
                }
                if (StringUtil.isEmpty(checkCode)) {
                    CustomToast.show(getString(R.string.tips_user_yzm), CustomToast.LENGTH_SHORT);
                    return;
                }
                if (StringUtil.isEmpty(pwd)) {
                    CustomToast.show(getString(R.string.tips_user_pwd), CustomToast.LENGTH_SHORT);
                    return;
                }
                if (pwd.length() < 6 && pwd.length() > 20) {
                    CustomToast.show(getString(R.string.tips_user_pwd_confirm), CustomToast.LENGTH_SHORT);
                    return;
                }
                requestCheckYZM();
                break;
            default:
                break;
        }
    }

    /**
     * 加载验证码
     */
    private void requestYZM() {
        RequestBeanBuilder b = RequestBeanBuilder.create(false);
        b.addBody("businesstype", AppConst.BUSINESS_TYPE_FINDPWD);
        b.addBody("mobile", et_phone.getText().toString());
        b.addBody("smsparam", "code");

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.GET_YZM;
        d.flag = AppConst.GET_YZM;

        if (!isProgressShowing()) {
            showProgressDialog(this);
        }
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    /**
     * 检验验证码
     */
    private void requestCheckYZM() {
        LogUtil.d(TAG, "requestCheckYZM()");
        RequestBeanBuilder b = RequestBeanBuilder.create(false);
        b.addBody("businesstype", AppConst.BUSINESS_TYPE_FINDPWD);
        b.addBody("mobile", et_phone.getText().toString());
        b.addBody("code", et_yzm.getText().toString());

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.CHECK_YZM;
        d.flag = AppConst.CHECK_YZM;

        if (!isProgressShowing()) {
            showProgressDialog(this);
        }

        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void requestResetPwd() {
        RequestBeanBuilder b = RequestBeanBuilder.create(false);
        b.addBody("businesstype", AppConst.BUSINESS_TYPE_FINDPWD);
        b.addBody("mobile", et_phone.getText().toString());
        b.addBody("code", et_yzm.getText().toString());
        b.addBody("password", MD5.getMessageDigest(et_pwd.getText().toString().getBytes()));

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.FIND_PASSWORD;
        d.flag = AppConst.FIND_PASSWORD;

        requestID = DataLoader.getInstance().loadData(this, d);
    }

    @Override
    public void onNotifyMessage(ResponseData request, ResponseData response) {
        if (request.flag == AppConst.GET_YZM) {
            removeProgressDialog();
            CustomToast.show(getString(R.string.tips_user_send_yzm), CustomToast.LENGTH_SHORT);
            tv_yzm.setEnabled(false);
            et_phone.setEnabled(false);
            mCountDownTimer.start();// 启动倒计时
            et_yzm.requestFocus();
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        } else if (request.flag == AppConst.FIND_PASSWORD) {
            if (response.body != null) {
                CustomToast.show("密码修改成功", 0);
                finish();
            }
        } else if (request.flag == AppConst.CHECK_YZM) {
            String jsonStr = response.body.toString();
            JSONObject mjson = JSON.parseObject(jsonStr);
            if (mjson.containsKey("status")) {
                String status = mjson.getString("status");
                switch (status) {
                    case "0":
                        isValid = false;
                        break;
                    case "1":
                        isValid = true;
                        break;
                    default:
                        break;
                }

                if (isValid) {
                    requestResetPwd();
                } else {
                    removeProgressDialog();
                    CustomToast.show(getString(R.string.tips_user_yzm_error), CustomToast.LENGTH_SHORT);
                }
            }
        }
    }

    @Override
    public void onNotifyError(ResponseData request) {
        if (request.flag == AppConst.CHECK_YZM) {
            isValid = false;
        }
    }

    /**
     * 设置倒计时
     */
    private void setCountDownTimer(long millisInFuture, long countDownInterval) {
        mCountDownTimer = new CountDownTimer(millisInFuture, countDownInterval) {

            @Override
            public void onTick(long millisUntilFinished) {
                tv_yzm.setText(millisUntilFinished / 1000 + "s");
            }

            @Override
            public void onFinish() {
                tv_yzm.setEnabled(true);
                tv_yzm.setText(R.string.tips_reget_yzm);
                et_phone.setEnabled(true);
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mCountDownTimer) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.user_login_exit_in, R.anim.user_login_exit_out);
    }
}
