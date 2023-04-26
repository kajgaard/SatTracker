package com.example.sattracker;

import java.time.LocalDateTime;

public class SittingStatus {

    private boolean sitting;
    private LocalDateTime timestamp;

    SittingStatus(boolean sitting, LocalDateTime timestamp) {
        this.sitting = sitting;
        this.timestamp = timestamp;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public boolean isSitting() {
        return sitting;
    }
}
