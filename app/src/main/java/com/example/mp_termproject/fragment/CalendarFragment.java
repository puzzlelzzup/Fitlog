package com.example.mp_termproject.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button; // [추가] Button import
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import androidx.navigation.fragment.NavHostFragment;

import com.ak.KalendarView;
import com.example.mp_termproject.DietScheduleActivity;
import com.example.mp_termproject.R;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarFragment extends Fragment {

    private static final String TAG = "CalendarFragment";

    private KalendarView mKalendarView;
    private TextView todayTitle; // 오늘의 기록 -> 날짜 텍스트 변경

    private LinearLayout llDailySummaryContainer;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private Map<String, Long> lastClickTime = new HashMap<>();
    private static final long DOUBLE_CLICK_TIME_DELTA = 500;

    private int displayedYear, displayedMonth;
    private int selectedDayForSummary;

    // UploadFragment로 전달할 데이터를 임시 저장할 멤버 변수
    private String lastLoadedWeight = "";
    private String lastLoadedExercises = "";
    private int lastLoadedCalories = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        Calendar currentCalendar = Calendar.getInstance();
        displayedYear = currentCalendar.get(Calendar.YEAR);
        displayedMonth = currentCalendar.get(Calendar.MONTH);
        selectedDayForSummary = currentCalendar.get(Calendar.DAY_OF_MONTH);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        mKalendarView = view.findViewById(R.id.kalendar_view_in_fragment);
        todayTitle = view.findViewById(R.id.today_title);
        llDailySummaryContainer = view.findViewById(R.id.ll_daily_summary_container);

        loadDailySummary(displayedYear, displayedMonth, selectedDayForSummary);

        mKalendarView.setDateSelector(selectedDate -> {
            if (!isAdded() || getActivity() == null) return;

            Log.d(TAG, "Selected Date: " + selectedDate.toString());
            Calendar cal = Calendar.getInstance();
            cal.setTime(selectedDate);

            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);

            handleDateClick(year, month, day);
        });

        mKalendarView.setMonthChanger(changedMonth -> {
            if (!isAdded()) return;
            Log.d(TAG, "Month Changed: " + changedMonth);
            Calendar newMonthCal = Calendar.getInstance();
            newMonthCal.setTime(changedMonth);
            displayedYear = newMonthCal.get(Calendar.YEAR);
            displayedMonth = newMonthCal.get(Calendar.MONTH);

            loadDailySummary(displayedYear, displayedMonth, 1);
            selectedDayForSummary = 1;
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDailySummary(displayedYear, displayedMonth, selectedDayForSummary);
    }

    private void handleDateClick(int year, int month, int day) {
        String dateKey = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
        long currentTime = System.currentTimeMillis();

        if (lastClickTime.containsKey(dateKey) && (currentTime - lastClickTime.get(dateKey)) < DOUBLE_CLICK_TIME_DELTA) {
            Log.d(TAG, "Double click detected for " + dateKey + ". Opening DietScheduleActivity in EDIT mode.");
            Intent intent = new Intent(getActivity(), DietScheduleActivity.class);
            intent.putExtra("year", year);
            intent.putExtra("month", month);
            intent.putExtra("day", day);
            intent.putExtra("editMode", true);
            startActivity(intent);
            lastClickTime.remove(dateKey);
        } else {
            Log.d(TAG, "Single click detected for " + dateKey + ". Loading daily summary.");
            selectedDayForSummary = day;
            if (todayTitle != null) {
                String dateText = String.format(Locale.getDefault(), "%d월 %d일 일정", month + 1, day);
                todayTitle.setText(dateText);
            }
            loadDailySummary(year, month, day);
            lastClickTime.put(dateKey, currentTime);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (lastClickTime.containsKey(dateKey) && lastClickTime.get(dateKey) == currentTime) {
                    lastClickTime.remove(dateKey);
                }
            }, DOUBLE_CLICK_TIME_DELTA);
        }
    }

    private void loadDailySummary(int year, int month, int day) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            llDailySummaryContainer.removeAllViews();
            TextView tv = new TextView(getContext());
            tv.setText("로그인이 필요합니다.");
            llDailySummaryContainer.addView(tv);
            return;
        }

        String dateKey = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
        DocumentReference dateDocRef = db.collection("users").document(currentUser.getUid())
                .collection("diet_schedule").document(dateKey);

        llDailySummaryContainer.removeAllViews();
        // 데이터 초기화
        lastLoadedWeight = "";
        lastLoadedExercises = "";
        lastLoadedCalories = 0;


        List<Task<?>> tasks = new ArrayList<>();
        final boolean[] hasDataRef = {false};

        // 1. 체중 정보 로드
        Task<Void> weightTask = dateDocRef.get().continueWithTask(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                String weight = task.getResult().getString("weight");
                if (weight != null && !weight.isEmpty()) {
                    addSummaryItem("몸무게", weight + " kg");
                    lastLoadedWeight = weight + " kg"; // [추가] 데이터 저장
                    hasDataRef[0] = true;
                }
            }
            return Tasks.forResult((Void) null);
        }).addOnFailureListener(e -> Log.e(TAG, "체중 정보 로드 실패: " + e.getMessage()));
        tasks.add(weightTask);

        // 2. 운동 정보 로드
        Task<Void> exerciseTask = dateDocRef.collection("exercises").get().continueWithTask(task -> {
            if (task.isSuccessful()) {
                List<String> exerciseNames = new ArrayList<>();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    String name = doc.getString("name");
                    if (name != null && !name.isEmpty()) {
                        exerciseNames.add(name);
                    }
                }
                if (!exerciseNames.isEmpty()) {
                    String summary = String.join(", ", exerciseNames);
                    addSummaryItem("운동", summary);
                    lastLoadedExercises = summary; // 데이터 저장
                    hasDataRef[0] = true;
                }
            }
            return Tasks.forResult((Void) null);
        }).addOnFailureListener(e -> Log.e(TAG, "운동 정보 로드 실패: " + e.getMessage()));
        tasks.add(exerciseTask);

        // 3. 식단 정보 로드
        Task<Void> mealTask = dateDocRef.collection("meals").get().continueWithTask(task -> {
            if (task.isSuccessful()) {
                int totalCalories = 0;
                for (QueryDocumentSnapshot mealDoc : task.getResult()) {
                    Map<String, Object> mealData = mealDoc.getData();
                    if (mealData != null && mealData.containsKey("foods")) {
                        Object foodsObj = mealData.get("foods");
                        if (foodsObj instanceof List<?>) {
                            List<?> foodsList = (List<?>) foodsObj;
                            for (Object foodItem : foodsList) {
                                if (foodItem instanceof Map) {
                                    Map<?, ?> foodMap = (Map<?, ?>) foodItem;
                                    Object caloriesObj = foodMap.get("calories");
                                    if (caloriesObj instanceof Number) {
                                        totalCalories += ((Number) caloriesObj).intValue();
                                    }
                                }
                            }
                        }
                    }
                }
                if (totalCalories > 0) {
                    addSummaryItem("총 섭취 칼로리", totalCalories + " kcal");
                    lastLoadedCalories = totalCalories; // [추가] 데이터 저장
                    hasDataRef[0] = true;
                }
            }
            return Tasks.forResult((Void) null);
        }).addOnFailureListener(e -> Log.e(TAG, "식단 정보 로드 실패: " + e.getMessage()));
        tasks.add(mealTask);

        Tasks.whenAllComplete(tasks).addOnCompleteListener(allTasks -> {
            if (!hasDataRef[0] && llDailySummaryContainer.getChildCount() == 0) {
                TextView noDataTv = new TextView(getContext());
                noDataTv.setText("저장된 일정이 없습니다. 일정을 추가해 보세요.");
                if (getContext() != null) {
                    noDataTv.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
                }
                noDataTv.setPadding(32, 32, 32, 32);
                noDataTv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                llDailySummaryContainer.addView(noDataTv);
            } else if (hasDataRef[0]) {
                // 데이터가 있을 경우에만 '게시물 공유' 버튼 추가
                addShareButton();
            }
        });
    }

    private void addSummaryItem(String title, String content) {
        if (getContext() == null || llDailySummaryContainer == null) return;

        View summaryItemView = LayoutInflater.from(getContext()).inflate(R.layout.item_daily_summary, llDailySummaryContainer, false);
        TextView tvTitle = summaryItemView.findViewById(R.id.tv_summary_title);
        TextView tvContent = summaryItemView.findViewById(R.id.tv_summary_content);

        tvTitle.setText(title);
        tvContent.setText(content);

        llDailySummaryContainer.addView(summaryItemView);
    }

    // '게시물로 공유' 버튼을 추가하는 메소드
    private void addShareButton() {
        if (getContext() == null || llDailySummaryContainer == null) return;

        // 버튼 생성
        Button shareButton = new Button(getContext());
        shareButton.setText("이 날의 기록으로 게시물 작성하기");
        // 버튼에 디자인을 적용하고 싶다면 스타일을 정의해서 사용 가능
        // 예: shareButton.setTextAppearance(R.style.MyButtonStyle);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        params.setMargins(32, 32, 32, 32);
        shareButton.setLayoutParams(params);
        shareButton.setBackgroundResource(R.drawable.button_background); // 예시: drawable에 버튼 배경 추가
        if(isAdded()) { // 프래그먼트가 액티비티에 붙어있는지 확인
            shareButton.setTextColor(ContextCompat.getColor(getContext(), android.R.color.white));
        }

        // 버튼 클릭 리스너 설정
        shareButton.setOnClickListener(v -> {
            navigateToUploadFragment();
        });

        // 컨테이너에 버튼 추가
        llDailySummaryContainer.addView(shareButton);
    }

    // UploadFragment로 이동하고 데이터를 전달하는 메소드
    private void navigateToUploadFragment() {
        if (!isAdded()) return; // 프래그먼트가 활성화 상태가 아니면 중단

        UploadFragment uploadFragment = new UploadFragment();

        // Bundle에 데이터 담기
        Bundle args = new Bundle();
        args.putString("ARG_EXERCISES", lastLoadedExercises);
        args.putString("ARG_WEIGHT", lastLoadedWeight);
        args.putInt("ARG_CALORIES", lastLoadedCalories);
        uploadFragment.setArguments(args);

        NavHostFragment.findNavController(CalendarFragment.this)
                .navigate(R.id.action_calendarFragment_to_uploadFragment, args);
    }
}