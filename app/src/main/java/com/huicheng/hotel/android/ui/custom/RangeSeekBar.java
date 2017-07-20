package com.huicheng.hotel.android.ui.custom;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.huicheng.hotel.android.R;

/**
 * @author kborid
 * @date 2016/11/10 0010
 */

public class RangeSeekBar extends View {
    public static final String[] textSummary = {"0", "100", "300", "500", "1000", "2000", "以上"};
    private Paint paint = new Paint();

    private int lineTop, lineBottom, lineLeft, lineRight;
    private int lineWidth;
    private int lineHeight;
    private RectF line = new RectF();

    private boolean isCanTouch = true;
    private int colorLineUnSelected;
    private int colorLineSelected;
    private int colorLineEdge;

    private SeekBar leftSB = new SeekBar();
    private SeekBar rightSB = new SeekBar();
    private SeekBar currTouch;

    private OnRangeChangedListener callback;

    private int seekBarResId;
    private float offsetValue;
    private float maxValue, minValue;
    private int cellsCount = 1;
    private float cellsPercent;
    private float reserveValue;
    private int reserveCount;
    private float reservePercent;
    private int cellCount = 1;

    private int thumbWidth;
    private int startPosition;

    private class SeekBar {
        int lineWidth;
        float currPercent;
        int left, right, top, bottom;
        Bitmap bmp;

        float material = 0;
        ValueAnimator anim;

        void onSizeChanged(int centerX, int centerY, int hSize, int parentLineWidth, boolean cellsMode, int bmpResId, Context context) {
            left = startPosition - thumbWidth / 2;
            right = parentLineWidth + startPosition + thumbWidth / 2;
            top = thumbWidth / 2;
            bottom = thumbWidth + thumbWidth / 2;

            if (cellsMode) {
                lineWidth = parentLineWidth;
            } else {
                lineWidth = parentLineWidth - thumbWidth;
            }
            Bitmap original = BitmapFactory.decodeResource(context.getResources(), bmpResId);
            Matrix matrix = new Matrix();
            float scaleWidth = ((float) thumbWidth) / original.getWidth();
            float scaleHeight = ((float) thumbWidth) / original.getHeight();
            matrix.postScale(scaleWidth, scaleHeight);
            bmp = Bitmap.createBitmap(original, 0, 0, original.getWidth(), original.getHeight(), matrix, true);
        }

        boolean collide(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();
            int offset = (int) (lineWidth * currPercent);
            return x > left + offset && x < right + offset && y > top && y < bottom;
        }

        void slide(float percent) {
            if (percent < 0) percent = 0;
            else if (percent > 1) percent = 1;
            currPercent = percent;
        }


        void draw(Canvas canvas) {
            int offset = (int) (lineWidth * currPercent);
            canvas.save();
            canvas.translate(offset, 0);
            if (bmp != null) {
                canvas.drawBitmap(bmp, left, top, null);
            } else {
                canvas.translate(left, 0);
            }
            canvas.restore();
        }

        private void materialRestore() {
            if (anim != null) anim.cancel();
            anim = ValueAnimator.ofFloat(material, 0);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    material = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    material = 0;
                    invalidate();
                }
            });
            anim.start();
        }
    }

    public interface OnRangeChangedListener {
        void onRangeChanged(RangeSeekBar view, float min, float max);
    }

    public RangeSeekBar(Context context) {
        this(context, null);
    }

    public RangeSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray t = context.obtainStyledAttributes(attrs, R.styleable.RangeSeekBar);
        seekBarResId = t.getResourceId(R.styleable.RangeSeekBar_seekBarResId, 0);
        colorLineUnSelected = t.getColor(R.styleable.RangeSeekBar_lineColorUnSelected, 0xFFFFFFFF);
        colorLineSelected = t.getColor(R.styleable.RangeSeekBar_lineColorSelected, 0xFF001a58);
        colorLineEdge = t.getColor(R.styleable.RangeSeekBar_lineColorEdge, 0xFFFFFFFF);
        float min = t.getFloat(R.styleable.RangeSeekBar_min, 0);
        float max = t.getFloat(R.styleable.RangeSeekBar_max, 1);
        float reserve = t.getFloat(R.styleable.RangeSeekBar_reserve, 0);
        cellCount = t.getInt(R.styleable.RangeSeekBar_cells, 1);
        setRules(min, max, reserve, cellCount);
        t.recycle();
    }

    public void setSeekBarResId(int resId) {
        seekBarResId = resId;

        Bitmap original = BitmapFactory.decodeResource(getContext().getResources(), resId);
        Matrix matrix = new Matrix();
        float scaleWidth = ((float) thumbWidth) / original.getWidth();
        float scaleHeight = ((float) thumbWidth) / original.getHeight();
        matrix.postScale(scaleWidth, scaleHeight);
        leftSB.bmp = Bitmap.createBitmap(original, 0, 0, original.getWidth(), original.getHeight(), matrix, true);
        rightSB.bmp = Bitmap.createBitmap(original, 0, 0, original.getWidth(), original.getHeight(), matrix, true);
        invalidate();
    }

    public void setColorLineEdge(int colorId) {
        colorLineEdge = colorId;
        invalidate();
    }

    public void setColorLineSelected(int colorId) {
        colorLineSelected = colorId;
        invalidate();
    }

    public void setColorLineUnSelected(int colorId) {
        colorLineUnSelected = colorId;
        invalidate();
    }

    public void setOnRangeChangedListener(OnRangeChangedListener listener) {
        callback = listener;
    }

    public void setValue(float min, float max) {
        min = min + offsetValue;
        max = max + offsetValue;

        if (min < minValue) {
            throw new IllegalArgumentException("setValue() min < (preset min - offsetValue) . #min:" + min + " #preset min:" + minValue + " #offsetValue:" + offsetValue);
        }
        if (max > maxValue) {
            throw new IllegalArgumentException("setValue() max > (preset max - offsetValue) . #max:" + max + " #preset max:" + maxValue + " #offsetValue:" + offsetValue);
        }

        if (reserveCount > 1) {
            if ((min - minValue) % reserveCount != 0) {
                throw new IllegalArgumentException("setValue() (min - preset min) % reserveCount != 0 . #min:" + min + " #preset min:" + minValue + "#reserveCount:" + reserveCount + "#reserve:" + reserveValue);
            }
            if ((max - minValue) % reserveCount != 0) {
                throw new IllegalArgumentException("setValue() (max - preset min) % reserveCount != 0 . #max:" + max + " #preset min:" + minValue + "#reserveCount:" + reserveCount + "#reserve:" + reserveValue);
            }
            leftSB.currPercent = (min - minValue) / reserveCount * cellsPercent;
            rightSB.currPercent = (max - minValue) / reserveCount * cellsPercent;
        } else {
            leftSB.currPercent = (min - minValue) / (maxValue - minValue);
            rightSB.currPercent = (max - minValue) / (maxValue - minValue);
        }

        invalidate();
    }

    public void setRules(float min, float max) {
        setRules(min, max, reserveCount, cellsCount);
    }

    public void setRules(float min, float max, float reserve, int cells) {
        if (max <= min) {
            throw new IllegalArgumentException("setRules() max must be greater than min ! #max:" + max + " #min:" + min);
        }
        if (min < 0) {
            offsetValue = 0 - min;
            min = min + offsetValue;
            max = max + offsetValue;
        }
        minValue = min;
        maxValue = max;

        if (reserve < 0) {
            throw new IllegalArgumentException("setRules() reserve must be greater than zero ! #reserve:" + reserve);
        }
        System.out.println("reserve = " + reserve);
        System.out.println("min = " + min);
        System.out.println("max = " + max);
        if (reserve >= max - min) {
            throw new IllegalArgumentException("setRules() reserve must be less than (max - min) ! #reserve:" + reserve + " #max - min:" + (max - min));
        }
        if (cells < 1) {
            throw new IllegalArgumentException("setRules() cells must be greater than 1 ! #cells:" + cells);
        }
        cellsCount = cells;
        cellsPercent = 1f / cellsCount;
        reserveValue = reserve;
        reservePercent = reserve / (max - min);
        reserveCount = (int) (reservePercent / cellsPercent + (reservePercent % cellsPercent != 0 ? 1 : 0));
        if (cellsCount > 1) {
            if (leftSB.currPercent + cellsPercent * reserveCount <= 1 && leftSB.currPercent + cellsPercent * reserveCount > rightSB.currPercent) {
                rightSB.currPercent = leftSB.currPercent + cellsPercent * reserveCount;
            } else if (rightSB.currPercent - cellsPercent * reserveCount >= 0 && rightSB.currPercent - cellsPercent * reserveCount < leftSB.currPercent) {
                leftSB.currPercent = rightSB.currPercent - cellsPercent * reserveCount;
            }
        } else {
            if (leftSB.currPercent + reservePercent <= 1 && leftSB.currPercent + reservePercent > rightSB.currPercent) {
                rightSB.currPercent = leftSB.currPercent + reservePercent;
            } else if (rightSB.currPercent - reservePercent >= 0 && rightSB.currPercent - reservePercent < leftSB.currPercent) {
                leftSB.currPercent = rightSB.currPercent - reservePercent;
            }
        }
        thumbWidth = 40;
        startPosition = (int) getResources().getDimension(R.dimen.seekMargin);
        invalidate();
    }

    public float[] getCurrentRange() {
        float range = maxValue - minValue;
        return new float[]{-offsetValue + minValue + range * leftSB.currPercent,
                -offsetValue + minValue + range * rightSB.currPercent};
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (heightSize * 1.8f > widthSize) {
            setMeasuredDimension(widthSize, (int) (widthSize / 1.8f));
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int seekBarRadius = h / 2;
        lineLeft = startPosition;
        lineRight = w - startPosition;
        lineTop = thumbWidth - 2;
        lineBottom = thumbWidth + 2;
        line.set(lineLeft, lineTop, lineRight, lineBottom);

        lineWidth = lineRight - lineLeft;
        lineHeight = lineBottom - lineTop;
        leftSB.onSizeChanged(seekBarRadius, seekBarRadius, h, lineWidth, cellsCount > 1, seekBarResId, getContext());
        rightSB.onSizeChanged(seekBarRadius, seekBarRadius, h, lineWidth, cellsCount > 1, seekBarResId, getContext());

        if (cellsCount == 1) {
            rightSB.left += thumbWidth;
            rightSB.right += thumbWidth;
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(colorLineUnSelected);
        paint.setAntiAlias(true);
        //画小圆点
        if (cellsPercent > 0) {
            paint.setStrokeWidth(10);
            for (int i = 0; i <= cellsCount; i++) {
                canvas.drawCircle(lineLeft + i * cellsPercent * lineWidth, lineTop + lineHeight / 2, 10, paint);
            }
        }
        //画圆角线
        canvas.drawRoundRect(line, lineHeight, lineHeight, paint);

        paint.setColor(colorLineEdge);
        //画字体
        for (int i = 0; i <= cellsCount; i++) {
            paint.setTextSize(getResources().getDimension(R.dimen.considerTextSize));
            paint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            canvas.drawText(textSummary[i], lineLeft + i * cellsPercent * lineWidth - paint.measureText(textSummary[i]) / 2, lineBottom + 10 + getResources().getDimension(R.dimen.considerTextMarginSize), paint);
        }

        //设置选中状态的画笔颜色
        paint.setColor(colorLineSelected);
        //画选中的小圆点
        for (int i = (int) (leftSB.currPercent * cellCount); i <= (int) (rightSB.currPercent * cellCount); i++) {
            canvas.drawCircle(lineLeft + i * cellsPercent * lineWidth, lineTop + lineHeight / 2, 10, paint);
        }
        //画选中的线
        canvas.drawRect(leftSB.left + thumbWidth / 2 + leftSB.lineWidth * leftSB.currPercent, lineTop,
                rightSB.left + thumbWidth / 2 + rightSB.lineWidth * rightSB.currPercent, lineBottom, paint);

        leftSB.draw(canvas);
        rightSB.draw(canvas);
    }

    public void setCanTouch(boolean isCanTouch) {
        this.isCanTouch = isCanTouch;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isCanTouch) {
            return true;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                boolean touchResult = false;
                if (rightSB.currPercent >= 1 && leftSB.collide(event)) {
                    currTouch = leftSB;
                    touchResult = true;
                } else if (rightSB.collide(event)) {
                    currTouch = rightSB;
                    touchResult = true;
                } else if (leftSB.collide(event)) {
                    currTouch = leftSB;
                    touchResult = true;
                }
                return touchResult;
            case MotionEvent.ACTION_MOVE:
                float percent;
                float x = event.getX();

                currTouch.material = currTouch.material >= 1 ? 1 : currTouch.material + 0.1f;

                if (currTouch == leftSB) {
                    if (cellsCount > 1) {
                        if (x < lineLeft) {
                            percent = 0;
                        } else {
                            percent = (x - lineLeft) * 1f / (lineWidth);
                        }
                        int touchLeftCellsValue = Math.round(percent / cellsPercent);
                        int currRightCellsValue = Math.round(rightSB.currPercent / cellsPercent);
                        percent = touchLeftCellsValue * cellsPercent;

                        while (touchLeftCellsValue > currRightCellsValue - reserveCount) {
                            touchLeftCellsValue--;
                            if (touchLeftCellsValue < 0) break;
                            percent = touchLeftCellsValue * cellsPercent;
                        }
                    } else {
                        if (x < lineLeft) {
                            percent = 0;
                        } else {
                            percent = (x - lineLeft) * 1f / (lineWidth - thumbWidth);
                        }

                        if (percent > rightSB.currPercent - reservePercent) {
                            percent = rightSB.currPercent - reservePercent;
                        }
                    }
                    leftSB.slide(percent);
                } else if (currTouch == rightSB) {
                    if (cellsCount > 1) {
                        if (x > lineRight) {
                            percent = 1;
                        } else {
                            percent = (x - lineLeft) * 1f / (lineWidth);
                        }
                        int touchRightCellsValue = Math.round(percent / cellsPercent);
                        int currLeftCellsValue = Math.round(leftSB.currPercent / cellsPercent);
                        percent = touchRightCellsValue * cellsPercent;

                        while (touchRightCellsValue < currLeftCellsValue + reserveCount) {
                            touchRightCellsValue++;
                            if (touchRightCellsValue > maxValue - minValue) break;
                            percent = touchRightCellsValue * cellsPercent;
                        }
                    } else {
                        if (x > lineRight) {
                            percent = 1;
                        } else {
                            percent = (x - lineLeft - thumbWidth) * 1f / (lineWidth - thumbWidth);
                        }
                        if (percent < leftSB.currPercent + reservePercent) {
                            percent = leftSB.currPercent + reservePercent;
                        }
                    }
                    rightSB.slide(percent);
                }

                if (callback != null) {
                    float[] result = getCurrentRange();
                    callback.onRangeChanged(this, result[0], result[1]);
                }
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                currTouch.materialRestore();

                if (callback != null) {
                    float[] result = getCurrentRange();
                    callback.onRangeChanged(this, result[0], result[1]);
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.minValue = minValue - offsetValue;
        ss.maxValue = maxValue - offsetValue;
        ss.reserveValue = reserveValue;
        ss.cellsCount = cellsCount;
        float[] results = getCurrentRange();
        ss.currSelectedMin = results[0];
        ss.currSelectedMax = results[1];
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        float min = ss.minValue;
        float max = ss.maxValue;
        float reserve = ss.reserveValue;
        int cells = ss.cellsCount;
        setRules(min, max, reserve, cells);
        float currSelectedMin = ss.currSelectedMin;
        float currSelectedMax = ss.currSelectedMax;
        setValue(currSelectedMin, currSelectedMax);
    }

    private class SavedState extends BaseSavedState {
        private float minValue;
        private float maxValue;
        private float reserveValue;
        private int cellsCount;
        private float currSelectedMin;
        private float currSelectedMax;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            minValue = in.readFloat();
            maxValue = in.readFloat();
            reserveValue = in.readFloat();
            cellsCount = in.readInt();
            currSelectedMin = in.readFloat();
            currSelectedMax = in.readFloat();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeFloat(minValue);
            out.writeFloat(maxValue);
            out.writeFloat(reserveValue);
            out.writeInt(cellsCount);
            out.writeFloat(currSelectedMin);
            out.writeFloat(currSelectedMax);
        }
    }
}
