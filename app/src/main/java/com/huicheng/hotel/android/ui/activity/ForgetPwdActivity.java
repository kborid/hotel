package com.huicheng.hotel.android.ui.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.NetURL;
import com.huicheng.hotel.android.common.pay.wxpay.MD5;
import com.huicheng.hotel.android.net.RequestBeanBuilder;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;
import com.prj.sdk.widget.CustomToast;

/**
 * 找回密码
 */
public class ForgetPwdActivity extends BaseActivity implements DataCallback, DialogInterface.OnCancelListener {
    private static final String TAG = "ForgetPwdActivity";

    private EditText et_phone, et_yzm, et_password, et_password2;
    private Button btn_reset, btn_getYZM;
    private String mPhoneNum;
    private CountDownTimer mCountDownTimer;
    private boolean isVaild = false;

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
        et_yzm.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    requestCheckYZM();
                }
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
                if (isVaild) {
                    requestResetPwd();
                } else {
                    CustomToast.show("验证码错误", CustomToast.LENGTH_SHORT);
                }
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
            showProgressDialog(this, "正在加载，请稍候...");
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
//        d.path = "http://192.168.1.78:8881/user/sendsms.json";
        d.flag = AppConst.FIND_PASSWORD;

        if (!isProgressShowing()) {
            showProgressDialog(this);
        }
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    @Override
    public void preExecute(ResponseData request) {
    }

    @Override
    public void notifyMessage(ResponseData request, ResponseData response) throws Exception {
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
                        CustomToast.show("验证码错误", CustomToast.LENGTH_SHORT);
                        isVaild = false;
                        break;
                    case "1":
                        CustomToast.show("验证码正确", CustomToast.LENGTH_SHORT);
                        isVaild = true;
                        break;
                    default:
                        break;
                }
            }
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
    public void onCancel(DialogInterface dialog) {
        DataLoader.getInstance().clear(requestID);
        removeProgressDialog();
    }
}