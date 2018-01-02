package com.huicheng.hotel.android.ui.custom;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.requestbuilder.RequestBeanBuilder;
import com.huicheng.hotel.android.ui.activity.DebugInfoActivity;
import com.huicheng.hotel.android.ui.activity.UcAboutActivity;
import com.huicheng.hotel.android.ui.activity.UcBountyActivity;
import com.huicheng.hotel.android.ui.activity.UcCouponsActivity;
import com.huicheng.hotel.android.ui.activity.UcFansHotelActivity;
import com.huicheng.hotel.android.ui.activity.UcFeedbackActivity;
import com.huicheng.hotel.android.ui.activity.UcLoginActivity;
import com.huicheng.hotel.android.ui.activity.UcMessagesActivity;
import com.huicheng.hotel.android.ui.activity.UcOrdersActivity;
import com.huicheng.hotel.android.ui.activity.UcPersonalInfoActivity;
import com.huicheng.hotel.android.ui.activity.UcRegisterActivity;
import com.huicheng.hotel.android.ui.activity.UcSettingActivity;
import com.huicheng.hotel.android.ui.activity.hotel.HotelOrdersAssessActivity;
import com.huicheng.hotel.android.ui.dialog.CustomToast;
import com.huicheng.hotel.android.ui.glide.CustomReqURLFormatModelImpl;
import com.prj.sdk.app.AppConst;
import com.prj.sdk.app.NetURL;
import com.prj.sdk.constants.BroadCastConst;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.net.data.ResponseData;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.StringUtil;

import java.net.ConnectException;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * @author kborid
 * @date 2016/7/7
 */
public class LeftDrawerLayout extends RelativeLayout implements View.OnClickListener, DataCallback {

    private static final String TAG = "LeftDrawerLayout";

    private Context context;
    private RelativeLayout unlogin_lay;
    private LinearLayout login_lay;

    private Button btn_login, btn_register;

    private CircleImageView iv_photo;
    private TextView tv_username, tv_userid;
    private TextView tv_usercenter, tv_myorder, tv_msg, tv_lxb, tv_yhq, tv_assess, tv_vip, tv_setting, tv_feedback;
    private TextView tv_msg_count;
    private TextView tv_qmh;
    private TextView tv_usage, tv_private;
    private Button btn_logout;

    private int mMsgId, mBtnLoginId;

    public LeftDrawerLayout(Context context) {
        this(context, null);
    }

    public LeftDrawerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LeftDrawerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;

        TypedArray ta = context.obtainStyledAttributes(R.styleable.MyTheme);
        mMsgId = ta.getResourceId(R.styleable.MyTheme_indicatorBgSel, R.drawable.indicator_btn_sel);
        mBtnLoginId = ta.getResourceId(R.styleable.MyTheme_mainColorBtnSel, R.drawable.user_btn_maincolor_selector);
        ta.recycle();

        init();
    }

    private void init() {
        LayoutInflater.from(context).inflate(R.layout.left_drawer_layout, this);
        findViews();
        setListeners();
        updateUserInfo();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadCastConst.UPDATE_USERINFO);
        context.registerReceiver(receiver, intentFilter);
    }

    private void findViews() {
//        LinearLayout root_lay = (LinearLayout) findViewById(R.id.root_lay);
//        root_lay.getBackground().mutate().setAlpha((int) (0.8 * 255));

        unlogin_lay = (RelativeLayout) findViewById(R.id.unlogin_lay);
        login_lay = (LinearLayout) findViewById(R.id.login_lay);

        btn_login = (Button) findViewById(R.id.btn_login);
        btn_login.setBackgroundResource(mBtnLoginId);
        btn_register = (Button) findViewById(R.id.btn_register);

        iv_photo = (CircleImageView) findViewById(R.id.iv_photo);
        tv_username = (TextView) findViewById(R.id.tv_username);
        tv_userid = (TextView) findViewById(R.id.tv_userid);

        tv_usercenter = (TextView) findViewById(R.id.tv_usercenter);
        tv_myorder = (TextView) findViewById(R.id.tv_myorder);
        tv_msg = (TextView) findViewById(R.id.tv_msg);
        tv_lxb = (TextView) findViewById(R.id.tv_lxb);
        tv_yhq = (TextView) findViewById(R.id.tv_yhq);
        tv_assess = (TextView) findViewById(R.id.tv_assess);
        tv_vip = (TextView) findViewById(R.id.tv_vip);
        tv_setting = (TextView) findViewById(R.id.tv_setting);
        tv_feedback = (TextView) findViewById(R.id.tv_feedback);

        tv_qmh = (TextView) findViewById(R.id.tv_qmh);

        tv_msg_count = (TextView) findViewById(R.id.tv_msg_count);
        tv_msg_count.setBackgroundResource(mMsgId);

        btn_logout = (Button) findViewById(R.id.btn_logout);

        tv_usage = (TextView) findViewById(R.id.tv_usage);
        tv_private = (TextView) findViewById(R.id.tv_private);
    }

    private void setListeners() {
        btn_login.setOnClickListener(this);
        btn_register.setOnClickListener(this);

        tv_usercenter.setOnClickListener(this);
        tv_myorder.setOnClickListener(this);
        tv_msg.setOnClickListener(this);
        tv_lxb.setOnClickListener(this);
        tv_yhq.setOnClickListener(this);
        tv_assess.setOnClickListener(this);
        tv_vip.setOnClickListener(this);
        tv_setting.setOnClickListener(this);
        tv_feedback.setOnClickListener(this);
        tv_qmh.setOnClickListener(this);
        btn_logout.setOnClickListener(this);

        tv_usage.setOnClickListener(this);
        tv_private.setOnClickListener(this);

        if (AppConst.ISDEVELOP) {
            final long[] mPressTime = {0, 0};
            btn_register.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        mPressTime[0] = System.currentTimeMillis();
                    } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        mPressTime[1] = System.currentTimeMillis();
                        if (mPressTime[1] - mPressTime[0] >= 5000) {
                            mPressTime[0] = mPressTime[1];
                            Intent intent = new Intent(context, DebugInfoActivity.class);
                            context.startActivity(intent);
                            return true;
                        }
                    }
                    return false;
                }
            });
        }
    }

    public void updateUserInfo() {
        if (SessionContext.isLogin()) {
            unlogin_lay.setVisibility(GONE);
            login_lay.setVisibility(VISIBLE);
            if (StringUtil.notEmpty(SessionContext.mUser.user.nickname)) {
                tv_username.setText(SessionContext.mUser.user.nickname);
            } else {
                tv_username.setText(SessionContext.mUser.user.username);
            }
            if (StringUtil.notEmpty(SessionContext.mUser.user.mobile)) {
                tv_userid.setVisibility(VISIBLE);
                tv_userid.setText(String.valueOf("帐号：" + SessionContext.mUser.user.mobile));
            } else {
                tv_userid.setVisibility(GONE);
            }

            if (StringUtil.notEmpty(SessionContext.mUser.user.headphotourl)) {
                Glide.with(context)
                        .load(new CustomReqURLFormatModelImpl(SessionContext.mUser.user.headphotourl))
                        .listener(new RequestListener<CustomReqURLFormatModelImpl, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, CustomReqURLFormatModelImpl model, Target<GlideDrawable> target, boolean isFirstResource) {
                                e.printStackTrace();
                                LogUtil.d(TAG, "onException(): model = " + model.requestReqFormatUrl() + ", isFirstResource = " + isFirstResource);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, CustomReqURLFormatModelImpl model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                LogUtil.d(TAG, "onResourceReady(): model = " + model.requestReqFormatUrl() + ", isFirstResource = " + isFirstResource);
                                iv_photo.setImageDrawable(resource);
                                return false;
                            }
                        })
                        .placeholder(R.drawable.def_photo)
                        .crossFade()
                        .centerCrop()
                        .override(150, 150)
                        .into(iv_photo);
            }
        } else {
            unlogin_lay.setVisibility(VISIBLE);
            login_lay.setVisibility(GONE);
            iv_photo.setImageResource(R.drawable.def_photo);
        }
    }

    public boolean updateMsgCount(String count) {
        if (!"0".equals(count)) {
            tv_msg_count.setVisibility(VISIBLE);
            tv_msg_count.setText(String.valueOf(count));
            return true;
        } else {
            tv_msg_count.setVisibility(GONE);
            return false;
        }
    }

    @Override
    public void onClick(View v) {
        Intent doActionIntent = null;
        switch (v.getId()) {
            case R.id.btn_login:
                doActionIntent = new Intent(context, UcLoginActivity.class);
                doActionIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                break;
            case R.id.btn_register:
                doActionIntent = new Intent(context, UcRegisterActivity.class);
                doActionIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                break;
            case R.id.tv_usercenter:
                doActionIntent = new Intent(context, UcPersonalInfoActivity.class);
                break;
            case R.id.tv_myorder:
                doActionIntent = new Intent(context, UcOrdersActivity.class);
                break;
            case R.id.tv_msg:
                doActionIntent = new Intent(context, UcMessagesActivity.class);
                break;
            case R.id.tv_lxb:
                doActionIntent = new Intent(context, UcBountyActivity.class);
                break;
            case R.id.tv_yhq:
                doActionIntent = new Intent(context, UcCouponsActivity.class);
                break;
            case R.id.tv_assess:
                doActionIntent = new Intent(context, HotelOrdersAssessActivity.class);
                break;
            case R.id.tv_vip:
                doActionIntent = new Intent(context, UcFansHotelActivity.class);
                break;
            case R.id.tv_setting:
                doActionIntent = new Intent(context, UcSettingActivity.class);
                break;
            case R.id.tv_feedback:
                doActionIntent = new Intent(context, UcFeedbackActivity.class);
                break;
            case R.id.tv_qmh:
                if (null != listener) {
                    listener.doQmhAction();
                }
                break;
            case R.id.btn_logout:
                clearTicket();
                break;
            case R.id.tv_usage:
                doActionIntent = new Intent(context, UcAboutActivity.class);
                doActionIntent.putExtra("title", getResources().getString(R.string.setting_usage_condition));
                doActionIntent.putExtra("index", UcAboutActivity.WORK_CONDITION);
                break;
            case R.id.tv_private:
                doActionIntent = new Intent(context, UcAboutActivity.class);
                doActionIntent.putExtra("title", getResources().getString(R.string.setting_private));
                doActionIntent.putExtra("index", UcAboutActivity.STATEMENT);
                break;
            default:
                break;
        }

        if (null != doActionIntent) {
            context.startActivity(doActionIntent);
        }
    }

    /**
     * 注销票据
     */
    private void clearTicket() {
        RequestBeanBuilder builder = RequestBeanBuilder.create(true);
        ResponseData data = builder.syncRequest(builder);
        data.path = NetURL.REMOVE_TICKET;
        data.flag = AppConst.REMOVE_TICKET;
        if (null != listener) {
            listener.showProgressDialog(context);
        }
        DataLoader.getInstance().loadData(this, data);
    }

    private void logout() {
        SessionContext.cleanUserInfo();
    }

    private void closeDrawer() {
        if (null != listener) {
            listener.closeDrawer();
        }
    }

    @Override
    public void preExecute(ResponseData request) {

    }

    @Override
    public void notifyMessage(ResponseData request, ResponseData response) throws Exception {
        if (request.flag == AppConst.REMOVE_TICKET) {
            if (null != listener) {
                listener.removeProgressDialog();
            }
            logout();
            context.sendBroadcast(new Intent(BroadCastConst.UPDATE_USERINFO));
        }
    }

    @Override
    public void notifyError(ResponseData request, ResponseData response, Exception e) {
        if (null != listener) {
            listener.removeProgressDialog();
        }
        String message;
        if (e != null && e instanceof ConnectException) {
            message = context.getResources().getString(R.string.dialog_tip_net_error);
        } else {
            message = response != null && response.data != null ? response.data.toString() : context.getResources().getString(R.string.dialog_tip_null_error);
        }
        CustomToast.show(message, CustomToast.LENGTH_SHORT);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals(BroadCastConst.UPDATE_USERINFO)) {
                updateUserInfo();
            }
        }
    };

    public void unregisterBroadReceiver() {
        if (receiver != null) {
            context.unregisterReceiver(receiver);
        }
    }

    public interface OnLeftDrawerListener {
        void closeDrawer();

        void showProgressDialog(Context context);

        void removeProgressDialog();

        void doQmhAction();
    }

    private OnLeftDrawerListener listener = null;

    public void setOnLeftDrawerListener(OnLeftDrawerListener listener) {
        this.listener = listener;
    }
}
