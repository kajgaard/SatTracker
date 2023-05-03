package com.example.sattracker.ui.dashboard;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.sattracker.R;
import com.example.sattracker.databinding.FragmentDashboardBinding;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    public ArrayList barArrayList; //soon to be data
    public BarChart barChart;
    public Button rangePicker;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //RangePicker - Not implemented
        rangePicker = binding.sevenDays;
        rangePicker.setPressed(true);
        MaterialButtonToggleGroup mockedSelection = binding.rangeSelectionGroup;
        mockedSelection.setOnClickListener(v -> Toast.makeText(getActivity(), "This feature has not been implemented yet! :(",
                Toast.LENGTH_LONG).show());


        //Chart - half implemented..
        barChart = binding.barchart;
        getData();

        BarDataSet barDataSet = new BarDataSet(barArrayList, "");
        BarData barData = new BarData(barDataSet);

        barDataSet.resetColors();
        barDataSet.setColor(R.color.green);
        barDataSet.setValueTextColor(R.color.dark_grey);
        barChart.setDrawValueAboveBar(true);
        barData.setValueTextSize(16);
        barChart.getXAxis().setCenterAxisLabels(true);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.setPinchZoom(false);
        barChart.setDragEnabled(false);
        barChart.setDrawBarShadow(false);
        barChart.setDrawGridBackground(false);
        barChart.getDescription().setEnabled(false);
        barChart.setTouchEnabled(false);
        barChart.setHighlightFullBarEnabled(false);
        barChart.getDescription().setEnabled(false);
        barChart.getAxisRight().setEnabled(false);


        //x-axis styling
        XAxis xAxis = barChart.getXAxis();
        xAxis.setTypeface(Typeface.DEFAULT);
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelCount(7);
        xAxis.setCenterAxisLabels(true);
        xAxis.setValueFormatter(new WeekDaysFormatter(barChart));

        //legends diabled
        Legend l = barChart.getLegend();
        l.setEnabled(false);

        //old x-axisformatter - will it work?
        //XAxisFormatter formatter = new XAxisFormatter();
        // barChart.getXAxis().setValueFormatter(formatter);

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

