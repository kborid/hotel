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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.ui.activity.InvoiceDetailActivity;
import com.huicheng.hotel.android.ui.activity.OrderPayActivity;
import com.huicheng.hotel.android.ui.activity.OrderPaySuccessActivity;
import com.huicheng.hotel.android.ui.dialog.ProgressDialog;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.image.ImageLoader;
import com.prj.sdk.util.ActivityTack;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.widget.CustomToast;
import com.umeng.analytics.MobclickAgent;

import java.net.ConnectException;

public abstract class BaseActivity extends AppCompatActivity implements OnClickListener {

    private ProgressDialog mProgressDialog;
    protected TextView tv_center_title, tv_center_summary;
    protected ImageView btn_back, btn_right;
    protected static String requestID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityTack.getInstanse().addActivity(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
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
//        removeProgressDialog();// pause时关闭加载框
        MobclickAgent.onPageEnd(this.getClass().getName());
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
                        || getClass().equals(OrderPaySuccessActivity.class)) {
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
        if (mProgressDialog != null) {
            return mProgressDialog.isShowing();
        } else {
            return false;
        }
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
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
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
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.VISIBLE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(0);
        }
    }

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

    protected void onNotifyError(ResponseData request) {
    }
}