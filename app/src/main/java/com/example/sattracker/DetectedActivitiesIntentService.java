package com.example.sattracker;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class DetectedActivitiesIntentService extends Service {



    private Optional<Integer> lastDetectedActivity = Optional.empty();
    protected static final String TAG = "DetectedActivitiesIS";

    /**
     * This constructor is required, and calls the super IntentService(String)
     * constructor with the name for a worker thread.
     */
    public DetectedActivitiesIntentService() {
        // Use the TAG to name the worker thread.
        super();
    }

    private static String toActivityString(int activity) {
        switch (activity) {
            case DetectedActivity.STILL:
                return "STILL";
            case DetectedActivity.WALKING:
                return "WALKING";
            default:
                return "UNKNOWN";
        }
    }

    private void sendMessage(DetectedActivity a) {
        Intent intent = new Intent("ACTIVITY_TRANSITION");
        intent.putExtra("confidence", a.getConfidence());
        intent.putExtra("type", a.getType());
        sendBroadcast(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
        assert result != null;

        // We assume activity with highest confidence is the actual activity performed by
        // the user.
        List<DetectedActivity> activities = result.getProbableActivities();
        assert activities.size() > 0;
        activities.sort(Comparator.comparing(DetectedActivity::getConfidence,
                                             Collections.reverseOrder()));
        DetectedActivity activity = activities.get(0);

        // We only send Intents if activity changes from previous state.
        if (!lastDetectedActivity.isPresent() ||
            (activity.getType() != lastDetectedActivity.get())) {
            lastDetectedActivity = Optional.of(activity.getType());

            Log.d(TAG, activity.toString());
            Log.d(TAG, String.valueOf(activity.getType()));

            sendMessage(activity);
        }

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
