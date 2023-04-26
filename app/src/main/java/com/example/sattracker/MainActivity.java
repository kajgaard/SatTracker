package com.example.sattracker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {

    private boolean sitting = false;
    private final static String TAG = "MainActivity";
    private boolean activityTrackingEnabled;

    private final boolean runningQOrLater =
            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q;

    // Action fired when transitions are triggered.
    private final String TRANSITIONS_RECEIVER_ACTION =
            BuildConfig.APPLICATION_ID + "TRANSITIONS_RECEIVER_ACTION";

    private PendingIntent mActivityTransitionsPendingIntent;
    private ActivityTransitionReceiver activityTransitionReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        askForActivityRecognitionPermission();
        activityTrackingEnabled = activityRecognitionPermissionApproved();

        if (!activityTrackingEnabled)
        {
            Log.e(TAG, "Permissions were not granted, terminating.");
            finishAndRemoveTask();
        }

        enableActivityTransitions();

        // Initialize PendingIntent that will be triggered when a activity transition occurs.
        Intent intent = new Intent(TRANSITIONS_RECEIVER_ACTION);
        mActivityTransitionsPendingIntent =
                PendingIntent.getBroadcast(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        activityTransitionReceiver = new ActivityTransitionReceiver();
        startService(new Intent(getBaseContext(), SensorService.class));
    }

    @Override
    protected void onStart() {

        super.onStart();
        registerReceiver(activityTransitionReceiver,
                        new IntentFilter(DetectedActivitiesIntentService.ACTIVITY_TRANSITION));
        registerReceiver(activityTransitionReceiver,
                new IntentFilter(SensorService.SENSOR_CHANGE));
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
                e -> Log.e(TAG, "Transitions Api could NOT be registered: " + e));

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

    public void askForActivityRecognitionPermission() {

        // Enable/Disable activity tracking and ask for permissions if needed.
        if (!activityRecognitionPermissionApproved()) {
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


    @Override
    protected void onResume() {
        super.onResume();
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

        unregisterReceiver(activityTransitionReceiver);

    }

    public class ActivityTransitionReceiver extends BroadcastReceiver {

        private int lastDetectedActivity = 0;
        private float[] accel;
        private float[] orient;

        public void updateSittingStatus() {



            float x_rot = orient[1];
            float y_rot = orient[2];
            //float z_rot = orient[0];

            if (x_rot >= -0.2 && x_rot <= 0.2 &&
                y_rot >= -0.3 && y_rot <= 0.3 && lastDetectedActivity == DetectedActivity.STILL)
            {
                sitting = true;
            }
            else
                sitting = false;

            TextView tv = findViewById(R.id.sitting_textView);
            String sittingText = String.format(getResources().getString(R.string.sitting), sitting);
            tv.setText(sittingText);
        }

        void receiveActivityTransition(Intent intent) {
            Log.d(TAG, "RECEIVED TRANSITION INTENT");
            int conf = intent.getIntExtra("confidence", 0);
            int type = intent.getIntExtra("type", 0);
            Log.d(TAG, String.valueOf(conf) + " " + toActivityString(type));

            lastDetectedActivity = type;

            TextView tv = findViewById(R.id.transition_textView);
            String s = String.format(getResources().getString(R.string.activity_transition),
                    toActivityString(type));
            tv.setText(s);
        }

        void receiveSensorChange(Intent intent) {
            accel = intent.getFloatArrayExtra("accel") ;
            orient = intent.getFloatArrayExtra("orient") ;

            TextView tv = findViewById(R.id.orientation_textView);
            String orientString =  String.format(getResources().getString(R.string.orientation),
                    orient[0],
                    orient[1],
                    orient[2]);
            tv.setText(orientString);

            tv = findViewById(R.id.accel_textView);
            String accelString =  String.format(getResources().getString(R.string.accel),
                    accel[0],
                    accel[1],
                    accel[2]);
            tv.setText(accelString);

        }

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(DetectedActivitiesIntentService.ACTIVITY_TRANSITION))
                receiveActivityTransition(intent);
            else if (intent.getAction().equals(SensorService.SENSOR_CHANGE))
                receiveSensorChange(intent);

            updateSittingStatus();

        }



    }
}