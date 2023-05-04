package com.example.sattracker.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {SittingStatus.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    private static final String DB_NAME = "sittingStatus.db";
    private static volatile AppDatabase instance;


    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null)
            instance = create(context);

        return instance;
    }

    private static AppDatabase create(Context context) {
        return Room.databaseBuilder(context, AppDatabase.class, DB_NAME)
                .allowMainThreadQueries().build();
    }


    public abstract SittingStatusDao sittingStatusDao();



}
