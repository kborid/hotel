package com.huicheng.hotel.android.ui.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.huicheng.hotel.android.R;

/**
 * @author kborid
 * @date 2016/12/9 0009
 */
public class CustomCirclePieChart extends View {
    private Context context;
    private int mWidth;
    private int mHeight;
    private int centerX, centerY;
    private Paint circlePaint, pointPaint, linePaint;
    private RectF mRectF;
    private int radius = 125; //圆环的半径

    private float hotelCost, airCost, trainCost, carCost, saveCost;
    private float hotelAngle, airAngle, trainAngle, carAngle, saveAngle;

    public CustomCirclePieChart(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public CustomCirclePieChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public CustomCirclePieChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        circlePaint = new Paint();
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(100);
        circlePaint.setAntiAlias(true);

        pointPaint = new Paint();
        pointPaint.setStyle(Paint.Style.FILL);
        pointPaint.setAntiAlias(true);

        linePaint = new Paint();
        linePaint.setStyle(Paint.Style.FILL);
        linePaint.setTextSize(24);
        linePaint.setAntiAlias(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mHeight = h;
        mWidth = w;

        centerX = mWidth / 2; //获取圆心的x坐标
        centerY = mHeight / 2;
        mRectF = new RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius);  //用于定义的圆弧的形状和大小的界限
    }

    public void setConst(float hotelCost, float airCost, float trainCost, float carCost, float saveCost) {
        this.hotelCost = hotelCost;
        this.airCost = airCost;
        this.trainCost = trainCost;
        this.carCost = carCost;
        this.saveCost = saveCost;

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float total = hotelCost + airCost + trainCost + carCost + saveCost;
        hotelAngle = hotelCost / total * 360;
        airAngle = airCost / total * 360;
        trainAngle = trainCost / total * 360;
        carAngle = carCost / total * 360;
        saveAngle = saveCost / total * 360;

        if (airAngle > 0) {
            circlePaint.setColor(context.getResources().getColor(R.color.airTicketColor));  //设置进度的颜色
            canvas.drawArc(mRectF, 0, airAngle + 2, false, circlePaint);  //根据进度画圆弧
            pointPaint.setColor(context.getResources().getColor(R.color.airTicketColor));
            linePaint.setColor(context.getResources().getColor(R.color.airTicketColor));
            drawLabelPoint(canvas, 0, airAngle, 100, airCost, "机票");
        }

        if (hotelAngle > 0) {
            circlePaint.setColor(context.getResources().getColor(R.color.hotelColor));
            canvas.drawArc(mRectF, airAngle, hotelAngle + 2, false, circlePaint);  //根据进度画圆弧
            pointPaint.setColor(context.getResources().getColor(R.color.hotelColor));
            linePaint.setColor(context.getResources().getColor(R.color.hotelColor));
            drawLabelPoint(canvas, airAngle, hotelAngle, 100, hotelCost, "酒店");
        }

        if (saveAngle > 0) {
            circlePaint.setColor(context.getResources().getColor(R.color.saveColor));
            canvas.drawArc(mRectF, airAngle + hotelAngle, saveAngle + 2, false, circlePaint);  //根据进度画圆弧
            pointPaint.setColor(context.getResources().getColor(R.color.saveColor));
            linePaint.setColor(context.getResources().getColor(R.color.saveColor));
            drawLabelPoint(canvas, airAngle + hotelAngle, saveAngle, 100, saveCost, "节省");
        }

        if (trainAngle > 0) {
            circlePaint.setColor(context.getResources().getColor(R.color.red));
            canvas.drawArc(mRectF, airAngle + hotelAngle + saveAngle, trainAngle + 2, false, circlePaint);  //根据进度画圆弧
            pointPaint.setColor(context.getResources().getColor(R.color.red));
            linePaint.setColor(context.getResources().getColor(R.color.red));
            drawLabelPoint(canvas, airAngle + hotelAngle + saveAngle, trainAngle, 100, trainCost, "火车票");
        }

        if (carAngle > 0) {
            circlePaint.setColor(context.getResources().getColor(R.color.yellow));
            canvas.drawArc(mRectF, airAngle + hotelAngle + saveAngle + trainAngle, carAngle + 2, false, circlePaint);  //根据进度画圆弧
            pointPaint.setColor(context.getResources().getColor(R.color.yellow));
            linePaint.setColor(context.getResources().getColor(R.color.yellow));
            drawLabelPoint(canvas, airAngle + hotelAngle + saveAngle + trainAngle, carAngle, 100, carCost, "租车");
        }
    }

    private void drawLabelPoint(Canvas canvas, float startAngle, float angle, int dis, float cost, String strMethod) {
        float x, y;
        float cenAngle = startAngle + angle / 2;
        x = (float) (Math.cos(cenAngle * Math.PI / 180) * (radius + dis) + centerX);
        y = (float) (Math.sin(cenAngle * Math.PI / 180) * (radius + dis) + centerY);

        canvas.drawCircle(x, y, 15, pointPaint);

        float endX, endY;

        float tempAngle;
        float end2X;
        boolean isflag = false;
        if (cenAngle > 0 && cenAngle <= 90) {
            tempAngle = 45;
            end2X = 120;
            isflag = false;
        } else if (cenAngle > 90 && cenAngle <= 180) {
            tempAngle = 135;
            end2X = -120;
            isflag = true;
        } else if (cenAngle > 180 && cenAngle <= 270) {
            tempAngle = 225;
            end2X = -120;
            isflag = true;
        } else {
            tempAngle = 315;
            end2X = 120;
            isflag = false;
        }
        endX = (float) (Math.cos(tempAngle * Math.PI / 180) * 50 + x);
        endY = (float) (Math.sin(tempAngle * Math.PI / 180) * 50 + y);
        canvas.drawLine(x, y, endX, endY, linePaint);
        canvas.drawLine(endX, endY, end2X + endX, endY, linePaint);

        linePaint.setColor(context.getResources().getColor(R.color.noDiscountColor));
        float upDis, downDis;
        if (isflag) {
            upDis = 10;
            downDis = 10;
        } else {
            upDis = -linePaint.measureText(context.getResources().getString(R.string.RMB) + String.valueOf(cost)) - 10;
            downDis = -linePaint.measureText(strMethod) - 10;
        }
        canvas.drawText(context.getResources().getString(R.string.RMB) + String.valueOf(cost), end2X + endX + upDis, endY - 15, linePaint);
        canvas.drawText(strMethod, end2X + endX + downDis, endY + 30, linePaint);
    }
}
