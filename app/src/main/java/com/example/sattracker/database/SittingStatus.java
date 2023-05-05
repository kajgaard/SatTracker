package com.example.sattracker.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;

@Entity(tableName = "sittingStatus")
public class SittingStatus {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "sitting")
    private Boolean sitting;

    @ColumnInfo(name = "timestamp")
    private Timestamp timestamp;

    public SittingStatus(int id, Boolean sitting, Timestamp timestamp) {
        this.id = id;
        this.sitting = sitting;
        this.timestamp = timestamp;
    }

    @Ignore
    public SittingStatus(Boolean sitting, Timestamp timestamp) {
        this.sitting = sitting;
        this.timestamp = timestamp;
    }

    @Ignore
    public SittingStatus(Boolean sitting) {
        this.sitting = sitting;
        this.timestamp = TimestampFactory.now();
    }

    public static long collectTotalSittingTime(List<SittingStatus> statuses) {
        long sittingTime = 0;

        // Early exit if not enough entries to collect sitting time
        if (statuses.size() < 2)
            return sittingTime;

        // Always start counting from a sitting position
        int startIndex = -1;
        for (int i = 0; i < statuses.size(); i++)
            if (statuses.get(i).isSitting()) {
                startIndex = i;
                break;
            }

        if (startIndex == -1)
            return sittingTime;

        // It would be easiest to assume that the pattern in the database is always
        // [sitting, not sitting, sitting, ... ], but due to unexpected crashes or shutdowns
        // of the app this may not be the case.

        for (int i = startIndex; i < statuses.size() - 1; i += 1) {
            SittingStatus s1 = statuses.get(i);
            SittingStatus s2 = statuses.get(i + 1);

            if (!s1.isSitting())
                continue;

            Instant i1 = Instant.ofEpochMilli(s1.getTimestamp().getTime());
            Instant i2 = Instant.ofEpochMilli(s2.getTimestamp().getTime());

            sittingTime += ChronoUnit.SECONDS.between(i1, i2);
        }

        return sittingTime;
    }


    public int getId() {
        return id;
    }

    public Boolean isSitting() {
        return sitting;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setSitting(Boolean sitting) { this.sitting = sitting; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

}
