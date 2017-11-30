package com.huicheng.hotel.android.ui.custom;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.prj.sdk.util.Utils;

/**
 * @auth Nate
 * @date 2016/7/22.
 */
public class VerticalStackTransformer extends VerticalBaseTransformer {

    private Context context;
    private int spaceBetweenFirAndSecWith = 10 * 2;//第一张卡片和第二张卡片宽度差  dp单位
    private int spaceBetweenFirAndSecHeight = 12;//第一张卡片和第二张卡片高度差   dp单位
    private int spaceBetweenSecAndThiHeight = 10;//第二张卡片和第三张卡片高度差   dp单位

    public VerticalStackTransformer(Context context) {
        this.context = context;
    }

    public VerticalStackTransformer(Context context, int spaceBetweenFirAndSecWith, int spaceBetweenFirAndSecHeight) {
        this.context = context;
        this.spaceBetweenFirAndSecWith = spaceBetweenFirAndSecWith;
        this.spaceBetweenFirAndSecHeight = spaceBetweenFirAndSecHeight;
    }

    @Override
    protected void onTransform(View page, float position) {
        if (position <= 0.0f) {
            page.setAlpha(1.0f);
            Log.e("onTransform", "position <= 0.0f ==>" + position);
            page.setTranslationY(0f);
            //控制停止滑动切换的时候，只有最上面的一张卡片可以点击
            page.setClickable(true);
        } else if (position <= 1.0f) {
            Log.e("onTransform", "position <= 1.0f ==>" + position);
            float scale = (float) (page.getWidth() - Utils.dp2px(spaceBetweenFirAndSecWith * position)) / (float) (page.getWidth());
            //控制下面卡片的可见度
            page.setAlpha(1.0f - 0.4f * position); // alpha最小0.6f
            //控制停止滑动切换的时候，只有最上面的一张卡片可以点击
            page.setClickable(false);
            page.setPivotX(page.getWidth() / 2f);
            page.setPivotY(page.getHeight() / 2f);
            page.setScaleX(scale);
            page.setScaleY(scale);
            page.setTranslationY(-page.getHeight() * position - (page.getHeight() * 0.5f) * (1 - scale) - Utils.dp2px(spaceBetweenFirAndSecHeight) * position);
        } else if (position <= 2.0f) {
            Log.e("onTransform", "position <= 2.0f ==>" + position);
            float scale = (float) (page.getWidth() - Utils.dp2px(spaceBetweenFirAndSecWith * position)) / (float) (page.getWidth());
            //控制下面卡片的可见度
            page.setAlpha(1.0f - 0.4f * position); // alpha最小0.2f
            //控制停止滑动切换的时候，只有最上面的一张卡片可以点击
            page.setClickable(false);
            page.setPivotX(page.getWidth() / 2f);
            page.setPivotY(page.getHeight() / 2f);
            page.setScaleX(scale);
            page.setScaleY(scale);
            page.setTranslationY(-page.getHeight() * position - (page.getHeight() * 0.5f) * (1 - scale) - Utils.dp2px(spaceBetweenSecAndThiHeight) * position);
        }
    }
}
