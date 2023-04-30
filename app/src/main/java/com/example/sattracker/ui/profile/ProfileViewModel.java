package com.example.sattracker.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ProfileViewModel extends ViewModel {

    private MutableLiveData<Double> mHours;

    public ProfileViewModel() {

        mHours = new MutableLiveData<>();
    }

    public LiveData<Double> getHours() {
        return mHours;
    }
}