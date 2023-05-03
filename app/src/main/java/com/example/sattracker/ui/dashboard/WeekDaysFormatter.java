package com.example.sattracker.ui.dashboard;

import android.util.Log;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.formatter.ValueFormatter;

public class WeekDaysFormatter extends ValueFormatter {
    private final String[] mDays = {"MO", "TU", "WE", "TH", "FR", "SA", "SU",};

    private final BarLineChartBase<?> chart;
    private int count;

    public WeekDaysFormatter(BarLineChartBase<?> chart) {
        this.chart = chart;
    }

    @Override
    public String getFormattedValue(float value) {
        Log.e("WeekDaysFormatter", "value = " + value);
        String day = "";
        try {
            day = mDays[count];
        } catch (IndexOutOfBoundsException ex) {
            count = 0;
            day = mDays[count];
        }

        count++;
        return day;
    }
}
