package com.example.sattracker.database;


import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.TimeZone;

public class TimestampFactory {

    private TimestampFactory() {}

    public static Timestamp of(
            int year,
            java.time.Month month,
            int dayOfMonth,
            int hour,
            int minute,
            int second
    ) {
        LocalDateTime d = LocalDateTime.of(year,
                month, dayOfMonth, hour, minute, second);
        ZonedDateTime zdt = d.atZone(TimeZone.getDefault().toZoneId());
        Instant i = zdt.toInstant();

        return new Timestamp(i.toEpochMilli());
    }

    public static Timestamp now() {
        Instant i = Instant.now();
        return new Timestamp(i.toEpochMilli());
    }


}
