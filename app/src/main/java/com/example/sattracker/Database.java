package com.example.sattracker;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.List;

public class Database extends SQLiteOpenHelper
{
    private static Database sInstance;

    public static synchronized Database getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new Database(context.getApplicationContext());
        }
        return sInstance;
    }

    private static final String TAG = "DATABASE";
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "SITTING_DATABASE";
    public static final String SITTING_COLUMN_ID = "_id";
    public static final String SITTING_STATUS = "sitting";
    public static final String SITTING_TIMESTAMP = "timestamp";


    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }



    @Override
    public void onCreate(SQLiteDatabase db) {
        String createDB = "CREATE TABLE " + DATABASE_NAME + " ( " +
                SITTING_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SITTING_STATUS + " INTEGER, " +
                SITTING_TIMESTAMP + " TEXT)";

        db.execSQL(createDB);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_NAME);
        onCreate(db);
    }

    public void addEntry(SittingStatus status) {
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();

            DateTimeFormatterBuilder dateFormat = new DateTimeFormatterBuilder();
            dateFormat.appendPattern(DATE_FORMAT);

            values.put(SITTING_STATUS, (int) (status.isSitting() ? 1 : 0));
            values.put(SITTING_TIMESTAMP, status.getTimestamp().format(dateFormat.toFormatter()));

            db.insertOrThrow(DATABASE_NAME, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        } finally {
            db.endTransaction();
        }

    }


    public List<SittingStatus> getEntry() {
        List<SittingStatus> statuses = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        String TABLE_QUERY = String.format("SELECT * FROM %s", DATABASE_NAME);
        Cursor cursor = db.rawQuery(TABLE_QUERY, null);

        try {
            if (cursor.moveToFirst()) {
               do {
                   @SuppressLint("Range")
                   int int_sitting = cursor.getInt(cursor.getColumnIndex(SITTING_STATUS));
                   boolean sitting = int_sitting == 1;

                   @SuppressLint("Range")
                   String stringDate = cursor.getString(cursor.getColumnIndex(SITTING_TIMESTAMP));
                   DateTimeFormatterBuilder dateFormat = new DateTimeFormatterBuilder();
                   dateFormat.appendPattern(DATE_FORMAT);
                   LocalDateTime date = LocalDateTime.parse(stringDate, dateFormat.toFormatter());


                   statuses.add(new SittingStatus(sitting, date));

                   Log.d(TAG, date + " " + sitting);

               } while (cursor.moveToNext());
            }

        } catch (Exception e) {
            Log.d(TAG, "Could not add entry");
        } finally {
            if (cursor != null && !cursor.isClosed())
                cursor.close();
        }

        return statuses;
    }



    public void deleteAll() {
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            db.delete(DATABASE_NAME, null, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Could not clear database");
        } finally {
            db.endTransaction();
        }
    }


}
