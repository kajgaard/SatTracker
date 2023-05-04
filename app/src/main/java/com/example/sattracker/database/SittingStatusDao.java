package com.example.sattracker.database;

import android.util.Log;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Ignore;
import androidx.room.Insert;
import androidx.room.MapInfo;
import androidx.room.Query;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

@Dao
public interface SittingStatusDao {

    @Query("SELECT * FROM sittingStatus ORDER BY id ASC")
    public List<SittingStatus> getAll();

    @Insert
    public void insertAll(SittingStatus ... sittingStatuses);

    @Delete
    public void delete(SittingStatus sittingStatus);

    @Ignore
    public default HashMap<Month, List<SittingStatus>> groupByMonth() {
        HashMap<Month, List<SittingStatus>> multimap = new HashMap<>();

        multimap.put(Month.JANUARY, new ArrayList<>());
        multimap.put(Month.FEBRUARY, new ArrayList<>());
        multimap.put(Month.MARCH, new ArrayList<>());
        multimap.put(Month.APRIL, new ArrayList<>());
        multimap.put(Month.MAY, new ArrayList<>());
        multimap.put(Month.JUNE, new ArrayList<>());
        multimap.put(Month.JULY, new ArrayList<>());
        multimap.put(Month.AUGUST, new ArrayList<>());
        multimap.put(Month.SEPTEMBER, new ArrayList<>());
        multimap.put(Month.OCTOBER, new ArrayList<>());
        multimap.put(Month.NOVEMBER, new ArrayList<>());
        multimap.put(Month.DECEMBER, new ArrayList<>());


        List<SittingStatus> all = getAll();

        for (int i = 0; i < all.size(); i++) {
            SittingStatus s = all.get(i);

            Instant moment = Instant.ofEpochMilli(s.getTimestamp().getTime());
            ZonedDateTime zdt = moment.atZone(TimeZone.getDefault().toZoneId());

            Month m = zdt.getMonth();
            multimap.get(m).add(s);
        }


        return multimap;
    }

    @Ignore
    public default List<SittingStatus> getToday() {
        List<SittingStatus> all = getAll();
        Log.d("HEY",  ":" + all.size());
        List<SittingStatus> todayList = new ArrayList<>();

        ZonedDateTime now = TimestampFactory.now_zoned();
        LocalDate today = now.toLocalDate();

        for (SittingStatus ss : all) {
            ZonedDateTime zdt = TimestampFactory.ofTimestamp(ss.getTimestamp());
            LocalDate date = zdt.toLocalDate();

            if (today.equals(date))
                todayList.add(ss);
        }

        return todayList;
    }

}
