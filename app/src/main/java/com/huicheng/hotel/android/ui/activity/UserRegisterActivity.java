package com.huicheng.hotel.android.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fm.openinstall.OpenInstall;
import com.huicheng.hotel.android.PRJApplication;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.NetURL;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.common.ShareTypeDef;
import com.huicheng.hotel.android.common.pay.wxpay.MD5;
import com.huicheng.hotel.android.net.RequestBeanBuilder;
import com.huicheng.hotel.android.net.bean.UserInfo;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.huicheng.hotel.android.ui.dialog.CustomToast;
import com.prj.sdk.constants.BroadCastConst;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;

import java.util.Set;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

public class UserRegisterActivity extends BaseActivity {

    private TextView tv_right;
    private EditText et_phone, et_yzm, et_pwd, et_yqm;
    private LinearLayout pwd_lay;
    private TextView tv_yzm;
    private TextView tv_action;
    private CheckBox cb_pwd_status_check, cb_agreement_check;
    private TextView tv_agreement;

    private CountDownTimer mCountDownTimer;
    private boolean isValid = false;

    //推荐信息
    private String recommendMobile;
    private String recommendUserId;
    private String recommendChannel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initMainWindow();
        overridePendingTransition(R.anim.user_login_enter_in, R.anim.user_login_enter_out);
        setContentView(R.layout.act_register_layout);
        initViews();
        initParams();
        initListeners();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        overridePendingTransition(R.anim.user_login_enter_in, R.anim.user_login_enter_out);
    }

    @Override
    public void initViews() {
        super.initViews();
        tv_right = (TextView) findViewById(R.id.tv_right);
        et_phone = (EditText) findViewById(R.id.et_phone);
        et_yzm = (EditText) findViewById(R.id.et_yzm);
        tv_yzm = (TextView) findViewById(R.id.tv_yzm);
        et_pwd = (EditText) findViewById(R.id.et_pwd);
        cb_pwd_status_check = (CheckBox) findViewById(R.id.cb_pwd_status_check);
        pwd_lay = (LinearLayout) findViewById(R.id.pwd_lay);
        et_yqm = (EditText) findViewById(R.id.et_yqm);
        tv_action = (TextView) findViewById(R.id.tv_action);
        cb_agreement_check = (CheckBox) findViewById(R.id.cb_agreement_check);
        tv_agreement = (TextView) findViewById(R.id.tv_agreement);
    }

    @Override
    public void initParams() {
        super.initParams();
        setCountDownTimer(60 * 1000, 1000);
        checkInputForActionBtnStatus();

        if (SessionContext.getOpenInstallAppData() != null
                && StringUtil.notEmpty(SessionContext.getOpenInstallAppData().getData())) {
            JSONObject mJson = JSON.parseObject(SessionContext.getOpenInstallAppData().getData());
            recommendUserId = mJson.containsKey("userID") ? mJson.getString("userID") : "";
            recommendChannel = mJson.containsKey("channel") ? mJson.getString("channel") : "";
            recommendMobile = mJson.containsKey("mobile") ? mJson.getString("mobile") : "";
            LogUtil.i(TAG, "OpenInstall Info:" + recommendChannel + ", " + recommendUserId + ", " + recommendMobile);
            if (ShareTypeDef.SHARE_B2C.equals(recommendChannel)) {
                et_yqm.setVisibility(View.GONE);
            } else if (ShareTypeDef.SHARE_C2C.equals(recommendChannel) && StringUtil.notEmpty(recommendMobile)) {
                et_yqm.setVisibility(View.VISIBLE);
                et_yqm.setEnabled(false);
                et_yqm.setText(recommendMobile);
            }
        }
    }

    @Override
    public void initListeners() {
        super.initListeners();
        tv_right.setOnClickListener(this);
        tv_action.setOnClickListener(this);
        tv_yzm.setOnClickListener(this);
        tv_agreement.setOnClickListener(this);

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
        cb_pwd_status_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    et_pwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    et_pwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
                et_pwd.setSelection(et_pwd.getText().length());// 设置光标位置
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
                checkInputForActionBtnStatus();
                if (s.length() == 11) {
                    tv_yzm.performClick();
                }
            }
        });

        et_yzm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                checkInputForActionBtnStatus();
            }
        });

        et_pwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                checkInputForActionBtnStatus();
            }
        });

        cb_agreement_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkInputForActionBtnStatus();
            }
        });
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.tv_right: {
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                Intent intent = new Intent(this, UserLoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                    startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this,
//                            new Pair<View, String>(et_phone, "share_phone"),
//                            new Pair<View, String>(pwd_lay, "share_pwd"),
//                            new Pair<View, String>(tv_action, "share_action")).toBundle());
//                } else {
                startActivity(intent);
//                    overridePendingTransition(0, 0);
//                }
                break;
            }
            case R.id.cb_agreement_check:
                boolean flag = cb_agreement_check.isChecked();
                cb_agreement_check.setChecked(!flag);
                break;
            case R.id.tv_yzm: {
                String phone = et_phone.getText().toString();
                if (StringUtil.notEmpty(phone)) {
                    if (Utils.isMobile(phone)) {
                        requestCheckPhoneNumber();
                    } else {
                        CustomToast.show(getString(R.string.tips_user_phone_confirm), CustomToast.LENGTH_SHORT);
                    }
                } else {
                    CustomToast.show(getString(R.string.tips_user_phone), CustomToast.LENGTH_SHORT);
                }

                break;
            }
            case R.id.tv_action: {
                String phone = et_phone.getText().toString();
                String yzm = et_yzm.getText().toString();
                String pwd = et_pwd.getText().toString();

                if (!Utils.isMobile(phone)) {
                    CustomToast.show(getString(R.string.tips_user_phone_confirm), CustomToast.LENGTH_SHORT);
                    return;
                }
                if (pwd.length() < 6 && pwd.length() > 20) {
                    CustomToast.show(getString(R.string.tips_user_pwd_confirm), CustomToast.LENGTH_SHORT);
                    return;
                }
                requestCheckYZM();
                break;
            }

            case R.id.tv_agreement:
                Intent intent = new Intent(this, AboutActivity.class);
                intent.putExtra("title", getResources().getString(R.string.setting_usage_condition));
                intent.putExtra("index", AboutActivity.WORK_CONDITION);
                startActivity(intent);
                break;
            default:
                break;
        }

    }

    private void checkInputForActionBtnStatus() {
        boolean flag = false;
        if (StringUtil.notEmpty(et_phone.getText().toString())
                && StringUtil.notEmpty(et_yzm.getText().toString())
                && StringUtil.notEmpty(et_pwd.getText().toString())
                && cb_agreement_check.isChecked()) {
            flag = true;
        }
        tv_action.setEnabled(flag);
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
            showProgressDialog(this);
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

        if (!isProgressShowing()) {
            showProgressDialog(this);
        }

        requestID = DataLoader.getInstance().loadData(this, d);
    }

    /**
     * 注册
     */
    private void requestRegister() {
        LogUtil.d(TAG, "requestRegister()");
        RequestBeanBuilder b = RequestBeanBuilder.create(false);
        b.addBody("sex", "1");
        b.addBody("businesstype", AppConst.BUSINESS_TYPE_REGISTER);
        b.addBody("mobile", et_phone.getText().toString());
        b.addBody("code", et_yzm.getText().toString());
        b.addBody("password", MD5.getMessageDigest(et_pwd.getText().toString().getBytes()));
        b.addBody("siteid", SharedPreferenceUtil.getInstance().getString(AppConst.SITEID, "", false));
        b.addBody("channelid", "00"); //注册渠道：00-app, 01-web
        b.addBody("ip", "");
        b.addBody("invitermobile", et_yqm.getText().toString());


        ResponseData data = b.syncRequest(b);
        data.path = NetURL.REGISTER;
        data.flag = AppConst.REGISTER;

        requestID = DataLoader.getInstance().loadData(this, data);
    }

    /**
     * 登录
     */
    private void requestLogin() {
        String userName = et_phone.getText().toString();
        String pwd = et_pwd.getText().toString();

        RequestBeanBuilder b = RequestBeanBuilder.create(false);
        b.addBody("login", userName);
        b.addBody("password", MD5.getMessageDigest(pwd.getBytes()));

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.LOGIN;
        d.flag = AppConst.LOGIN;

        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void requestUserInfo(String ticket) {
        LogUtil.i(TAG, "getUserInfo() ticket = " + ticket);
        RequestBeanBuilder builder = RequestBeanBuilder.create(true);

        ResponseData data = builder.syncRequest(builder);
        data.path = NetURL.CENTER_USERINFO;
        data.flag = AppConst.CENTER_USERINFO;
        requestID = DataLoader.getInstance().loadData(this, data);
    }

    private void requestSaveRecommandData() {
        LogUtil.i(TAG, "requestSaveRecommandData()");
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("recommanduserid", recommendUserId);
        b.addBody("userid", SessionContext.mUser.user.userid);
        b.addBody("channel", recommendChannel);

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.SAVE_RECOMMAND;
        d.flag = AppConst.SAVE_RECOMMAND;

        requestID = DataLoader.getInstance().loadData(this, d);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SessionContext.isLogin()) {
            finish();
        }
    }

    @Override
    public void onNotifyMessage(ResponseData request, ResponseData response) {
        if (response != null && response.body != null) {
            if (request.flag == AppConst.CHECK_PHONE) {
                LogUtil.i(TAG, "json = " + response.body.toString());
                JSONObject mJson = JSON.parseObject(response.body.toString());
                if (mJson.containsKey("status")) {
                    String status = mJson.getString("status");
                    switch (status) {
                        case "001010":
                            requestYZM();
                            break;
                        case "001011":
                            removeProgressDialog();
                            CustomToast.show(getString(R.string.tips_user_phone_isused), CustomToast.LENGTH_SHORT);
                            break;
                        case "001002":
                            removeProgressDialog();
                            CustomToast.show(getString(R.string.tips_user_phone_isempty), CustomToast.LENGTH_SHORT);
                            break;
                        default:
                            break;
                    }
                }
            } else if (request.flag == AppConst.GET_YZM) {
                removeProgressDialog();
                CustomToast.show(getString(R.string.tips_user_send_yzm), CustomToast.LENGTH_SHORT);
                tv_yzm.setEnabled(false);
                et_phone.setEnabled(false);
                mCountDownTimer.start();// 启动倒计时
                et_yzm.requestFocus();
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            } else if (request.flag == AppConst.CHECK_YZM) {
                LogUtil.i(TAG, "json = " + response.body.toString());
                JSONObject mJson = JSON.parseObject(response.body.toString());
                if (mJson.containsKey("status")) {
                    String status = mJson.getString("status");
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
                        requestRegister();
                    } else {
                        removeProgressDialog();
                        et_yzm.requestFocus();
                        CustomToast.show(getString(R.string.tips_user_yzm_error), CustomToast.LENGTH_SHORT);
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
                    CustomToast.show("获取用户信息失败，请重试", 0);
                    return;
                }
                SessionContext.mUser = JSON.parseObject(response.body.toString(), UserInfo.class);
                LogUtil.i(TAG, response.body.toString());

                if (SessionContext.mUser == null || StringUtil.isEmpty(SessionContext.mUser)) {
                    CustomToast.show("获取用户信息失败，请重试", 0);
                    return;
                }

                String userName = et_phone.getText().toString();
                SharedPreferenceUtil.getInstance().setString(AppConst.USERNAME, userName, true);// 保存用户名
                SharedPreferenceUtil.getInstance().setString(AppConst.LAST_LOGIN_DATE, DateUtil.getCurDateStr(null), false);// 保存登录时间
                SharedPreferenceUtil.getInstance().setString(AppConst.USER_PHOTO_URL, SessionContext.mUser != null ? SessionContext.mUser.user.headphotourl : "", false);
                SharedPreferenceUtil.getInstance().setString(AppConst.USER_INFO, response.body.toString(), true);
                CustomToast.show("登录成功", 0);
                JPushInterface.setAliasAndTags(PRJApplication.getInstance(), SessionContext.mUser.user.mobile, null, new TagAliasCallback() {
                    @Override
                    public void gotResult(int i, String s, Set<String> set) {
                        LogUtil.i(TAG, ((i == 0) ? "设置成功" : "设置失败") + ", Alias = " + s + ", Tag = " + set);
                    }
                });
                sendBroadcast(new Intent(BroadCastConst.UPDATE_USERINFO));

                //注册成功，自动登录，根据性别设置主题
                int index = SessionContext.mUser.user.sex.equals("1") ? 0 : 1;
                SharedPreferenceUtil.getInstance().setInt(AppConst.SKIN_INDEX, index);

                if (SessionContext.getOpenInstallAppData() != null) {
                    OpenInstall.reportRegister();
                    requestSaveRecommandData();
                } else {
                    removeProgressDialog();
                    finish();
                }
            } else if (request.flag == AppConst.SAVE_RECOMMAND) {
                SessionContext.setOpenInstallAppData(null);
                removeProgressDialog();
                finish();
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
                et_phone.setEnabled(true);
                tv_yzm.setText(R.string.tips_reget_yzm);
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
