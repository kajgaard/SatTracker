package com.example.sattracker;

import static java.lang.Math.abs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.ActivityTransitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorTracker sensorTracker;
    private boolean sitting = false;
    private String latestActivityStatus = "";
    private final static String TAG = "MainActivity";
    private boolean activityTrackingEnabled;

    private List<ActivityTransition> activityTransitionList;

    private boolean runningQOrLater =
            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q;

    // Action fired when transitions are triggered.
    private final String TRANSITIONS_RECEIVER_ACTION =
            BuildConfig.APPLICATION_ID + "TRANSITIONS_RECEIVER_ACTION";

    private PendingIntent mActivityTransitionsPendingIntent;
    private ActivityTransitionReceiver activityTransitionReceiver;


    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, intent.getStringExtra("message"));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorTracker = new SensorTracker(sensorManager, getResources());

        activityTrackingEnabled = false;

        // Initialize PendingIntent that will be triggered when a activity transition occurs.
        Intent intent = new Intent(TRANSITIONS_RECEIVER_ACTION);
        mActivityTransitionsPendingIntent =
                PendingIntent.getBroadcast(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        activityTransitionReceiver = new ActivityTransitionReceiver();
    }

    @Override
    protected void onStart() {

        super.onStart();
        registerReceiver(activityTransitionReceiver, new IntentFilter("ACTIVITY_TRANSITION"));
    }

    ActivityTransitionRequest buildTransitionRequest() {
        // List of activity transitions to track.
        activityTransitionList = new ArrayList<>();


        // Track WALKING (Enter and Exit), STILL (Enter and Exit)
        activityTransitionList.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.WALKING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build());
        activityTransitionList.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.WALKING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build());
        activityTransitionList.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.STILL)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build());
        activityTransitionList.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.STILL)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build());

        return new ActivityTransitionRequest(activityTransitionList);
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

    private PendingIntent getActivityDetectionPendingIntent() {
        Intent intent = new Intent(this, DetectedActivitiesIntentService.class);

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mMessageReceiver,
                        new IntentFilter("my-integer"));

        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // requestActivityUpdates() and removeActivityUpdates().
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
    }

    /**
     * Registers callbacks for {@link ActivityTransition} events via a custom
     * {@link BroadcastReceiver}
     */
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
                e -> {
                    Log.e(TAG, "Transitions Api could NOT be registered: " + e);

                });

    }

    /**
     * Unregisters callbacks for {@link ActivityTransition} events via a custom
     * {@link BroadcastReceiver}
     */
    private void disableActivityTransitions() {

        Log.d(TAG, "disableActivityTransitions()");

        // Stop listening for activity changes.
        ActivityRecognition.getClient(this).removeActivityTransitionUpdates(mActivityTransitionsPendingIntent)
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

    public void onClickEnableOrDisableActivityRecognition(View view) {

        // Enable/Disable activity tracking and ask for permissions if needed.
        if (activityRecognitionPermissionApproved()) {

            if (activityTrackingEnabled)
                disableActivityTransitions();
            else
                enableActivityTransitions();

        } else {
            // Request permission and start activity for result. If the permission is approved, we
            // want to make sure we start activity recognition tracking.
            Intent startIntent = new Intent(this, PermissionRationalActivity.class);
            startActivity(startIntent);

        }
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

    private static String toTransitionType(int transitionType) {
        switch (transitionType) {
            case ActivityTransition.ACTIVITY_TRANSITION_ENTER:
                return "ENTER";
            case ActivityTransition.ACTIVITY_TRANSITION_EXIT:
                return "EXIT";
            default:
                return "UNKNOWN";
        }
    }

    // Get readings from accelerometer and magnetometer.
    @Override
    public void onSensorChanged(SensorEvent event) {
        sensorTracker.receiveSensorData(event);

        // Set the orientation and accel text
        TextView tv = findViewById(R.id.orientation_textView);
        tv.setText(sensorTracker.orientDataToString());

        tv = findViewById(R.id.accel_textView);
        tv.setText(sensorTracker.accelDataToString());

        updateSittingStatus();
    }

    public void updateSittingStatus() {
        /*
        double x_rot = orientationAngles[1];
        double y_rot = orientationAngles[2];
        double z_rot = orientationAngles[0];

        if (x_rot >= -0.09 && x_rot <= 0.09 &&
            z_rot >= -0.02 && z_rot <= 0.02 &&
            y_rot >= -0.02 && y_rot <= 0.02)
        {
            sitting = true;
        }
        else
            sitting = false;

         */

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onResume() {
        super.onResume();

        sensorTracker.resume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Don't receive any more updates from either sensor.
        sensorTracker.pause(this);

        // Disable activity transitions when user leaves the app.
        if (activityTrackingEnabled) {
            disableActivityTransitions();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        unregisterReceiver(activityTransitionReceiver);

    }

    public class ActivityTransitionReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals("ACTIVITY_TRANSITION")) {
                Log.d(TAG, "RECEIVED TRANSITION INTENT");
                int conf = intent.getIntExtra("confidence", 0);
                int type = intent.getIntExtra("type", 0);
                Log.d(TAG, String.valueOf(conf) + " " + toActivityString(type));

                TextView tv = findViewById(R.id.transition_textView);
                String s = String.format(getResources().getString(R.string.activity_transition),
                        toActivityString(type));
                tv.setText(s);
            }


        }



    }
}