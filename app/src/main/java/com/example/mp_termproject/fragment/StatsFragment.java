package com.example.mp_termproject.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.mp_termproject.GoalSettingActivity;
import com.example.mp_termproject.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class StatsFragment extends Fragment {

    private static final String TAG = "StatsFragment";

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private ChipGroup chipGroupPeriod;
    private LineChart chartWeightChange;
    private PieChart chartDietAnalysis;
    private LinearLayout layoutGoalSetup, layoutGoalSummary;
    private Button btnGoToGoalSetting, btnEditGoal;
    private TextView tvDaysRemaining, tvSummaryTargetWeight, tvSummaryRecoCalories;
    private TextView tvRecoCarbs, tvRecoProtein, tvRecoFat;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeFirebase();
        initializeViews(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (currentUser != null) {
            loadUserGoals();
        }
    }

    private void initializeFirebase() {
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    private void initializeViews(View view) {
        chipGroupPeriod = view.findViewById(R.id.chip_group_period);
        chartWeightChange = view.findViewById(R.id.chart_weight_change);
        chartDietAnalysis = view.findViewById(R.id.chart_diet_analysis);

        layoutGoalSetup = view.findViewById(R.id.layout_goal_setup);
        layoutGoalSummary = view.findViewById(R.id.layout_goal_summary);
        btnGoToGoalSetting = view.findViewById(R.id.btn_go_to_goal_setting);
        btnEditGoal = view.findViewById(R.id.btn_edit_goal);
        tvDaysRemaining = view.findViewById(R.id.tv_days_remaining);
        tvSummaryTargetWeight = view.findViewById(R.id.tv_summary_target_weight);
        tvSummaryRecoCalories = view.findViewById(R.id.tv_summary_reco_calories);

        tvRecoCarbs = view.findViewById(R.id.tv_reco_carbs);
        tvRecoProtein = view.findViewById(R.id.tv_reco_protein);
        tvRecoFat = view.findViewById(R.id.tv_reco_fat);

        View.OnClickListener goalSettingClickListener = v -> {
            Intent intent = new Intent(getActivity(), GoalSettingActivity.class);
            startActivity(intent);
        };
        btnGoToGoalSetting.setOnClickListener(goalSettingClickListener);
        btnEditGoal.setOnClickListener(goalSettingClickListener);

        setupPeriodSelector();
    }

    private void setupPeriodSelector() {
        chipGroupPeriod.setOnCheckedChangeListener((group, checkedId) -> {
            Chip chip = group.findViewById(checkedId);
            if (chip != null && chip.isChecked()) {
                String period = chip.getText().toString(); // ← 추가
                loadWeightData(chip.getText().toString());

            }
        });
    }

    private void loadUserGoals() {
        if (currentUser == null) return;
        DocumentReference userDocRef = db.collection("users").document(currentUser.getUid());

        userDocRef.get().addOnSuccessListener(snapshot -> {
            if (!isAdded()) return;

            if (snapshot.exists() && snapshot.contains("targetDailyCalories")) {
                layoutGoalSetup.setVisibility(View.GONE);
                layoutGoalSummary.setVisibility(View.VISIBLE);

                double targetWeight = snapshot.getDouble("targetWeight");
                double recoCalories = snapshot.getDouble("targetDailyCalories");
                Timestamp endDateTimestamp = snapshot.getTimestamp("dietEndDate");

                long daysRemaining = 0;
                if (endDateTimestamp != null) {
                    long diff = endDateTimestamp.toDate().getTime() - new Date().getTime();
                    daysRemaining = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
                }

                tvDaysRemaining.setText(String.format(Locale.getDefault(), "D-%d", daysRemaining));
                tvSummaryTargetWeight.setText(String.format(Locale.getDefault(), "%.1fkg", targetWeight));
                tvSummaryRecoCalories.setText(String.format(Locale.getDefault(), "%.0fkcal", recoCalories));

                double recoCarbs = snapshot.getDouble("targetCarbsGrams");
                double recoProtein = snapshot.getDouble("targetProteinGrams");
                double recoFat = snapshot.getDouble("targetFatGrams");
                tvRecoCarbs.setText(String.format(Locale.getDefault(), "%.0fg", recoCarbs));
                tvRecoProtein.setText(String.format(Locale.getDefault(), "%.0fg", recoProtein));
                tvRecoFat.setText(String.format(Locale.getDefault(), "%.0fg", recoFat));

                // 데이터를 사용해서 차트 그리기
                setupDietAnalysisCharts(recoProtein, recoCarbs, recoFat);

            } else {
                layoutGoalSetup.setVisibility(View.VISIBLE);
                layoutGoalSummary.setVisibility(View.GONE);

                // 목표 없을 때 차트 비우기
                chartDietAnalysis.clear();
                chartDietAnalysis.setNoDataText("목표를 설정하면 권장 비율이 표시됩니다.");
                chartDietAnalysis.invalidate();
            }

            loadWeightData("주간");
        });
    }

    private void loadWeightData(String period) {
        if (currentUser == null) return;
        CollectionReference scheduleRef = db.collection("users").document(currentUser.getUid()).collection("diet_schedule");

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String endDateStr = sdf.format(calendar.getTime());
        switch (period) {
            case "주간": calendar.add(Calendar.DAY_OF_YEAR, -6); break;
            case "월간": calendar.add(Calendar.DAY_OF_YEAR, -29); break;
            case "연간": calendar.add(Calendar.YEAR, -1); break;
        }
        String startDateStr = sdf.format(calendar.getTime());

        scheduleRef.whereLessThan("date", startDateStr)
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(lastKnownWeightSnapshot -> {
                    final Float[] lastKnownWeight = {null};
                    if (!lastKnownWeightSnapshot.isEmpty()) {
                        DocumentSnapshot doc = lastKnownWeightSnapshot.getDocuments().get(0);
                        if (doc.contains("weight") && doc.getString("weight") != null) {
                            try {
                                lastKnownWeight[0] = Float.parseFloat(doc.getString("weight"));
                            } catch (NumberFormatException e) {
                            }
                        }
                    }

                    scheduleRef.whereGreaterThanOrEqualTo("date", startDateStr)
                            .whereLessThanOrEqualTo("date", endDateStr)
                            .orderBy("date", Query.Direction.ASCENDING)
                            .get()
                            .addOnSuccessListener(periodDataSnapshot -> {
                                if (!isAdded()) return;
                                Map<String, Float> existingData = new HashMap<>();
                                for (DocumentSnapshot doc : periodDataSnapshot) {
                                    if (doc.contains("weight") && doc.getString("weight") != null) {
                                        try {
                                            existingData.put(doc.getString("date"), Float.parseFloat(doc.getString("weight")));
                                        } catch (NumberFormatException e) {
                                        }
                                    }
                                }
                                Map<String, Float> filledData = new HashMap<>();
                                Calendar cal = Calendar.getInstance();
                                try {
                                    cal.setTime(Objects.requireNonNull(sdf.parse(startDateStr)));
                                } catch (ParseException e) { return; }
                                Date endDate;
                                try {
                                    endDate = sdf.parse(endDateStr);
                                } catch (ParseException e) { return; }
                                while (!cal.getTime().after(endDate)) {
                                    String currentDateStr = sdf.format(cal.getTime());
                                    if (existingData.containsKey(currentDateStr)) {
                                        lastKnownWeight[0] = existingData.get(currentDateStr);
                                    }
                                    if (lastKnownWeight[0] != null) {
                                        filledData.put(currentDateStr, lastKnownWeight[0]);
                                    }
                                    cal.add(Calendar.DAY_OF_YEAR, 1);
                                }
                                setupLineChart(filledData);
                            });
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load weight data", e));
    }

    private void setupLineChart(Map<String, Float> data) {
        if (!isAdded() || data.isEmpty()) {
            chartWeightChange.clear();
            chartWeightChange.setNoDataText("표시할 체중 데이터가 없습니다.");
            chartWeightChange.invalidate();
            return;
        }
        List<String> sortedDates = new ArrayList<>(data.keySet());
        Collections.sort(sortedDates);
        ArrayList<Entry> entries = new ArrayList<>();
        for (int i = 0; i < sortedDates.size(); i++) {
            entries.add(new Entry(i, data.get(sortedDates.get(i))));
        }
        LineDataSet dataSet = new LineDataSet(entries, "체중(kg)");
        dataSet.setColor(ContextCompat.getColor(requireContext(), R.color.purple_500));
        dataSet.setCircleColor(ContextCompat.getColor(requireContext(), R.color.purple_700));
        dataSet.setLineWidth(2f);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);

        chartWeightChange.setData(new LineData(dataSet));
        chartWeightChange.getDescription().setEnabled(false);
        chartWeightChange.getLegend().setEnabled(false);
        chartWeightChange.getAxisRight().setEnabled(false);
        chartWeightChange.getXAxis().setDrawGridLines(false);
        chartWeightChange.getAxisLeft().setDrawGridLines(false);
        XAxis xAxis = chartWeightChange.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, com.github.mikephil.charting.components.AxisBase axis) {
                int index = (int) value;
                if (index == 0 || index == sortedDates.size() / 2 || index == sortedDates.size() - 1) {
                    if (index < sortedDates.size()) {
                        return sortedDates.get(index).substring(5);
                    }
                }
                return "";
            }
        });
        chartWeightChange.invalidate();
    }

    // 식단 분석 차트 설정
    private void setupDietAnalysisCharts(double recoProtein, double recoCarbs, double recoFat) {
        if (!isAdded()) return;

        // 칼로리로 변환하여 비율 계산
        float proteinCal = (float) recoProtein * 4;
        float carbsCal = (float) recoCarbs * 4;
        float fatCal = (float) recoFat * 9;

        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        pieEntries.add(new PieEntry(carbsCal, "")); // 5
        pieEntries.add(new PieEntry(proteinCal, "")); // 3
        pieEntries.add(new PieEntry(fatCal, ""));   // 2

        PieDataSet pieDataSet = new PieDataSet(pieEntries, "");

        // 색상 설정 (탄수화물, 단백질, 지방 순서)
        final int[] MY_COLORS = {
                Color.rgb(63, 204, 181),  // 탄수화물 (청록 계열)
                Color.rgb(238, 83, 83),   // 단백질 (빨강 계열)
                Color.rgb(255, 204, 75)   // 지방 (노랑 계열)
        };
        ArrayList<Integer> colors = new ArrayList<>();
        for(int c: MY_COLORS) colors.add(c);
        pieDataSet.setColors(colors);

        pieDataSet.setDrawValues(false); // 차트 조각 위에 값 표시 안 함

        PieData pieData = new PieData(pieDataSet);

        chartDietAnalysis.setData(pieData);
        chartDietAnalysis.setUsePercentValues(true);
        chartDietAnalysis.getDescription().setEnabled(false);
        chartDietAnalysis.setRotationEnabled(false);
        chartDietAnalysis.setHighlightPerTapEnabled(false);

        // 도넛 차트로 만들기
        chartDietAnalysis.setDrawHoleEnabled(true);
        chartDietAnalysis.setHoleColor(Color.TRANSPARENT);
        chartDietAnalysis.setHoleRadius(45f);
        chartDietAnalysis.setTransparentCircleRadius(48f);

        // 범례(Legend) 스타일링
        Legend l = chartDietAnalysis.getLegend();
        l.setEnabled(false); // 범례는 숨김

        // 차트 새로고침
        chartDietAnalysis.invalidate();
        LinearLayout legendLayout = requireView().findViewById(R.id.layout_diet_legend);
        legendLayout.removeAllViews(); // 기존 뷰 제거

        String[] labels = {"탄수화물", "단백질", "지방"};
        float[] kcalValues = {carbsCal, proteinCal, fatCal};

        for (int i = 0; i < labels.length; i++) {
            TextView legendItem = new TextView(requireContext());
            legendItem.setText("■ " + labels[i] + "  " + Math.round(kcalValues[i]) + "kcal");
            legendItem.setTextColor(MY_COLORS[i]);
            legendItem.setTextSize(14);
            legendItem.setPadding(0, 8, 0, 8);
            legendLayout.addView(legendItem);
        }
    }
}