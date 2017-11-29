package com.huicheng.hotel.android.ui.activity.hotel;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;

import com.alibaba.fastjson.JSON;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.HotelCommDef;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.control.ShareControl;
import com.huicheng.hotel.android.requestbuilder.RequestBeanBuilder;
import com.huicheng.hotel.android.requestbuilder.bean.FreeOneNightBean;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.huicheng.hotel.android.ui.custom.CustomSharePopup;
import com.huicheng.hotel.android.ui.custom.RoundedAllImageView;
import com.huicheng.hotel.android.ui.dialog.CustomToast;
import com.prj.sdk.app.AppConst;
import com.prj.sdk.app.NetURL;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.net.data.ResponseData;
import com.prj.sdk.util.BitmapUtils;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;

import java.util.HashMap;

/**
 * @author kborid
 * @date 2016/11/14 0014
 */
public class Hotel0YuanHomeActivity extends BaseActivity {

    private Button btn_start;
    private RoundedAllImageView iv_share;
    private FreeOneNightBean bean = null;

    private PopupWindow mSharePopupWindow = null;
    private CustomSharePopup mCustomShareView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_0yuanhome_layout);
        initViews();
        initParams();
        initListeners();
        if (null == savedInstanceState) {
            requestCurrentHasFreeActive();
        }
    }

    @Override
    public void initViews() {
        super.initViews();
        btn_start = (Button) findViewById(R.id.btn_start);
        iv_share = (RoundedAllImageView) findViewById(R.id.iv_share);
    }

    @Override
    public void initParams() {
        super.initParams();
        setBackButtonResource(R.drawable.iv_back_white);
        Bitmap bm = BitmapUtils.getGrayBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.iv_share));
        iv_share.setImageBitmap(bm);
        iv_share.setEnabled(false);
    }

    private void showSharePopupWindow() {
        if (null == mSharePopupWindow) {
            mCustomShareView = new CustomSharePopup(this);
            mCustomShareView.setOnCancelListener(new CustomSharePopup.OnCanceledListener() {
                @Override
                public void onDismiss() {
                    mSharePopupWindow.dismiss();
                }
            });
            mSharePopupWindow = new PopupWindow(mCustomShareView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        }
        mSharePopupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        mSharePopupWindow.setAnimationStyle(R.style.share_anmi);
        mSharePopupWindow.setBackgroundDrawable(new ColorDrawable(0));
        mSharePopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams params = getWindow().getAttributes();
                params.alpha = 1.0f;
                getWindow().setAttributes(params);
            }
        });
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.alpha = 0.8f;
        getWindow().setAttributes(params);
        mSharePopupWindow.showAtLocation(getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
    }

    private void requestCurrentHasFreeActive() {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        ResponseData d = b.syncRequest(b);
        d.path = NetURL.FREE_CURRENT_ACTIVE;
        d.flag = AppConst.FREE_CURRENT_ACTIVE;

        if (!isProgressShowing()) {
            showProgressDialog(this);
        }

        requestID = DataLoader.getInstance().loadData(this, d);
    }

    @Override
    public void initListeners() {
        super.initListeners();
        btn_start.setOnClickListener(this);
        iv_share.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.btn_start:
                if (bean != null) {
                    Intent intent = new Intent(this, Hotel0YuanChooseActivity.class);
                    intent.putExtra("active", bean);
                    startActivity(intent);
                }
                break;
            case R.id.iv_share:
                HashMap<String, String> params = new HashMap<>();
                params.put("type", "invite");
                params.put("userID", SessionContext.mUser.user.userid);
                params.put("channel", HotelCommDef.SHARE_FREE);
                String url = SessionContext.getUrl(NetURL.SHARE, params);

                UMWeb web = new UMWeb(url);
                web.setTitle("邀请您注册" + " " + getResources().getString(R.string.app_name));
                web.setThumb(new UMImage(this, R.drawable.logo));
                web.setDescription("开启无中介预订时代！");

                ShareControl.getInstance().setUMWebContent(this, web, null);
                showSharePopupWindow();

                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ShareControl.getInstance().destroy();
    }

    @Override
    public void onNotifyMessage(ResponseData request, ResponseData response) {
        if (response != null && response.body != null) {
            if (request.flag == AppConst.FREE_CURRENT_ACTIVE) {
                removeProgressDialog();
                if (!response.body.toString().equals("{}")) {
                    bean = JSON.parseObject(response.body.toString(), FreeOneNightBean.class);
                    btn_start.setEnabled(true);
                    iv_share.setImageResource(R.drawable.iv_share);
                    iv_share.setEnabled(true);
                } else {
                    CustomToast.show("活动已结束", CustomToast.LENGTH_SHORT);
                }
            }
        }
    }
}
