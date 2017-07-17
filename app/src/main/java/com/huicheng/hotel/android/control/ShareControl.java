package com.huicheng.hotel.android.control;

import android.app.Activity;
import android.content.Context;

import com.huicheng.hotel.android.ui.activity.HotelSpaceDetailActivity;
import com.prj.sdk.widget.CustomToast;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMWeb;

/**
 * 分享操作
 */
public class ShareControl {
    private ShareAction mShareAction = null;
    private static ShareControl mInstance = null;
    private Context context;

    private ShareControl(Context context) {
        mShareAction = new ShareAction((Activity) context);
        this.context = context;
    }

    /**
     * 获得实例的唯一全局访问点
     */
    public static ShareControl getInstance(Context context) {
        if (mInstance == null) {
            // 增加类锁,保证只初始化一次
            synchronized (ShareControl.class) {
                if (mInstance == null) {
                    mInstance = new ShareControl(context);
                }
            }
        }
        return mInstance;
    }

    public void openShareDisplay(UMWeb web, final HotelSpaceDetailActivity.ShareResultListener listener) {
        new ShareAction((Activity) context).withMedia(web).setDisplayList(SHARE_MEDIA.QQ, SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE).setCallback(new UMShareListener() {
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
        }).open();
    }

    public void openShareDisplay(UMWeb web) {
        openShareDisplay(web, null);
    }
}
