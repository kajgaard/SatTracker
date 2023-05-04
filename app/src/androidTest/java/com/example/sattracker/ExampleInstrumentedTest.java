package com.example.sattracker;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import java.util.List;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    private Database db;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Database.getInstance(context);
    }

    @Test
    public void isEmpty() {
        List<SittingStatus> statuses = db.getEntry();

        assertEquals(0, statuses.size());
    }


}