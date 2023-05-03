package com.example.sattracker.ui.dashboard;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.sattracker.databinding.FragmentDashboardBinding;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.gms.common.logging.Logger;

import java.util.ArrayList;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;

    public ArrayList barArrayList; //soon to be data
    public BarChart barChart;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        barChart = binding.barchart;

        getData();

        BarDataSet barDataSet = new BarDataSet(barArrayList, "");
        BarData barData = new BarData(barDataSet);

        barDataSet.setColors(Color.BLUE);
        barDataSet.setValueTextColor(Color.BLACK);

        barChart.setDrawValueAboveBar(true);
        barData.setValueTextSize(16);
        //barChart.getXAxis().setCenterAxisLabels(true);
        //barChart.getXAxis().setDrawGridLines(false);
        //barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        //barChart.getXAxis().setLabelCount(barArrayList.size());
        barChart.setPinchZoom(false);
        barChart.setDragEnabled(false);
        barChart.setDrawBarShadow(false);
        barChart.setDrawGridBackground(false);
        barChart.getDescription().setEnabled(false);
        barChart.setDrawValueAboveBar(false);
        barChart.setTouchEnabled(false);
        barChart.setHighlightFullBarEnabled(false);

        XAxis xAxis = barChart.getXAxis();
//        xAxis.setTypeface(tfLight);
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelCount(7);
        xAxis.setCenterAxisLabels(true);
        xAxis.setValueFormatter(new WeekDaysFormatter(barChart));


        Legend l = barChart.getLegend();
        l.setEnabled(false);
        barChart.getDescription().setEnabled(false);


        XAxisFormatter formatter = new XAxisFormatter();
        // barChart.getXAxis().setValueFormatter(formatter);

        barChart.getAxisRight().setEnabled(false);

        barChart.setData(barData);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void getData() {
        barArrayList = new ArrayList();

        //Dummy data
        barArrayList.add(new BarEntry(0, 60));
        barArrayList.add(new BarEntry(1, 20));
        barArrayList.add(new BarEntry(2, 30));
        barArrayList.add(new BarEntry(3, 40));
        barArrayList.add(new BarEntry(4, 50));
        barArrayList.add(new BarEntry(5, 60));
        barArrayList.add(new BarEntry(6, 60));


    }
}


class XAxisFormatter extends ValueFormatter {
 //Bliver PT ikke brugt
    private String[] days = {"Mo", "Tu", "Wed", "Th", "Fr", "Sa", "Su"};

    @Override
    public String getAxisLabel(float value, AxisBase axis) {
        Log.e("BAR", "Value is "+value + "Int=" + (int)value);
        if (days[(int) value] != null) {
            return days[(int) value];
        } else {
            Log.e("BAR", "Else is hit: Value is: " + value);
            return String.valueOf(value);
        }
    }
}

