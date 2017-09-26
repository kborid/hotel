package com.huicheng.hotel.android.ui.glide;

import android.content.Context;

import com.bumptech.glide.load.model.stream.BaseGlideUrlLoader;

/**
 * @auth kborid
 * @date 2017/9/26.
 */

public class CustomReqURLFormatLoader extends BaseGlideUrlLoader<CustomReqURLFormatModel> {
    public CustomReqURLFormatLoader(Context context) {
        super(context);
    }

    @Override
    protected String getUrl(CustomReqURLFormatModel model, int width, int height) {
        return model.requestReqFormatUrl();
    }
}
