package com.example.sharedailycard.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sharedailycard.R;
import com.example.sharedailycard.model.DailyLog;

import java.util.List;

public class DailyLogAdapter extends RecyclerView.Adapter<DailyLogAdapter.ViewHolder> {
    private final Context context;
    private final List<DailyLog> logList; // 업로드된 Daily 객체들을 담을 리스트

    public DailyLogAdapter(Context context, List<DailyLog> logList) {
        this.context = context;
        this.logList = logList;
    }

    @NonNull
    @Override
    public DailyLogAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.card_daily_log, parent, false);
        return new ViewHolder(view);
    }

    // 카드 UI에 입력 값(데이터) 세팅
    @Override
    public void onBindViewHolder(@NonNull DailyLogAdapter.ViewHolder holder, int position) {
        DailyLog log = logList.get(position);

        // 이미지
        if (!log.getImageUri().isEmpty()) {
            holder.cardImg.setImageURI(Uri.parse(log.getImageUri()));
        }

        holder.cardDate.setText(log.getDate());
        holder.cardWeight.setText("몸무게: " + log.getWeight() + "kg");
        holder.cardFatRatio.setText("체지방: " + log.getBodyFat() + "%");
        holder.cardFitness.setText("운동: " + log.getWorkout());

        // 식단 요약 문자열 구성
        String mealSummary = "아침: " + log.getBreakfast() + " (" + log.getKcalBreakfast() + "kcal)\n"
                + "점심: " + log.getLunch() + " (" + log.getKcalLunch() + "kcal)\n"
                + "저녁: " + log.getDinner() + " (" + log.getKcalDinner() + "kcal)";
        holder.cardMeals.setText(mealSummary);
    }

    @Override
    public int getItemCount() {
        return logList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView cardImg;
        TextView cardDate, cardWeight, cardFatRatio, cardFitness, cardMeals;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardImg = itemView.findViewById(R.id.cardImg);
            cardDate = itemView.findViewById(R.id.cardDate);
            cardWeight = itemView.findViewById(R.id.cardWeight);
            cardFatRatio = itemView.findViewById(R.id.cardFatRatio);
            cardFitness = itemView.findViewById(R.id.cardFitness);
            cardMeals = itemView.findViewById(R.id.cardMeals);
        }
    }
}
