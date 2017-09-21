package com.huicheng.hotel.android.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huicheng.hotel.android.PRJApplication;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.NetURL;
import com.huicheng.hotel.android.common.SessionContext;
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
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.UMShareConfig;
import com.umeng.socialize.bean.SHARE_MEDIA;

import java.util.Map;
import java.util.Set;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

/**
 * 登录
 */
public class LoginActivity extends BaseActivity implements DialogInterface.OnCancelListener, OnCheckedChangeListener {

    private static final String TAG = "LoginActivity";
    private EditText et_phone, et_pwd;
    private Button btn_login;
    private TextView tv_forget_pwd, tv_reigster;
    private static onCancelLoginListener mCancelLogin;
    private CheckBox checkBox;
    private ImageView btn_cancel;
    private String usertoken;
    private UMShareAPI mShareAPI = null;
    private String mPlatform; //（01-新浪微博，02-腾讯QQ，03-微信，04-支付宝）
    private String thirdpartusername, thirdpartuserheadphotourl, openid, unionid;
    private Button btn_wx, btn_qq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_login_layout);
        initViews();
        initParams();
        initListeners();
    }

    @Override
    public void initViews() {
        super.initViews();
        et_phone = (EditText) findViewById(R.id.et_username);
        et_pwd = (EditText) findViewById(R.id.et_pwd);
        btn_login = (Button) findViewById(R.id.btn_login);
        tv_forget_pwd = (TextView) findViewById(R.id.tv_forget_pwd);
        tv_reigster = (TextView) findViewById(R.id.tv_reigster);
        checkBox = (CheckBox) findViewById(R.id.cb_change);
        btn_cancel = (ImageView) findViewById(R.id.btn_cancel);

        btn_wx = (Button) findViewById(R.id.btn_wx);
        btn_qq = (Button) findViewById(R.id.btn_qq);
    }

    @Override
    public void initParams() {
        super.initParams();
        SessionContext.cleanUserInfo();
        String name = SharedPreferenceUtil.getInstance().getString(AppConst.USERNAME, "", true);
        if (StringUtil.notEmpty(name)) {
            et_phone.setText(name);// 设置默认用户名
        }
        UMShareConfig config = new UMShareConfig();
        config.isNeedAuthOnGetUserInfo(true);
        mShareAPI = UMShareAPI.get(this);
        mShareAPI.setShareConfig(config);
    }

    @Override
    public void initListeners() {
        btn_login.setOnClickListener(this);
        tv_forget_pwd.setOnClickListener(this);
        tv_reigster.setOnClickListener(this);
        checkBox.setOnCheckedChangeListener(this);
        btn_cancel.setOnClickListener(this);
        btn_wx.setOnClickListener(this);
        btn_qq.setOnClickListener(this);
        et_pwd.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    btn_login.performClick();
                    return true;
                }
                return false;
            }
        });
        super.initListeners();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                if (mCancelLogin != null) {
                    mCancelLogin.isCancelLogin(true);
                }
                this.finish();
                break;
            case R.id.btn_login:
                requestCheckUserstatus();
                break;
            case R.id.tv_reigster:
                Intent intent = new Intent();
                intent.setClass(this, RegisterActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_forget_pwd:
                Intent intent2 = new Intent();
                intent2.setClass(this, ForgetPwdActivity.class);
                startActivity(intent2);
                break;
            case R.id.btn_cancel:// 置空
                et_phone.setText("");
                break;
            case R.id.btn_qq:
                login(SHARE_MEDIA.QQ);
                break;
            case R.id.btn_wx:
                if (WXAPIFactory.createWXAPI(this, null).isWXAppInstalled()) {
                    login(SHARE_MEDIA.WEIXIN);
                } else {
                    CustomToast.show(getString(R.string.not_install_wx), 0);
                }
                break;
        }
        super.onClick(v);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SessionContext.isLogin()) {
            this.finish();
        }
    }

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

    /**
     * 授权。如果授权成功，则获取用户信息</br>
     */
    private void login(final SHARE_MEDIA platform) {
        mShareAPI.getPlatformInfo(this, platform, new UMAuthListener() {
            @Override
            public void onStart(SHARE_MEDIA share_media) {
                CustomToast.show("开始授权", CustomToast.LENGTH_SHORT);
            }

            @Override
            public void onComplete(SHARE_MEDIA platform, int i, Map<String, String> info) {
                try {
                    if (platform == SHARE_MEDIA.QQ) { // QQ
                        mPlatform = "02";
                        openid = info.get("openid");
                        usertoken = info.get("access_token");
                        thirdpartusername = info.get("name");
                        thirdpartuserheadphotourl = info.get("iconurl");
                        if (AppConst.ISDEVELOP) {
                            LogUtil.i(TAG, "QQ third name = " + thirdpartusername);
                            LogUtil.i(TAG, "QQ third url = " + thirdpartuserheadphotourl);
                            LogUtil.i(TAG, "QQ openid = " + openid);
                            LogUtil.i(TAG, "QQ uid = " + info.get("uid"));
                            LogUtil.i(TAG, "QQ usertoken = " + usertoken);
                        }
                    } else if (platform == SHARE_MEDIA.WEIXIN) { // 微信
                        mPlatform = "03";
//                        openid = info.get("openid");
                        openid = info.get("unionid");
                        usertoken = info.get("access_token");
                        thirdpartusername = info.get("name");
                        thirdpartuserheadphotourl = info.get("iconurl");
                        if (AppConst.ISDEVELOP) {
                            LogUtil.i(TAG, "WX third name = " + thirdpartusername);
                            LogUtil.i(TAG, "WX third url = " + thirdpartuserheadphotourl);
                            LogUtil.i(TAG, "WX openid = " + openid);
                            LogUtil.i(TAG, "WX uid = " + info.get("unionid"));
                            LogUtil.i(TAG, "WX usertoken = " + usertoken);
                        }
                    }

                    checkThirdLogin();

                    Set<String> keys = info.keySet();
                    for (String key : keys) {
                        LogUtil.i(TAG, key + "=" + info.get(key) + "\r\n");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    CustomToast.show("授权失败", Toast.LENGTH_SHORT);
                }
            }

            @Override
            public void onError(SHARE_MEDIA share_media, int i, Throwable throwable) {
                CustomToast.show("授权错误", CustomToast.LENGTH_SHORT);
            }

            @Override
            public void onCancel(SHARE_MEDIA share_media, int i) {
                CustomToast.show("取消授权", CustomToast.LENGTH_SHORT);
            }

        });
    }

    /**
     * 登录前检查用户状态
     */
    private void requestCheckUserstatus() {
        String userName = et_phone.getText().toString().trim();
        String pwd = et_pwd.getText().toString().trim();

        if (StringUtil.isEmpty(userName)) {
            CustomToast.show("用户名不能为空", CustomToast.LENGTH_SHORT);
            return;
        }
        if (StringUtil.isEmpty(pwd)) {
            CustomToast.show("密码不能为空", CustomToast.LENGTH_SHORT);
            return;
        }

        RequestBeanBuilder b = RequestBeanBuilder.create(false);
        b.addBody("login", userName);

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.CHECK_USER;
        d.flag = AppConst.CHECK_USER;

        if (!isProgressShowing()) {
            showProgressDialog(this);
        }

        requestID = DataLoader.getInstance().loadData(this, d);
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

    /**
     * 判断是否绑定了三方帐号
     */
    private void checkThirdLogin() {
        RequestBeanBuilder builder = RequestBeanBuilder.create(false);
        builder.addBody("openid", openid);// 微信openid值为uid
        builder.addBody("platform", mPlatform);
        ResponseData data = builder.syncRequest(builder);
        data.path = NetURL.BIND_CHECK;
        data.flag = AppConst.BIND_CHECK;

        if (!isProgressShowing()) {
            showProgressDialog(this);
        }
        requestID = DataLoader.getInstance().loadData(this, data);
    }

    /**
     * 获取用户信息
     */
    private void getUserInfo(String ticket) {
        LogUtil.i(TAG, "getUserInfo() ticket = " + ticket);
        RequestBeanBuilder builder = RequestBeanBuilder.create(true);

        ResponseData data = builder.syncRequest(builder);
        data.path = NetURL.CENTER_USERINFO;
        data.flag = AppConst.CENTER_USERINFO;
        requestID = DataLoader.getInstance().loadData(this, data);
    }

    @Override
    public void preExecute(ResponseData request) {
    }

    @Override
    public void onNotifyMessage(ResponseData request, ResponseData response) {
        if (request.flag == AppConst.CHECK_USER) {
            JSONObject mJson = JSON.parseObject(response.body.toString());
            if (mJson.containsKey("status")) {
                if (mJson.getString("status").equals("1")) {
                    requestLogin();
                } else {
                    removeProgressDialog();
                    CustomToast.show(mJson.getString("message"), CustomToast.LENGTH_SHORT);
                }
            }
        } else if (request.flag == AppConst.LOGIN) {
            JSONObject mJson = JSON.parseObject(response.body.toString());
            if (mJson.containsKey("status")) {
                if (mJson.getString("status").equals("1")) {
                    String ticket = mJson.getString("accessTicket");
                    SharedPreferenceUtil.getInstance().setString(AppConst.ACCESS_TICKET, ticket, true);
                    SessionContext.setTicket(ticket);
                    getUserInfo(ticket);
                } else {
                    removeProgressDialog();
                    CustomToast.show(mJson.getString("message"), CustomToast.LENGTH_SHORT);
                }
            }
        } else if (request.flag == AppConst.CENTER_USERINFO) {
            removeProgressDialog();
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
            if (mCancelLogin != null) {
                mCancelLogin.isCancelLogin(false);
            }

            //登录成功，根据性别设置主题
            int index = SessionContext.mUser.user.sex.equals("1") ? 0 : 1;
            SharedPreferenceUtil.getInstance().setInt(AppConst.SKIN_INDEX, index);
            this.finish();

        } else if (request.flag == AppConst.BIND_CHECK) {// 如果绑定，直接获取用户信息，没有绑定到绑定页面
            removeProgressDialog();
            JSONObject mJson = JSON.parseObject(response.body.toString());
            int flag = mJson.getInteger("flag");
            if (flag == 1) {// flag:0-未绑定 ，1-已绑定，当flag=1时，还返回accessTicket
                String accessTicket = mJson.getString("accessTicket");
                SharedPreferenceUtil.getInstance().setString(AppConst.ACCESS_TICKET, accessTicket, true);// 保存ticket
                SessionContext.setTicket(accessTicket);
                getUserInfo(accessTicket);
            } else {
                Intent intent = new Intent(this, BindPhoneActivity.class);
                intent.putExtra("thirdpartusername", thirdpartusername);
                intent.putExtra("thirdpartuserheadphotourl", thirdpartuserheadphotourl);
                intent.putExtra("openid", openid);
                intent.putExtra("platform", mPlatform);
                intent.putExtra("usertoken", usertoken);
                startActivity(intent);
            }
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        DataLoader.getInstance().clear(requestID);
        removeProgressDialog();
        if (mCancelLogin != null) {
            mCancelLogin.isCancelLogin(true);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (mCancelLogin != null) {
                mCancelLogin.isCancelLogin(true);
            }
            this.finish();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    /**
     * 处理取消登录回调接口
     */
    public interface onCancelLoginListener {
        /**
         * @param isCancel true:取消登录；false:登录成功
         */
        public void isCancelLogin(boolean isCancel);
    }

    /**
     * 设置登录状态监听
     *
     * @param cancelLogin
     */
    public static final void setCancelLogin(onCancelLoginListener cancelLogin) {
        mCancelLogin = cancelLogin;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCancelLogin != null) {
            mCancelLogin = null;
        }

        if (null != mShareAPI) {
            mShareAPI.release();
            mShareAPI = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
    }
}
