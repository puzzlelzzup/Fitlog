package com.example.mp_termproject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SignupScreen extends AppCompatActivity {

    private EditText edtemail, edtpassword, edtname;
    private Button bsignup;
    private TextView text_login;

    private AuthRepository authRepository;
    private ProgressDialog progressDialog;

    // 모든 신규 사용자에게 적용될 기본 프로필 이미지 URL
    private static final String DEFAULT_PROFILE_IMAGE_URL = "https://firebasestorage.googleapis.com/v0/b/fitlog-f5540.appspot.com/o/profile_images%2Fdefault_profile_image.png?alt=media&token=c19f4a05-2487-4412-858a-3e44917a1519";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_screen);

        authRepository = new AuthRepository();

        edtemail = findViewById(R.id.editText_emailAddress);
        edtpassword = findViewById(R.id.editText_password);
        edtname = findViewById(R.id.editText_name);
        bsignup = findViewById(R.id.button_signup);
        text_login = findViewById(R.id.text_login);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        bsignup.setOnClickListener(view -> registerUser());
        text_login.setOnClickListener(view ->
                startActivity(new Intent(getApplicationContext(), LoginScreen.class)));
    }

    private void registerUser() {
        String email = edtemail.getText().toString().trim();
        String password = edtpassword.getText().toString().trim();
        String name = edtname.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
            Toast.makeText(this, "모든 항목을 입력해주세요", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "비밀번호는 최소 6자 이상이어야 합니다", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("가입 처리 중...");
        progressDialog.show();

        // authRepository를 호출할 때, 기본 이미지 URL(DEFAULT_PROFILE_IMAGE_URL)을 함께 전달
        authRepository.createUserAccount(email, password, name, DEFAULT_PROFILE_IMAGE_URL, new AuthRepository.AuthListener() {
            @Override
            public void onSuccess() {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "회원가입이 완료되었습니다!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(SignupScreen.this, MainActivity.class));
                finish();
            }

            @Override
            public void onFailure(String message) {
                progressDialog.dismiss();
                Toast.makeText(SignupScreen.this, "오류: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }
}