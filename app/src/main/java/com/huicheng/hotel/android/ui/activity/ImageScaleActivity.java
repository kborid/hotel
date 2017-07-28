package com.huicheng.hotel.android.ui.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.huicheng.hotel.android.ui.custom.SmoothImageView;
import com.prj.sdk.app.AppContext;
import com.prj.sdk.net.image.ImageLoader;
import com.prj.sdk.widget.CustomToast;

/**
 * @author kborid
 * @date 2017/3/30 0030
 */
public class ImageScaleActivity extends BaseActivity {

    private String mUrl;
    private int mLocationX, mLocationY, mWidth, mHeight;
    private SmoothImageView iv_picture;
    private boolean isTransating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.ImageTransparent);
        overridePendingTransition(0, 0);
        iv_picture = new SmoothImageView(this);
        setContentView(iv_picture);
        initParams();
        initListeners();
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

        ImageLoader.getInstance().loadBitmap(new ImageLoader.ImageCallback() {
            @Override
            public void imageCallback(Bitmap bm, String url, String imageTag) {
                if (null != bm) {
                    iv_picture.setImageBitmap(bm);
                } else {
                    finish();
                    CustomToast.show("图片获取失败", CustomToast.LENGTH_SHORT);
                }
            }
        }, mUrl, mUrl, 1920, 1080, -1);
    }

    @Override
    public void initListeners() {
        super.initListeners();
        iv_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transformEnd();
            }
        });
        iv_picture.setOnTransformListener(new SmoothImageView.TransformListener() {
            @Override
            public void onTransformComplete(int mode) {
                isTransating = false;
            }
        });
    }

    private void transformEnd() {
        if (isTransating) {
            return;
        }
        isTransating = true;
        iv_picture.transformOut();
        AppContext.mMainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                iv_picture.setAlpha(0f);
                finish();
                overridePendingTransition(0, 0);
            }
        }, 250);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == keyCode) {
            transformEnd();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
