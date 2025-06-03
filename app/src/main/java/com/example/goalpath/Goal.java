package com.example.goalpath;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Goal implements Serializable {
    private String title;
    private String description;
    private int progress; // текущее значение прогресса

    // История прогресса — список записей
    private List<ProgressEntry> progressHistory;

    // Конструктор без прогресса
    public Goal(String title, String description) {
        this.title = title;
        this.description = description;
        this.progress = 0;
        this.progressHistory = new ArrayList<>();
    }

    // Конструктор с прогрессом
    public Goal(String title, String description, int progress) {
        this.title = title;
        this.description = description;
        this.progress = progress;
        this.progressHistory = new ArrayList<>();
        // Добавим начальный прогресс в историю
        addProgressEntry(progress);
    }

    // Геттеры и сеттеры
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getProgress() {
        return progress;
    }

    // При установке прогресса обновляем текущее значение и добавляем запись в историю
    public void setProgress(int progress) {
        this.progress = progress;
        addProgressEntry(progress);
    }

    // Получить историю прогресса
    public List<ProgressEntry> getProgressHistory() {
        return progressHistory;
    }

    // Добавить запись в историю прогресса с текущим временем
    public void addProgressEntry(int progress) {
        if (progressHistory == null) {
            progressHistory = new ArrayList<>();
        }
        progressHistory.add(new ProgressEntry(System.currentTimeMillis(), progress));
    }
}

