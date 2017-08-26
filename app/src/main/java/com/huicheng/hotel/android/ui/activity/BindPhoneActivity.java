package com.huicheng.hotel.android.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.alibaba.fastjson.JSON;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.NetURL;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.net.RequestBeanBuilder;
import com.huicheng.hotel.android.net.bean.UserInfo;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.prj.sdk.constants.BroadCastConst;
import com.prj.sdk.net.bean.ResponseData;
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

/**
 * 手机绑定
 *
 * @author LiaoBo
 */
public class BindPhoneActivity extends BaseActivity implements DialogInterface.OnCancelListener {
    private static final String TAG = "BindPhoneActivity";
    private EditText et_phone, et_yzm;
    private Button btn_yzm, btn_bind;
    private String thirdpartusername, thirdpartuserheadphotourl, openid, mPlatform, usertoken;
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
        tv_center_title.setText("手机绑定");
        findViewById(R.id.comm_title_rl).setBackgroundResource(R.color.transparent);
        setCountDownTimer(60 * 1000, 1000);
        et_phone = (EditText) findViewById(R.id.et_phone);
        et_yzm = (EditText) findViewById(R.id.et_yzm);
        btn_yzm = (Button) findViewById(R.id.btn_yzm);
        btn_bind = (Button) findViewById(R.id.btn_bind);
    }

    @Override
    public void initParams() {
        super.initParams();
        dealIntent();
    }

    @Override
    public void dealIntent() {
        super.dealIntent();
        Bundle bundle = getIntent().getExtras();
        if (null != bundle) {
            thirdpartusername = getIntent().getExtras().getString("thirdpartusername");
            thirdpartuserheadphotourl = getIntent().getExtras().getString("thirdpartuserheadphotourl");
            openid = getIntent().getExtras().getString("openid");
            mPlatform = getIntent().getExtras().getString("platform");
            usertoken = getIntent().getExtras().getString("usertoken");
        }
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
        b.addBody("platform", mPlatform);
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
        btn_yzm.setOnClickListener(this);
        btn_bind.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.btn_yzm:
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
    public void onNotifyMessage(ResponseData request, ResponseData response) {
        if (response != null && response.body != null) {
            if (request.flag == AppConst.GET_YZM) {
                removeProgressDialog();
                CustomToast.show("验证码已发送，请稍候...", CustomToast.LENGTH_SHORT);
                btn_yzm.setEnabled(false);
                mCountDownTimer.start();// 启动倒计时
            } else if (request.flag == AppConst.BIND) {
                String ticket = response.body.toString();
                SharedPreferenceUtil.getInstance().setString(AppConst.ACCESS_TICKET, ticket, true);
                SessionContext.setTicket(ticket);
                requestUserInfo(ticket);
            } else if (request.flag == AppConst.CENTER_USERINFO) {
                removeProgressDialog();
                if (StringUtil.isEmpty(response.body.toString()) || response.body.toString().equals("{}")) {
                    CustomToast.show("获取用户信息失败，请重试1", 0);
                    return;
                }
                SessionContext.mUser = JSON.parseObject(response.body.toString(), UserInfo.class);

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
                        LogUtil.i(TAG, result + ", Alias = " + s + ", Tag = " + set);
                    }
                });
                sendBroadcast(new Intent(BroadCastConst.UPDATE_USERINFO));

                //绑定成功，根据性别设置主题
                int index = SessionContext.mUser.user.sex.equals("1") ? 0 : 1;
                SharedPreferenceUtil.getInstance().setInt(AppConst.SKIN_INDEX, index);
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
}
