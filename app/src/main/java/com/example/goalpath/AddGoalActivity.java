package com.example.goalpath;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class AddGoalActivity extends AppCompatActivity {

    private EditText editTextGoalTitle;
    private EditText editTextGoalDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_goal);

        editTextGoalTitle = findViewById(R.id.editTextGoalTitle);
        editTextGoalDescription = findViewById(R.id.editTextGoalDescription);
        Button buttonSaveGoal = findViewById(R.id.buttonSaveGoal);

        buttonSaveGoal.setOnClickListener(v -> {
            String title = editTextGoalTitle.getText().toString();
            String description = editTextGoalDescription.getText().toString();

            Intent resultIntent = new Intent();
            resultIntent.putExtra("goalTitle", title);
            resultIntent.putExtra("goalDescription", description);
            setResult(RESULT_OK, resultIntent);
            finish(); // Закрывает текущую активность и возвращает результат
        });
    }
}
