package com.example.mp_termproject.fragment;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ak.ColoredDate;
import com.ak.EventObjects;
import com.ak.KalendarView;
import com.example.mp_termproject.R;
import com.example.mp_termproject.DietScheduleActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarFragment extends Fragment {

    private KalendarView mKalendarView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        mKalendarView = view.findViewById(R.id.kalendar_view_in_fragment);

        List<ColoredDate> datesColors = new ArrayList<>();
        if (isAdded() && getContext() != null) {

        }
        mKalendarView.setColoredDates(datesColors);

        List<EventObjects> events = new ArrayList<>();

        mKalendarView.setEvents(events);

        mKalendarView.setDateSelector(selectedDate -> {
            if (!isAdded() || getActivity() == null) return;

            Log.d("CalendarFragment", "Selected Date: " + selectedDate.toString());
            Calendar cal = Calendar.getInstance();
            cal.setTime(selectedDate);

            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH); // 0-based
            int day = cal.get(Calendar.DAY_OF_MONTH);

            Intent intent = new Intent(getActivity(), DietScheduleActivity.class);
            intent.putExtra("year", year);
            intent.putExtra("month", month);
            intent.putExtra("day", day);
            startActivity(intent);
        });

        mKalendarView.setMonthChanger(changedMonth -> {
            if (!isAdded()) return;
            Log.d("CalendarFragment", "Month Changed: " + changedMonth);
        });

        return view;
    }
}