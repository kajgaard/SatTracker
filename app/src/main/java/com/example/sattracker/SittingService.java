package com.example.sattracker;

import static com.example.sattracker.ActivityConverter.toActivityString;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import com.example.sattracker.database.AppDatabase;
import com.example.sattracker.database.SittingStatus;

import androidx.annotation.Nullable;

import com.example.sattracker.database.SittingStatusDao;
import com.example.sattracker.database.TimestampFactory;
import com.google.android.gms.location.DetectedActivity;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class SittingService extends Service {

    private boolean sitting;
    private SittingStatus sittingStatus;
    private Receiver receiver;

    private static final String TAG = "SITTING_SERVICE";


    @Override
    public void onCreate() {
        sitting = false;
        receiver = new Receiver();
        sittingStatus = new SittingStatus(false, TimestampFactory.now());

        registerReceiver(receiver,
                new IntentFilter(DetectedActivitiesIntentService.ACTIVITY_TRANSITION));
        registerReceiver(receiver,
                new IntentFilter(SensorService.SENSOR_CHANGE));


        Log.d(TAG, "Created SittingService");
    }

    public void updateSittingStatus(float[] orient, int lastDetectedActivity) {
        final float X_ROT_THRESHOLD = 0.2f;
        final float Y_ROT_THRESHOLD = 0.2f;


        float x_rot = orient[1];
        float y_rot = orient[2];
        //float z_rot = orient[0];

        boolean newSitting = x_rot >= -X_ROT_THRESHOLD && x_rot <= X_ROT_THRESHOLD &&
                y_rot >= -Y_ROT_THRESHOLD && y_rot <= Y_ROT_THRESHOLD &&
                lastDetectedActivity == DetectedActivity.STILL;

        Timestamp now = TimestampFactory.now();
        SittingStatus newStatus = new SittingStatus(newSitting, now);

        // stateChangeThreshold number of seconds must pass before sitting status is changed.
        // This helps prevent sitting status from flickering rapidly between states.
        Instant i1 = Instant.ofEpochMilli(sittingStatus.getTimestamp().getTime());
        Instant i2 = Instant.ofEpochMilli(now.getTime());
        long diff = ChronoUnit.SECONDS.between(i1, i2);
        long stateChangeThreshold = 10;

        // We cannot assume that the following is true on start up
        //assert now.isAfter(sittingStatus.getTimestamp());

        // Change in sitting status detected.
        if (newSitting != sittingStatus.isSitting() && diff > stateChangeThreshold) {
            AppDatabase db = AppDatabase.getInstance(this);
            SittingStatusDao ssDao = db.sittingStatusDao();
            ssDao.insertAll(newStatus);
        }


        sittingStatus = newStatus;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    public class Receiver extends BroadcastReceiver {

        private int lastDetectedActivity = 10;
        private float[] accel;
        private float[] orient;


        void receiveActivityTransition(Intent intent) {
            Log.d(TAG, "RECEIVED TRANSITION INTENT");
            int conf = intent.getIntExtra("confidence", 0);
            int type = intent.getIntExtra("type", 0);
            Log.d(TAG, String.valueOf(conf) + " " + toActivityString(type));

            lastDetectedActivity = type;

        }

        void receiveSensorChange(Intent intent) {
            accel = intent.getFloatArrayExtra("accel") ;
            orient = intent.getFloatArrayExtra("orient") ;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DetectedActivitiesIntentService.ACTIVITY_TRANSITION))
                receiveActivityTransition(intent);
            else if (intent.getAction().equals(SensorService.SENSOR_CHANGE))
                receiveSensorChange(intent);

            updateSittingStatus(orient, lastDetectedActivity);

        }



    }
}




