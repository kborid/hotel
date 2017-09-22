package com.huicheng.hotel.android.ui.activity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
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
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;

/**
 * 找回密码
 */
public class ForgetPwdActivity extends BaseActivity {

    private EditText et_phone, et_yzm, et_password, et_password2;
    private Button btn_reset, btn_getYZM;
    private String mPhoneNum;
    private CountDownTimer mCountDownTimer;
    private boolean isValid = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        et_password = (EditText) findViewById(R.id.et_password);
        et_password2 = (EditText) findViewById(R.id.et_password2);
        btn_getYZM = (Button) findViewById(R.id.btn_getYZM);
        btn_reset = (Button) findViewById(R.id.btn_reset);
    }

    @Override
    public void initParams() {
        tv_center_title.setText(R.string.reset_pwd);
        setCountDownTimer(60 * 1000, 1000);
        super.initParams();
    }

    @Override
    public void initListeners() {
        super.initListeners();
        btn_reset.setOnClickListener(this);
        btn_getYZM.setOnClickListener(this);
        et_password2.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    btn_reset.performClick();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.btn_getYZM:
                mPhoneNum = et_phone.getText().toString().trim();
                if (StringUtil.notEmpty(mPhoneNum)) {
                    if (Utils.isMobile(mPhoneNum)) {
                        requestYZM();
                    } else {
                        CustomToast.show("请输入正确的手机号", 0);
                    }
                } else {
                    CustomToast.show("请输入手机号", 0);
                }

                break;
            case R.id.btn_reset:
                String phoneNumber = et_phone.getText().toString().trim();
                String checkCode = et_yzm.getText().toString().trim();
                String pwd1 = et_password.getText().toString().trim();
                String pwd2 = et_password2.getText().toString().trim();

                if (StringUtil.isEmpty(phoneNumber)) {
                    CustomToast.show("请输入手机号码", 0);
                    return;
                }
                if (!Utils.isMobile(phoneNumber)) {
                    CustomToast.show("请输入正确的手机号码", 0);
                    return;
                }
                if (StringUtil.isEmpty(checkCode)) {
                    CustomToast.show("请输入验证码", 0);
                    return;
                }
                if (StringUtil.isEmpty(pwd1)) {
                    CustomToast.show("请输入密码", 0);
                    return;
                }
                if (pwd1.length() < 6 && pwd1.length() > 20) {
                    CustomToast.show("请输入6-20个字符的密码", 0);
                    return;
                }
                if (!pwd1.equals(pwd2)) {
                    CustomToast.show("两次密码不一致", 0);
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
        b.addBody("mobile", mPhoneNum);
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
        b.addBody("password", MD5.getMessageDigest(et_password.getText().toString().getBytes()));

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.FIND_PASSWORD;
        d.flag = AppConst.FIND_PASSWORD;

        requestID = DataLoader.getInstance().loadData(this, d);
    }

    @Override
    public void onNotifyMessage(ResponseData request, ResponseData response) {
        if (request.flag == AppConst.GET_YZM) {
            removeProgressDialog();
            CustomToast.show("验证码已发送，请稍后...", 0);
            btn_getYZM.setEnabled(false);
            mCountDownTimer.start();// 启动倒计时
        } else if (request.flag == AppConst.FIND_PASSWORD) {
            if (response.body != null) {
                CustomToast.show("密码已修改", 0);
                this.finish();
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
                    CustomToast.show("验证码错误", CustomToast.LENGTH_SHORT);
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
                btn_getYZM.setText(getString(R.string.get_checknumber) + "(" + millisUntilFinished / 1000 + "s)");
            }

            @Override
            public void onFinish() {
                btn_getYZM.setEnabled(true);
                btn_getYZM.setText(R.string.get_checknumber);
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
}
