package com.example.sharedailycard;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.example.sharedailycard.model.DailyLog;

public class DailyLogActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION_IMAGE = 1000;
    private Uri selectedImageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ImageView imgPreview;
    private Button selectImgBtn, uploadBtn;
    private EditText userWeight, userFatRatio, userFitness;
    private EditText userBreakfast, KcalBreakfast, userLunch, KcalLunch, userDinner, KcalDinner;

    private float parseFloat(String input) {
        try {
            return Float.parseFloat(input);
        } catch (NumberFormatException e) {
            return 0f;
        }
    }

    private int parseInt(String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_log);

        imgPreview = findViewById(R.id.imgPreview);
        selectImgBtn = findViewById(R.id.selectImgBtn);
        uploadBtn = findViewById(R.id.uploadBtn);

        userWeight = findViewById(R.id.userWeight);
        userFatRatio = findViewById(R.id.userFatRatio);
        userFitness = findViewById(R.id.userFitness);

        userBreakfast = findViewById(R.id.userBreakfast);
        KcalBreakfast = findViewById(R.id.KcalBreakfast);
        userLunch = findViewById(R.id.userLunch);
        KcalLunch = findViewById(R.id.KcalLunch);
        userDinner = findViewById(R.id.userDinner);
        KcalDinner = findViewById(R.id.KcalDinner);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13 이상
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        REQUEST_PERMISSION_IMAGE);
            }
        }

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        imgPreview.setImageURI(selectedImageUri);
                    }
                }
        );

        selectImgBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        uploadBtn.setOnClickListener(v -> {
            String imageUriStr = (selectedImageUri != null) ? selectedImageUri.toString() : "";

            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            float weight = parseFloat(userWeight.getText().toString());
            float fatRatio = parseFloat(userFatRatio.getText().toString());
            String fitness = userFitness.getText().toString();

            String breakfast = userBreakfast.getText().toString();
            int kcalBreakfast = parseInt(KcalBreakfast.getText().toString());

            String lunch = userLunch.getText().toString();
            int kcalLunch = parseInt(KcalLunch.getText().toString());

            String dinner = userDinner.getText().toString();
            int kcalDinner = parseInt(KcalDinner.getText().toString());

            DailyLog log = new DailyLog(
                    date, imageUriStr, weight, fatRatio, fitness,
                    breakfast, kcalBreakfast, lunch, kcalLunch, dinner, kcalDinner
            );

            Intent intent = new Intent(DailyLogActivity.this, MainActivity.class);
            intent.putExtra("dailyLog", log);
            startActivity(intent);
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_IMAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "이미지 권한 허용됨", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "이미지 권한이 필요합니다.", Toast.LENGTH_LONG).show();
            }
        }
    }
}