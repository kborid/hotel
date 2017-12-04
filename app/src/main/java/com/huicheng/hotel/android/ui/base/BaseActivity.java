package com.huicheng.hotel.android.ui.base;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.huicheng.hotel.android.PRJApplication;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.HotelCommDef;
import com.huicheng.hotel.android.common.HotelOrderManager;
import com.huicheng.hotel.android.common.NetURL;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.net.RequestBeanBuilder;
import com.huicheng.hotel.android.net.bean.HotelDetailInfoBean;
import com.huicheng.hotel.android.tools.FixIMMLeaksTools;
import com.huicheng.hotel.android.ui.activity.InvoiceDetailActivity;
import com.huicheng.hotel.android.ui.activity.OrderPayActivity;
import com.huicheng.hotel.android.ui.activity.OrderPaySuccessActivity;
import com.huicheng.hotel.android.ui.activity.UserCenterActivity;
import com.huicheng.hotel.android.ui.activity.WelcomeActivity;
import com.huicheng.hotel.android.ui.dialog.CustomDialog;
import com.huicheng.hotel.android.ui.dialog.CustomToast;
import com.huicheng.hotel.android.ui.dialog.ProgressDialog;
import com.huicheng.hotel.android.ui.glide.CustomReqURLFormatModelImpl;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.ActivityTack;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;
import com.squareup.leakcanary.RefWatcher;
import com.umeng.analytics.MobclickAgent;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BaseActivity extends AppCompatActivity implements OnClickListener, DataCallback {

    protected final String TAG = getClass().getSimpleName();

    private CustomDialog mDialogVip;
    protected static boolean isHotelVipRefresh = false;

    private ProgressDialog mProgressDialog;
    protected TextView tv_center_title, tv_center_summary;
    protected ImageView btn_back, iv_back, btn_right;
    protected static String requestID;

    protected static boolean isReStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.d(TAG, "onCreate()");
//        if (SharedPreferenceUtil.getInstance().getInt(AppConst.SKIN_INDEX, 0) == 1) {
//            setTheme(R.style.AppTheme_femaleTheme);
//        } else {
        setTheme(R.style.AppTheme_defaultTheme);
//        }

        if (null != savedInstanceState && !isReStarted) {
            if (!SessionContext.isLogin()) {
                SessionContext.initUserInfo();
            }
            Intent intent = new Intent(this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.alpha_fade_in, R.anim.alpha_fade_out);
        } else {
            isReStarted = false;
            ActivityTack.getInstanse().addActivity(this);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void initLaunchWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            int option =
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                    | View.SYSTEM_UI_FLAG_IMMERSIVE
//                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    ;
            window.getDecorView().setSystemUiVisibility(option);
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(getResources().getColor(R.color.transparent20));
//            window.setNavigationBarColor(getResources().getColor(R.color.transparent));
        } else {
            Window win = getWindow();
            WindowManager.LayoutParams winParams = win.getAttributes();
            winParams.flags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            win.setAttributes(winParams);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void initMainWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            int option =
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            window.getDecorView().setSystemUiVisibility(option);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.transparent20));
//            window.setNavigationBarColor(getResources().getColor(R.color.transparent));
        } else {
            Window win = getWindow();
            WindowManager.LayoutParams winParams = win.getAttributes();
            winParams.flags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            win.setAttributes(winParams);
        }
    }

    // 初始化组件
    public void initViews() {
        tv_center_title = (TextView) findViewById(R.id.tv_center_title);
        if (null != tv_center_title) {
            tv_center_title.setTypeface(Typeface.DEFAULT_BOLD);
        }
        tv_center_summary = (TextView) findViewById(R.id.tv_center_summary);
        iv_back = (ImageView) findViewById(R.id.iv_back);
        btn_back = (ImageView) findViewById(R.id.btn_back);
        btn_right = (ImageView) findViewById(R.id.btn_right);
    }

    public void dealIntent() {
    }

    public void initParams() {
        dealIntent();
    }

    //设置title的描述
    public void setTitleSummaryString(String summary) {
        if (null != tv_center_summary) {
            if (StringUtil.notEmpty(summary)) {
                tv_center_summary.setText(summary);
                tv_center_summary.setVisibility(View.VISIBLE);
            } else {
                tv_center_summary.setVisibility(View.GONE);
            }
        }
    }

    // 监听设置
    public void initListeners() {
        if (btn_back != null) {
            btn_back.setOnClickListener(this);
        }
        if (iv_back != null) {
            iv_back.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
            case R.id.btn_back:
                if (getClass().equals(InvoiceDetailActivity.class)
                        || getClass().equals(OrderPayActivity.class)
                        || getClass().equals(OrderPaySuccessActivity.class)
                        || getClass().equals(UserCenterActivity.class)) {
                    //do nothing
                    LogUtil.i(TAG, "do nothing~~~");
                } else {
                    finish();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.d(TAG, "onResume()");
        MobclickAgent.onPageStart(this.getClass().getName()); //统计页面(仅有Activity的应用中SDK自动调用，不需要单独写。"SplashScreen"为页面名称，可自定义)
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtil.d(TAG, "onPause()");
        MobclickAgent.onPageEnd(this.getClass().getName());
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.d(TAG, "onDestroy()");
        FixIMMLeaksTools.fixFocusedViewLeak(PRJApplication.getInstance());
        RefWatcher refWatcher = PRJApplication.getRefWatcher(this);
        refWatcher.watch(this);
//        DataLoader.getInstance().clearRequests();
        ActivityTack.getInstanse().removeActivity(this);
    }

    /**
     * 显示loading对话框
     */
    public final void showProgressDialog(Context context) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(context);
        }
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }


    public final boolean isProgressShowing() {
        return mProgressDialog != null && mProgressDialog.isShowing();
    }

    public final void removeProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    /**
     * 隐藏虚拟按键，并且全屏
     */
    protected void hideBottomAndStatusBar() {
        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    protected void showBottomAndStatusBar() {
        if (Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.VISIBLE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(0);
        }
    }

    protected void showAddVipDialog(final Context context, HotelDetailInfoBean hotelDetailInfoBean) {
        if (mDialogVip != null && mDialogVip.isShowing()) {
            return;
        }
        mDialogVip = new CustomDialog(context);
        mDialogVip.findViewById(R.id.content_layout).setPadding(0, 0, 0, 0);
        final View view = LayoutInflater.from(this).inflate(R.layout.dialog_vip_layout, null);
        final LinearLayout replace_lay = (LinearLayout) view.findViewById(R.id.replace_lay);
        final ImageView iv_close = (ImageView) view.findViewById(R.id.iv_close);
        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialogVip.dismiss();
            }
        });

        final LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(Utils.dip2px(300), ViewGroup.LayoutParams.WRAP_CONTENT);
        //VIP Home Layout
        final View viewHome = LayoutInflater.from(context).inflate(R.layout.dialog_vip_home_layout, null);
        //VIP Detail Layout
        final View viewDetail = LayoutInflater.from(context).inflate(R.layout.dialog_vip_detail_layout, null);

        {
            //VIP Home Layout
            ImageView iv_hotel_bg = (ImageView) viewHome.findViewById(R.id.iv_hotel_bg);
            Glide.with(context)
                    .load(new CustomReqURLFormatModelImpl(hotelDetailInfoBean.picPath.get(0)))
                    .placeholder(R.drawable.def_hotel_banner)
                    .crossFade()
                    .centerCrop()
                    .override(700, 400)
                    .into(iv_hotel_bg);

            TextView tv_vip_name = (TextView) viewHome.findViewById(R.id.tv_vip_name);
            tv_vip_name.getPaint().setFakeBoldText(true);
            tv_vip_name.setText(hotelDetailInfoBean.name);
            ((TextView) viewHome.findViewById(R.id.tv_vip_tips)).getPaint().setFakeBoldText(true);
            Button btn_join = (Button) viewHome.findViewById(R.id.btn_join);
            btn_join.getPaint().setFakeBoldText(true);
            btn_join.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    replace_lay.removeAllViews();
                    replace_lay.addView(viewDetail, llp);
                }
            });

            //VIP Detail Layout
            final EditText et_username = (EditText) viewDetail.findViewById(R.id.et_username);
            final EditText et_id = (EditText) viewDetail.findViewById(R.id.et_id);
            final EditText et_email = (EditText) viewDetail.findViewById(R.id.et_email);
            final TagFlowLayout tagFlowLayout = (TagFlowLayout) viewDetail.findViewById(R.id.flowlayout);

            et_username.setText(SharedPreferenceUtil.getInstance().getString(AppConst.FANS_NAME, "", true));
            et_id.setText(SharedPreferenceUtil.getInstance().getString(AppConst.FANS_ID, "", true));
            et_email.setText(SharedPreferenceUtil.getInstance().getString(AppConst.FANS_EMAIL, "", true));

            final int[] selectedIndex = {0};
            ArrayList<String> tagList = new ArrayList<>();
            tagList.addAll(Arrays.asList(getResources().getStringArray(R.array.TravelType)));
            tagFlowLayout.setAdapter(new TagAdapter<String>(tagList) {
                @Override
                public View getView(FlowLayout parent, int position, String o) {
                    TextView tv_tag = (TextView) LayoutInflater.from(context).inflate(R.layout.dialog_vip_tag_item, tagFlowLayout, false);
                    tv_tag.setText(o);
                    return tv_tag;
                }
            });
            tagFlowLayout.setMaxSelectCount(1);
            tagFlowLayout.setOnSelectListener(new TagFlowLayout.OnSelectListener() {
                @Override
                public void onSelected(Set<Integer> selectPosSet) {
                    if (selectPosSet != null && selectPosSet.iterator() != null && selectPosSet.iterator().hasNext()) {
                        selectedIndex[0] = selectPosSet.iterator().next();
                    } else {
                        selectedIndex[0] = -1;
                    }
                }
            });
            Set<Integer> tagSet = new HashSet<>();
            tagSet.add(selectedIndex[0]);
            tagFlowLayout.getAdapter().setSelectedList(tagSet);
            Button btn_ok = (Button) viewDetail.findViewById(R.id.btn_ok);
            btn_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (StringUtil.isEmpty(et_username.getText().toString())) {
                        CustomToast.show("请输入名字", CustomToast.LENGTH_SHORT);
                        return;
                    }
                    if (StringUtil.isEmpty(et_id.getText().toString())) {
                        CustomToast.show("请输入身份证号", CustomToast.LENGTH_SHORT);
                        return;
                    }
                    if (StringUtil.isEmpty(et_email.getText().toString())) {
                        CustomToast.show("请输入邮箱地址", CustomToast.LENGTH_SHORT);
                        return;
                    } else if (!Utils.isEmail(et_email.getText().toString())) {
                        CustomToast.show("邮箱地址格式错误", CustomToast.LENGTH_SHORT);
                        return;
                    }
                    if (-1 == selectedIndex[0]) {
                        CustomToast.show("请选择出行类型", CustomToast.LENGTH_SHORT);
                        return;
                    }

                    //缓存粉丝信息
                    SharedPreferenceUtil.getInstance().setString(AppConst.FANS_NAME, et_username.getText().toString(), true);
                    SharedPreferenceUtil.getInstance().setString(AppConst.FANS_ID, et_id.getText().toString(), true);
                    SharedPreferenceUtil.getInstance().setString(AppConst.FANS_EMAIL, et_email.getText().toString(), true);

                    requestHotelVip2(et_email.getText().toString(),
                            et_id.getText().toString(),
                            et_username.getText().toString(),
                            HotelCommDef.convertTravelType(selectedIndex[0]));
                }
            });
        }


        replace_lay.removeAllViews();
        replace_lay.addView(viewHome, llp);
        mDialogVip.addView(view);
        mDialogVip.setCanceledOnTouchOutside(true);
        mDialogVip.show();
    }

    protected LayoutAnimationController getAnimationController() {
        LayoutAnimationController controller;
        Animation alphaAnim = new AlphaAnimation(0f, 1f);
//        Animation anim = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f,
//                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
//                0.5f);// 从0.5倍放大到1倍
        alphaAnim.setDuration(500);
        controller = new LayoutAnimationController(alphaAnim, 0.1f);
        controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
        return controller;
    }

    private void requestHotelVip2(String email, String idcode, String realname, String traveltype) {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);

        b.addBody("email", email);
        b.addBody("hotelId", String.valueOf(HotelOrderManager.getInstance().getHotelDetailInfo().id));
        b.addBody("idcode", idcode);
        b.addBody("realname", realname);
        b.addBody("traveltype", traveltype);

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.HOTEL_VIP;
        d.flag = AppConst.HOTEL_VIP;

        if (!isProgressShowing()) {
            showProgressDialog(this);
        }

        requestID = DataLoader.getInstance().loadData(this, d);
    }

    public void refreshScreenInfoVipPrice() {
    }

    @Override
    public void preExecute(ResponseData request) {
    }

    @Override
    public void notifyMessage(ResponseData request, ResponseData response) throws Exception {
        if (request.flag == AppConst.HOTEL_VIP) {
            removeProgressDialog();
            if (mDialogVip != null) {
                mDialogVip.dismiss();
            }
            LogUtil.i("BaseActivity", "Json = " + response.body.toString());
            isHotelVipRefresh = true;
            HotelOrderManager.getInstance().getHotelDetailInfo().isPopup = false;
            HotelOrderManager.getInstance().getHotelDetailInfo().isVip = true;
            btn_right.setVisibility(View.VISIBLE);
            btn_right.setImageResource(R.drawable.iv_viped);
            refreshScreenInfoVipPrice();
            CustomToast.show(getString(R.string.isViped), CustomToast.LENGTH_SHORT);
        }
        onNotifyMessage(request, response);
    }

    @Override
    public void notifyError(ResponseData request, ResponseData response, Exception e) {
        removeProgressDialog();
        if (request.flag == AppConst.AD_GDT_IF) {
            return;
        }
        String message;
        if (e != null && e instanceof ConnectException) {
            message = getString(R.string.dialog_tip_net_error);
        } else {
            message = response != null && response.data != null ? response.data.toString() : getString(R.string.dialog_tip_null_error);
        }
        CustomToast.show(message, CustomToast.LENGTH_LONG);
        onNotifyError(request);
    }

    public void onNotifyMessage(ResponseData request, ResponseData response) {
        LogUtil.d(TAG, "onNotifyMessage()");
    }

    public void onNotifyError(ResponseData request) {
        LogUtil.d(TAG, "onNotifyError()");
    }
}
