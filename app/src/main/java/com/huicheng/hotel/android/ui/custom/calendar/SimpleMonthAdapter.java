package com.huicheng.hotel.android.ui.custom.calendar;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.huicheng.hotel.android.R;
import com.prj.sdk.util.LogUtil;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * @author kborid
 * @date 2016/11/18 0018
 */
public class SimpleMonthAdapter extends RecyclerView.Adapter<SimpleMonthAdapter.ViewHolder> implements SimpleMonthView.OnDayClickListener {

    private final String TAG = getClass().getSimpleName();

    protected static final int MONTHS_IN_YEAR = 12;
    private static final int SHOW_MONTHS = 6;

    private final TypedArray typedArray;
    private final Context mContext;
    private final CalendarSelectedListener mController;
    private final Calendar calendar;
    private final SelectedDays<CalendarDay> selectedDays;

    public SimpleMonthAdapter(Context context, CalendarSelectedListener datePickerController, TypedArray typedArray) {
        this.typedArray = typedArray;
        calendar = Calendar.getInstance();
        selectedDays = new SelectedDays<>();
        mContext = context;
        mController = datePickerController;
        init();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        final SimpleMonthView simpleMonthView = new SimpleMonthView(mContext, typedArray);
        return new ViewHolder(simpleMonthView, this);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        final SimpleMonthView v = viewHolder.simpleMonthView;
        final HashMap<String, Integer> drawingParams = new HashMap<>();
        int month;
        int year;

        month = (calendar.get(Calendar.MONTH) + (position % MONTHS_IN_YEAR)) % MONTHS_IN_YEAR;
        year = position / MONTHS_IN_YEAR + calendar.get(Calendar.YEAR) + ((calendar.get(Calendar.MONTH) + (position % MONTHS_IN_YEAR)) / MONTHS_IN_YEAR);

        int selectedFirstDay = -1;
        int selectedLastDay = -1;
        int selectedFirstMonth = -1;
        int selectedLastMonth = -1;
        int selectedFirstYear = -1;
        int selectedLastYear = -1;

        if (selectedDays.getFirst() != null) {
            selectedFirstDay = selectedDays.getFirst().day;
            selectedFirstMonth = selectedDays.getFirst().month;
            selectedFirstYear = selectedDays.getFirst().year;
        }

        if (selectedDays.getLast() != null) {
            selectedLastDay = selectedDays.getLast().day;
            selectedLastMonth = selectedDays.getLast().month;
            selectedLastYear = selectedDays.getLast().year;
        }

        v.reuse();

        drawingParams.put(SimpleMonthView.VIEW_PARAMS_SELECTED_BEGIN_YEAR, selectedFirstYear);
        drawingParams.put(SimpleMonthView.VIEW_PARAMS_SELECTED_LAST_YEAR, selectedLastYear);
        drawingParams.put(SimpleMonthView.VIEW_PARAMS_SELECTED_BEGIN_MONTH, selectedFirstMonth);
        drawingParams.put(SimpleMonthView.VIEW_PARAMS_SELECTED_LAST_MONTH, selectedLastMonth);
        drawingParams.put(SimpleMonthView.VIEW_PARAMS_SELECTED_BEGIN_DAY, selectedFirstDay);
        drawingParams.put(SimpleMonthView.VIEW_PARAMS_SELECTED_LAST_DAY, selectedLastDay);
        drawingParams.put(SimpleMonthView.VIEW_PARAMS_YEAR, year);
        drawingParams.put(SimpleMonthView.VIEW_PARAMS_MONTH, month);
        drawingParams.put(SimpleMonthView.VIEW_PARAMS_WEEK_START, calendar.getFirstDayOfWeek());
        v.setMonthParams(drawingParams);
        v.invalidate();
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return SHOW_MONTHS;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final SimpleMonthView simpleMonthView;

        public ViewHolder(View itemView, SimpleMonthView.OnDayClickListener onDayClickListener) {
            super(itemView);
            simpleMonthView = (SimpleMonthView) itemView;
            simpleMonthView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            simpleMonthView.setClickable(true);
            simpleMonthView.setOnDayClickListener(onDayClickListener);
        }
    }

    protected void init() {
        if (typedArray.getBoolean(R.styleable.CustomCalendarRecyclerView_currentDaySelected, false))
            onDayTapped(new CalendarDay(Calendar.getInstance()));
    }

    public void onDayClick(SimpleMonthView simpleMonthView, CalendarDay calendarDay) {
        LogUtil.d(TAG, "onDayClick() calendarday = " + calendarDay.year + ", " + calendarDay.month + ", " + calendarDay.day);
        onDayTapped(calendarDay);
    }

    protected void onDayTapped(CalendarDay calendarDay) {
        LogUtil.d(TAG, "onDayTapped()");
        mController.onDayOfMonthSelected(calendarDay.year, calendarDay.month, calendarDay.day);
        setSelectedDay(calendarDay);
    }

    public void setSelectedDay(CalendarDay calendarDay) {
        if (selectedDays.getFirst() != null && selectedDays.getLast() == null) {
            if (selectedDays.getFirst().year == calendarDay.year
                    && selectedDays.getFirst().month == calendarDay.month
                    && selectedDays.getFirst().day == calendarDay.day) {
                LogUtil.i(TAG, "selectedDays.equal(calendarDay) return!");
                return;
            }
            LogUtil.d(TAG, "selectedDays.getFirst() = " + selectedDays.getFirst().year + ", " + selectedDays.getFirst().month + ", " + selectedDays.getFirst().day);
            LogUtil.d(TAG, "setSelectedDay() calendarday = " + calendarDay.year + ", " + calendarDay.month + ", " + calendarDay.day);
            if (!CalendarUtils.isSortBeginLast(selectedDays.getFirst(), calendarDay)) {
                selectedDays.setFirst(calendarDay);
            } else {
                selectedDays.setLast(calendarDay);
            }

            if (selectedDays.getFirst().month < calendarDay.month) {
                for (int i = 0; i < selectedDays.getFirst().month - calendarDay.month - 1; ++i)
                    mController.onDayOfMonthSelected(selectedDays.getFirst().year, selectedDays.getFirst().month + i, selectedDays.getFirst().day);
            }
            mController.onDateRangeSelected(selectedDays);
        } else if (selectedDays.getLast() != null) {
            selectedDays.setFirst(calendarDay);
            selectedDays.setLast(null);
        } else {
            selectedDays.setFirst(calendarDay);
        }

        notifyDataSetChanged();
    }

    public void setBeginAndEndCalendarDay(CalendarDay beginDay, CalendarDay endDay) {
        selectedDays.setFirst(null);
        selectedDays.setFirst(beginDay);
        selectedDays.setLast(null);
        selectedDays.setLast(endDay);
        notifyDataSetChanged();
    }

    public static class CalendarDay implements Serializable {
        private static final long serialVersionUID = -5456695978688356202L;
        private Calendar calendar;

        int day;
        int month;
        int year;

        public CalendarDay() {
            setTime(System.currentTimeMillis());
        }

        public CalendarDay(int year, int month, int day) {
            setDay(year, month, day);
        }

        public CalendarDay(long timeInMillis) {
            setTime(timeInMillis);
        }

        public CalendarDay(Calendar calendar) {
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            day = calendar.get(Calendar.DAY_OF_MONTH);
        }

        private void setTime(long timeInMillis) {
            if (calendar == null) {
                calendar = Calendar.getInstance();
            }
            calendar.setTimeInMillis(timeInMillis);
            month = this.calendar.get(Calendar.MONTH);
            year = this.calendar.get(Calendar.YEAR);
            day = this.calendar.get(Calendar.DAY_OF_MONTH);
        }

        public int getYear() {
            return year;
        }

        public int getMonth() {
            return month;
        }

        public int getDay() {
            return day;
        }

        public void set(CalendarDay calendarDay) {
            year = calendarDay.year;
            month = calendarDay.month;
            day = calendarDay.day;
        }

        public void setDay(int year, int month, int day) {
            this.year = year;
            this.month = month;
            this.day = day;
        }

        public Date getDate() {
            if (calendar == null) {
                calendar = Calendar.getInstance();
            }
            calendar.set(year, month, day, 0, 0, 0);
            return calendar.getTime();
        }

        @Override
        public String toString() {
            final StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("{ year: ");
            stringBuilder.append(year);
            stringBuilder.append(", month: ");
            stringBuilder.append(month);
            stringBuilder.append(", day: ");
            stringBuilder.append(day);
            stringBuilder.append(" }");

            return stringBuilder.toString();
        }
    }

    public SelectedDays<CalendarDay> getSelectedDays() {
        return selectedDays;
    }

    public static class SelectedDays<K> implements Serializable {
        private static final long serialVersionUID = 3942549765282708376L;
        private K first;
        private K last;

        public K getFirst() {
            return first;
        }

        public void setFirst(K first) {
            this.first = first;
        }

        public K getLast() {
            return last;
        }

        public void setLast(K last) {
            this.last = last;
        }
    }
}
