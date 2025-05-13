package com.example.mp_termproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class EditActivity extends AppCompatActivity {

    private EditText weightInput;
    private TextView dateTextView;
    private LinearLayout exerciseContainer;
    private Button addExerciseButton;
    private Button saveButton;
    private EditText breakfastMenuInput, lunchMenuInput, dinnerMenuInput, breakfastCalorieInput, lunchCalorieInput, dinnerCalorieInput;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        weightInput = findViewById(R.id.weight_input);
        exerciseContainer = findViewById(R.id.exercise_container);
        addExerciseButton = findViewById(R.id.add_exercise_button);
        saveButton = findViewById(R.id.save_button);
        breakfastMenuInput = findViewById(R.id.breakfast_menu_input);
        lunchMenuInput = findViewById(R.id.lunch_menu_input);
        dinnerMenuInput = findViewById(R.id.dinner_menu_input);
        breakfastCalorieInput = findViewById(R.id.breakfast_calorie_input);
        lunchCalorieInput = findViewById(R.id.lunch_calorie_input);
        dinnerCalorieInput = findViewById(R.id.dinner_calorie_input);

        dateTextView = findViewById(R.id.date_text);

        Intent intent = getIntent();
        int year = intent.getIntExtra("year", 0);
        int month = intent.getIntExtra("month", 0);
        int day = intent.getIntExtra("day", 0);

        String dateText = year + "년 " + (month + 1) + "월 " + day + "일";
        dateTextView.setText(dateText);


        addExerciseButton.setOnClickListener(v -> addExerciseField());
        saveButton.setOnClickListener(v -> saveData());
    }

    private void addExerciseField() {
        EditText newExercise = new EditText(this);
        newExercise.setHint("운동 입력");
        exerciseContainer.addView(newExercise);
    }

    private void saveData() {
        String weight = weightInput.getText().toString();
        Set<String> exerciseSet = new HashSet<>();
        String breakfastMenu = breakfastMenuInput.getText().toString().trim();
        String lunchMenu = lunchMenuInput.getText().toString().trim();
        String dinnerMenu = dinnerMenuInput.getText().toString().trim();

        String breakfastCalorie = breakfastCalorieInput.getText().toString().trim();
        String lunchCalorie= lunchCalorieInput.getText().toString().trim();
        String dinnerCalorie = dinnerCalorieInput.getText().toString().trim();


        for (int i = 0; i < exerciseContainer.getChildCount(); i++) {
            View view = exerciseContainer.getChildAt(i);
            if (view instanceof EditText) {
                String exercise = ((EditText) view).getText().toString().trim();
                if (!exercise.isEmpty()) {
                    exerciseSet.add(exercise);
                }
            }
        }

        Intent resultIntent = new Intent();

        resultIntent.putExtra("weight", weight);
        resultIntent.putExtra("exercises", exerciseSet.toArray(new String[0]));
        resultIntent.putExtra("breakfast_menu", breakfastMenu);
        resultIntent.putExtra("lunch_menu", lunchMenu);
        resultIntent.putExtra("dinner_menu", dinnerMenu);
        resultIntent.putExtra("breakfast_calorie", breakfastCalorie);
        resultIntent.putExtra("lunch_calorie", lunchCalorie);
        resultIntent.putExtra("dinner_calorie", dinnerCalorie);

        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
