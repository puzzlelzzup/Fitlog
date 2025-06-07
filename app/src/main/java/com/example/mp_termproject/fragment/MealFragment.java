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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mp_termproject.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
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
        addMealItem("", "");
    }

    public void addMealItem(String foodName, String calories) {
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

        // Delete button listener
        if (btnDeleteMeal != null) {
            btnDeleteMeal.setOnClickListener(v -> {
                llMealItems.removeView(mealView);
                mealItems.remove(mealItem);

                // Show empty message if no meals
                if (mealItems.isEmpty()) {
                    if (tvEmptyMessage != null) {
                        tvEmptyMessage.setVisibility(View.VISIBLE);
                    }
                    if (llTotalCalories != null) {
                        llTotalCalories.setVisibility(View.GONE);
                    }
                    if (llCaloriesRow != null) {
                        llCaloriesRow.setVisibility(View.GONE);
                    }
                }

                updateTotalCalories();
            });
        }
        // Add to layout
        llMealItems.addView(mealView);

        // Update total calories
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
                        // Ignore invalid numbers

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

                if (!foodName.isEmpty()) {
                    Map<String, Object> food = new HashMap<>();
                    food.put("name", foodName);

                    try {
                        int calories = caloriesText.isEmpty() ? 0 : Integer.parseInt(caloriesText);
                        food.put("calories", calories);
                        foods.add(food);
                    } catch (NumberFormatException e) {
                        food.put("calories", 0);
                        foods.add(food);
                    }
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
                        // Ignore invalid numbers
                    }
                }
            }
        }
        return total;
    }

    public void loadMealData(DataSnapshot mealSnapshot) {

        // Clear existing items first
        clearAllMealItems();

        if (mealSnapshot.exists()) {

            if (mealSnapshot.hasChild("foods")) {
                for (DataSnapshot foodSnapshot : mealSnapshot.child("foods").getChildren()) {
                    String name = foodSnapshot.child("name").getValue(String.class);
                    Integer calories = foodSnapshot.child("calories").getValue(Integer.class);

                    if (name != null) {
                        String caloriesStr = calories != null ? String.valueOf(calories) : "0";
                        addMealItem(name, caloriesStr);
                    }
                }
            } else {

                for (DataSnapshot foodSnapshot : mealSnapshot.getChildren()) {
                    String name = foodSnapshot.child("name").getValue(String.class);
                    Integer calories = foodSnapshot.child("calories").getValue(Integer.class);

                    if (name != null) {
                        String caloriesStr = calories != null ? String.valueOf(calories) : "0";
                        addMealItem(name, caloriesStr);
                    }
                }
            }
        } else {
            Log.d(TAG, "No meal data exists for: " + mealType);
        }

        // Update UI visibility based on whether we have items
        updateUIVisibility();
    }

    private void clearAllMealItems() {
        if (llMealItems != null) {
            llMealItems.removeAllViews();
        }
        mealItems.clear();
    }

    private void updateUIVisibility() {
        if (mealItems.isEmpty()) {
            if (tvEmptyMessage != null) {
                tvEmptyMessage.setVisibility(View.VISIBLE);
            }
            if (llTotalCalories != null) {
                llTotalCalories.setVisibility(View.GONE);
            }
            if (llCaloriesRow != null) {
                llCaloriesRow.setVisibility(View.GONE);
            }
        } else {
            if (tvEmptyMessage != null) {
                tvEmptyMessage.setVisibility(View.GONE);
            }
            if (llTotalCalories != null) {
                llTotalCalories.setVisibility(View.VISIBLE);
            }
            if (llCaloriesRow != null) {
                llCaloriesRow.setVisibility(View.VISIBLE);
            }
        }
    }

    // Utility methods for external access
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

    public int getTotalCalories() {
        return calculateTotalCalories();
    }

    public int getMealItemCount() {
        return mealItems.size();
    }

    public MealItem getMealItem(int index) {
        if (index >= 0 && index < mealItems.size()) {
            return mealItems.get(index);
        }
        return null;
    }

    // Helper class to hold meal item data
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

        public String getFoodName() {
            return foodNameEditText != null ? foodNameEditText.getText().toString().trim() : "";
        }

        public String getCalories() {
            return caloriesEditText != null ? caloriesEditText.getText().toString().trim() : "";
        }
    }
}