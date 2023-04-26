package com.example.sattracker;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.location.DetectedActivity;

public class SittingService extends Service {

    private boolean sitting;
    private Receiver receiver;

    private static final String TAG = "SITTING_SERVICE";


    @Override
    public void onCreate() {
        sitting = false;
        receiver = new Receiver();


    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
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

    public class Receiver extends BroadcastReceiver {

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
        }

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

            updateSittingStatus();

        }



    }
}




