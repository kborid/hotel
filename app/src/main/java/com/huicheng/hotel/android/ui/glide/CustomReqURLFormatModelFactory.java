package com.huicheng.hotel.android.ui.glide;

import android.content.Context;

import com.bumptech.glide.load.model.GenericLoaderFactory;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;

import java.io.InputStream;

/**
 * @auth kborid
 * @date 2017/9/26.
 */

public class CustomReqURLFormatModelFactory implements ModelLoaderFactory<CustomReqURLFormatModel, InputStream> {
    @Override
    public ModelLoader<CustomReqURLFormatModel, InputStream> build(Context context, GenericLoaderFactory factories) {
        return new CustomReqURLFormatLoader(context);
    }

    @Override
    public void teardown() {

    }
}
