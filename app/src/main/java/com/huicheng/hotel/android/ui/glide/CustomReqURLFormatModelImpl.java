package com.huicheng.hotel.android.ui.glide;

import com.prj.sdk.util.Utils;

/**
 * @auth kborid
 * @date 2017/9/26.
 */

public class CustomReqURLFormatModelImpl implements CustomReqURLFormatModel {
    private String baseUrl;

    public CustomReqURLFormatModelImpl(String url) {
        this.baseUrl = url;
    }

    @Override
    public String requestReqFormatUrl() {
        return Utils.covertProtocol2Lower(baseUrl);
    }
}
