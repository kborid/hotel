package com.huicheng.hotel.android.ui.custom.calendar;

/**
 * @author kborid
 * @date 2016/11/18 0018
 */
public interface CalendarSelectedListener {
    void onDayOfMonthSelected(int year, int month, int day);

    void onDateRangeSelected(final SimpleMonthAdapter.SelectedDays<SimpleMonthAdapter.CalendarDay> selectedDays);
}
