package com.example.sattracker.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.sattracker.databinding.FragmentHomeBinding;
import com.example.sattracker.ui.profile.ProfileFragment;
import com.example.sattracker.ui.profile.ProfileViewModel;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    private int progr = 0; //from 0-1000 -> 100% should equal to 660.
    int deg = 0; //No need to touch
    private int personalGoal = 240; //Should use getter in ProfileFragment
    private Double goalToDegreeRatio;

    //temp buttons
    private Button decr, incr;

    private boolean firstLookFlag = true;

    private TextView progressTextView; //textview in the middle of progressbar
    private ProgressBar progressBar; //actual moving progressbar
    private ProgressBar progressBarBg; //static for aestetic purposes


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);


        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //buttons for debugging
        incr = binding.buttonIncr;
        decr = binding.buttonDecr;
        incr.setVisibility(View.GONE);
        decr.setVisibility(View.GONE);
        //

        ImageView mockedWeek = binding.ImageViewMockedWeek;
        mockedWeek.setOnClickListener(v -> Toast.makeText(getActivity(), "This feature has not been implemented yet! :(",
                Toast.LENGTH_LONG).show());


        progressBar = binding.progressBar;
        progressBarBg = binding.progressBarBg;

        progressTextView = binding.progressText;

        progressBar.setMax(1000);
        progressBarBg.setMax(1000);
        progressBarBg.setProgress(660);
        updateProgressBar();

        goalToDegreeRatio = 660.0/personalGoal;

        if(firstLookFlag) {
            increaseProgress(160); //For demo purposes
            firstLookFlag = false;
        }

        /* For debugging
        incr.setVisibility(View.VISIBLE);
        decr.setVisibility(View.VISIBLE);
        incr.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (progr <= 90){
                    progr += 10;
                    deg = deg+66;
                    updateProgressBar();
                }
            }
        });
        decr.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (progr >= 10){
                    progr -= 10;
                    deg = deg-66;
                    updateProgressBar();
                }
            }
        });
         */

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void updateProgressBar() { //UI + text
        progressBar.setProgress(deg,true);
        progressTextView.setText(progr+"");
    }

    public void increaseProgress(double amount){ //amount being the minutes wanted to add
        if (progr <= 240-amount){
            progr += amount;
            double part = amount*goalToDegreeRatio;
            deg = (int) (deg+part);
            updateProgressBar();
        } else {
            progr += amount;
            deg = 660;
            updateProgressBar();
        }
    }
}