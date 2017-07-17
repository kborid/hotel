package com.huicheng.hotel.android.ui.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.huicheng.hotel.android.R;

/**
 * @author kborid
 * @date 2017/1/13 0013
 */
public class CommonAssessStarsLayout extends RelativeLayout {
    private Context context;

    private ImageView iv_color_stars;

    public CommonAssessStarsLayout(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public CommonAssessStarsLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public CommonAssessStarsLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        LayoutInflater.from(context).inflate(R.layout.comm_assess_stars_layout, this);
        findViews();
    }

    private void findViews() {
        iv_color_stars = (ImageView) findViewById(R.id.iv_color_stars);
    }

    public void setColorStars(int count) {
        switch (count) {
            case 0:
                iv_color_stars.setImageResource(R.drawable.iv_5stars_gray);
                break;
            case 1:
                iv_color_stars.setImageResource(R.drawable.iv_1stars_color);
                break;
            case 2:
                iv_color_stars.setImageResource(R.drawable.iv_2stars_color);
                break;
            case 3:
                iv_color_stars.setImageResource(R.drawable.iv_3stars_color);
                break;
            case 4:
                iv_color_stars.setImageResource(R.drawable.iv_4stars_color);
                break;
            case 5:
                iv_color_stars.setImageResource(R.drawable.iv_5stars_color);
                break;
            default:
                iv_color_stars.setImageResource(R.drawable.iv_5stars_gray);
                break;
        }
    }
}
