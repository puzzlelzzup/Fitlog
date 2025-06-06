package com.example.mp_termproject.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mp_termproject.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData; // 예시 import (MPAndroidChart)
import com.github.mikephil.charting.data.LineData; // 예시 import
import com.github.mikephil.charting.data.PieData; // 예시 import
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

// TODO: MPAndroidChart 관련 import 추가 (데이터 설정 시 필요)
// import com.github.mikephil.charting.data.BarDataSet;
// import com.github.mikephil.charting.data.BarEntry;
// import com.github.mikephil.charting.data.LineDataSet;
// import com.github.mikephil.charting.data.Entry;
// import com.github.mikephil.charting.data.PieDataSet;
// import com.github.mikephil.charting.data.PieEntry;
// import com.github.mikephil.charting.utils.ColorTemplate;
// import java.util.ArrayList;

public class StatsFragment extends Fragment {

    private ChipGroup chipGroupPeriod;
    private ImageButton btnCalendarSelect;

    private TextView tvWeightChangeTotal;
    private LineChart chartWeightChange;

    private BarChart chartWorkoutStats;
    private TextView tvTotalWorkoutTime, tvWorkoutDays, tvCaloriesBurned;

    private PieChart chartDietAnalysis;
    private ProgressBar progressProtein, progressCarbs, progressFat;
    private TextView tvProteinIntake, tvCarbsIntake, tvFatIntake;

    private SeekBar seekbarTargetWeight, seekbarWorkoutGoalDays, seekbarCalorieIntakeGoal;
    private TextView tvTargetWeightValue, tvWorkoutGoalDaysValue, tvCalorieIntakeGoalValue;

    public StatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 뷰 초기화
        chipGroupPeriod = view.findViewById(R.id.chip_group_period);
        btnCalendarSelect = view.findViewById(R.id.btn_calendar_select);

        tvWeightChangeTotal = view.findViewById(R.id.tv_weight_change_total);
        chartWeightChange = view.findViewById(R.id.chart_weight_change);

        chartWorkoutStats = view.findViewById(R.id.chart_workout_stats);
        tvTotalWorkoutTime = view.findViewById(R.id.tv_total_workout_time);
        tvWorkoutDays = view.findViewById(R.id.tv_workout_days);
        tvCaloriesBurned = view.findViewById(R.id.tv_calories_burned);

        chartDietAnalysis = view.findViewById(R.id.chart_diet_analysis);
        progressProtein = view.findViewById(R.id.progress_protein);
        tvProteinIntake = view.findViewById(R.id.tv_protein_intake);
        progressCarbs = view.findViewById(R.id.progress_carbs);
        tvCarbsIntake = view.findViewById(R.id.tv_carbs_intake);
        progressFat = view.findViewById(R.id.progress_fat);
        tvFatIntake = view.findViewById(R.id.tv_fat_intake);

        seekbarTargetWeight = view.findViewById(R.id.seekbar_target_weight);
        tvTargetWeightValue = view.findViewById(R.id.tv_target_weight_value);
        seekbarWorkoutGoalDays = view.findViewById(R.id.seekbar_workout_goal_days);
        tvWorkoutGoalDaysValue = view.findViewById(R.id.tv_workout_goal_days_value);
        seekbarCalorieIntakeGoal = view.findViewById(R.id.seekbar_calorie_intake_goal);
        tvCalorieIntakeGoalValue = view.findViewById(R.id.tv_calorie_intake_goal_value);

        setupPeriodSelector();
        setupGoalSeekBars();

        // TODO: Firebase 또는 로컬 DB에서 데이터 로드 및 차트/UI 업데이트
        loadAndDisplayData("weekly"); // 기본값: 주간
    }

    private void setupPeriodSelector() {
        chipGroupPeriod.setOnCheckedChangeListener((group, checkedId) -> {
            Chip chip = group.findViewById(checkedId);
            if (chip != null) {
                String period = chip.getText().toString().toLowerCase(); // "주간", "월간", "연간"
                Toast.makeText(getContext(), period + " 선택됨", Toast.LENGTH_SHORT).show();
                loadAndDisplayData(period);
            }
        });

        btnCalendarSelect.setOnClickListener(v -> {
            // TODO: DatePickerDialog 등을 사용하여 날짜 범위 선택 UI 표시
            Toast.makeText(getContext(), "날짜 범위 선택 기능 구현 예정", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupGoalSeekBars() {
        // 목표 체중 SeekBar
        seekbarTargetWeight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvTargetWeightValue.setText(String.format("%dkg", progress));
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO: 변경된 목표 체중 저장
            }
        });

        // 주간 운동 목표 SeekBar
        seekbarWorkoutGoalDays.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvWorkoutGoalDaysValue.setText(String.format("%d일", progress));
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO: 변경된 주간 운동 목표 저장
            }
        });

        // 일일 칼로리 섭취 목표 SeekBar
        seekbarCalorieIntakeGoal.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 50 단위로 표시 (선택사항)
                int displayProgress = (progress / 50) * 50;
                tvCalorieIntakeGoalValue.setText(String.format("%dkcal", displayProgress));
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {
                int finalProgress = (seekBar.getProgress() / 50) * 50;
                // TODO: 변경된 일일 칼로리 섭취 목표 저장 (finalProgress 사용)
            }
        });
        // TODO: 저장된 목표값 불러와서 SeekBar 초기값 설정
    }

    private void loadAndDisplayData(String period) {
        // TODO: 선택된 'period'에 따라 Firebase 또는 로컬 DB에서 데이터 가져오기
        // 1. 체중 변화 데이터 가져와서 LineChart 업데이트
        //    - setupLineChart(ArrayList<Entry> weightData);
        // 2. 운동 통계 데이터 가져와서 BarChart 및 TextView 업데이트
        //    - setupBarChart(ArrayList<BarEntry> workoutData);
        //    - tvTotalWorkoutTime.setText(...);
        // 3. 식단 분석 데이터 가져와서 PieChart 및 ProgressBar, TextView 업데이트
        //    - setupPieChart(ArrayList<PieEntry> dietData);
        //    - progressProtein.setProgress(...); tvProteinIntake.setText(...);
        Toast.makeText(getContext(), period + " 데이터 로딩 및 표시 (구현 예정)", Toast.LENGTH_SHORT).show();

        // --- 임시 데이터 및 차트 설정 예시 (실제 데이터로 교체 필요) ---
        // setupLineChartExample();
        // setupBarChartExample();
        // setupPieChartExample();
        // updateProgressBarsExample();
    }

    // --- 다음은 MPAndroidChart 설정 예시 함수들입니다. 실제 데이터로 채워야 합니다. ---
    /*
    private void setupLineChartExample() {
        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 64f)); // x: index, y: value
        entries.add(new Entry(1, 63.5f));
        entries.add(new Entry(2, 63f));
        entries.add(new Entry(3, 62.5f));
        entries.add(new Entry(4, 62f));

        LineDataSet dataSet = new LineDataSet(entries, "체중");
        dataSet.setColor(ContextCompat.getColor(getContext(), R.color.purple_500)); // 색상 설정
        dataSet.setValueTextColor(ContextCompat.getColor(getContext(), R.color.black));
        // ... 기타 LineDataSet 스타일링 ...

        LineData lineData = new LineData(dataSet);
        chartWeightChange.setData(lineData);
        chartWeightChange.getDescription().setEnabled(false); // 설명 비활성화
        chartWeightChange.invalidate(); // 차트 새로고침
    }

    private void setupBarChartExample() {
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, 60f));  // x: index, y: value (운동 시간 분)
        entries.add(new BarEntry(1, 90f));
        entries.add(new BarEntry(2, 75f));
        entries.add(new BarEntry(3, 120f));
        entries.add(new BarEntry(4, 45f));

        BarDataSet dataSet = new BarDataSet(entries, "운동 시간(분)");
        dataSet.setColor(ContextCompat.getColor(getContext(), R.color.teal_700));
        // ... 기타 BarDataSet 스타일링 ...

        BarData barData = new BarData(dataSet);
        chartWorkoutStats.setData(barData);
        chartWorkoutStats.setFitBars(true); // 막대 너비 자동 조절
        chartWorkoutStats.getDescription().setEnabled(false);
        chartWorkoutStats.invalidate();
    }

    private void setupPieChartExample() {
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(40f, "단백질"));
        entries.add(new PieEntry(35f, "탄수화물"));
        entries.add(new PieEntry(25f, "지방"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS); // 다양한 색상 템플릿
        dataSet.setValueTextSize(12f);
        // ... 기타 PieDataSet 스타일링 ...

        PieData pieData = new PieData(dataSet);
        chartDietAnalysis.setData(pieData);
        chartDietAnalysis.getDescription().setEnabled(false);
        chartDietAnalysis.setDrawHoleEnabled(true); // 도넛 모양
        chartDietAnalysis.setHoleColor(android.R.color.transparent);
        chartDietAnalysis.setUsePercentValues(true); // 백분율 표시
        chartDietAnalysis.setEntryLabelTextSize(0f); // 항목 레이블 숨기기 (선택사항)
        chartDietAnalysis.getLegend().setEnabled(false); // 범례 숨기기 (선택사항)
        chartDietAnalysis.invalidate();
    }

    private void updateProgressBarsExample() {
        // 임시 값 설정
        progressProtein.setMax(140);
        progressProtein.setProgress(120);
        tvProteinIntake.setText("120g / 140g");

        progressCarbs.setMax(200);
        progressCarbs.setProgress(180);
        tvCarbsIntake.setText("180g / 200g");

        progressFat.setMax(60);
        progressFat.setProgress(45);
        tvFatIntake.setText("45g / 60g");
    }
    */
}