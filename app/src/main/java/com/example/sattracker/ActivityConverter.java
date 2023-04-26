package com.example.sattracker;

import com.google.android.gms.location.DetectedActivity;

public class ActivityConverter {

    private ActivityConverter() {}

    public static String toActivityString(int activity) {
        switch (activity) {
            case DetectedActivity.STILL:
                return "STILL";
            case DetectedActivity.WALKING:
                return "WALKING";
            default:
                return "UNKNOWN";
        }
    }
}
