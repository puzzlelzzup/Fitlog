package com.example.mp_termproject;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.ak.ColoredDate;
import com.ak.EventObjects;
import com.ak.KalendarView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    KalendarView mKalendarView;
    Button btBottomDialog, btLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mKalendarView = findViewById(R.id.kalendar);
        btBottomDialog = findViewById(R.id.bt_bottom_dialog);
        btLogout = findViewById(R.id.bt_logout); //  로그아웃 버튼 연결

        // 날짜 색 지정
        List<ColoredDate> datesColors = new ArrayList<>();
        datesColors.add(new ColoredDate(new Date(), getResources().getColor(R.color.red_holiday)));
        mKalendarView.setColoredDates(datesColors);

        // 이벤트 등록
        List<EventObjects> events = new ArrayList<>();
        events.add(new EventObjects("meeting", new Date()));
        mKalendarView.setEvents(events);

        // 날짜 클릭 이벤트
        mKalendarView.setDateSelector(selectedDate -> {
            Log.d("DateSel", selectedDate.toString());
            Calendar cal = Calendar.getInstance();
            cal.setTime(selectedDate);

            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);

            Intent intent = new Intent(MainActivity.this, ViewActivity.class);
            intent.putExtra("year", year);
            intent.putExtra("month", month);
            intent.putExtra("day", day);
            startActivity(intent);
        });

        // 월 변경 로그
        mKalendarView.setMonthChanger(changedMonth -> Log.d("Changed", "month changed " + changedMonth));

        // 다른 날짜 이벤트 추가
        Calendar tempCal = Calendar.getInstance();
        tempCal.set(Calendar.DATE, 11);
        List<EventObjects> events2 = new ArrayList<>();
        events2.add(new EventObjects("meeting", tempCal.getTime()));

        tempCal.set(Calendar.DATE, 15);
        List<ColoredDate> datesColors2 = new ArrayList<>();
        datesColors2.add(new ColoredDate(tempCal.getTime(), getResources().getColor(R.color.red_holiday)));
        mKalendarView.addColoredDates(datesColors2);
        mKalendarView.addEvents(events2);

        // 하단 다이얼로그 열기 버튼
        btBottomDialog.setOnClickListener(view -> showDialog());

        // 로그아웃 버튼 누르면 로그인 화면으로 이동
        btLogout.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MainActivity.this, LoginScreen.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // 백스택 제거
            startActivity(intent);
        });
    }

    void showDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View bottomSheet = getLayoutInflater().inflate(R.layout.bottom_dialog, null);

        Button demo = bottomSheet.findViewById(R.id.bt_bottom_dialog);
        KalendarView kalendarView = bottomSheet.findViewById(R.id.kalendar);

        demo.setOnClickListener(view -> kalendarView.setInitialSelectedDate(new Date()));

        dialog.setContentView(bottomSheet);
        dialog.show();
    }
}
