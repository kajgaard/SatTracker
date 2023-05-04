package com.example.sattracker;

import static org.junit.Assert.assertEquals;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.sattracker.database.AppDatabase;
import com.example.sattracker.database.SittingStatusDao;
import com.example.sattracker.database.TimestampFactory;
import com.example.sattracker.database.SittingStatus;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.Timestamp;
import java.time.Month;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class SittingTimeTest {

    private SittingStatusDao ssDao;
    private AppDatabase db;


    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        ssDao = db.sittingStatusDao();
    }

    @After
    public void closeDb() {
        db.close();
    }


    @Test
    public void collectSittingTimeFromTwoStatuses() {
        Timestamp t1 = TimestampFactory.of(2023,
                Month.MAY, 4, 5, 30, 0);
        Timestamp t2 = TimestampFactory.of(2023,
                Month.MAY, 4, 5, 40, 0);

        SittingStatus s1 = new SittingStatus(true, t1);
        SittingStatus s2 = new SittingStatus(false, t2);

        ssDao.insertAll(s1, s2);
        List<SittingStatus> s = ssDao.getAll();

        long expectedSittingTime = 10;
        long sittingTime = SittingStatus.collectTotalSittingTime(s);

        assertEquals(2, s.size());
        assertEquals(expectedSittingTime, sittingTime);
    }

    @Test
    public void collectSittingTimeButNotEnoughEntries() {
        // 0 elements
        List<SittingStatus> s = ssDao.getAll();

        long expectedSittingTime = 0;
        long sittingTime = SittingStatus.collectTotalSittingTime(s);
        assertEquals(expectedSittingTime, sittingTime);

        // 1 element
        SittingStatus s1 = new SittingStatus(true);
        ssDao.insertAll(s1);
        s = ssDao.getAll();
        sittingTime = SittingStatus.collectTotalSittingTime(s);
        assertEquals(expectedSittingTime, sittingTime);
    }

    @Test
    public void collectSittingTimeButFirstElementIsSittingEqualsFalse() {
        Timestamp t1 = TimestampFactory.of(2023,
                Month.MAY, 4, 5, 30, 0);
        Timestamp t2 = TimestampFactory.of(2023,
                Month.MAY, 4, 5, 40, 0);
        Timestamp t3 = TimestampFactory.of(2023,
                Month.MAY, 4, 5, 59, 0);

        SittingStatus s1 = new SittingStatus(false, t1);
        SittingStatus s2 = new SittingStatus(true, t2);
        SittingStatus s3 = new SittingStatus(false, t3);

        ssDao.insertAll(s1, s2, s3);
        List<SittingStatus> s = ssDao.getAll();

        long expectedSittingTime = 19;
        long sittingTime = SittingStatus.collectTotalSittingTime(s);

        assertEquals(3, s.size());
        assertEquals(expectedSittingTime, sittingTime);
    }

    @Test
    public void collectSittingTimeButDifferenceInTimeIsLessThanOneMinute() {
        Timestamp t1 = TimestampFactory.of(2023,
                Month.MAY, 4, 5, 30, 0);
        Timestamp t2 = TimestampFactory.of(2023,
                Month.MAY, 4, 5, 30, 59);

        SittingStatus s1 = new SittingStatus(true, t1);
        SittingStatus s2 = new SittingStatus(false, t2);

        ssDao.insertAll(s1, s2);
        List<SittingStatus> s = ssDao.getAll();

        long expectedSittingTime = 0;
        long sittingTime = SittingStatus.collectTotalSittingTime(s);

        assertEquals(2, s.size());
        assertEquals(expectedSittingTime, sittingTime);
    }


    @Test
    public void collectSittingTimeWithMultipleVariedDates() {
        Timestamp t1 = TimestampFactory.of(2021,
                Month.MAY, 4, 5, 30, 0);
        Timestamp t2 = TimestampFactory.of(2021,
                Month.MAY, 4, 6, 35, 35);

        Timestamp t3 = TimestampFactory.of(2021,
                Month.JUNE, 21, 12, 2, 1);
        Timestamp t4 = TimestampFactory.of(2021,
                Month.JUNE, 21, 18, 35, 49);

        Timestamp t5 = TimestampFactory.of(2021,
                Month.JUNE, 21, 18, 35, 50);
        Timestamp t6 = TimestampFactory.of(2021,
                Month.JUNE, 21, 18, 49, 0);

        SittingStatus s1 = new SittingStatus(true, t1);
        SittingStatus s2 = new SittingStatus(false, t2);
        SittingStatus s3 = new SittingStatus(true, t3);
        SittingStatus s4 = new SittingStatus(false, t4);
        SittingStatus s5 = new SittingStatus(true, t5);
        SittingStatus s6 = new SittingStatus(false, t6);

        ssDao.insertAll(s1, s2, s3, s4, s5, s6);
        List<SittingStatus> s = ssDao.getAll();

        long expectedSittingTime = 65 + 6 * 60 + 33 + 13;
        long sittingTime = SittingStatus.collectTotalSittingTime(s);

        assertEquals(6, s.size());
        assertEquals(expectedSittingTime, sittingTime);
    }

}
