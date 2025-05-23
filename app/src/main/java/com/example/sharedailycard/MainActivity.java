package com.example.sharedailycard;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sharedailycard.adapter.DailyLogAdapter;
import com.example.sharedailycard.model.DailyLog;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private DailyLogAdapter adapter;
    private List<DailyLog> logList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        logList = new ArrayList<>();
        adapter = new DailyLogAdapter(this, logList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Intent로 전달된 데이터 받아오기
        Intent intent = getIntent();
        DailyLog receivedLog = (DailyLog) intent.getSerializableExtra("dailyLog");
        if (receivedLog != null) {
            logList.add(receivedLog);
            adapter.notifyItemInserted(logList.size() - 1);
        }
    }
}