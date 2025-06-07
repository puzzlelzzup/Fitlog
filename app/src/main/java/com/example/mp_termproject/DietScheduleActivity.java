package com.example.mp_termproject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.example.mp_termproject.fragment.MealFragment;

public class DietScheduleActivity extends AppCompatActivity {

    private static final String TAG = "DietScheduleActivity";

    // Firebase
    private FirebaseDatabase database;
    private DatabaseReference userRef;
    private FirebaseUser currentUser;

    // UI Components
    private EditText etWeight;
    private LinearLayout llExerciseList;
    private TextView tvNoExercise;
    private TextView tvExerciseCount;
    private FloatingActionButton fabAddExercise;
    private TabLayout tabMeals;
    private ViewPager2 viewPagerMeals;
    private Button btnSave;
    private Button btnBack;
    private TextView selected_date_textview;
    private int year, month, day;

    // Data
    private List<ExerciseItem> exerciseList;
    private MealPagerAdapter mealPagerAdapter;
    private int exerciseCounter = 0;

    // Meal fragments - store references directly
    private String[] mealTypes = {"breakfast", "lunch", "dinner"};
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dietschedule);

        // Initialize handler
        mainHandler = new Handler(Looper.getMainLooper());

        // Initialize Firebase
        database = FirebaseDatabase.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String uid = currentUser.getUid();
        userRef = database.getReference("Users").child(uid);

        Intent intent = getIntent();
        year = intent.getIntExtra("year", 0);
        month = intent.getIntExtra("month", 0);
        day = intent.getIntExtra("day", 0);

        String dateString = year + "년 " + (month + 1) + "월 " + day + "일";

        TextView dateTextView = findViewById(R.id.selected_date_textview);
        dateTextView.setText(dateString);

        // Initialize data
        exerciseList = new ArrayList<>();

        // Initialize views
        initViews();

        // Setup UI
        setupUI();

        // Setup listeners
        setupListeners();

        // Load existing data with proper delay
        loadDataWithDelay();
    }

    private void initViews() {
        etWeight = findViewById(R.id.et_weight);
        llExerciseList = findViewById(R.id.ll_exercise_list);
        tvNoExercise = findViewById(R.id.tv_no_exercise);
        tvExerciseCount = findViewById(R.id.tv_exercise_count);
        fabAddExercise = findViewById(R.id.fab_add_exercise);
        tabMeals = findViewById(R.id.tab_meals);
        viewPagerMeals = findViewById(R.id.viewpager_meals);
        btnSave = findViewById(R.id.btn_save);
        btnBack = findViewById(R.id.btn_back);
    }

    private void setupUI() {
        // Setup meal tabs and ViewPager
        mealPagerAdapter = new MealPagerAdapter(this);
        viewPagerMeals.setAdapter(mealPagerAdapter);

        new TabLayoutMediator(tabMeals, viewPagerMeals, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("아침");
                    break;
                case 1:
                    tab.setText("점심");
                    break;
                case 2:
                    tab.setText("저녁");
                    break;
            }
        }).attach();

        // Initialize exercise count display
        updateExerciseCount();
    }

    private void setupListeners() {
        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Save button
        btnSave.setOnClickListener(v -> {
            Log.d(TAG, "Save button clicked");
            if (validateData()) {
                saveData();
            }
        });

        // Add exercise button
        fabAddExercise.setOnClickListener(v -> addExerciseItem());
    }

    private void loadDataWithDelay() {

        mainHandler.postDelayed(() -> {

            viewPagerMeals.setOffscreenPageLimit(3);

            mainHandler.postDelayed(this::loadData, 200);
        }, 500);
    }

    private void addExerciseItem() {
        addExerciseItem("", "", "");
    }

    private void addExerciseItem(String name, String hours, String minutes) {
        exerciseCounter++;
        tvNoExercise.setVisibility(View.GONE);

        // Inflate exercise item layout
        View exerciseView = LayoutInflater.from(this).inflate(R.layout.item_exercise, llExerciseList, false);

        // Get views from inflated layout
        TextView tvExerciseNumber = exerciseView.findViewById(R.id.tv_exercise_number);
        EditText etExerciseName = exerciseView.findViewById(R.id.et_exercise_name);
        EditText etExerciseHours = exerciseView.findViewById(R.id.et_exercise_hours);
        EditText etExerciseMinutes = exerciseView.findViewById(R.id.et_exercise_minutes);
        ImageButton btnDeleteExercise = exerciseView.findViewById(R.id.btn_delete_exercise);

        // Set data
        tvExerciseNumber.setText("운동 " + exerciseCounter);
        etExerciseName.setText(name);
        etExerciseHours.setText(hours);
        etExerciseMinutes.setText(minutes);

        // Create exercise item object
        ExerciseItem exerciseItem = new ExerciseItem(exerciseView, etExerciseName, etExerciseHours, etExerciseMinutes);
        exerciseList.add(exerciseItem);

        // Delete button listener
        btnDeleteExercise.setOnClickListener(v -> {
            llExerciseList.removeView(exerciseView);
            exerciseList.remove(exerciseItem);
            exerciseCounter--;

            // Update exercise numbers
            updateExerciseNumbers();
            updateExerciseCount();

            // Show empty message if no exercises
            if (exerciseList.isEmpty()) {
                tvNoExercise.setVisibility(View.VISIBLE);
            }
        });

        // Add to layout
        llExerciseList.addView(exerciseView);
        updateExerciseCount();
    }

    private void updateExerciseNumbers() {
        for (int i = 0; i < exerciseList.size(); i++) {
            View exerciseView = exerciseList.get(i).getView();
            TextView tvExerciseNumber = exerciseView.findViewById(R.id.tv_exercise_number);
            tvExerciseNumber.setText("운동 " + (i + 1));
        }
    }

    private void updateExerciseCount() {
        int count = getCompletedExerciseCount();
        String countText;

        if (count == 0) {
            countText = "오늘 운동을 하지 않았습니다";
        } else if (count == 1) {
            countText = "오늘 1개의 운동을 했습니다";
        } else {
            countText = "오늘 " + count + "개의 운동을 했습니다";
        }

        tvExerciseCount.setText(countText);
    }

    private int getCompletedExerciseCount() {
        int count = 0;
        for (ExerciseItem item : exerciseList) {
            String exerciseName = item.getNameEditText().getText().toString().trim();
            if (!exerciseName.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    private boolean validateData() {
        // Check weight format if entered
        String weightStr = etWeight.getText().toString().trim();
        if (!weightStr.isEmpty()) {
            try {
                double weight = Double.parseDouble(weightStr);
                if (weight <= 0 || weight > 1000) {
                    showToast("몸무게를 올바르게 입력하세요 (1-1000kg)");
                    return false;
                }
            } catch (NumberFormatException e) {
                showToast("몸무게는 숫자만 입력하세요");
                return false;
            }
        }

        // Check exercise times
        for (ExerciseItem item : exerciseList) {
            String exerciseName = item.getNameEditText().getText().toString().trim();
            if (!exerciseName.isEmpty()) {
                String hours = item.getHoursEditText().getText().toString().trim();
                String minutes = item.getMinutesEditText().getText().toString().trim();

                try {
                    int h = hours.isEmpty() ? 0 : Integer.parseInt(hours);
                    int m = minutes.isEmpty() ? 0 : Integer.parseInt(minutes);

                    if (h < 0 || h > 24 || m < 0 || m >= 60) {
                        showToast("운동 시간을 올바르게 입력하세요 (시간: 0-24, 분: 0-59)");
                        return false;
                    }
                } catch (NumberFormatException e) {
                    showToast("운동 시간은 숫자만 입력하세요");
                    return false;
                }
            }
        }

        return true;
    }

    private void saveData() {
        String dateKey = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
        DatabaseReference dateRef = userRef.child("diet_schedule").child(dateKey);

        // 몸무게 저장
        String weight = etWeight.getText().toString().trim();
        if (!weight.isEmpty()) {
            dateRef.child("weight").setValue(weight);
        }

        // 운동 루틴 저장
        dateRef.child("exercises").removeValue();
        for (int i = 0; i < exerciseList.size(); i++) {
            ExerciseItem item = exerciseList.get(i);
            Map<String, Object> exerciseData = new HashMap<>();
            exerciseData.put("name", item.getName());
            exerciseData.put("hours", item.getHours());
            exerciseData.put("minutes", item.getMinutes());
            dateRef.child("exercises").child(String.valueOf(i)).setValue(exerciseData);
        }

        // 식단 저장
        for (int i = 0; i < mealTypes.length; i++) {
            String mealType = mealTypes[i];
            MealFragment fragment = mealPagerAdapter.getFragment(i);

            if (fragment != null) {
                DatabaseReference mealRef = dateRef.child("meals").child(mealType);
                Map<String, Object> mealData = fragment.getMealData();

                mealRef.setValue(mealData).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Meal data saved successfully for " + mealType);
                    } else {
                        Log.e(TAG, "Failed to save meal data for " + mealType, task.getException());
                    }
                });
            } else {
                Log.w(TAG, "Fragment is null for meal type: " + mealType);
            }
        }

        updateExerciseCount();
        showToast("저장되었습니다!");
    }

    private void loadData() {
        String dateKey = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
        DatabaseReference dateRef = userRef.child("diet_schedule").child(dateKey);

        dateRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    Log.d(TAG, "Loading data for date: " + dateKey);

                    // 몸무게 불러오기
                    if (snapshot.hasChild("weight")) {
                        String weight = snapshot.child("weight").getValue(String.class);
                        etWeight.setText(weight != null ? weight : "");
                    }

                    // 운동 루틴 불러오기
                    exerciseList.clear();
                    llExerciseList.removeAllViews();
                    exerciseCounter = 0;

                    if (snapshot.hasChild("exercises")) {
                        for (DataSnapshot exerciseSnapshot : snapshot.child("exercises").getChildren()) {
                            String name = exerciseSnapshot.child("name").getValue(String.class);
                            String hours = exerciseSnapshot.child("hours").getValue(String.class);
                            String minutes = exerciseSnapshot.child("minutes").getValue(String.class);
                            addExerciseItem(name != null ? name : "",
                                    hours != null ? hours : "",
                                    minutes != null ? minutes : "");
                        }
                    }

                    // 식단 불러오기
                    if (snapshot.hasChild("meals")) {
                        for (int i = 0; i < mealTypes.length; i++) {
                            String mealType = mealTypes[i];
                            MealFragment fragment = mealPagerAdapter.getFragment(i);

                            if (fragment != null) {
                                if (snapshot.child("meals").hasChild(mealType)) {
                                    DataSnapshot mealSnapshot = snapshot.child("meals").child(mealType);
                                    fragment.loadMealData(mealSnapshot);
                                } else {
                                    Log.d(TAG, "No data found for meal type: " + mealType);
                                }
                            } else {
                                Log.w(TAG, "Fragment is null for meal type: " + mealType + " (index: " + i + ")");
                                // Retry loading this meal data after a delay
                                final String finalMealType = mealType;
                                final int finalIndex = i;
                                mainHandler.postDelayed(() -> {
                                    MealFragment retryFragment = mealPagerAdapter.getFragment(finalIndex);
                                    if (retryFragment != null && snapshot.child("meals").hasChild(finalMealType)) {
                                        DataSnapshot mealSnapshot = snapshot.child("meals").child(finalMealType);
                                        retryFragment.loadMealData(mealSnapshot);
                                    }
                                }, 300);
                            }
                        }
                    }

                    // 운동 없을 경우 메시지 표시
                    if (exerciseList.isEmpty()) {
                        tvNoExercise.setVisibility(View.VISIBLE);
                    } else {
                        tvNoExercise.setVisibility(View.GONE);
                    }

                    updateExerciseCount();
                } catch (Exception e) {
                    showToast("데이터 불러오기 중 오류가 발생했습니다");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToast("데이터 불러오기 실패: " + error.getMessage());
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // ViewPager Adapter for meal tabs
    private class MealPagerAdapter extends FragmentStateAdapter {
        private final List<MealFragment> fragments = new ArrayList<>();

        public MealPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
            fragments.add(MealFragment.newInstance("breakfast", "아침 식단"));
            fragments.add(MealFragment.newInstance("lunch", "점심 식단"));
            fragments.add(MealFragment.newInstance("dinner", "저녁 식단"));
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return fragments.get(position);
        }

        @Override
        public int getItemCount() {
            return 3;
        }

        public MealFragment getFragment(int position) {
            if (position >= 0 && position < fragments.size()) {
                return fragments.get(position);
            }
            return null;
        }
    }

    // Helper class to hold exercise item data
    private static class ExerciseItem {
        private final View view;
        private final EditText nameEditText;
        private final EditText hoursEditText;
        private final EditText minutesEditText;

        public ExerciseItem(View view, EditText nameEditText, EditText hoursEditText, EditText minutesEditText) {
            this.view = view;
            this.nameEditText = nameEditText;
            this.hoursEditText = hoursEditText;
            this.minutesEditText = minutesEditText;
        }

        public View getView() {
            return view;
        }

        public EditText getNameEditText() {
            return nameEditText;
        }

        public EditText getHoursEditText() {
            return hoursEditText;
        }

        public EditText getMinutesEditText() {
            return minutesEditText;
        }

        public String getName() {
            return nameEditText.getText().toString().trim();
        }

        public String getHours() {
            return hoursEditText.getText().toString().trim();
        }

        public String getMinutes() {
            return minutesEditText.getText().toString().trim();
        }
    }
}