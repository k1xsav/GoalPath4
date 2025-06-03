package com.example.goalpath;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private GoalAdapter adapter;
    private ArrayList<Goal> goalList;
    private static final int ADD_GOAL_REQUEST = 1;
    private static final int REQUEST_CODE_POST_NOTIFICATIONS = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        goalList = new ArrayList<>();
        loadGoals(); // Загрузка целей при старте приложения

        RecyclerView recyclerView = findViewById(R.id.recyclerViewGoals);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GoalAdapter(goalList);
        recyclerView.setAdapter(adapter);

        Button buttonAddGoal = findViewById(R.id.buttonAddGoal);
        buttonAddGoal.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddGoalActivity.class);
            startActivityForResult(intent, ADD_GOAL_REQUEST);
        });

        // Установка слушателя для адаптера
        adapter.setOnItemClickListener(new GoalAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Toast.makeText(MainActivity.this, "Clicked: " + goalList.get(position).getTitle(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemDelete(int position) {
                showDeleteConfirmationDialog(position);
            }
        });

        // Запрос разрешения на уведомления для Android 13+
        requestNotificationPermissionIfNeeded();

        // Запланируйте выполнение NotificationWorker один раз с задержкой
        scheduleNotificationWorker();
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Запрашиваем разрешение
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_CODE_POST_NOTIFICATIONS);
            } else {
                Toast.makeText(this, "Разрешение на уведомления уже предоставлено", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Разрешение на уведомления не требуется для этой версии Android", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmationDialog(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Подтверждение удаления")
                .setMessage("Вы уверены, что хотите удалить эту цель?")
                .setPositiveButton("Да", (dialog, which) -> {
                    goalList.remove(position); // Удаление элемента из списка
                    adapter.notifyItemRemoved(position); // Уведомление адаптера об изменении
                    saveGoals(); // Сохранение изменений после удаления
                })
                .setNegativeButton("Нет", null)
                .show();
    }

    private void scheduleNotificationWorker() {
        // Создаем OneTimeWorkRequest с задержкой 1 час
        OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                .setInitialDelay(15, TimeUnit.HOURS) // задержка 1 час, можно изменить
                .build();

        // Запускаем работу один раз, если с таким именем еще нет
        WorkManager.getInstance(this).enqueueUniqueWork(
                "goalpath_notification",
                ExistingWorkPolicy.KEEP, // если работа с таким именем уже есть, не запускать новую
                notificationWork);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_GOAL_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                String title = data.getStringExtra("goalTitle");
                String description = data.getStringExtra("goalDescription");
                Goal newGoal = new Goal(title, description);
                goalList.add(newGoal);
                adapter.notifyDataSetChanged();
                saveGoals(); // Сохранение изменений после добавления
            }
        }
    }

    private void saveGoals() {
        try {
            SharedPreferences sharedPreferences = getSharedPreferences("goal_prefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            Gson gson = new Gson();
            String json = gson.toJson(goalList); // Конвертация списка целей в JSON
            editor.putString("goal_list", json);
            editor.apply(); // Применение изменений
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка при сохранении целей", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadGoals() {
        try {
            SharedPreferences sharedPreferences = getSharedPreferences("goal_prefs", MODE_PRIVATE);
            Gson gson = new Gson();
            String json = sharedPreferences.getString("goal_list", null);
            Type type = new TypeToken<ArrayList<Goal>>() {}.getType();
            goalList = gson.fromJson(json, type);
            if (goalList == null) {
                goalList = new ArrayList<>(); // Если список пуст, инициализируем новый
            }
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка при загрузке целей", Toast.LENGTH_SHORT).show();
        }
    }

    // Обработка результата запроса разрешения
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_POST_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Разрешение на уведомления предоставлено", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Разрешение на уведомления отклонено", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
