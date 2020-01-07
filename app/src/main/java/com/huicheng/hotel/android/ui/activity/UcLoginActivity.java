package com.huicheng.hotel.android.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AnticipateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huicheng.hotel.android.PRJApplication;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.content.AppConst;
import com.huicheng.hotel.android.content.NetURL;
import com.huicheng.hotel.android.pay.wxpay.MD5;
import com.huicheng.hotel.android.requestbuilder.RequestBeanBuilder;
import com.huicheng.hotel.android.requestbuilder.bean.UserInfo;
import com.huicheng.hotel.android.ui.base.BaseAppActivity;
import com.huicheng.hotel.android.ui.dialog.CustomToast;
import com.prj.sdk.constants.BroadCastConst;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.net.data.ResponseData;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;
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

public class UcLoginActivity extends BaseAppActivity {

    private EditText et_phone, et_pwd;
    private CheckBox cb_pwd_status_check;
    private LinearLayout pwd_lay;
    private TextView tv_register, tv_forget;
    private LinearLayout btn_layout;
    private TextView tv_action;

    private String mPlatform; //（01-新浪微博，02-腾讯QQ，03-微信，04-支付宝）
    private String thirdpartusername, thirdpartuserheadphotourl, openid, unionid;
    private String usertoken;

    private static final int ANIMATOR_OPENED = 1;
    private static final int ANIMATOR_PLAYING = 0;
    private static final int ANIMATOR_CLOSED = -1;
    private boolean isFirst = false;
    private int mAnimStatus = ANIMATOR_CLOSED;
    private LinearLayout third_login_layout, third_btn_lay;
    private int mThirdBtnLayoutHeight = 0;
    private RelativeLayout arrow_lay;
    private ImageView iv_arrow;
    private TextView tv_third_label;
    private TextView tv_wx, tv_qq;

    @Override
    protected void preOnCreate() {
        super.preOnCreate();
        initMainWindow();
        overridePendingTransition(R.anim.user_login_enter_in, R.anim.user_login_enter_out);
    }

    @Override
    protected void setContentView() {
        setContentView(R.layout.act_uc_login);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        overridePendingTransition(R.anim.user_login_enter_in, R.anim.user_login_enter_out);
    }

    @Override
    public void initViews() {
        super.initViews();
        et_phone = (EditText) findViewById(R.id.et_phone);
        et_pwd = (EditText) findViewById(R.id.et_pwd);
        cb_pwd_status_check = (CheckBox) findViewById(R.id.cb_pwd_status_check);
        pwd_lay = (LinearLayout) findViewById(R.id.pwd_lay);
        tv_register = (TextView) findViewById(R.id.tv_register);
        tv_forget = (TextView) findViewById(R.id.tv_forget);
        btn_layout = (LinearLayout) findViewById(R.id.btn_layout);
        tv_action = (TextView) findViewById(R.id.tv_action);

        third_login_layout = (LinearLayout) findViewById(R.id.third_login_layout);
        third_btn_lay = (LinearLayout) findViewById(R.id.third_btn_lay);
        arrow_lay = (RelativeLayout) findViewById(R.id.arrow_lay);
        iv_arrow = (ImageView) findViewById(R.id.iv_arrow);
        tv_third_label = (TextView) findViewById(R.id.tv_third_label);
        tv_qq = (TextView) findViewById(R.id.tv_qq);
        tv_wx = (TextView) findViewById(R.id.tv_wx);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        LogUtil.i(TAG, "onWindowFocusChanged()");
        if (!isFirst) {
            isFirst = true;
            mThirdBtnLayoutHeight = third_btn_lay.getHeight();
//            third_btn_lay.getLayoutParams().height = 0;
            ((LinearLayout.LayoutParams) third_btn_lay.getLayoutParams()).bottomMargin = -mThirdBtnLayoutHeight;
            third_btn_lay.requestLayout();
        }
    }

    private void openThirdLayoutAnim() {
        ObjectAnimator arrowRotation = ObjectAnimator.ofFloat(iv_arrow, "rotation", 180, 0);
        ObjectAnimator actionBtnTranslation = ObjectAnimator.ofFloat(btn_layout, "translationY", 0, -mThirdBtnLayoutHeight / 2);
        ValueAnimator thirdBtnHeight = ValueAnimator.ofFloat(0, mThirdBtnLayoutHeight);
        thirdBtnHeight.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //获取当前的height值，动态更新view的高度
                int value = ((Float) animation.getAnimatedValue()).intValue();
                tv_third_label.setPadding(0, Utils.dp2px(13), 0, Utils.dp2px(17) - (int) ((float) Math.abs(value) / mThirdBtnLayoutHeight * Utils.dp2px(10)));
                if (value <= 0) {
                    value = 0;
                }
//                third_btn_lay.getLayoutParams().height = value <= 0 ? 0 : value;
                ((LinearLayout.LayoutParams) third_btn_lay.getLayoutParams()).bottomMargin = value - mThirdBtnLayoutHeight;
                third_btn_lay.requestLayout();
            }
        });

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(800);
        animatorSet.setInterpolator(new AnticipateInterpolator());
        animatorSet.play(arrowRotation).with(thirdBtnHeight).with(actionBtnTranslation);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mAnimStatus = ANIMATOR_PLAYING;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimStatus = ANIMATOR_OPENED;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        animatorSet.start();
    }

    private void closeThirdLayoutAnim() {
        ObjectAnimator arrowRotation = ObjectAnimator.ofFloat(iv_arrow, "rotation", 0, 180);
        ObjectAnimator actionBtnTranslation = ObjectAnimator.ofFloat(btn_layout, "translationY", -mThirdBtnLayoutHeight / 2, 0);
        ValueAnimator thirdBtnHeight = ValueAnimator.ofFloat(mThirdBtnLayoutHeight, 0);
        thirdBtnHeight.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //获取当前的height值，动态更新view的高度
                int value = ((Float) animation.getAnimatedValue()).intValue();
//                third_btn_lay.getLayoutParams().height = Math.abs(value);
                ((LinearLayout.LayoutParams) third_btn_lay.getLayoutParams()).bottomMargin = value - mThirdBtnLayoutHeight;
                third_btn_lay.requestLayout();
                tv_third_label.setPadding(0, Utils.dp2px(13), 0, Utils.dp2px(7) + (int) ((1 - (float) Math.abs(value) / mThirdBtnLayoutHeight) * Utils.dp2px(10)));
            }
        });

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(800);
        animatorSet.setInterpolator(new AnticipateInterpolator());
        animatorSet.play(arrowRotation).with(thirdBtnHeight).with(actionBtnTranslation);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mAnimStatus = ANIMATOR_PLAYING;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimStatus = ANIMATOR_CLOSED;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        animatorSet.start();
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
        checkInputForActionBtnStatus();
    }

    private void checkInputForActionBtnStatus() {
        boolean flag = false;
        if (StringUtil.notEmpty(et_phone.getText().toString())
                && StringUtil.notEmpty(et_pwd.getText().toString())) {
            flag = true;
        }
        tv_action.setEnabled(flag);
    }

    @Override
    public void initListeners() {
        super.initListeners();
        tv_action.setOnClickListener(this);
        tv_register.setOnClickListener(this);
        tv_forget.setOnClickListener(this);
        tv_wx.setOnClickListener(this);
        tv_qq.setOnClickListener(this);

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
        arrow_lay.setOnClickListener(this);

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
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.arrow_lay:
                if (mAnimStatus == ANIMATOR_PLAYING) {
                    return;
                }
                if (mAnimStatus == ANIMATOR_CLOSED) {
                    openThirdLayoutAnim();
                } else {
                    closeThirdLayoutAnim();
                }
                break;
            case R.id.tv_action:
                requestCheckUserStatus();
                break;
            case R.id.tv_register: {
                Intent intent = new Intent();
                intent.setClass(this, UcRegisterActivity.class);
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
            case R.id.tv_forget: {
                Intent intent = new Intent();
                intent.setClass(this, UcForgetPwdActivity.class);
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
            case R.id.tv_qq:
                login(SHARE_MEDIA.QQ);
                break;
            case R.id.tv_wx:
                login(SHARE_MEDIA.WEIXIN);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SessionContext.isLogin()) {
            finish();
        }
    }

    /**
     * 授权。如果授权成功，则获取用户信息</br>
     */
    private void login(final SHARE_MEDIA platform) {
    }

    /**
     * 登录前检查用户状态
     */
    private void requestCheckUserStatus() {
        RequestBeanBuilder b = RequestBeanBuilder.create(false);
        b.addBody("login", et_phone.getText().toString());
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
        RequestBeanBuilder b = RequestBeanBuilder.create(false);
        b.addBody("login", et_phone.getText().toString());
        b.addBody("password", MD5.getMessageDigest(et_pwd.getText().toString().getBytes()));
        ResponseData d = b.syncRequest(b);
        d.path = NetURL.LOGIN;
        d.flag = AppConst.LOGIN;

        if (!isProgressShowing()) {
            showProgressDialog(this);
        }
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
    private void requestUserInfo(String ticket) {
        LogUtil.i(TAG, "requestUserInfo() ticket = " + ticket);
        RequestBeanBuilder builder = RequestBeanBuilder.create(true);
        ResponseData data = builder.syncRequest(builder);
        data.path = NetURL.CENTER_USERINFO;
        data.flag = AppConst.CENTER_USERINFO;

        if (!isProgressShowing()) {
            showProgressDialog(this);
        }
        requestID = DataLoader.getInstance().loadData(this, data);
    }

    @Override
    public void onNotifyMessage(ResponseData request, ResponseData response) {
        if (response != null && response.body != null) {
            if (request.flag == AppConst.CHECK_USER) {
                LogUtil.i(TAG, "json = " + response.body.toString());
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
                LogUtil.i(TAG, "json = " + response.body.toString());
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
                LogUtil.i(TAG, "json = " + response.body.toString());
                if ("{}".equals(response.body.toString())) {
                    CustomToast.show("获取用户信息失败，请重试", 0);
                    return;
                }
                SessionContext.mUser = JSON.parseObject(response.body.toString(), UserInfo.class);

                //缓存用户信息
                SharedPreferenceUtil.getInstance().setString(AppConst.USERNAME, et_phone.getText().toString(), true);// 保存用户名
                SharedPreferenceUtil.getInstance().setString(AppConst.LAST_LOGIN_DATE, DateUtil.getCurDateStr(null), false);// 保存登录时间
                SharedPreferenceUtil.getInstance().setString(AppConst.USER_PHOTO_URL, SessionContext.mUser != null ? SessionContext.mUser.user.headphotourl : "", false);
                SharedPreferenceUtil.getInstance().setString(AppConst.USER_INFO, response.body.toString(), true);
                JPushInterface.setAliasAndTags(PRJApplication.getInstance(), SessionContext.mUser.user.mobile, null, new TagAliasCallback() {
                    @Override
                    public void gotResult(int i, String s, Set<String> set) {
                        LogUtil.i(TAG, ((i == 0) ? "设置成功" : "设置失败") + ", Alias = " + s + ", Tag = " + set);
                    }
                });
                removeProgressDialog();
                CustomToast.show("登录成功", CustomToast.LENGTH_LONG);
                sendBroadcast(new Intent(BroadCastConst.UPDATE_USERINFO));

                //登录成功，根据性别设置主题
                int index = SessionContext.mUser.user.sex.equals("1") ? 0 : 1;
                SharedPreferenceUtil.getInstance().setInt(AppConst.SKIN_INDEX, index);
                finish();
            } else if (request.flag == AppConst.BIND_CHECK) {// 如果绑定，直接获取用户信息，没有绑定到绑定页面
                removeProgressDialog();
                LogUtil.i(TAG, "json = " + response.body.toString());
                JSONObject mJson = JSON.parseObject(response.body.toString());
                int flag = mJson.getInteger("flag");
                if (flag == 1) {// flag:0-未绑定 ，1-已绑定，当flag=1时，还返回accessTicket
                    String accessTicket = mJson.getString("accessTicket");
                    SharedPreferenceUtil.getInstance().setString(AppConst.ACCESS_TICKET, accessTicket, true);// 保存ticket
                    SessionContext.setTicket(accessTicket);
                    requestUserInfo(accessTicket);
                } else {
                    Intent intent = new Intent(this, UcBindPhoneActivity.class);
                    intent.putExtra("thirdpartusername", thirdpartusername);
                    intent.putExtra("thirdpartuserheadphotourl", thirdpartuserheadphotourl);
                    intent.putExtra("openid", openid);
                    intent.putExtra("platform", mPlatform);
                    intent.putExtra("usertoken", usertoken);
                    startActivity(intent);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                    startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this,
//                            new Pair<View, String>(et_phone, "share_phone"),
//                            new Pair<View, String>(tv_action, "share_action")).toBundle());
//                } else {
//                        startActivity(intent);
//                    overridePendingTransition(0, 0);
//                }
                }
            }
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.user_login_exit_in, R.anim.user_login_exit_out);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
    }
}
