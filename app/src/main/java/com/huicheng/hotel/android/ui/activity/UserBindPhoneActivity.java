package com.huicheng.hotel.android.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fm.openinstall.OpenInstall;
import com.huicheng.hotel.android.PRJApplication;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.NetURL;
import com.huicheng.hotel.android.common.SessionContext;
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

/**
 * 手机绑定
 */
public class UserBindPhoneActivity extends BaseActivity implements DialogInterface.OnCancelListener {

    private TextView tv_title_summary;
    private EditText et_phone, et_yzm;
    private TextView tv_yzm;
    private Button btn_bind;
    private String thirdpartusername, thirdpartuserheadphotourl, openid, mPlatformCode, mPlatform, usertoken;
    private CountDownTimer mCountDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_bindphone_layout);
        initViews();
        initParams();
        initListeners();
    }

    @Override
    public void initViews() {
        super.initViews();
        tv_title_summary = (TextView) findViewById(R.id.tv_title_summary);
        et_phone = (EditText) findViewById(R.id.et_phone);
        et_yzm = (EditText) findViewById(R.id.et_yzm);
        tv_yzm = (TextView) findViewById(R.id.tv_yzm);
        btn_bind = (Button) findViewById(R.id.btn_bind);
    }

    @Override
    public void dealIntent() {
        super.dealIntent();
        Bundle bundle = getIntent().getExtras();
        if (null != bundle) {
            thirdpartusername = bundle.getString("thirdpartusername");
            thirdpartuserheadphotourl = bundle.getString("thirdpartuserheadphotourl");
            openid = bundle.getString("openid");
            mPlatformCode = bundle.getString("platform");
            usertoken = bundle.getString("usertoken");
        }
        if (StringUtil.notEmpty(mPlatformCode)) {
            if ("02".equals(mPlatformCode)) {
                mPlatform = "QQ";
            } else if ("03".equals(mPlatformCode)) {
                mPlatform = "微信";
            }
        }
    }

    @Override
    public void initParams() {
        super.initParams();
        tv_title_summary.setText(String.format(getString(R.string.tips_user_third_login), mPlatform));
        setCountDownTimer(60 * 1000, 1000);
    }

    /**
     * 加载验证码
     */
    private void requestYZM() {
        LogUtil.d(TAG, "requestYZM()");
        RequestBeanBuilder b = RequestBeanBuilder.create(false);
        b.addBody("businesstype", AppConst.BUSINESS_TYPE_BIND);
        b.addBody("mobile", et_phone.getText().toString());
        b.addBody("smsparam", "code");

        ResponseData data = b.syncRequest(b);
        data.path = NetURL.GET_YZM;
        data.flag = AppConst.GET_YZM;

        requestID = DataLoader.getInstance().loadData(this, data);
    }

    private void requestBindPhone() {
        RequestBeanBuilder b = RequestBeanBuilder.create(false);
        b.addBody("openid", openid);
        b.addBody("mobile", et_phone.getText().toString());
        b.addBody("siteid", SharedPreferenceUtil.getInstance().getString(AppConst.SITEID, "", false));
        b.addBody("thirdpartuserheadphotourl", thirdpartuserheadphotourl);
        b.addBody("platform", mPlatformCode);
        b.addBody("code", et_yzm.getText().toString());
        b.addBody("thirdpartusername", thirdpartusername);
        b.addBody("businesstype", AppConst.BUSINESS_TYPE_BIND);
        b.addBody("channelid", "00");

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.BIND;
        d.flag = AppConst.BIND;
        if (!isProgressShowing()) {
            showProgressDialog(this);
        }
        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void requestSaveRecommandData() {
        JSONObject mJson = JSON.parseObject(SessionContext.getOpenInstallAppData().getData());
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("recommanduserid", mJson.containsKey("userID") ? mJson.getString("userID") : "");
        b.addBody("userid", SessionContext.mUser.user.userid);
        b.addBody("channel", mJson.containsKey("channel") ? mJson.getString("channel") : "");

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.SAVE_RECOMMAND;
        d.flag = AppConst.SAVE_RECOMMAND;

        requestID = DataLoader.getInstance().loadData(this, d);
    }

    private void requestUserInfo(String ticket) {
        LogUtil.i(TAG, "requestUserInfo() ticket = " + ticket);
        RequestBeanBuilder builder = RequestBeanBuilder.create(true);

        ResponseData data = builder.syncRequest(builder);
        data.path = NetURL.CENTER_USERINFO;
        data.flag = AppConst.CENTER_USERINFO;
        requestID = DataLoader.getInstance().loadData(this, data);
    }

    @Override
    public void initListeners() {
        super.initListeners();
        tv_yzm.setOnClickListener(this);
        btn_bind.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.tv_yzm:
                if (!Utils.isMobile(et_phone.getText().toString())) {
                    CustomToast.show("请输入正确的手机号码", CustomToast.LENGTH_SHORT);
                    return;
                }
                requestYZM();
                break;
            case R.id.btn_bind:
                if (StringUtil.isEmpty(et_yzm.getText().toString())) {
                    CustomToast.show("请输入验证码", CustomToast.LENGTH_SHORT);
                    return;
                }
                requestBindPhone();
                break;
            default:
                break;
        }

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
        overridePendingTransition(0, 0);
    }

    @Override
    public void onNotifyMessage(ResponseData request, ResponseData response) {
        if (response != null && response.body != null) {
            if (request.flag == AppConst.GET_YZM) {
                removeProgressDialog();
                CustomToast.show("验证码已发送，请稍候...", CustomToast.LENGTH_SHORT);
                tv_yzm.setEnabled(false);
                mCountDownTimer.start();// 启动倒计时
            } else if (request.flag == AppConst.BIND) {
                String ticket = response.body.toString();
                SharedPreferenceUtil.getInstance().setString(AppConst.ACCESS_TICKET, ticket, true);
                SessionContext.setTicket(ticket);
                requestUserInfo(ticket);
            } else if (request.flag == AppConst.CENTER_USERINFO) {
                removeProgressDialog();
                if (StringUtil.isEmpty(response.body.toString()) || response.body.toString().equals("{}")) {
                    CustomToast.show("获取用户信息失败，请重试", 0);
                    return;
                }
                SessionContext.mUser = JSON.parseObject(response.body.toString(), UserInfo.class);

                if (SessionContext.mUser == null || StringUtil.isEmpty(SessionContext.mUser)) {
                    CustomToast.show("获取用户信息失败，请重试", 0);
                    return;
                }

                String userName = et_phone.getText().toString().trim();
                SharedPreferenceUtil.getInstance().setString(AppConst.USERNAME, userName, true);// 保存用户名
                SharedPreferenceUtil.getInstance().setString(AppConst.LAST_LOGIN_DATE, DateUtil.getCurDateStr(null), false);// 保存登录时间
                SharedPreferenceUtil.getInstance().setString(AppConst.USER_PHOTO_URL, SessionContext.mUser != null ? SessionContext.mUser.user.headphotourl : "", false);
                SharedPreferenceUtil.getInstance().setString(AppConst.USER_INFO, response.body.toString(), true);
                // SharedPreferenceUtil.getInstance().setString(AppConst.THIRDPARTYBIND, "", false);//置空第三方绑定信息，需要在详情页面重新获取
                CustomToast.show("登录成功", 0);
                JPushInterface.setAliasAndTags(PRJApplication.getInstance(), SessionContext.mUser.user.mobile, null, new TagAliasCallback() {
                    @Override
                    public void gotResult(int i, String s, Set<String> set) {
                        String result = (i == 0) ? "设置成功" : "设置失败";
                        LogUtil.i(TAG, result + ", Alias = " + s + ", Tag = " + set);
                    }
                });
                sendBroadcast(new Intent(BroadCastConst.UPDATE_USERINFO));

                //绑定成功，根据性别设置主题
//                int index = SessionContext.mUser.user.sex.equals("1") ? 0 : 1;
                SharedPreferenceUtil.getInstance().setInt(AppConst.SKIN_INDEX, 0);
                if (SessionContext.getOpenInstallAppData() != null) {
                    OpenInstall.reportRegister();
                    requestSaveRecommandData();
                } else {
                    removeProgressDialog();
                    this.finish();
                }
            } else if (request.flag == AppConst.SAVE_RECOMMAND) {
                SessionContext.setOpenInstallAppData(null);
                removeProgressDialog();
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
                tv_yzm.setText(millisUntilFinished / 1000 + "s");
            }

            @Override
            public void onFinish() {
                tv_yzm.setEnabled(true);
                tv_yzm.setText(R.string.tips_reget_yzm);
            }
        };
    }
}
