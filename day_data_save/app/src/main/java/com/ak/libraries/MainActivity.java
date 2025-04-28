package com.ak.libraries;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ak.ColoredDate;
import com.ak.EventObjects;
import com.ak.KalendarView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private Date lastClickDate = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        KalendarView mKalendarView = findViewById(R.id.kalendar);
        // Access a Cloud Firestore instance from your Acitvity
        db = FirebaseFirestore.getInstance();

        List<ColoredDate> datesColors = new ArrayList<>();
        datesColors.add(new ColoredDate(new Date(), getResources().getColor(R.color.red_holiday)));
        mKalendarView.setColoredDates(datesColors);

        List<EventObjects> events = new ArrayList<>();
        events.add(new EventObjects("meeting",new Date()));
        mKalendarView.setEvents(events);

        mKalendarView.setDateSelector(new KalendarView.DateSelector() {
            @Override
            public void onDateClicked(Date selectedDate) {
                Log.d("DateSel",selectedDate.toString());
                // 날짜 더블 클릭시 생성
                if (lastClickDate != null && isSameDay(lastClickDate, selectedDate)){
                    showDialog1(selectedDate);
                    lastClickDate = null;
                }
                else {
                    lastClickDate = selectedDate;
                }
            }
        });
        // 달이 바뀌는 경우 로그 출력
        mKalendarView.setMonthChanger(changedMonth -> Log.d("Changed","month changed "+changedMonth));
//        Calendar tempCal = Calendar.getInstance();
//        tempCal.set(Calendar.DATE,11);
//        List<EventObjects> events2 = new ArrayList<>();
//        events2.add(new EventObjects("meeting",tempCal.getTime()));
//
//        tempCal.set(Calendar.DATE,15);
//        List<ColoredDate> datesColors2 = new ArrayList<>();
//        datesColors2.add(new ColoredDate(tempCal.getTime(), getResources().getColor(R.color.red_holiday)));
//        mKalendarView.addColoredDates(datesColors2);
//
//        mKalendarView.addEvents(events2);

//        Button btBottomDialog = findViewById(R.id.bt_bottom_dialog);
//        btBottomDialog.setOnClickListener(view -> showDialog());

    }

//    void showDialog(){
//        BottomSheetDialog dialog = new BottomSheetDialog(this);
//        View bottomSheet = getLayoutInflater().inflate(R.layout.bottom_dialog, null);
//
//        Button demo = bottomSheet.findViewById(R.id.bt_bottom_dialog);
//        KalendarView kalendarView = bottomSheet.findViewById(R.id.kalendar);
//        demo.setOnClickListener(
//                view -> {
//                    kalendarView.setInitialSelectedDate(new Date());
//                }
//        );
//
//        dialog.setContentView(bottomSheet);
//        dialog.show();
//    }
    // 같은 날짜 인지 비교
    private boolean isSameDay(Date date1, Date date2){
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    void showDialog1(Date selectedDate){

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View bottomSheet = getLayoutInflater().inflate(R.layout.bottom_dialog, null);
        // 날짜 표시
        TextView selectedDateText = bottomSheet.findViewById(R.id.selected_date);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = sdf.format(selectedDate);
        selectedDateText.setText(dateString);

        // id 찾기
        EditText weightInput = bottomSheet.findViewById(R.id.weight_input);
        EditText bodyFatInput = bottomSheet.findViewById(R.id.body_fat_input);
        EditText workoutInput = bottomSheet.findViewById(R.id.workout_input);
        EditText mealInput = bottomSheet.findViewById(R.id.meal_input);
        Button saveButton = bottomSheet.findViewById(R.id.save_button);
        // 저장 버튼 클릭 시 Firestore에 데이터 저장
        saveButton.setOnClickListener(view -> {
            String weight = weightInput.getText().toString();
            String bodyFat = bodyFatInput.getText().toString();
            String workout = workoutInput.getText().toString();
            String meal = mealInput.getText().toString();

            // Firestore에 데이터 저장
            Map<String, Object> data = new HashMap<>();
            data.put("weight", weight);
            data.put("bodyFat", bodyFat);
            data.put("workout", workout);
            data.put("meal", meal);
            data.put("date", selectedDate);

            db.collection("dailyRecords").add(data)
                    .addOnSuccessListener(documentReference -> {
                        Log.d("Firestore", "DocumentSnapshot added with ID: " + documentReference.getId());
                        dialog.dismiss();  // 다이얼로그 닫기
                    })
                    .addOnFailureListener(e -> Log.w("Firestore", "Error adding document", e));
        });

        dialog.setContentView(bottomSheet);
        dialog.show();
    }

}