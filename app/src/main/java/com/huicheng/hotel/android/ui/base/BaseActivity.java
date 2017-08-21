package com.huicheng.hotel.android.ui.base;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.AppConst;
import com.huicheng.hotel.android.common.HotelCommDef;
import com.huicheng.hotel.android.common.HotelOrderManager;
import com.huicheng.hotel.android.common.NetURL;
import com.huicheng.hotel.android.net.RequestBeanBuilder;
import com.huicheng.hotel.android.net.bean.HotelDetailInfoBean;
import com.huicheng.hotel.android.ui.activity.InvoiceDetailActivity;
import com.huicheng.hotel.android.ui.activity.OrderPayActivity;
import com.huicheng.hotel.android.ui.activity.OrderPaySuccessActivity;
import com.huicheng.hotel.android.ui.activity.UserCenterActivity;
import com.huicheng.hotel.android.ui.dialog.CustomDialog;
import com.huicheng.hotel.android.ui.dialog.ProgressDialog;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.net.image.ImageLoader;
import com.prj.sdk.util.ActivityTack;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;
import com.prj.sdk.widget.CustomToast;
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

    private CustomDialog mDialogVip;
    private ProgressDialog mProgressDialog;
    protected TextView tv_center_title, tv_center_summary;
    protected ImageView btn_back, btn_right;
    protected static String requestID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (SharedPreferenceUtil.getInstance().getInt(AppConst.SKIN_INDEX, 0) == 1) {
            setTheme(R.style.femaleTheme);
        } else {
            setTheme(R.style.defaultTheme);
        }
        ActivityTack.getInstanse().addActivity(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(this.getClass().getName()); // 统计页面(仅有Activity的应用中SDK自动调用，不需要单独写。"SplashScreen"为页面名称，可自定义)
        MobclickAgent.onResume(this); // 统计时长
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(this.getClass().getName());
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataLoader.getInstance().clear(requestID);
        ActivityTack.getInstanse().removeActivity(this);
    }

    // 初始化组件
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void initViews() {
        tv_center_title = (TextView) findViewById(R.id.tv_center_title);
        if (tv_center_title != null) {
            tv_center_title.setTypeface(Typeface.DEFAULT_BOLD);
        }
        tv_center_summary = (TextView) findViewById(R.id.tv_center_summary);
        btn_back = (ImageView) findViewById(R.id.btn_back);
        btn_right = (ImageView) findViewById(R.id.btn_right);
    }

    public void dealIntent() {
    }

    // 参数设置
    public void initParams() {
        dealIntent();

        // 设置title的描述
        if (tv_center_summary != null) {
            if (StringUtil.isEmpty(tv_center_summary.getText())) {
                tv_center_summary.setVisibility(View.GONE);
            }
        }
    }

    // 监听设置
    public void initListeners() {
        if (btn_back != null) {
            btn_back.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                if (getClass().equals(InvoiceDetailActivity.class)
                        || getClass().equals(OrderPayActivity.class)
                        || getClass().equals(OrderPaySuccessActivity.class)
                        || getClass().equals(UserCenterActivity.class)) {
                    //do nothing
                } else {
                    finish();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 显示loading对话框
     */
    public final void showProgressDialog(Context context) {
        showProgressDialog(context, null);
    }

    public final void showProgressDialog(Context cxt, String tip) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(cxt);
        }
//        mProgressDialog.setMessage(tip);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    public final boolean isProgressShowing() {
        return mProgressDialog != null && mProgressDialog.isShowing();
    }

    /**
     * 销毁loading对话框
     */
    public final void removeProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void finish() {
        super.finish();
    }

    public void loadImage(final View view, String url, int width, int height) {
        loadImage(view, -1, url, width, height);
    }

    public void loadImage(final View view, int defId, String url, int width, int height) {
        int resId = R.color.hintColor;
        if (defId != -1) {
            resId = defId;
        }
        if (view instanceof ImageView) {
            ((ImageView) view).setImageResource(resId);
        } else {
            view.setBackgroundResource(resId);
        }

        if (StringUtil.notEmpty(url)) {
            ImageLoader.getInstance().loadBitmap(new ImageLoader.ImageCallback() {
                @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void imageCallback(Bitmap bm, String url, String imageTag) {
                    if (null != bm) {
                        if (view instanceof ImageView) {
                            ((ImageView) view).setImageBitmap(bm);
                        } else {
                            view.setBackground(new BitmapDrawable(bm));
                        }
                    }
                }
            }, url, url, width, height, -1);
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
            loadImage(iv_hotel_bg, R.drawable.def_hotel_banner, hotelDetailInfoBean.picPath.get(0), 800, 800);

            TextView tv_name = (TextView) viewHome.findViewById(R.id.tv_name);
            tv_name.setText(hotelDetailInfoBean.name);
            TextView tv_address = (TextView) viewHome.findViewById(R.id.tv_address);
            tv_address.setText(hotelDetailInfoBean.address);
            Button btn_join = (Button) viewHome.findViewById(R.id.btn_join);
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
                    TextView tv_tag = (TextView) LayoutInflater.from(context).inflate(R.layout.lv_tag_item, tagFlowLayout, false);
                    tv_tag.setText(o);
                    return tv_tag;
                }
            });
            tagFlowLayout.setMaxSelectCount(1);
            tagFlowLayout.setOnSelectListener(new TagFlowLayout.OnSelectListener() {
                @Override
                public void onSelected(Set<Integer> selectPosSet) {
                    selectedIndex[0] = selectPosSet.iterator().next();
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

    private void dismissAddVipDialog() {
        if (mDialogVip != null) {
            mDialogVip.dismiss();
        }
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

    @Override
    public void notifyMessage(ResponseData request, ResponseData response) throws Exception {
        if (request.flag == AppConst.HOTEL_VIP) {
            removeProgressDialog();
            dismissAddVipDialog();
            LogUtil.i("BaseActivity", "Json = " + response.body.toString());
            CustomToast.show("您已成为该酒店会员", CustomToast.LENGTH_SHORT);
            HotelOrderManager.getInstance().getHotelDetailInfo().isPopup = false;
            btn_right.setVisibility(View.INVISIBLE);
        }
        onNotifyMessage(request, response);
    }

    @Override
    public void preExecute(ResponseData request) {
    }

    @Override
    public void notifyError(ResponseData request, ResponseData response, Exception e) {
        removeProgressDialog();
        String message;
        if (e != null && e instanceof ConnectException) {
            message = getString(R.string.dialog_tip_net_error);
        } else {
            message = response != null && response.data != null ? response.data.toString() : getString(R.string.dialog_tip_null_error);
        }
        CustomToast.show(message, CustomToast.LENGTH_SHORT);
        onNotifyError(request);
    }

    public void onNotifyMessage(ResponseData request, ResponseData response) {
    }

    public void onNotifyError(ResponseData request) {
    }
}
