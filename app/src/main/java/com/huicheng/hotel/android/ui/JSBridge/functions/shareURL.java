package com.huicheng.hotel.android.ui.JSBridge.functions;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.control.ShareControl;
import com.huicheng.hotel.android.ui.JSBridge.WVJBWebViewClient;
import com.huicheng.hotel.android.ui.JSBridge.WVJBWebViewClient.WVJBResponseCallback;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;

public class shareURL implements WVJBWebViewClient.WVJBHandler {
    private Context mContext;

    /**
     * 构造函数，获取上下文
     *
     * @param context
     */
    public shareURL(Context context) {
        mContext = context;
    }

    @Override
    public void request(Object data, WVJBResponseCallback callback) {
        try {
            if (data != null) {
                JSONObject mJson = JSON.parseObject(data.toString());
                UMWeb web = new UMWeb(mJson.getString("url"));
                web.setTitle(mJson.getString("title"));
                web.setThumb(new UMImage(mContext, R.drawable.logo));
                web.setDescription(mJson.getString("description"));
                ShareControl.getInstance().setUMWebContent(mContext, web, null);
                SHARE_MEDIA media = SHARE_MEDIA.QQ;
                if (mJson.containsKey("channel")) {
                    switch (mJson.getString("channel")) {
                        case "01":
                            media = SHARE_MEDIA.WEIXIN;
                            break;
                        case "02":
                            media = SHARE_MEDIA.WEIXIN_CIRCLE;
                            break;
                        case "03":
                            media = SHARE_MEDIA.QQ;
                            break;
                    }
                }
                ShareControl.getInstance().shareUMWeb(media);
//                ((HtmlActivity) mContext).showSharePopupWindow();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
