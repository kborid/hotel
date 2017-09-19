package com.huicheng.hotel.android.control;

import android.app.Activity;
import android.content.Context;

import com.huicheng.hotel.android.ui.dialog.CustomToast;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMWeb;

/**
 * 分享操作
 */
public class ShareControl {
    private static ShareControl mInstance = null;
    private Context context;
    private UMWeb mUMWeb = null;
    private IShareResultListener listener = null;

    private ShareControl() {
    }

    public static ShareControl getInstance() {
        if (mInstance == null) {
            synchronized (ShareControl.class) {
                if (mInstance == null) {
                    mInstance = new ShareControl();
                }
            }
        }
        return mInstance;
    }

    public void setUMWebContent(Context context, UMWeb web, IShareResultListener listener) {
        this.context = context;
        this.mUMWeb = web;
        this.listener = listener;
    }

    public void shareUMWeb(SHARE_MEDIA platform) {
        if (null == mUMWeb) {
            return;
        }
        new ShareAction((Activity) context).withMedia(mUMWeb).setPlatform(platform).setCallback(new UMShareListener() {
            @Override
            public void onStart(SHARE_MEDIA share_media) {
                CustomToast.show("开始分享", CustomToast.LENGTH_SHORT);
            }

            @Override
            public void onResult(SHARE_MEDIA share_media) {
                CustomToast.show("分享成功", CustomToast.LENGTH_SHORT);
                if (listener != null) {
                    listener.onShareResult(true);
                }
            }

            @Override
            public void onError(SHARE_MEDIA share_media, Throwable throwable) {
                CustomToast.show("分享错误", CustomToast.LENGTH_SHORT);
                if (listener != null) {
                    listener.onShareResult(false);
                }
            }

            @Override
            public void onCancel(SHARE_MEDIA share_media) {
                CustomToast.show("取消分享", CustomToast.LENGTH_SHORT);
                if (listener != null) {
                    listener.onShareResult(false);
                }
            }
        }).share();
    }
}
