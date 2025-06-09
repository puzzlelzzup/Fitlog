package com.example.mp_termproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.mp_termproject.model.Post; // 수정된 Post 모델 import
import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class PostDetailActivity extends AppCompatActivity {

    public static final String POST_DATA = "post_data"; // Intent Key

    private ImageView ivImage;
    private TextView tvWriter, tvContent, tvTags, tvCategory, tvTimestamp;
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_post_detail);

        ivImage = findViewById(R.id.iv_post_detail_image);
        tvWriter = findViewById(R.id.tv_post_detail_writer);
        tvContent = findViewById(R.id.tv_post_detail_content);
        tvTags = findViewById(R.id.tv_post_detail_tags);
        tvCategory = findViewById(R.id.tv_post_detail_category);
        tvTimestamp = findViewById(R.id.tv_post_detail_timestamp);
        btnBack = findViewById(R.id.btn_back_to_profile);

        // Intent에서 Post 객체를 통째로 받기
        Post post = getIntent().getParcelableExtra(POST_DATA);

        if (post != null) {
            // UI에 데이터 채우기
            Glide.with(this).load(post.getImageUrl()).into(ivImage);
            tvWriter.setText(post.getWriter());
            tvContent.setText(post.getContent());
            tvTags.setText(post.getTags());
            tvCategory.setText(post.getCategory());

            // 타임스탬프를 보기 좋은 형식으로 변환하여 표시
            tvTimestamp.setText(formatTimestamp(post.getTimestamp()));
        }

        btnBack.setOnClickListener(v -> finish());
    }

    /**
     * Firestore의 Timestamp를 "방금 전", "5분 전" 등 상대적인 시간으로 변환하는 메소드
     * @param timestamp Firestore에서 받은 Timestamp 객체
     * @return 사용자가 보기 편한 시간 문자열
     */
    public static String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) {
            return "";
        }
        long currentTime = System.currentTimeMillis();
        long postTime = timestamp.toDate().getTime();
        long diff = currentTime - postTime;

        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        long days = TimeUnit.MILLISECONDS.toDays(diff);

        if (minutes < 1) {
            return "방금 전";
        } else if (minutes < 60) {
            return minutes + "분 전";
        } else if (hours < 24) {
            return hours + "시간 전";
        } else if (hours < 48 && isYesterday(timestamp)) {
            return "어제";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 M월 d일", Locale.KOREA);
            return sdf.format(timestamp.toDate());
        }
    }

    // 어제인지 확인하는 helper 메소드
    private static boolean isYesterday(Timestamp timestamp) {
        Calendar postCal = Calendar.getInstance();
        postCal.setTime(timestamp.toDate());

        Calendar yesterdayCal = Calendar.getInstance();
        yesterdayCal.add(Calendar.DAY_OF_YEAR, -1);

        return postCal.get(Calendar.YEAR) == yesterdayCal.get(Calendar.YEAR)
                && postCal.get(Calendar.DAY_OF_YEAR) == yesterdayCal.get(Calendar.DAY_OF_YEAR);
    }
}