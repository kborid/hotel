package com.huicheng.hotel.android.ui.custom.calendar;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.format.Time;
import android.view.MotionEvent;
import android.view.View;

import com.huicheng.hotel.android.R;
import com.prj.sdk.util.LogUtil;

import java.security.InvalidParameterException;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

/**
 * @author kborid
 * @date 2016/11/18 0018
 */
class SimpleMonthView extends View {

    private static final String TAG = "SimpleMonthView";

    public static final String VIEW_PARAMS_HEIGHT = "height";
    public static final String VIEW_PARAMS_MONTH = "month";
    public static final String VIEW_PARAMS_YEAR = "year";
    public static final String VIEW_PARAMS_SELECTED_BEGIN_DAY = "selected_begin_day";
    public static final String VIEW_PARAMS_SELECTED_LAST_DAY = "selected_last_day";
    public static final String VIEW_PARAMS_SELECTED_BEGIN_MONTH = "selected_begin_month";
    public static final String VIEW_PARAMS_SELECTED_LAST_MONTH = "selected_last_month";
    public static final String VIEW_PARAMS_SELECTED_BEGIN_YEAR = "selected_begin_year";
    public static final String VIEW_PARAMS_SELECTED_LAST_YEAR = "selected_last_year";
    public static final String VIEW_PARAMS_WEEK_START = "week_start";

    private static final int SELECTED_CIRCLE_ALPHA = 128;
    private static int DEFAULT_HEIGHT = 45;
    private static final int DEFAULT_NUM_ROWS = 6;
    private static int DAY_SELECTED_CIRCLE_SIZE;
    private static int DAY_SELECTED_CIRCLE_SIZE_X;
    private static int MINI_DAY_NUMBER_TEXT_SIZE;
    private static int MIN_HEIGHT = 20;
    private static int MONTH_DAY_LABEL_TEXT_SIZE;
    private static int MONTH_HEADER_SIZE;
    private static int MONTH_LABEL_TEXT_SIZE;

    private int mPadding = 0;

    private String mDayOfWeekTypeface;
    private String mMonthTitleTypeface;

    private Paint mMonthTitlePaint;
    private Paint mMonthDayLabelPaint;
    private Paint mMonthNumPaint;
    private Paint mChinesePaint;
    private Paint mIntoLeaveDayPaint;
    private Paint mSelectedCircleBLPaint;
    private Paint mSelectedCirclePaint;
    private Paint mTodayPaint;
    private int mCurrentDayCircleColor;
    private int mCurrentDayTextColor;
    private int mMonthTextColor;
    private int mDayTextColor;
    private int mDayNumColor;
    private int mMonthTitleBGColor;
    private int mPreviousDayColor;
    private int mSelectedDaysColor;

    private boolean mHasToday = false;
    private boolean mIsPrev = false;
    private boolean isShowChineseDate = false;
    private int mSelectedBeginDay = -1;
    private int mSelectedLastDay = -1;
    private int mSelectedBeginMonth = -1;
    private int mSelectedLastMonth = -1;
    private int mSelectedBeginYear = -1;
    private int mSelectedLastYear = -1;
    private int mWeekStart = 1;
    private int mNumDays = 7;
    private int mNumCells = mNumDays;
    private int mDayOfWeekStart = 0;
    private int mMonth;
    private Boolean mDrawRect;
    private int mRowHeight = DEFAULT_HEIGHT;
    private int mWidth;
    private int mYear;
    final Time today;

    private final Calendar mCalendar;
    private final Calendar mDayLabelCalendar;
    private final Boolean isPrevDayEnabled;
    private int mNumRows = DEFAULT_NUM_ROWS;

    private DateFormatSymbols mDateFormatSymbols = new DateFormatSymbols();
    private OnDayClickListener mOnDayClickListener;
    private CalendarUtils cal;

    public SimpleMonthView(Context context, TypedArray typedArray) {
        super(context);

        Resources resources = context.getResources();
        mDayLabelCalendar = Calendar.getInstance();
        mCalendar = Calendar.getInstance();
        today = new Time(Time.getCurrentTimezone());
        today.setToNow();
//        mDayOfWeekTypeface = resources.getString(R.string.sans_serif);
//        mMonthTitleTypeface = resources.getString(R.string.sans_serif);
        mCurrentDayCircleColor = typedArray.getColor(R.styleable.CustomCalendarView_colorCurrentDayCircle, resources.getColor(R.color.indicatorColor));
        mCurrentDayTextColor = typedArray.getColor(R.styleable.CustomCalendarView_colorCurrentDay, resources.getColor(R.color.mainColorAccent));
        mMonthTextColor = typedArray.getColor(R.styleable.CustomCalendarView_colorMonthName, resources.getColor(R.color.mainColorAccent));
        mDayTextColor = typedArray.getColor(R.styleable.CustomCalendarView_colorDayName, resources.getColor(R.color.mainColorAccent));
        mDayNumColor = typedArray.getColor(R.styleable.CustomCalendarView_colorNormalDay, resources.getColor(R.color.mainColorAccent));
        mPreviousDayColor = typedArray.getColor(R.styleable.CustomCalendarView_colorPreviousDay, resources.getColor(R.color.lableColor));
        mSelectedDaysColor = typedArray.getColor(R.styleable.CustomCalendarView_colorSelectedDayBackground, resources.getColor(R.color.selDayBackground));
        mMonthTitleBGColor = typedArray.getColor(R.styleable.CustomCalendarView_colorSelectedDayText, resources.getColor(R.color.white));
        mDrawRect = typedArray.getBoolean(R.styleable.CustomCalendarView_drawRoundRect, false);
        MINI_DAY_NUMBER_TEXT_SIZE = typedArray.getDimensionPixelSize(R.styleable.CustomCalendarView_textSizeDay, resources.getDimensionPixelSize(R.dimen.text_size_day));
        MONTH_LABEL_TEXT_SIZE = typedArray.getDimensionPixelSize(R.styleable.CustomCalendarView_textSizeMonth, resources.getDimensionPixelSize(R.dimen.text_size_month));
        MONTH_DAY_LABEL_TEXT_SIZE = typedArray.getDimensionPixelSize(R.styleable.CustomCalendarView_textSizeDayName, resources.getDimensionPixelSize(R.dimen.text_size_day_name));
        MONTH_HEADER_SIZE = typedArray.getDimensionPixelOffset(R.styleable.CustomCalendarView_headerMonthHeight, resources.getDimensionPixelOffset(R.dimen.header_month_height));
        DAY_SELECTED_CIRCLE_SIZE = typedArray.getDimensionPixelSize(R.styleable.CustomCalendarView_selectedDayRadius, resources.getDimensionPixelOffset(R.dimen.selected_day_radius));
        DAY_SELECTED_CIRCLE_SIZE_X = typedArray.getDimensionPixelSize(R.styleable.CustomCalendarView_selectedDayRadius, resources.getDimensionPixelOffset(R.dimen.selected_day_x));
        mRowHeight = ((typedArray.getDimensionPixelSize(R.styleable.CustomCalendarView_calendarHeight, resources.getDimensionPixelOffset(R.dimen.calendar_height)) - MONTH_HEADER_SIZE) / 6);
        isPrevDayEnabled = typedArray.getBoolean(R.styleable.CustomCalendarView_enablePreviousDay, true);

        initView();
    }

    public void setShowChineseDate(boolean flag) {
        isShowChineseDate = flag;
    }

    private int calculateNumRows() {
        int offset = findDayOffset();
        int dividend = (offset + mNumCells) / mNumDays;
        int remainder = (offset + mNumCells) % mNumDays;
        return (dividend + (remainder > 0 ? 1 : 0));
    }

    // 每个月画星期栏
    private void drawMonthDayLabels(Canvas canvas) {
        int y = MONTH_HEADER_SIZE - (MONTH_DAY_LABEL_TEXT_SIZE / 2);
        int dayWidthHalf = (mWidth - mPadding * 2) / (mNumDays * 2);

        for (int i = 0; i < mNumDays; i++) {
            int calendarDay = (i + mWeekStart) % mNumDays;
            int x = (2 * i + 1) * dayWidthHalf + mPadding;
            mDayLabelCalendar.set(Calendar.DAY_OF_WEEK, calendarDay);
            canvas.drawText(mDateFormatSymbols.getShortWeekdays()[mDayLabelCalendar.get(Calendar.DAY_OF_WEEK)].toUpperCase(Locale.getDefault()), x, y, mMonthDayLabelPaint);
        }
    }

    private int findDayOffset() {
        return (mDayOfWeekStart < mWeekStart ? (mDayOfWeekStart + mNumDays) : mDayOfWeekStart)
                - mWeekStart;
    }

    private void onDayClick(SimpleMonthAdapter.CalendarDay calendarDay) {
        if (mOnDayClickListener != null && (isPrevDayEnabled || !((calendarDay.month == today.month) && (calendarDay.year == today.year) && calendarDay.day < today.monthDay))) {
            mOnDayClickListener.onDayClick(this, calendarDay);
        }
    }

    private boolean sameDay(int monthDay, Time time) {
        return (mYear == time.year) && (mMonth == time.month) && (monthDay == time.monthDay);
    }

    private boolean prevDay(int monthDay, Time time) {
        return ((mYear < time.year)) || (mYear == time.year && mMonth < time.month) || (mMonth == time.month && monthDay < time.monthDay);
    }

    private void drawMonthTitle(Canvas canvas) {
        int y = MONTH_HEADER_SIZE / 3 * 2;
        String monthStr = /*mYear + "年" + */CalendarUtils.getMonthStringByNum(mMonth);
        canvas.drawText(monthStr, mMonthTitlePaint.measureText(monthStr) / 2 + 40, y, mMonthTitlePaint);
    }

    private void drawMonthNums(Canvas canvas) {
        int y = MONTH_HEADER_SIZE + mRowHeight / 2;
        int paddingDay = (mWidth - 2 * mPadding) / (2 * mNumDays);
        int dayOffset = findDayOffset();
        int day = 1;

        while (day <= mNumCells) {
            int x = paddingDay * (1 + dayOffset * 2) + mPadding;

            if (mHasToday && sameDay(day, today)) {
                mMonthNumPaint.setColor(mCurrentDayTextColor);
                mChinesePaint.setColor(mCurrentDayTextColor);
                if (mSelectedBeginYear == mYear && mSelectedBeginMonth == mMonth && mSelectedBeginDay == day) {
                    mTodayPaint.setColor(getResources().getColor(R.color.transparent));
                } else {
                    mTodayPaint.setColor(mCurrentDayCircleColor);
                }
                canvas.drawCircle(x, y - MINI_DAY_NUMBER_TEXT_SIZE / 3, DAY_SELECTED_CIRCLE_SIZE, mTodayPaint);
            } else {
                mMonthNumPaint.setColor(mDayNumColor);
                mChinesePaint.setColor(mDayNumColor);
            }

            if (mSelectedBeginYear == mYear && mSelectedBeginMonth == mMonth && mSelectedBeginDay == day) {
//                if (sameDay(day, today)) {
//                    mTodayPaint.setColor(getResources().getColor(R.color.red));
//                    canvas.drawCircle(x, y - MINI_DAY_NUMBER_TEXT_SIZE / 3, DAY_SELECTED_CIRCLE_SIZE, mTodayPaint);
//                }
                mMonthNumPaint.setColor(mMonthTitleBGColor);
                mChinesePaint.setColor(mMonthTitleBGColor);
                canvas.drawCircle(x, y - MINI_DAY_NUMBER_TEXT_SIZE / 3, DAY_SELECTED_CIRCLE_SIZE, mSelectedCircleBLPaint);
//                canvas.drawText("入住", x, y - mRowHeight / 2, mIntoLeaveDayPaint);

                //入住选择背景
                if (mSelectedLastYear != -1 && mSelectedLastMonth != -1 && mSelectedLastDay != -1) {
                    canvas.drawRect(new RectF(x, y - DAY_SELECTED_CIRCLE_SIZE - MINI_DAY_NUMBER_TEXT_SIZE / 3, x + DAY_SELECTED_CIRCLE_SIZE_X / 3 * 4, y + DAY_SELECTED_CIRCLE_SIZE - MINI_DAY_NUMBER_TEXT_SIZE / 3), mSelectedCircleBLPaint);
                }
//                canvas.drawText(String.format("%d", mSelectedBeginDay), inx, yy, mMonthNumPaint);
            }
            if (mSelectedLastYear == mYear && mSelectedLastMonth == mMonth && mSelectedLastDay == day) {
//                if (sameDay(day, today)) {
//                    mTodayPaint.setColor(mSelectedDaysColor);
//                    canvas.drawCircle(x, y - MINI_DAY_NUMBER_TEXT_SIZE / 3, DAY_SELECTED_CIRCLE_SIZE, mTodayPaint);
//                }

                mMonthNumPaint.setColor(mMonthTitleBGColor);
                mChinesePaint.setColor(mMonthTitleBGColor);
                canvas.drawCircle(x, y - MINI_DAY_NUMBER_TEXT_SIZE / 3, DAY_SELECTED_CIRCLE_SIZE, mSelectedCircleBLPaint);
//                canvas.drawText("离开", x, y - mRowHeight / 2, mIntoLeaveDayPaint);

                //离开选择背景
                canvas.drawRect(new RectF(x - DAY_SELECTED_CIRCLE_SIZE_X / 3 * 4, y - DAY_SELECTED_CIRCLE_SIZE - MINI_DAY_NUMBER_TEXT_SIZE / 3, x, y + DAY_SELECTED_CIRCLE_SIZE - MINI_DAY_NUMBER_TEXT_SIZE / 3), mSelectedCircleBLPaint);
            }

            if ((mSelectedBeginDay != -1 && mSelectedLastDay != -1 && mSelectedBeginYear == mSelectedLastYear && mSelectedBeginYear == mYear) &&
                    (((mMonth == mSelectedBeginMonth && mSelectedLastMonth == mSelectedBeginMonth) && ((mSelectedBeginDay < mSelectedLastDay && day > mSelectedBeginDay && day < mSelectedLastDay) || (mSelectedBeginDay > mSelectedLastDay && day < mSelectedBeginDay && day > mSelectedLastDay))) ||
                            ((mSelectedBeginMonth < mSelectedLastMonth && mMonth == mSelectedBeginMonth && day > mSelectedBeginDay) || (mSelectedBeginMonth < mSelectedLastMonth && mMonth == mSelectedLastMonth && day < mSelectedLastDay)) ||
                            ((mSelectedBeginMonth > mSelectedLastMonth && mMonth == mSelectedBeginMonth && day < mSelectedBeginDay) || (mSelectedBeginMonth > mSelectedLastMonth && mMonth == mSelectedLastMonth && day > mSelectedLastDay)))) {
                mMonthNumPaint.setColor(mMonthTitleBGColor);
                mChinesePaint.setColor(mMonthTitleBGColor);
                LogUtil.d(TAG, "test 001");
//                canvas.drawCircle(x, y - MINI_DAY_NUMBER_TEXT_SIZE / 3, DAY_SELECTED_CIRCLE_SIZE, mSelectedCirclePaint);
                canvas.drawRect(new RectF(x - DAY_SELECTED_CIRCLE_SIZE_X, y - DAY_SELECTED_CIRCLE_SIZE - MINI_DAY_NUMBER_TEXT_SIZE / 3, x + DAY_SELECTED_CIRCLE_SIZE_X / 3 * 4, y + DAY_SELECTED_CIRCLE_SIZE - MINI_DAY_NUMBER_TEXT_SIZE / 3), mSelectedCircleBLPaint);
            }

            if ((mSelectedBeginDay != -1 && mSelectedLastDay != -1 && mSelectedBeginYear != mSelectedLastYear && ((mSelectedBeginYear == mYear && mMonth == mSelectedBeginMonth) || (mSelectedLastYear == mYear && mMonth == mSelectedLastMonth)) &&
                    (((mSelectedBeginMonth < mSelectedLastMonth && mMonth == mSelectedBeginMonth && day < mSelectedBeginDay) || (mSelectedBeginMonth < mSelectedLastMonth && mMonth == mSelectedLastMonth && day > mSelectedLastDay)) ||
                            ((mSelectedBeginMonth > mSelectedLastMonth && mMonth == mSelectedBeginMonth && day > mSelectedBeginDay) || (mSelectedBeginMonth > mSelectedLastMonth && mMonth == mSelectedLastMonth && day < mSelectedLastDay))))) {
                mMonthNumPaint.setColor(mMonthTitleBGColor);
                mChinesePaint.setColor(mMonthTitleBGColor);
                LogUtil.d(TAG, "test 002");
//                canvas.drawCircle(x, y - MINI_DAY_NUMBER_TEXT_SIZE / 3, DAY_SELECTED_CIRCLE_SIZE, mSelectedCirclePaint);
                canvas.drawRect(new RectF(x - DAY_SELECTED_CIRCLE_SIZE_X, y - DAY_SELECTED_CIRCLE_SIZE - MINI_DAY_NUMBER_TEXT_SIZE / 3, x + DAY_SELECTED_CIRCLE_SIZE_X / 3 * 4, y + DAY_SELECTED_CIRCLE_SIZE - MINI_DAY_NUMBER_TEXT_SIZE / 3), mSelectedCircleBLPaint);
            }

            if ((mSelectedBeginDay != -1 && mSelectedLastDay != -1 && mSelectedBeginYear == mSelectedLastYear && mYear == mSelectedBeginYear) &&
                    ((mMonth > mSelectedBeginMonth && mMonth < mSelectedLastMonth && mSelectedBeginMonth < mSelectedLastMonth) ||
                            (mMonth < mSelectedBeginMonth && mMonth > mSelectedLastMonth && mSelectedBeginMonth > mSelectedLastMonth))) {
                mMonthNumPaint.setColor(mMonthTitleBGColor);
                mChinesePaint.setColor(mMonthTitleBGColor);
                LogUtil.d(TAG, "test 003");
//                canvas.drawCircle(x, y - MINI_DAY_NUMBER_TEXT_SIZE / 3, DAY_SELECTED_CIRCLE_SIZE, mSelectedCirclePaint);
                canvas.drawRect(new RectF(x - DAY_SELECTED_CIRCLE_SIZE_X, y - DAY_SELECTED_CIRCLE_SIZE - MINI_DAY_NUMBER_TEXT_SIZE / 3, x + DAY_SELECTED_CIRCLE_SIZE_X / 3 * 4, y + DAY_SELECTED_CIRCLE_SIZE - MINI_DAY_NUMBER_TEXT_SIZE / 3), mSelectedCircleBLPaint);
            }

            if ((mSelectedBeginDay != -1 && mSelectedLastDay != -1 && mSelectedBeginYear != mSelectedLastYear)
                    && ((mSelectedBeginYear < mSelectedLastYear && ((mMonth > mSelectedBeginMonth && mYear == mSelectedBeginYear)
                    || (mMonth < mSelectedLastMonth && mYear == mSelectedLastYear)))
                    || (mSelectedBeginYear > mSelectedLastYear && ((mMonth < mSelectedBeginMonth && mYear == mSelectedBeginYear)
                    || (mMonth > mSelectedLastMonth && mYear == mSelectedLastYear))))) {
                mMonthNumPaint.setColor(mMonthTitleBGColor);
                mChinesePaint.setColor(mMonthTitleBGColor);
                LogUtil.d(TAG, "test 004");
//                canvas.drawCircle(x, y - MINI_DAY_NUMBER_TEXT_SIZE / 3, DAY_SELECTED_CIRCLE_SIZE, mSelectedCirclePaint);
                canvas.drawRect(new RectF(x - DAY_SELECTED_CIRCLE_SIZE_X, y - DAY_SELECTED_CIRCLE_SIZE - MINI_DAY_NUMBER_TEXT_SIZE / 3, x + DAY_SELECTED_CIRCLE_SIZE_X / 3 * 4, y + DAY_SELECTED_CIRCLE_SIZE - MINI_DAY_NUMBER_TEXT_SIZE / 3), mSelectedCircleBLPaint);
            }

            if (!isPrevDayEnabled && prevDay(day, today) && today.month == mMonth && today.year == mYear) {
                mMonthNumPaint.setColor(mPreviousDayColor);
                mChinesePaint.setColor(mPreviousDayColor);
            }

            if (isShowChineseDate) {
                canvas.drawText(String.format("%d", day), x, y - MINI_DAY_NUMBER_TEXT_SIZE / 2, mMonthNumPaint);
                canvas.drawText(cal.getChineseDay(mYear, mMonth + 1, day), x, y + MINI_DAY_NUMBER_TEXT_SIZE / 2, mChinesePaint);
            } else {
                canvas.drawText(String.format("%d", day), x, y, mMonthNumPaint);
            }

            dayOffset++;
            if (dayOffset == mNumDays) {
                dayOffset = 0;
                y += mRowHeight;
            }
            day++;
        }
    }

    public SimpleMonthAdapter.CalendarDay getDayFromLocation(float x, float y) {
        LogUtil.d(TAG, "getDayFromLocation()");
        int padding = mPadding;
        if ((x < padding) || (x > mWidth - mPadding)) {
            return null;
        }

        int yDay = (int) (y - MONTH_HEADER_SIZE) / mRowHeight;
        int day = 1 + ((int) ((x - padding) * mNumDays / (mWidth - padding - mPadding)) - findDayOffset()) + yDay * mNumDays;

        if (mMonth > 11 || mMonth < 0 || CalendarUtils.daysInGregorianMonth(mYear, mMonth + 1) < day || day < 1)
            return null;

        return new SimpleMonthAdapter.CalendarDay(mYear, mMonth, day);
    }

    private void initView() {
        mMonthTitlePaint = new Paint();
//        mMonthTitlePaint.setFakeBoldText(true);
        mMonthTitlePaint.setAntiAlias(true);
        mMonthTitlePaint.setTextSize(MONTH_LABEL_TEXT_SIZE);
        mMonthTitlePaint.setColor(mMonthTextColor);
        mMonthTitlePaint.setTextAlign(Paint.Align.CENTER);
        mMonthTitlePaint.setStyle(Paint.Style.FILL);

        mIntoLeaveDayPaint = new Paint();
        mIntoLeaveDayPaint.setAntiAlias(true);
        mIntoLeaveDayPaint.setColor(mMonthTextColor);
        mIntoLeaveDayPaint.setTextSize(getResources().getDimension(R.dimen.common_10sp));
        mIntoLeaveDayPaint.setTextAlign(Paint.Align.CENTER);
        mIntoLeaveDayPaint.setStyle(Paint.Style.FILL);

        mSelectedCircleBLPaint = new Paint();
        mSelectedCircleBLPaint.setFakeBoldText(true);
        mSelectedCircleBLPaint.setAntiAlias(true);
        mSelectedCircleBLPaint.setColor(mSelectedDaysColor);
        mSelectedCircleBLPaint.setTextAlign(Paint.Align.CENTER);
        mSelectedCircleBLPaint.setStyle(Paint.Style.FILL);

        mSelectedCirclePaint = new Paint();
        mSelectedCirclePaint.setFakeBoldText(true);
        mSelectedCirclePaint.setAntiAlias(true);
        mSelectedCirclePaint.setColor(mSelectedDaysColor);
        mSelectedCirclePaint.setTextAlign(Paint.Align.CENTER);
        mSelectedCirclePaint.setStyle(Paint.Style.FILL);
        mSelectedCirclePaint.setAlpha(SELECTED_CIRCLE_ALPHA);

        mMonthDayLabelPaint = new Paint();
        mMonthDayLabelPaint.setAntiAlias(true);
        mMonthDayLabelPaint.setTextSize(MONTH_DAY_LABEL_TEXT_SIZE);
        mMonthDayLabelPaint.setColor(mDayTextColor);
        mMonthDayLabelPaint.setTypeface(Typeface.create(mDayOfWeekTypeface, Typeface.NORMAL));
        mMonthDayLabelPaint.setStyle(Paint.Style.FILL);
        mMonthDayLabelPaint.setTextAlign(Paint.Align.CENTER);
        mMonthDayLabelPaint.setFakeBoldText(true);

        mMonthNumPaint = new Paint();
        mMonthNumPaint.setAntiAlias(true);
        mMonthNumPaint.setTextSize(MINI_DAY_NUMBER_TEXT_SIZE);
        mMonthNumPaint.setStyle(Paint.Style.FILL);
        mMonthNumPaint.setTextAlign(Paint.Align.CENTER);
        mMonthNumPaint.setFakeBoldText(false);

        mChinesePaint = new Paint();
        mChinesePaint.setAntiAlias(true);
        mChinesePaint.setTextSize(getResources().getDimension(R.dimen.common_10sp));
        mChinesePaint.setStyle(Paint.Style.FILL);
        mChinesePaint.setTextAlign(Paint.Align.CENTER);
        mChinesePaint.setFakeBoldText(false);

        mTodayPaint = new Paint();
        mTodayPaint.setAntiAlias(true);
        mTodayPaint.setColor(getResources().getColor(R.color.indicatorColor));
        mTodayPaint.setStyle(Paint.Style.STROKE);
        mTodayPaint.setStrokeWidth(2f);

        cal = new CalendarUtils();
    }

    protected void onDraw(Canvas canvas) {
        LogUtil.d(TAG, "onDraw()");
        drawMonthTitle(canvas);
//        drawMonthDayLabels(canvas);
        drawMonthNums(canvas);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), mRowHeight * mNumRows + MONTH_HEADER_SIZE);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWidth = w;
    }

    public boolean onTouchEvent(MotionEvent event) {
        LogUtil.d(TAG, "onTouchEvent()");
        if (event.getAction() == MotionEvent.ACTION_UP) {
            LogUtil.d(TAG, "onTouchEvent(MotionEvent.ACTION_UP)");
            SimpleMonthAdapter.CalendarDay calendarDay = getDayFromLocation(event.getX(), event.getY());
            if (calendarDay != null) {
                LogUtil.d(TAG, "OnDayClick() in OnTouchEvent()");
                onDayClick(calendarDay);
            }
        }
        return true;
    }

    public void reuse() {
        mNumRows = DEFAULT_NUM_ROWS;
        requestLayout();
    }

    public void setMonthParams(HashMap<String, Integer> params) {
        if (!params.containsKey(VIEW_PARAMS_MONTH) && !params.containsKey(VIEW_PARAMS_YEAR)) {
            throw new InvalidParameterException("You must specify month and year for this view");
        }
        setTag(params);

        if (params.containsKey(VIEW_PARAMS_HEIGHT)) {
            mRowHeight = params.get(VIEW_PARAMS_HEIGHT);
            if (mRowHeight < MIN_HEIGHT) {
                mRowHeight = MIN_HEIGHT;
            }
        }
        if (params.containsKey(VIEW_PARAMS_SELECTED_BEGIN_DAY)) {
            mSelectedBeginDay = params.get(VIEW_PARAMS_SELECTED_BEGIN_DAY);
        }
        if (params.containsKey(VIEW_PARAMS_SELECTED_LAST_DAY)) {
            mSelectedLastDay = params.get(VIEW_PARAMS_SELECTED_LAST_DAY);
        }
        if (params.containsKey(VIEW_PARAMS_SELECTED_BEGIN_MONTH)) {
            mSelectedBeginMonth = params.get(VIEW_PARAMS_SELECTED_BEGIN_MONTH);
        }
        if (params.containsKey(VIEW_PARAMS_SELECTED_LAST_MONTH)) {
            mSelectedLastMonth = params.get(VIEW_PARAMS_SELECTED_LAST_MONTH);
        }
        if (params.containsKey(VIEW_PARAMS_SELECTED_BEGIN_YEAR)) {
            mSelectedBeginYear = params.get(VIEW_PARAMS_SELECTED_BEGIN_YEAR);
        }
        if (params.containsKey(VIEW_PARAMS_SELECTED_LAST_YEAR)) {
            mSelectedLastYear = params.get(VIEW_PARAMS_SELECTED_LAST_YEAR);
        }

        mMonth = params.get(VIEW_PARAMS_MONTH);
        mYear = params.get(VIEW_PARAMS_YEAR);

        mHasToday = false;
        mCalendar.set(Calendar.MONTH, mMonth);
        mCalendar.set(Calendar.YEAR, mYear);
        mCalendar.set(Calendar.DAY_OF_MONTH, 1);
        mDayOfWeekStart = mCalendar.get(Calendar.DAY_OF_WEEK);

        if (params.containsKey(VIEW_PARAMS_WEEK_START)) {
            mWeekStart = params.get(VIEW_PARAMS_WEEK_START);
        } else {
            mWeekStart = mCalendar.getFirstDayOfWeek();
        }

        mNumCells = CalendarUtils.daysInGregorianMonth(mYear, mMonth + 1);
        for (int i = 0; i < mNumCells; i++) {
            final int day = i + 1;
            if (sameDay(day, today)) {
                mHasToday = true;
            }

            mIsPrev = prevDay(day, today);
        }

        mNumRows = calculateNumRows();
    }

    public void setOnDayClickListener(OnDayClickListener onDayClickListener) {
        mOnDayClickListener = onDayClickListener;
    }

    public interface OnDayClickListener {
        void onDayClick(SimpleMonthView simpleMonthView, SimpleMonthAdapter.CalendarDay calendarDay);
    }
}
