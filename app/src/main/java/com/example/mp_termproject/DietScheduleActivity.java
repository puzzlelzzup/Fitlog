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

import com.example.mp_termproject.fragment.MealFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch; // WriteBatch import 추가

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DietScheduleActivity extends AppCompatActivity {

    private static final String TAG = "DietScheduleActivity";

    // Firebase
    private FirebaseFirestore firestore;
    private DocumentReference userDocRef;
    private FirebaseUser currentUser;

    // UI Components
    private EditText etWeight;
    private LinearLayout llExerciseList;
    private TextView tvNoExercise;
    private TextView tvExerciseCount;
    private FloatingActionButton fabAddExercise;
    private TabLayout tabMeals;
    private ViewPager2 viewPagerMeals;
    private Button btnSave, backCancel;
    private ImageButton btnBack;
    private Button btnToggleEditMode; // 새로 추가된 편집/보기 전환 버튼
    private TextView selected_date_textview;
    private int year, month, day;

    // Data
    private List<ExerciseItem> exerciseList;
    private MealPagerAdapter mealPagerAdapter;
    private int exerciseCounter = 0;
    private boolean isViewMode = true; // 현재 보기 모드인지 편집 모드인지 상태 변수

    // Meal fragments
    private String[] mealTypes = {"breakfast", "lunch", "dinner"};
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dietschedule);

        // Initialize handler
        mainHandler = new Handler(Looper.getMainLooper());

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String uid = currentUser.getUid();
        userDocRef = firestore.collection("users").document(uid);

        Intent intent = getIntent();
        year = intent.getIntExtra("year", 0);
        month = intent.getIntExtra("month", 0);
        day = intent.getIntExtra("day", 0);

        String dateString = year + "년 " + (month + 1) + "월 " + day + "일";

        selected_date_textview = findViewById(R.id.selected_date_textview);
        selected_date_textview.setText(dateString);

        // Initialize data
        exerciseList = new ArrayList<>();

        // Initialize views
        initViews();

        // Setup UI
        setupUI();

        // Setup listeners
        setupListeners();

        // Check if coming from double click (edit mode)
        // Intent에 editMode extra가 있다면 바로 편집 모드로 진입
        isViewMode = !intent.getBooleanExtra("editMode", false);
        Log.d(TAG, "Initial isViewMode: " + isViewMode);

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
        backCancel = findViewById(R.id.back_cancel);
        btnToggleEditMode = findViewById(R.id.btn_toggle_edit_mode); // 편집/보기 버튼 초기화
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

        // exercise count display 초기화
        updateExerciseCount();
    }

    private void setupListeners() {
        // Back button
        btnBack.setOnClickListener(v -> finish());
        backCancel.setOnClickListener(v -> finish());

        // Save button (visibility controlled by toggleEditMode)
        btnSave.setOnClickListener(v -> {
            Log.d(TAG, "Save button clicked");
            if (validateData()) {
                saveData();
                showToast("저장되었습니다!");
                // 저장 후 다시 보기 모드로 전환 (선택 사항)
                isViewMode = true; // 현재 모드를 보기로 설정
                toggleEditMode(); // UI 업데이트
            }
        });

        // Add exercise button (visibility controlled by toggleEditMode)
        fabAddExercise.setOnClickListener(v -> addExerciseItem());

        // Toggle Edit Mode Button
        btnToggleEditMode.setOnClickListener(v -> {
            Log.d(TAG, "Toggle Edit Mode button clicked. Current isViewMode: " + isViewMode);
            toggleEditMode();
        });
    }

    private void loadDataWithDelay() {
        mainHandler.postDelayed(() -> {
            viewPagerMeals.setOffscreenPageLimit(3);
            mainHandler.postDelayed(this::loadData, 200);
        }, 500);
    }

    private void addExerciseItem() {
        // 편집 모드일 때만 추가 허용
        if (!isViewMode) {
            addExerciseItem("", "", "", !isViewMode); // isEnabled 값을 전달
        } else {
            showToast("편집 모드에서 운동을 추가할 수 있습니다.");
        }
    }

    private void addExerciseItem(String name, String hours, String minutes) {
        addExerciseItem(name, hours, minutes, !isViewMode); // isEnabled 값을 전달
    }

    private void addExerciseItem(String name, String hours, String minutes, boolean enabled) {
        exerciseCounter++;
        tvNoExercise.setVisibility(View.GONE);

        View view = LayoutInflater.from(this).inflate(R.layout.item_exercise, llExerciseList, false);

        TextView tvNumber = view.findViewById(R.id.tv_exercise_number);
        EditText etName = view.findViewById(R.id.et_exercise_name);
        EditText etHours = view.findViewById(R.id.et_exercise_hours);
        EditText etMinutes = view.findViewById(R.id.et_exercise_minutes);
        ImageButton btnDelete = view.findViewById(R.id.btn_delete_exercise);

        tvNumber.setText("운동 " + exerciseCounter);
        etName.setText(name);
        etHours.setText(hours);
        etMinutes.setText(minutes);

        // Set initial enabled state
        etName.setEnabled(enabled);
        etHours.setEnabled(enabled);
        etMinutes.setEnabled(enabled);
        btnDelete.setVisibility(enabled ? View.VISIBLE : View.GONE);

        ExerciseItem item = new ExerciseItem(view, etName, etHours, etMinutes);
        exerciseList.add(item);

        btnDelete.setOnClickListener(v -> {
            // Delete only allowed in edit mode
            if (!isViewMode) {
                llExerciseList.removeView(view);
                exerciseList.remove(item);
                exerciseCounter--;
                updateExerciseNumbers();
                updateExerciseCount();
                if (exerciseList.isEmpty()) tvNoExercise.setVisibility(View.VISIBLE);
            } else {
                showToast("편집 모드에서 운동을 삭제할 수 있습니다.");
            }
        });

        llExerciseList.addView(view);
        updateExerciseCount();
    }

    private void updateExerciseNumbers() {
        for (int i = 0; i < exerciseList.size(); i++) {
            TextView tvNumber = exerciseList.get(i).getView().findViewById(R.id.tv_exercise_number);
            tvNumber.setText("운동 " + (i + 1));
        }
    }

    private void updateExerciseCount() {
        int count = getCompletedExerciseCount();
        String text = count == 0 ? "오늘 운동을 하지 않았습니다" : "오늘 " + count + "개의 운동을 했습니다";
        tvExerciseCount.setText(text);
    }

    private int getCompletedExerciseCount() {
        int count = 0;
        for (ExerciseItem item : exerciseList) {
            // 운동 이름이 비어있지 않으면 유효한 운동으로 간주
            if (!item.getName().isEmpty()) count++;
        }
        return count;
    }

    private boolean validateData() {
        String weight = etWeight.getText().toString().trim();
        if (!weight.isEmpty()) {
            try {
                double w = Double.parseDouble(weight);
                if (w <= 0 || w > 1000) {
                    showToast("몸무게를 올바르게 입력하세요 (1-1000kg)");
                    return false;
                }
            } catch (NumberFormatException e) {
                showToast("몸무게는 숫자만 입력하세요");
                return false;
            }
        }
        for (ExerciseItem item : exerciseList) {
            if (!item.getName().isEmpty()) { // 운동 이름이 있는 경우에만 시간 유효성 검사
                try {
                    int h = item.getHours().isEmpty() ? 0 : Integer.parseInt(item.getHours());
                    int m = item.getMinutes().isEmpty() ? 0 : Integer.parseInt(item.getMinutes());
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
        DocumentReference dateRef = userDocRef.collection("diet_schedule").document(dateKey);

        Map<String, Object> mainData = new HashMap<>();
        String weight = etWeight.getText().toString().trim();

        // data 필드 추가 저장
        mainData.put("date", dateKey); // 날짜 필드 추가
        if (!weight.isEmpty()) {
            mainData.put("weight", weight);
        } else {
            mainData.put("weight", null); // 몸무게가 비어있으면 삭제 또는 null로 저장
        }

        dateRef.set(mainData, SetOptions.merge())
                .addOnFailureListener(e -> Log.e(TAG, "Main data save failed: " + e.getMessage()));

        // (아래 운동, 식단 저장 로직은 그대로 유지)
        dateRef.collection("exercises").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                WriteBatch batch = firestore.batch();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    batch.delete(doc.getReference());
                }
                batch.commit().addOnSuccessListener(aVoid -> {
                    saveNewExerciseData(dateRef);
                }).addOnFailureListener(e -> Log.e(TAG, "Failed to delete old exercise data: " + e.getMessage()));
            } else {
                Log.e(TAG, "Failed to fetch old exercise data: " + task.getException());
            }
        });

        for (int i = 0; i < mealTypes.length; i++) {
            final String currentMealType = mealTypes[i];
            MealFragment fragment = mealPagerAdapter.getFragment(i);
            if (fragment != null) {
                Map<String, Object> mealData = fragment.getMealData();
                if (mealData != null && mealData.containsKey("foods") && !((List)mealData.get("foods")).isEmpty()) {
                    dateRef.collection("meals").document(currentMealType).set(mealData)
                            .addOnFailureListener(e -> Log.e(TAG, "Meal data save failed for " + currentMealType + ": " + e.getMessage()));
                } else {
                    dateRef.collection("meals").document(currentMealType).delete()
                            .addOnFailureListener(e -> Log.e(TAG, "Failed to delete empty meal data for " + currentMealType + ": " + e.getMessage()));
                }
            }
        }
    }

    private void saveNewExerciseData(DocumentReference dateRef) {
        // 운동 데이터가 하나도 없으면 exercise 컬렉션 전체 삭제
        if (exerciseList.isEmpty() || getCompletedExerciseCount() == 0) {
            dateRef.collection("exercises").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    WriteBatch deleteBatch = firestore.batch();
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        deleteBatch.delete(doc.getReference());
                    }
                    deleteBatch.commit().addOnFailureListener(e -> Log.e(TAG, "Failed to delete exercises collection: " + e.getMessage()));
                }
            });
            return;
        }

        CollectionReference exerciseRef = dateRef.collection("exercises");
        WriteBatch batch = firestore.batch();
        for (int i = 0; i < exerciseList.size(); i++) {
            Map<String, Object> data = new HashMap<>();
            ExerciseItem item = exerciseList.get(i);
            // 운동 이름이 비어있지 않은 유효한 항목만 저장
            if (!item.getName().isEmpty()) {
                data.put("name", item.getName());
                data.put("hours", item.getHours());
                data.put("minutes", item.getMinutes());
                batch.set(exerciseRef.document(String.valueOf(i)), data); // 문서 ID를 인덱스로 사용
            }
        }
        batch.commit().addOnFailureListener(e -> Log.e(TAG, "Failed to save new exercise data: " + e.getMessage()));
    }

    private void loadData() {
        String dateKey = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
        DocumentReference dateRef = userDocRef.collection("diet_schedule").document(dateKey);

        dateRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String weight = snapshot.getString("weight");
                etWeight.setText(weight != null ? weight : "");
            } else {
                // 문서가 없는 경우 (새로운 날짜)
                etWeight.setText("");
            }
            // 초기 UI 모드 설정 (editMode intent 또는 isViewMode 상태에 따라)
            toggleEditMode(); // 이 호출로 isViewMode가 한번 더 반전됨 -> 원하는 최종 모드에 맞춰 isViewMode 설정 필요

            dateRef.collection("exercises").get().addOnSuccessListener(exSnap -> {
                exerciseList.clear();
                llExerciseList.removeAllViews();
                exerciseCounter = 0;
                for (QueryDocumentSnapshot doc : exSnap) {
                    addExerciseItem(doc.getString("name"), doc.getString("hours"), doc.getString("minutes"), !isViewMode); // isViewMode가 반전되어 전달됨
                }
                if (exerciseList.isEmpty()) tvNoExercise.setVisibility(View.VISIBLE);
                updateExerciseCount();
            }).addOnFailureListener(e -> Log.e(TAG, "Exercise data load failed: " + e.getMessage()));

            dateRef.collection("meals").get().addOnSuccessListener(mealSnap -> {
                for (QueryDocumentSnapshot meal : mealSnap) {
                    int idx = Arrays.asList(mealTypes).indexOf(meal.getId());
                    if (idx != -1) {
                        MealFragment frag = mealPagerAdapter.getFragment(idx);
                        if (frag != null) {
                            frag.loadMealData(meal.getData());
                            frag.setEnabled(!isViewMode); // MealFragment의 입력 필드 활성화/비활성화
                        }
                    }
                }
            }).addOnFailureListener(e -> Log.e(TAG, "Meal data load failed: " + e.getMessage()));

        }).addOnFailureListener(e -> {
            showToast("데이터 불러오기 실패: " + e.getMessage());
            Log.e(TAG, "Diet schedule data load failed: " + e.getMessage());
        });
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void toggleEditMode() {
        Log.d(TAG, "toggleEditMode called. Current isViewMode (for UI setup): " + isViewMode);

        // UI 요소의 활성화/비활성화 및 가시성 설정
        etWeight.setEnabled(!isViewMode);
        fabAddExercise.setVisibility(isViewMode ? View.GONE : View.VISIBLE);
        btnSave.setVisibility(isViewMode ? View.GONE : View.VISIBLE); // 저장 버튼은 편집 모드에서만 보이게

        // 운동 항목의 EditText도 활성화/비활성화해야 함.
        for (ExerciseItem item : exerciseList) {
            item.getNameEditText().setEnabled(!isViewMode);
            item.getHoursEditText().setEnabled(!isViewMode);
            item.getMinutesEditText().setEnabled(!isViewMode);
            item.getView().findViewById(R.id.btn_delete_exercise).setVisibility(isViewMode ? View.GONE : View.VISIBLE);
        }

        // MealFragment들도 업데이트
        for (int i = 0; i < mealTypes.length; i++) {
            MealFragment frag = mealPagerAdapter.getFragment(i);
            if (frag != null) {
                frag.setEnabled(!isViewMode); // MealFragment의 setEnabled 메서드 호출
            }
        }

        // 버튼 텍스트 변경
        btnToggleEditMode.setText(isViewMode ? "편집" : "보기");
    }

    private class MealPagerAdapter extends FragmentStateAdapter {
        private final List<MealFragment> fragments = List.of(
                MealFragment.newInstance("breakfast", "아침 식단"),
                MealFragment.newInstance("lunch", "점심 식단"),
                MealFragment.newInstance("dinner", "저녁 식단")
        );

        public MealPagerAdapter(@NonNull FragmentActivity fa) {
            super(fa);
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
            return position >= 0 && position < fragments.size() ? fragments.get(position) : null;
        }
    }

    private static class ExerciseItem {
        private final View view;
        private final EditText nameEditText, hoursEditText, minutesEditText;

        public ExerciseItem(View v, EditText name, EditText h, EditText m) {
            this.view = v;
            this.nameEditText = name;
            this.hoursEditText = h;
            this.minutesEditText = m;
        }

        public View getView() { return view; }
        public EditText getNameEditText() { return nameEditText; }
        public EditText getHoursEditText() { return hoursEditText; }
        public EditText getMinutesEditText() { return minutesEditText; }

        public String getName() { return nameEditText.getText().toString().trim(); }
        public String getHours() { return hoursEditText.getText().toString().trim(); }
        public String getMinutes() { return minutesEditText.getText().toString().trim(); }
    }
}