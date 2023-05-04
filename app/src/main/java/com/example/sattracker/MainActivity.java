package com.example.sattracker;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.sattracker.database.AppDatabase;
import com.example.sattracker.database.SittingStatus;
import com.example.sattracker.database.SittingStatusDao;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = "MainActivity";
    private boolean activityTrackingEnabled;

    private final boolean runningQOrLater =
            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q;

    // Action fired when transitions are triggered.
    private final String TRANSITIONS_RECEIVER_ACTION =
            BuildConfig.APPLICATION_ID + "TRANSITIONS_RECEIVER_ACTION";

    private PendingIntent mActivityTransitionsPendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        askForActivityRecognitionPermission();
        activityTrackingEnabled = activityRecognitionPermissionApproved();

        /*
        if (!activityTrackingEnabled)
        {
            Log.e(TAG, "Permissions were not granted, terminating.");
            finishAndRemoveTask();
        }
         */

        enableActivityTransitions();

        // Initialize PendingIntent that will be triggered when a activity transition occurs.
        Intent intent = new Intent(TRANSITIONS_RECEIVER_ACTION);
        mActivityTransitionsPendingIntent =
                PendingIntent.getBroadcast(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        if (!sensorServiceRunning())
            startForegroundService(new Intent(getBaseContext(), SensorService.class));

        if (!sittingServiceRunning())
            startForegroundService(new Intent(getBaseContext(), SittingService.class));

        updateSittingTime();


    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d(TAG, "OnActivityResult");
        // Start activity recognition if the permission was approved.
        if (activityRecognitionPermissionApproved() && !activityTrackingEnabled) {
            enableActivityTransitions();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public boolean sensorServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service: activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if(SensorService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public boolean sittingServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service: activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if(SittingService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void updateSittingTime() {
        AppDatabase db = AppDatabase.getInstance(this);
        SittingStatusDao ssDao = db.sittingStatusDao();
        List<SittingStatus> s = ssDao.getToday();
        Log.d(TAG, "Today size: " + s.size());



        TextView sittingTime = (TextView) findViewById(R.id.daily_sitting_time);
        String text = String.format(getResources().getString(R.string.daily_sitting_time),
                SittingStatus.collectTotalSittingTime(s));

        sittingTime.setText(text);
    }

    private PendingIntent getActivityDetectionPendingIntent() {
        Intent intent = new Intent(this, DetectedActivitiesIntentService.class);


        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // requestActivityUpdates() and removeActivityUpdates().
        return PendingIntent.getService(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
    }

    private void enableActivityTransitions() {

        Log.d(TAG, "enableActivityTransitions()");

        // ActivityTransitions API is wonky and we just have to continually poll to look for
        // changes in state.
        Task<Void> task = ActivityRecognition.getClient(this)
                        .requestActivityUpdates(1000, getActivityDetectionPendingIntent());


        task.addOnSuccessListener(
                result -> {
                    activityTrackingEnabled = true;
                    Log.i(TAG, "Transitions Api was successfully registered.");

                });
        task.addOnFailureListener(
                e -> Log.e(TAG, "Transitions Api could NOT be registered: " + e));

    }

    /**
     * Unregisters callbacks for {@link ActivityTransition} events via a custom
     * {@link BroadcastReceiver}
     */
    private void disableActivityTransitions() {

        Log.d(TAG, "disableActivityTransitions()");

        // Stop listening for activity changes.
        ActivityRecognition.getClient(this)
                .removeActivityTransitionUpdates(mActivityTransitionsPendingIntent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        activityTrackingEnabled = false;
                        Log.i(TAG, "Transitions successfully unregistered.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Transitions could not be unregistered: " + e);
                    }
                });

    }

    /**
     * On devices Android 10 and beyond (29+), you need to ask for the ACTIVITY_RECOGNITION via the
     * run-time permissions.
     */
    private boolean activityRecognitionPermissionApproved() {

        // TODO: Review permission check for 29+.
        if (runningQOrLater) {

            return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACTIVITY_RECOGNITION
            );
        } else {
            return true;
        }
    }

    public void askForActivityRecognitionPermission() {

        // Enable/Disable activity tracking and ask for permissions if needed.
        if (!activityRecognitionPermissionApproved()) {
            // Request permission and start activity for result. If the permission is approved, we
            // want to make sure we start activity recognition tracking.
            Intent startIntent = new Intent(this, PermissionRationalActivity.class);
            startActivity(startIntent);

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateSittingTime();
    }

    @Override
    protected void onPause() {
        super.onPause();

        /*
        if (activityTrackingEnabled) {
            disableActivityTransitions();
        }
         */
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

}