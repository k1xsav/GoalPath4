package com.example.goalpath;

import java.io.Serializable;

// Класс для записи прогресса с отметкой времени
public class ProgressEntry implements Serializable {
    private long timestamp; // время в миллисекундах
    private int progress;

    public ProgressEntry(long timestamp, int progress) {
        this.timestamp = timestamp;
        this.progress = progress;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getProgress() {
        return progress;
    }
}
