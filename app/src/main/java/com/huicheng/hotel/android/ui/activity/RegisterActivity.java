package com.huicheng.hotel.android.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.NetURL;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.common.pay.wxpay.MD5;
import com.huicheng.hotel.android.net.RequestBeanBuilder;
import com.huicheng.hotel.android.net.bean.UserInfo;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.prj.sdk.constants.BroadCastConst;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;
import com.prj.sdk.widget.CustomToast;

import java.util.Set;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

public class RegisterActivity extends BaseActivity implements DataCallback, DialogInterface.OnCancelListener {
    private static final String TAG = "RegisterActivity";
    private EditText et_phone, et_yzm, et_pwd;
    private int sex_index = 0;
    private RadioGroup rg_sex_lay;
    private CheckBox cb_check;
    private TextView tv_agreement;
    private Button btn_register, btn_yzm;
    private CountDownTimer mCountDownTimer;
    private RelativeLayout checkbox_lay;
    private CheckBox cb_change;
    private boolean isValided = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_register_layout);
        initViews();
        initParams();
        initListeners();
    }

    @Override
    public void initViews() {
        super.initViews();
        et_phone = (EditText) findViewById(R.id.et_phone);
        et_yzm = (EditText) findViewById(R.id.et_yzm);
        btn_yzm = (Button) findViewById(R.id.btn_yzm);
        et_pwd = (EditText) findViewById(R.id.et_pwd);
        rg_sex_lay = (RadioGroup) findViewById(R.id.rg_sex_lay);
        rg_sex_lay.getChildAt(0).performClick();
        cb_check = (CheckBox) findViewById(R.id.cb_check);
        checkbox_lay = (RelativeLayout) findViewById(R.id.checkBox_lay);
        tv_agreement = (TextView) findViewById(R.id.tv_agreement);
        btn_register = (Button) findViewById(R.id.btn_register);
        cb_change = (CheckBox) findViewById(R.id.cb_change);
    }

    @Override
    public void initParams() {
        super.initParams();
        findViewById(R.id.comm_title_rl).setBackgroundResource(R.color.transparent);
        setCountDownTimer(60 * 1000, 1000);
    }

    @Override
    public void initListeners() {
        super.initListeners();
        btn_register.setOnClickListener(this);
        btn_yzm.setOnClickListener(this);
        tv_agreement.setOnClickListener(this);
        checkbox_lay.setOnClickListener(this);

        et_yzm.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    requestCheckYZM();
                }
            }
        });
        rg_sex_lay.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_sex_female:
                        sex_index = 0;
                        break;
                    case R.id.rb_sex_male:
                        sex_index = 1;
                        break;
                }
            }
        });
        cb_change.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // 设置为明文显示
                    et_pwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    // 设置为密文显示
                    et_pwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
                et_pwd.setSelection(et_pwd.getText().length());// 设置光标位置
            }
        });
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.checkBox_lay:
                boolean flag = cb_check.isChecked();
                cb_check.setChecked(!flag);
                break;
            case R.id.btn_yzm: {
                String phone = et_phone.getText().toString().trim();
                if (StringUtil.notEmpty(phone)) {
                    if (Utils.isMobile(phone)) {
                        requestCheckPhoneNumber();
                    } else {
                        CustomToast.show("请输入正确的手机号", CustomToast.LENGTH_SHORT);
                    }
                } else {
                    CustomToast.show("请输入手机号", CustomToast.LENGTH_SHORT);
                }

                break;
            }
            case R.id.btn_register: {
                if (!isValided) {
                    CustomToast.show("验证码错误", CustomToast.LENGTH_SHORT);
                    return;
                }
                if (cb_check.isChecked()) {
                    String phone = et_phone.getText().toString().trim();
                    String yzm = et_yzm.getText().toString().trim();
                    String pwd = et_pwd.getText().toString().trim();

                    if (StringUtil.isEmpty(phone)) {
                        CustomToast.show("请输入手机号码", CustomToast.LENGTH_SHORT);
                        return;
                    } else {
                        if (!Utils.isMobile(phone)) {
                            CustomToast.show("请输入正确的手机号码", CustomToast.LENGTH_SHORT);
                            return;
                        }
                    }
                    if (StringUtil.isEmpty(yzm)) {
                        CustomToast.show("请输入验证码", CustomToast.LENGTH_SHORT);
                        return;
                    }
                    if (StringUtil.isEmpty(pwd)) {
                        CustomToast.show("密码不允许为空", CustomToast.LENGTH_SHORT);
                        return;
                    } else {
                        if (pwd.length() < 6 || pwd.length() > 20) {
                            CustomToast.show("请输入6-20个字符的密码", CustomToast.LENGTH_SHORT);
                            return;
                        }
                    }
                    requestRegister();
                } else {
                    CustomToast.show("请先阅读《注册协议》", CustomToast.LENGTH_SHORT);
                }
                break;
            }
            case R.id.tv_agreement:
//                Intent mIntent = new Intent(this, WebViewActivity.class);
//                mIntent.putExtra("title", "注册协议");
//                mIntent.putExtra("path", NetURL.REGISTER_AGEMENNT);
//                startActivity(mIntent);
                break;
            default:
                break;
        }

    }

    /**
     * 检测手机号是否已经注册
     */
    public void requestCheckPhoneNumber() {
        LogUtil.d(TAG, "requestCheckPhoneNumber()");
        RequestBeanBuilder b = RequestBeanBuilder.create(false);
        b.addBody("mobile", et_phone.getText().toString());

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.CHECK_PHONE;
        d.flag = AppConst.CHECK_PHONE;

        if (!isProgressShowing()) {
            showProgressDialog(this, getString(R.string.loading));
        }
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    /**
     * 加载验证码
     */
    private void requestYZM() {
        LogUtil.d(TAG, "requestYZM()");
        RequestBeanBuilder b = RequestBeanBuilder.create(false);
        b.addBody("businesstype", AppConst.BUSINESS_TYPE_REGISTER);
        b.addBody("mobile", et_phone.getText().toString());
        b.addBody("smsparam", "code");

        ResponseData data = b.syncRequest(b);
        data.path = NetURL.GET_YZM;
        data.flag = AppConst.GET_YZM;

        requestID = DataLoader.getInstance().loadData(this, data);
    }

    /**
     * 校验验证码
     */
    private void requestCheckYZM() {
        LogUtil.d(TAG, "requestCheckYZM()");
        RequestBeanBuilder b = RequestBeanBuilder.create(false);
        b.addBody("businesstype", AppConst.BUSINESS_TYPE_REGISTER);
        b.addBody("mobile", et_phone.getText().toString());
        b.addBody("code", et_yzm.getText().toString());

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.CHECK_YZM;
        d.flag = AppConst.CHECK_YZM;

        requestID = DataLoader.getInstance().loadData(this, d);
    }

    /**
     * 注册
     */
    private void requestRegister() {
        LogUtil.d(TAG, "requestRegister()");
        System.out.println("index = " + sex_index);
        System.out.println("sex = " + getResources().getStringArray(R.array.sex)[sex_index]);
        RequestBeanBuilder b = RequestBeanBuilder.create(false);
        b.addBody("sex", String.valueOf(sex_index));
        b.addBody("businesstype", AppConst.BUSINESS_TYPE_REGISTER);
        b.addBody("mobile", et_phone.getText().toString());
        b.addBody("code", et_yzm.getText().toString());
        b.addBody("password", MD5.getMessageDigest(et_pwd.getText().toString().getBytes()));
        b.addBody("siteid", SharedPreferenceUtil.getInstance().getString(AppConst.SITEID, "", false));
        b.addBody("channelid", "00"); //注册渠道：00-app, 01-web
        b.addBody("ip", "");

        ResponseData data = b.syncRequest(b);
        data.path = NetURL.REGISTER;
        data.flag = AppConst.REGISTER;

        if (!isProgressShowing()) {
            showProgressDialog(this);
        }

        requestID = DataLoader.getInstance().loadData(this, data);
    }

    /**
     * 登录
     */
    private void requestLogin() {
        String userName = et_phone.getText().toString().trim();
        String pwd = et_pwd.getText().toString().trim();

        RequestBeanBuilder b = RequestBeanBuilder.create(false);
        b.addBody("login", userName);
        b.addBody("password", MD5.getMessageDigest(pwd.getBytes()));

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.LOGIN;
        d.flag = AppConst.LOGIN;

        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void requestUserInfo(String ticket) {
        System.out.println("getUserInfo() ticket = " + ticket);
        RequestBeanBuilder builder = RequestBeanBuilder.create(true);

        ResponseData data = builder.syncRequest(builder);
        data.path = NetURL.CENTER_USERINFO;
        data.flag = AppConst.CENTER_USERINFO;
        requestID = DataLoader.getInstance().loadData(this, data);
    }

    private void requestSaveRecommandData() {
        JSONObject mJson = JSON.parseObject(SessionContext.getRecommandAppData().getData());
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("recommanduserid", mJson.getString("userID"));
        b.addBody("userid", SessionContext.mUser.user.userid);
        b.addBody("channel", mJson.getString("channel"));

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.SAVE_RECOMMAND;
        d.flag = AppConst.SAVE_RECOMMAND;

        requestID = DataLoader.getInstance().loadData(this, d);
    }

    @Override
    public void preExecute(ResponseData request) {
    }

    @Override
    public void notifyMessage(ResponseData request, ResponseData response) throws Exception {

        if (response != null && response.body != null) {
            if (request.flag == AppConst.CHECK_PHONE) {
                System.out.println("json = " + response.body.toString());
                JSONObject mJson = JSON.parseObject(response.body.toString());
                if (mJson.containsKey("status")) {
                    String status = mJson.getString("status");
                    switch (status) {
                        case "001010":
                            requestYZM();
                            break;
                        case "001011":
                            removeProgressDialog();
                            CustomToast.show("你输入的手机号码已被占用", CustomToast.LENGTH_SHORT);
                            break;
                        case "001002":
                            removeProgressDialog();
                            CustomToast.show("你输入的手机号码为空", CustomToast.LENGTH_SHORT);
                            break;
                        default:
                            break;
                    }
                }
            } else if (request.flag == AppConst.GET_YZM) {
                removeProgressDialog();
                CustomToast.show("验证码已发送，请稍候...", Toast.LENGTH_LONG);
                btn_yzm.setEnabled(false);
                mCountDownTimer.start();// 启动倒计时
            } else if (request.flag == AppConst.CHECK_YZM) {
                System.out.println("json = " + response.body.toString());
                JSONObject mJson = JSON.parseObject(response.body.toString());
                if (mJson.containsKey("status")) {
                    String status = mJson.getString("status");
                    switch (status) {
                        case "0":
                            CustomToast.show("验证码错误", CustomToast.LENGTH_SHORT);
                            isValided = false;
                            break;
                        case "1":
                            CustomToast.show("验证码正确", CustomToast.LENGTH_SHORT);
                            isValided = true;
                            break;
                        default:
                            break;
                    }
                }
            } else if (request.flag == AppConst.REGISTER) {
                CustomToast.show("注册成功", CustomToast.LENGTH_SHORT);
                requestLogin();
            } else if (request.flag == AppConst.LOGIN) {
                JSONObject mJson = JSON.parseObject(response.body.toString());
                if (mJson.containsKey("status")) {
                    if (mJson.getString("status").equals("1")) {
                        String ticket = mJson.getString("accessTicket");
                        SharedPreferenceUtil.getInstance().setString(AppConst.ACCESS_TICKET, ticket, true);
                        SessionContext.setTicket(ticket);
                        requestUserInfo(ticket);
                    } else {
                        removeProgressDialog();
                        CustomToast.show(mJson.getString("message"), CustomToast.LENGTH_SHORT);
                    }
                }
            } else if (request.flag == AppConst.CENTER_USERINFO) {
                if (StringUtil.isEmpty(response.body.toString()) || response.body.toString().equals("{}")) {
                    CustomToast.show("获取用户信息失败，请重试1", 0);
                    return;
                }
                SessionContext.mUser = JSON.parseObject(response.body.toString(), UserInfo.class);
                System.out.println(response.body.toString());

                if (SessionContext.mUser == null || StringUtil.isEmpty(SessionContext.mUser)) {
                    CustomToast.show("获取用户信息失败，请重试2", 0);
                    return;
                }

                String userName = et_phone.getText().toString().trim();
                SharedPreferenceUtil.getInstance().setString(AppConst.USERNAME, userName, true);// 保存用户名
                SharedPreferenceUtil.getInstance().setString(AppConst.LAST_LOGIN_DATE, DateUtil.getCurDateStr(null), false);// 保存登录时间
                SharedPreferenceUtil.getInstance().setString(AppConst.USER_PHOTO_URL, SessionContext.mUser != null ? SessionContext.mUser.user.headphotourl : "", false);
                SharedPreferenceUtil.getInstance().setString(AppConst.USER_INFO, response.body.toString(), true);
                // SharedPreferenceUtil.getInstance().setString(AppConst.THIRDPARTYBIND, "", false);//置空第三方绑定信息，需要在详情页面重新获取
                CustomToast.show("登录成功", 0);
                JPushInterface.setAliasAndTags(this, SessionContext.mUser.user.mobile, null, new TagAliasCallback() {
                    @Override
                    public void gotResult(int i, String s, Set<String> set) {
                        String result = (i == 0) ? "设置成功" : "设置失败";
                        System.out.println(result + ", Alias = " + s + ", Tag = " + set);
                    }
                });
                sendBroadcast(new Intent(BroadCastConst.UPDATE_USERINFO));
                if (SessionContext.getRecommandAppData() != null) {
                    requestSaveRecommandData();
                } else {
                    this.finish();
                }
            } else if (request.flag == AppConst.SAVE_RECOMMAND) {
                removeProgressDialog();
                System.out.println("json = " + response.body.toString());
                this.finish();
            }
        }

    }

    @Override
    public void onCancel(DialogInterface dialog) {
        DataLoader.getInstance().clear(requestID);
        removeProgressDialog();
    }

    /**
     * 设置倒计时
     */
    private void setCountDownTimer(long millisInFuture, long countDownInterval) {
        mCountDownTimer = new CountDownTimer(millisInFuture, countDownInterval) {

            @Override
            public void onTick(long millisUntilFinished) {
                btn_yzm.setText(getString(R.string.get_checknumber) + "(" + millisUntilFinished / 1000 + "s)");
            }

            @Override
            public void onFinish() {
                btn_yzm.setEnabled(true);
                btn_yzm.setText(R.string.get_checknumber);
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mCountDownTimer) {
            mCountDownTimer.cancel();
        }
    }
}