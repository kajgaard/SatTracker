package com.example.sattracker.database;

import androidx.room.TypeConverter;

import java.sql.Timestamp;

public class Converters {

    @TypeConverter
    public static Timestamp fromTimestamp(Long value) {
        return value == null ? null : new Timestamp(value);
    }

    @TypeConverter
    public static Long instantToTimestamp(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.getTime();
    }
}
