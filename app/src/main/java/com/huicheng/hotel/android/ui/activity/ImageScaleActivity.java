package com.huicheng.hotel.android.ui.activity;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.ui.base.BaseAppActivity;
import com.huicheng.hotel.android.ui.custom.SmoothImageView;
import com.huicheng.hotel.android.ui.dialog.CustomToast;
import com.huicheng.hotel.android.ui.glide.CustomReqURLFormatModelImpl;

/**
 * @author kborid
 * @date 2017/3/30 0030
 */
public class ImageScaleActivity extends BaseAppActivity {

    private String mUrl;
    private int mLocationX, mLocationY, mWidth, mHeight;
    private SmoothImageView iv_picture;
    private boolean isTransating = false;
    private boolean isError = false;

    @Override
    protected void preOnCreate() {
        super.preOnCreate();
        initStatus();
        setTheme(R.style.AppTheme_ImageTransparent);
        overridePendingTransition(0, 0);
    }

    @Override
    protected void setContentView() {
        iv_picture = new SmoothImageView(this);
        setContentView(iv_picture);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void initStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View decoderView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
            decoderView.setSystemUiVisibility(option);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        } else {
            WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    @Override
    public void dealIntent() {
        super.dealIntent();
        mUrl = getIntent().getStringExtra("url");
        mLocationX = getIntent().getIntExtra("locationX", 0);
        mLocationY = getIntent().getIntExtra("locationY", 0);
        mWidth = getIntent().getIntExtra("width", 0);
        mHeight = getIntent().getIntExtra("height", 0);
    }

    @Override
    public void initParams() {
        super.initParams();
        iv_picture.setOriginalInfo(mWidth, mHeight, mLocationX, mLocationY);
        iv_picture.transformIn();
        isTransating = true;
        iv_picture.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        iv_picture.setScaleType(ImageView.ScaleType.FIT_CENTER);
        iv_picture.setImageResource(R.color.transparent);
        Glide.with(this)
                .load(new CustomReqURLFormatModelImpl(mUrl))
                .asBitmap()
                .thumbnail(0.1f)
                .fitCenter()
                .listener(new RequestListener<CustomReqURLFormatModelImpl, Bitmap>() {
                    @Override
                    public boolean onException(Exception e, CustomReqURLFormatModelImpl model, Target<Bitmap> target, boolean isFirstResource) {
                        isError = true;
                        CustomToast.show("图片获取失败", CustomToast.LENGTH_SHORT);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap bm, CustomReqURLFormatModelImpl model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        iv_picture.setImageBitmap(bm);
                        return false;
                    }
                })
                .override(1080, 1920)
                .into(iv_picture);
    }

    @Override
    public void initListeners() {
        super.initListeners();
        iv_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isError) {
                    finishActivity();
                    return;
                }
                if (isTransating) {
                    return;
                }
                isTransating = true;
                iv_picture.transformOut();
            }
        });
        iv_picture.setOnTransformListener(new SmoothImageView.TransformListener() {
            @Override
            public void onTransformComplete(int mode) { //mode STATE_TRANSFORM_IN 1 ,STATE_TRANSFORM_OUT 2
                isTransating = false;
                if (2 == mode) {
                    finishActivity();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void finishActivity() {
        finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == keyCode) {
            iv_picture.performClick();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
