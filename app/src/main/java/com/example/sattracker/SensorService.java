package com.example.sattracker;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

public class SensorService extends Service implements SensorEventListener {

    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];

    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];

    private SensorManager sensorManager;

    private final String TAG = "SENSOR_SERVICE";


    public static final String SENSOR_CHANGE = "SENSOR_CHANGED_INTENT";

    private void updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(rotationMatrix, null,
                accelerometerReading, magnetometerReading);

        SensorManager.getOrientation(rotationMatrix, orientationAngles);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            System.arraycopy(event.values, 0, accelerometerReading,
                    0, accelerometerReading.length);
        else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            System.arraycopy(event.values, 0, magnetometerReading,
                    0, magnetometerReading.length);

        // Set the orientation angle text
        updateOrientationAngles();

        sendMessage();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    private void sendMessage() {
        Intent intent = new Intent(SENSOR_CHANGE);
        intent.putExtra("orient", orientationAngles);
        intent.putExtra("accel", accelerometerReading);
        sendBroadcast(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "Created SensorService");
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
        Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magneticField != null) {
            sensorManager.registerListener(this, magneticField,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Log.d(TAG, "onStart (sensorservice");


        sendMessage();

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
