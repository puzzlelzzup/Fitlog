package com.example.mp_termproject;

// PostDetailActivity.java
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;


public class PostDetailActivity extends AppCompatActivity {
    private ImageView ivImage;
    private TextView tvWriter, tvContent, tvTags, tvCategory, tvTimestamp;
    Button btnBack;

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
        // intent로부터 Post 객체 정보 수신
        Intent intent = getIntent();
        String writer = intent.getStringExtra("writer");
        String imageUrl = intent.getStringExtra("imageUrl");
        String content = intent.getStringExtra("content");
        String tags = intent.getStringExtra("tags");
        String category = intent.getStringExtra("category");
        String timestampStr = intent.getStringExtra("timestamp");

        // null 체크
        if (imageUrl == null) imageUrl = "";
        if (content == null) content = "";
        if (tags == null) tags = "";
        if (category == null) category = "";
        if (timestampStr == null) timestampStr = "";

        Glide.with(this).load(imageUrl).into(ivImage);
        if (writer != null && !writer.isEmpty()) {
            tvWriter.setText(writer);
        } else {
            tvWriter.setText("(알 수 없음)");
        }
        tvContent.setText(content);
        tvTags.setText(tags);
        tvCategory.setText(category);
        tvTimestamp.setText(timestampStr);

        btnBack.setOnClickListener(v -> {
            finish();
        });
    }
}
