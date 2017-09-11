package com.huicheng.hotel.android.ui.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.huicheng.hotel.android.R;

/**
 * @author kborid
 * @date 2016/11/9 0009
 */
public class RoundedTopImageView extends ImageView {
    /*圆角的半径，依次为左上角xy半径，右上角，右下角，左下角*/
    private float[] rids;

    public RoundedTopImageView(Context context) {
        this(context, null);
    }

    public RoundedTopImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundedTopImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.roundedImage);
        float radius = (int) mTypedArray.getDimension(R.styleable.roundedImage_radius, 10);
        rids = new float[]{radius, radius, radius, radius, 0, 0, 0, 0};
        mTypedArray.recycle();
    }

    /**
     * 画图
     * by Hankkin at:2015-08-30 21:15:53
     *
     * @param canvas
     */
    protected void onDraw(Canvas canvas) {
        Path path = new Path();
        int w = this.getWidth();
        int h = this.getHeight();
        /*向路径中添加圆角矩形。radii数组定义圆角矩形的四个圆角的x,y半径。radii长度必须为8*/
        path.addRoundRect(new RectF(0, 0, w, h), rids, Path.Direction.CW);
        canvas.clipPath(path);
        super.onDraw(canvas);
    }
}
