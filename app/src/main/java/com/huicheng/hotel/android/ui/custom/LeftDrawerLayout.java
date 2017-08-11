package com.huicheng.hotel.android.ui.custom;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.NetURL;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.net.RequestBeanBuilder;
import com.huicheng.hotel.android.ui.activity.AboutActivity;
import com.huicheng.hotel.android.ui.activity.AssessOrdersActivity;
import com.huicheng.hotel.android.ui.activity.FansHotelActivity;
import com.huicheng.hotel.android.ui.activity.FeedbackActivity;
import com.huicheng.hotel.android.ui.activity.MessageListActivity;
import com.huicheng.hotel.android.ui.activity.MyDiscountCouponActivity;
import com.huicheng.hotel.android.ui.activity.MyOrdersActivity;
import com.huicheng.hotel.android.ui.activity.SettingActivity;
import com.huicheng.hotel.android.ui.activity.UserCenterActivity;
import com.huicheng.hotel.android.ui.dialog.ProgressDialog;
import com.prj.sdk.app.AppContext;
import com.prj.sdk.constants.BroadCastConst;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.net.image.ImageLoader;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.widget.CustomToast;

import java.net.ConnectException;

/**
 * @author kborid
 * @date 2016/7/7
 */
public class LeftDrawerLayout extends RelativeLayout implements View.OnClickListener, DataCallback {
    private Context context;
    private ProgressDialog mProgressDialog;
    private RelativeLayout unlogin_lay;
    private LinearLayout login_lay;

    private Button btn_login;
    private Button btn_cancel;

    private CircleImageViewWithBound iv_photo;
    private TextView tv_username, tv_userid;
    private TextView tv_usercenter, tv_myorder, tv_msg, tv_yhq, tv_assess, tv_vip, tv_setting, tv_feedback;
    private TextView tv_msg_count;
    private TextView tv_usage, tv_private;
    private Button btn_logout;

    private Intent doActionIntent = null;

    public LeftDrawerLayout(Context context) {
        this(context, null);
    }

    public LeftDrawerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LeftDrawerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
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
        btn_cancel = (Button) findViewById(R.id.btn_cancel);

        iv_photo = (CircleImageViewWithBound) findViewById(R.id.iv_photo);
        tv_username = (TextView) findViewById(R.id.tv_username);
        tv_userid = (TextView) findViewById(R.id.tv_userid);

        tv_usercenter = (TextView) findViewById(R.id.tv_usercenter);
        tv_myorder = (TextView) findViewById(R.id.tv_myorder);
        tv_msg = (TextView) findViewById(R.id.tv_msg);
        tv_yhq = (TextView) findViewById(R.id.tv_yhq);
        tv_assess = (TextView) findViewById(R.id.tv_assess);
        tv_vip = (TextView) findViewById(R.id.tv_vip);
        tv_setting = (TextView) findViewById(R.id.tv_setting);
        tv_feedback = (TextView) findViewById(R.id.tv_feedback);

        tv_msg_count = (TextView) findViewById(R.id.tv_msg_count);

        btn_logout = (Button) findViewById(R.id.btn_logout);

        tv_usage = (TextView) findViewById(R.id.tv_usage);
        tv_private = (TextView) findViewById(R.id.tv_private);
    }

    private void setListeners() {
        btn_login.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);

        tv_usercenter.setOnClickListener(this);
        tv_myorder.setOnClickListener(this);
        tv_msg.setOnClickListener(this);
        tv_yhq.setOnClickListener(this);
        tv_assess.setOnClickListener(this);
        tv_vip.setOnClickListener(this);
        tv_setting.setOnClickListener(this);
        tv_feedback.setOnClickListener(this);
        btn_logout.setOnClickListener(this);

        tv_usage.setOnClickListener(this);
        tv_private.setOnClickListener(this);
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
            final String url = SessionContext.mUser.user.headphotourl;
            iv_photo.setImageResource(R.drawable.def_photo);
            AppContext.mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (!StringUtil.isEmpty(url)) {
                        ImageLoader.getInstance().loadBitmap(new ImageLoader.ImageCallback() {
                            @Override
                            public void imageCallback(Bitmap bm, String url, String imageTag) {
                                if (null != bm) {
                                    iv_photo.setImageBitmap(bm);
                                }
                            }
                        }, url);
                    }
                }
            });
        } else {
            unlogin_lay.setVisibility(VISIBLE);
            login_lay.setVisibility(GONE);
            iv_photo.setImageResource(R.drawable.def_photo);
        }
    }

    public void updateMsgCount(String count) {
        if (!"0".equals(count)) {
            tv_msg_count.setVisibility(VISIBLE);
            tv_msg_count.setText(String.valueOf(count));
        } else {
            tv_msg_count.setVisibility(GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                context.sendBroadcast(new Intent(BroadCastConst.UNLOGIN_ACTION));
                break;
            case R.id.btn_cancel:
                doActionIntent = null;
                closeDrawer();
                break;
            case R.id.tv_usercenter:
                doActionIntent = new Intent(context, UserCenterActivity.class);
                break;
            case R.id.tv_myorder:
                doActionIntent = new Intent(context, MyOrdersActivity.class);
                break;
            case R.id.tv_msg:
                doActionIntent = new Intent(context, MessageListActivity.class);
                break;
            case R.id.tv_yhq:
                doActionIntent = new Intent(context, MyDiscountCouponActivity.class);
                break;
            case R.id.tv_assess:
                doActionIntent = new Intent(context, AssessOrdersActivity.class);
                break;
            case R.id.tv_vip:
                doActionIntent = new Intent(context, FansHotelActivity.class);
                break;
            case R.id.tv_setting:
                doActionIntent = new Intent(context, SettingActivity.class);
                break;
            case R.id.tv_feedback:
                doActionIntent = new Intent(context, FeedbackActivity.class);
                break;
            case R.id.btn_logout:
                clearTicket();
                break;
            case R.id.tv_usage:
                doActionIntent = new Intent(context, AboutActivity.class);
                doActionIntent.putExtra("title", getResources().getString(R.string.setting_usage_condition));
                doActionIntent.putExtra("index", AboutActivity.WORK_CONDITION);
                break;
            case R.id.tv_private:
                doActionIntent = new Intent(context, AboutActivity.class);
                doActionIntent.putExtra("title", getResources().getString(R.string.setting_private));
                doActionIntent.putExtra("index", AboutActivity.STATEMENT);
                break;
            default:
                break;
        }

        if (null != doActionIntent) {
            closeDrawer();
        }
    }

    public void doActionIntent() {
        if (doActionIntent != null) {
//            closeDrawer();
            context.startActivity(doActionIntent);
        }
        doActionIntent = null;
    }

    /**
     * 注销票据
     */
    private void clearTicket() {
        RequestBeanBuilder builder = RequestBeanBuilder.create(true);
        ResponseData data = builder.syncRequest(builder);
        data.path = NetURL.REMOVE_TICKET;
        data.flag = AppConst.REMOVE_TICKET;
        showProgressDialog();
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
            if (mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            logout();
            context.sendBroadcast(new Intent(BroadCastConst.UPDATE_USERINFO));
//            if (listener != null) {
//                listener.onReCreate();
//            }
        }
    }

    @Override
    public void notifyError(ResponseData request, ResponseData response, Exception e) {
        mProgressDialog.dismiss();
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

        void onReCreate();
    }

    private OnLeftDrawerListener listener = null;

    public void setOnLeftDrawerListener(OnLeftDrawerListener listener) {
        this.listener = listener;
    }

    private void showProgressDialog() {
        if (null == mProgressDialog) {
            mProgressDialog = new ProgressDialog(context);
        }
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }
}
