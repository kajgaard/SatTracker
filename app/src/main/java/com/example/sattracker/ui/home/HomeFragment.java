package com.example.sattracker.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.sattracker.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    private int progr = 0; //from 0-1000 -> 100% should equal to 660.

    int deg = 0; //dont need to touch

    //temp buttons
    private Button incr;
    private Button decr;

    private TextView progressTextView; //textview in the middle of progressbar

    private ProgressBar progressBar; //actual moving progressbar

    private ProgressBar progressBarBg; //static for aestetic purposes

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);


        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        incr = binding.buttonIncr;
        incr.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (progr <= 90){
                    progr += 10;
                    deg = deg+66;
                    updateProgressBar();
                }
            }
        });
        decr = binding.buttonDecr;

        decr.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (progr >= 10){
                    progr -= 10;
                    deg = deg-66;
                    updateProgressBar();
                }
            }
        });

        progressBar = binding.progressBar;
        progressBarBg = binding.progressBarBg;

        progressTextView = binding.progressText;

        progressBar.setMax(1000);
        progressBarBg.setMax(1000);
        progressBarBg.setProgress(660);
        updateProgressBar();
        //final TextView textView = binding.textHome;
        //homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void updateProgressBar() {

        progressBar.setProgress(deg,true);
        progressTextView.setText(progr+"");

    }
}