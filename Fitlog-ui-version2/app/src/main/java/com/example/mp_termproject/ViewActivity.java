package com.example.mp_termproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ViewActivity extends AppCompatActivity {

    private TextView weightTextView;
    private TextView dateTextView;
    private LinearLayout exerciseCheckboxContainer;
    private Button editButton;
    private TextView breakfastMenuTextView, lunchMenuTextView, dinnerMenuTextView;
    private TextView breakfastCalorieTextView, lunchCalorieTextView, dinnerCalorieTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        dateTextView = findViewById(R.id.date_text);
        weightTextView = findViewById(R.id.weight_text);
        exerciseCheckboxContainer = findViewById(R.id.exercise_checkbox_container);
        editButton = findViewById(R.id.edit_button);
        breakfastMenuTextView = findViewById(R.id.breakfast_menu_text);
        lunchMenuTextView = findViewById(R.id.lunch_menu_text);
        dinnerMenuTextView = findViewById(R.id.dinner_menu_text);
        breakfastCalorieTextView = findViewById(R.id.breakfast_calorie_text);
        lunchCalorieTextView = findViewById(R.id.lunch_calorie_text);
        dinnerCalorieTextView = findViewById(R.id.dinner_calorie_text);

        Intent intent = getIntent();
        int year = intent.getIntExtra("year", 0);
        int month = intent.getIntExtra("month", 0); // 주의: month는 0부터 시작해 (1월이 0)
        int day = intent.getIntExtra("day", 0);

        String dateText = year + "년 " + (month + 1) + "월 " + day + "일";
        dateTextView.setText(dateText);


        editButton.setOnClickListener(v -> {
            Intent editIntent = new Intent(ViewActivity.this, EditActivity.class);

            editIntent.putExtra("year", year);
            editIntent.putExtra("month", month);
            editIntent.putExtra("day", day);

            startActivityForResult(editIntent, 1);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {

            String weight = data.getStringExtra("weight");
            String[] exercises = data.getStringArrayExtra("exercises");
            String breakfastMenu = data.getStringExtra("breakfast_menu");
            String lunchMenu = data.getStringExtra("lunch_menu");
            String dinnerMenu = data.getStringExtra("dinner_menu");
            String breakfastCalorie = data.getStringExtra("breakfast_calorie"); // ★ 추가
            String lunchCalorie = data.getStringExtra("lunch_calorie");           // ★ 추가
            String dinnerCalorie = data.getStringExtra("dinner_calorie");

            weightTextView.setText("몸무게: " + weight + " kg");
            breakfastMenuTextView.setText(breakfastMenu);
            lunchMenuTextView.setText(lunchMenu);
            dinnerMenuTextView.setText(dinnerMenu);
            breakfastCalorieTextView.setText(breakfastCalorie + " kcal");
            lunchCalorieTextView.setText(lunchCalorie + " kcal");
            dinnerCalorieTextView.setText(dinnerCalorie + " kcal");

            exerciseCheckboxContainer.removeAllViews();
            if (exercises != null) {
                for (String exercise : exercises) {
                    CheckBox checkBox = new CheckBox(this);
                    checkBox.setText(exercise);
                    exerciseCheckboxContainer.addView(checkBox);
                }
            }
        }
    }
}
