package com.example.mp_termproject;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class GoalSettingActivity extends AppCompatActivity {

    private EditText etAge, etCurrentWeight, etHeight, etTargetLoss, etDietDuration;
    private RadioGroup rgGender;
    private Spinner spinnerActivityLevel;
    private Button btnSaveGoal;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_setting);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        initializeViews();
        setupSpinner();

        btnSaveGoal.setOnClickListener(v -> {
            if (validateInput()) {
                calculateAndSaveAll();
            }
        });
    }

    private void initializeViews() {
        etAge = findViewById(R.id.et_age);
        etCurrentWeight = findViewById(R.id.et_current_weight);
        etHeight = findViewById(R.id.et_height);
        etTargetLoss = findViewById(R.id.et_target_loss);
        etDietDuration = findViewById(R.id.et_diet_duration);
        rgGender = findViewById(R.id.rg_gender);
        spinnerActivityLevel = findViewById(R.id.spinner_activity_level);
        btnSaveGoal = findViewById(R.id.btn_save_goal);
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.activity_levels, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerActivityLevel.setAdapter(adapter);
    }

    private boolean validateInput() {
        if (TextUtils.isEmpty(etAge.getText()) || TextUtils.isEmpty(etCurrentWeight.getText()) ||
                TextUtils.isEmpty(etHeight.getText()) || TextUtils.isEmpty(etTargetLoss.getText()) ||
                TextUtils.isEmpty(etDietDuration.getText()) || rgGender.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (Integer.parseInt(etDietDuration.getText().toString()) == 0) {
            Toast.makeText(this, "다이어트 기간은 1일 이상이어야 합니다.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void calculateAndSaveAll() {
        // 1. 입력값 가져오기
        int age = Integer.parseInt(etAge.getText().toString());
        double currentWeight = Double.parseDouble(etCurrentWeight.getText().toString());
        double height = Double.parseDouble(etHeight.getText().toString());
        double targetLoss = Double.parseDouble(etTargetLoss.getText().toString());
        int dietDuration = Integer.parseInt(etDietDuration.getText().toString());
        boolean isMale = ((RadioButton)findViewById(rgGender.getCheckedRadioButtonId())).getText().toString().equals("남자");
        int activityLevelPosition = spinnerActivityLevel.getSelectedItemPosition();

        // 2. BMR, TDEE, 최종 목표 칼로리 계산
        double bmr = isMale ? (10 * currentWeight + 6.25 * height - 5 * age + 5) : (10 * currentWeight + 6.25 * height - 5 * age - 161);

        double activityMultiplier = 1.2;
        switch (activityLevelPosition) {
            case 1: activityMultiplier = 1.375; break;
            case 2: activityMultiplier = 1.55; break;
            case 3: activityMultiplier = 1.725; break;
        }

        double tdee = bmr * activityMultiplier;
        double targetDailyCalories = tdee - (targetLoss * 7700 / dietDuration);

        // targetDailyCalories 계산 검증
        if (targetDailyCalories < 800) { // 최소 섭취 칼로리(예: 800)보다 낮으면
            Toast.makeText(this, "목표가 너무 과도합니다. 기간을 늘리거나 감량 목표를 조절해주세요.", Toast.LENGTH_LONG).show();
            return; // 저장하지 않고 함수 종료
        }

        // 3. 탄단지 그램 계산 (5:3:2 비율)
        double targetCarbsGrams = (targetDailyCalories * 0.5) / 4;
        double targetProteinGrams = (targetDailyCalories * 0.3) / 4;
        double targetFatGrams = (targetDailyCalories * 0.2) / 9;

        // 4. 목표 종료일 계산
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, dietDuration);

        // 5. Firestore에 모든 정보 저장
        Map<String, Object> userGoalData = new HashMap<>();
        userGoalData.put("age", age);
        userGoalData.put("gender", isMale ? "male" : "female");
        userGoalData.put("height", height);
        userGoalData.put("activityLevel", activityLevelPosition);
        userGoalData.put("initialWeight", currentWeight);
        userGoalData.put("targetWeight", currentWeight - targetLoss);
        userGoalData.put("dietStartDate", Calendar.getInstance().getTime());
        userGoalData.put("dietEndDate", cal.getTime());
        userGoalData.put("targetDailyCalories", targetDailyCalories);
        userGoalData.put("targetCarbsGrams", targetCarbsGrams);
        userGoalData.put("targetProteinGrams", targetProteinGrams);
        userGoalData.put("targetFatGrams", targetFatGrams);

        if (currentUser != null) {
            DocumentReference userDocRef = db.collection("users").document(currentUser.getUid());
            userDocRef.set(userGoalData, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "목표가 저장되었습니다!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
}