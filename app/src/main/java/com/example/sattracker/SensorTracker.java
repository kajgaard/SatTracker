package com.example.sattracker;

import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import androidx.annotation.NonNull;

import java.util.List;

public class SensorTracker {

    private final SensorManager sensorManager;
    private final Resources res;

    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];

    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];


    SensorTracker(@NonNull SensorManager sensorManager, Resources res) {
        this.sensorManager = sensorManager;
        this.res = res;

        // For debugging
        List<Sensor> deviceSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        System.out.println(deviceSensors.toString());
    }

    public void receiveSensorData(@NonNull SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            System.arraycopy(event.values, 0, accelerometerReading,
                    0, accelerometerReading.length);
        else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            System.arraycopy(event.values, 0, magnetometerReading,
                    0, magnetometerReading.length);

        // Set the orientation angle text
        updateOrientationAngles();
    }

    public void pause(SensorEventListener listener) {
        sensorManager.unregisterListener(listener);
    }

    public void resume(SensorEventListener listener)
    {
        // Get updates from the accelerometer and magnetometer at a constant rate.
        // To make batch operations more efficient and reduce power consumption,
        // provide support for delaying updates to the application.
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(listener, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
        Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magneticField != null) {
            sensorManager.registerListener(listener, magneticField,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
    }

    // Compute the three orientation angles based on the most recent readings from
    // the device's accelerometer and magnetometer.
    private void updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(rotationMatrix, null,
                accelerometerReading, magnetometerReading);

        SensorManager.getOrientation(rotationMatrix, orientationAngles);
    }

    public String accelDataToString()
    {
        return String.format(res.getString(R.string.accel),
                accelerometerReading[0],
                accelerometerReading[1],
                accelerometerReading[2]);
    }

    public String orientDataToString()
    {
        return String.format(res.getString(R.string.orientation),
                orientationAngles[0],
                orientationAngles[1],
                orientationAngles[2]);
    }

}
