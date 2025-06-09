package com.example.mp_termproject.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mp_termproject.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MealFragment extends Fragment {

    private static final String TAG = "MealFragment";
    private static final String ARG_MEAL_TYPE = "meal_type";
    private static final String ARG_MEAL_TITLE = "meal_title";

    private String mealType;
    private String mealTitle;

    // UI Components
    private TextView tvMealTitle;
    private LinearLayout llMealItems;
    private TextView tvEmptyMessage;
    private LinearLayout llTotalCalories;
    private LinearLayout llCaloriesRow;
    private TextView tvTotalCalories;
    private FloatingActionButton fabAddMeal;

    // Data
    private List<MealItem> mealItems;

    public static MealFragment newInstance(String mealType, String mealTitle) {
        MealFragment fragment = new MealFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MEAL_TYPE, mealType);
        args.putString(ARG_MEAL_TITLE, mealTitle);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mealType = getArguments().getString(ARG_MEAL_TYPE);
            mealTitle = getArguments().getString(ARG_MEAL_TITLE);
        }
        mealItems = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_meal, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupListeners();
    }

    private void initViews(View view) {
        tvMealTitle = view.findViewById(R.id.tv_meal_title);
        llMealItems = view.findViewById(R.id.ll_meal_items);
        tvEmptyMessage = view.findViewById(R.id.tv_empty_message);
        llTotalCalories = view.findViewById(R.id.ll_total_calories);
        llCaloriesRow = view.findViewById(R.id.ll_calories_row);
        tvTotalCalories = view.findViewById(R.id.tv_total_calories);
        fabAddMeal = view.findViewById(R.id.fab_add_meal);

        // Set meal title
        if (tvMealTitle != null && mealTitle != null) {
            tvMealTitle.setText(mealTitle);
        }

        // Set empty message
        if (tvEmptyMessage != null && mealTitle != null) {
            tvEmptyMessage.setText(mealTitle.replace(" 식단", "") + " 식단을 추가해 보세요");
        }
    }

    private void setupListeners() {
        if (fabAddMeal != null) {
            fabAddMeal.setOnClickListener(v -> addMealItem());
        }
    }

    public void addMealItem() {
        addMealItem("", "", true); // 기본적으로 활성화 상태로 추가
    }

    public void addMealItem(String foodName, String calories) {
        addMealItem(foodName, calories, true); // 기본적으로 활성화 상태로 추가
    }

    // enabled 상태를 받을 수 있도록 오버로드된 addMealItem 메서드 추가
    public void addMealItem(String foodName, String calories, boolean enabled) {
        if (getContext() == null || llMealItems == null) {
            return;
        }

        if (tvEmptyMessage != null) {
            tvEmptyMessage.setVisibility(View.GONE);
        }
        if (llTotalCalories != null) {
            llTotalCalories.setVisibility(View.VISIBLE);
        }
        if (llCaloriesRow != null) {
            llCaloriesRow.setVisibility(View.VISIBLE);
        }

        // Inflate meal item layout
        View mealView = LayoutInflater.from(getContext()).inflate(R.layout.item_meal, llMealItems, false);

        // Get views from inflated layout
        EditText etFoodName = mealView.findViewById(R.id.et_food_name);
        EditText etCalories = mealView.findViewById(R.id.et_calories);
        ImageButton btnDeleteMeal = mealView.findViewById(R.id.btn_delete_meal);

        // Set data
        if (etFoodName != null) {
            etFoodName.setText(foodName != null ? foodName : "");
        }
        if (etCalories != null) {
            etCalories.setText(calories != null ? calories : "");
        }

        // Set initial enabled state
        if (etFoodName != null) etFoodName.setEnabled(enabled);
        if (etCalories != null) etCalories.setEnabled(enabled);
        if (btnDeleteMeal != null) btnDeleteMeal.setVisibility(enabled ? View.VISIBLE : View.GONE);


        // Create meal item object
        MealItem mealItem = new MealItem(mealView, etFoodName, etCalories);
        mealItems.add(mealItem);

        // Add text watcher to calories field for real-time total calculation
        if (etCalories != null) {
            etCalories.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    updateTotalCalories();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        // 버튼 리스너 삭제
        if (btnDeleteMeal != null) {
            btnDeleteMeal.setOnClickListener(v -> {
                // 삭제는 enabled(편집) 상태일 때만 허용
                if (etFoodName.isEnabled()) { // etFoodName이 enabled 상태면 편집 모드
                    llMealItems.removeView(mealView);
                    mealItems.remove(mealItem);

                    // Show empty message if no meals
                    if (mealItems.isEmpty()) {
                        if (tvEmptyMessage != null) tvEmptyMessage.setVisibility(View.VISIBLE);
                        if (llTotalCalories != null) llTotalCalories.setVisibility(View.GONE);
                        if (llCaloriesRow != null) llCaloriesRow.setVisibility(View.GONE);
                    }

                    updateTotalCalories();
                } else {
                    // 편집 모드가 아니면 토스트 메시지
                    Toast.makeText(getContext(), "편집 모드에서 식단을 삭제할 수 있습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Add to layout
        llMealItems.addView(mealView);
        updateTotalCalories();
    }

    private void updateTotalCalories() {
        int totalCalories = 0;

        for (MealItem item : mealItems) {
            if (item.getCaloriesEditText() != null) {
                String caloriesText = item.getCaloriesEditText().getText().toString().trim();
                if (!caloriesText.isEmpty()) {
                    try {
                        totalCalories += Integer.parseInt(caloriesText);
                    } catch (NumberFormatException e) {
                    }
                }
            }
        }

        if (tvTotalCalories != null) {
            tvTotalCalories.setText(totalCalories + " kcal");
        }
    }

    public Map<String, Object> getMealData() {
        Map<String, Object> mealData = new HashMap<>();
        List<Map<String, Object>> foods = new ArrayList<>();

        for (MealItem item : mealItems) {
            if (item.getFoodNameEditText() != null && item.getCaloriesEditText() != null) {
                String foodName = item.getFoodNameEditText().getText().toString().trim();
                String caloriesText = item.getCaloriesEditText().getText().toString().trim();

                // 이름이 비어있지 않은 항목만 추가
                if (!foodName.isEmpty()) {
                    Map<String, Object> food = new HashMap<>();
                    food.put("name", foodName);

                    try {
                        int calories = caloriesText.isEmpty() ? 0 : Integer.parseInt(caloriesText);
                        food.put("calories", calories);
                    } catch (NumberFormatException e) {
                        food.put("calories", 0);
                    }

                    foods.add(food);
                }
            }
        }

        mealData.put("foods", foods);
        mealData.put("total_calories", calculateTotalCalories());

        return mealData;
    }

    private int calculateTotalCalories() {
        int total = 0;
        for (MealItem item : mealItems) {
            if (item.getCaloriesEditText() != null) {
                String caloriesText = item.getCaloriesEditText().getText().toString().trim();
                if (!caloriesText.isEmpty()) {
                    try {
                        total += Integer.parseInt(caloriesText);
                    } catch (NumberFormatException e) {
                        // Ignore
                    }
                }
            }
        }
        return total;
    }

    // Firestore 기준 데이터 로딩
    public void loadMealData(Map<String, Object> mealData) {
        clearAllMealItems();

        if (mealData == null || !mealData.containsKey("foods")) {
            Log.d(TAG, "No meal data exists or foods not found for: " + mealType);
            updateUIVisibility();
            return;
        }

        Object foodsObj = mealData.get("foods");
        if (foodsObj instanceof List<?>) {
            List<?> foodsList = (List<?>) foodsObj;
            for (Object foodItem : foodsList) {
                if (foodItem instanceof Map) {
                    Map<?, ?> foodMap = (Map<?, ?>) foodItem;
                    String name = foodMap.get("name") != null ? foodMap.get("name").toString() : "";
                    String calories = foodMap.get("calories") != null ? foodMap.get("calories").toString() : "0";
                    if (!name.isEmpty()) {
                        addMealItem(name, calories); // addMealItem(name, calories, true)로 호출되므로 초기에는 활성화됨
                    }
                }
            }
        }

        updateUIVisibility();
    }

    private void clearAllMealItems() {
        if (llMealItems != null) {
            llMealItems.removeAllViews();
        }
        mealItems.clear();
    }

    private void updateUIVisibility() {
        boolean empty = mealItems.isEmpty();
        if (tvEmptyMessage != null) tvEmptyMessage.setVisibility(empty ? View.VISIBLE : View.GONE);
        if (llTotalCalories != null) llTotalCalories.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (llCaloriesRow != null) llCaloriesRow.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    // Helper methods for external access
    public List<String> getMealList() {
        List<String> foods = new ArrayList<>();
        for (MealItem item : mealItems) {
            if (item.getFoodNameEditText() != null) {
                String foodName = item.getFoodNameEditText().getText().toString().trim();
                if (!foodName.isEmpty()) {
                    foods.add(foodName);
                }
            }
        }
        return foods;
    }

    // UI 컴포넌트들을 활성화/비활성화하는 메서드 추가
    public void setEnabled(boolean enabled) {
        // 식단 추가 FloatingActionButton 제어
        if (fabAddMeal != null) {
            fabAddMeal.setVisibility(enabled ? View.VISIBLE : View.GONE);
        }

        // 현재 추가되어 있는 모든 MealItem의 입력 필드들을 제어
        // 이 for 루프는 addMealItem() 호출 후에 모든 MealItem이 mealItems 리스트에 채워진 다음에 실행되어야 함.
        // loadMealData() -> addMealItem() 호출 시에는 addMealItem 내부에서 enabled 상태를 설정해야 함.
        // 이 setEnabled는 이미 로드된 아이템들의 상태를 변경하거나, 새롭게 아이템을 추가한 후에 전체 상태를 맞출 때 사용.
        for (MealItem item : mealItems) {
            if (item.getFoodNameEditText() != null) item.getFoodNameEditText().setEnabled(enabled);
            if (item.getCaloriesEditText() != null) item.getCaloriesEditText().setEnabled(enabled);
            // 삭제 버튼은 편집 모드(enabled=true)일 때만 보이도록 설정
            if (item.getView() != null) {
                ImageButton btnDeleteMeal = item.getView().findViewById(R.id.btn_delete_meal);
                if (btnDeleteMeal != null) {
                    btnDeleteMeal.setVisibility(enabled ? View.VISIBLE : View.GONE);
                }
            }
        }
        // tvEmptyMessage, llTotalCalories 등의 가시성은 기존 로직 유지 (데이터 유무에 따라)
        updateUIVisibility(); // 가시성 업데이트 (필요시)
    }

    // Helper class
    public static class MealItem {
        private final View view;
        private final EditText foodNameEditText;
        private final EditText caloriesEditText;

        public MealItem(View view, EditText foodNameEditText, EditText caloriesEditText) {
            this.view = view;
            this.foodNameEditText = foodNameEditText;
            this.caloriesEditText = caloriesEditText;
        }

        public View getView() {
            return view;
        }

        public EditText getFoodNameEditText() {
            return foodNameEditText;
        }

        public EditText getCaloriesEditText() {
            return caloriesEditText;
        }

//        public String getFoodName() {
//            return foodNameEditText != null ? foodNameEditText.getText().toString().trim() : "";
//        }
//
//        public String getCalories() {
//            return caloriesEditText != null ? caloriesEditText.getText().toString().trim() : "";
//        }
    }
}