package com.example.mp_termproject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignupScreen extends AppCompatActivity {

    EditText edtemail, edtpassword, edtname, edtmobile;
    String email, password, mobile, name;
    Button bsignup;
    StorageReference postrefrance;
    Uri uri;
    CircleImageView profile_image;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference mDatabase;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_screen);

        bsignup = findViewById(R.id.bttnsignup);
        edtemail = findViewById(R.id.editText_emailAddress);
        edtpassword = findViewById(R.id.editText_password);
        edtmobile = findViewById(R.id.editText_mobile);
        edtname = findViewById(R.id.editText_name);

        profile_image = findViewById(R.id.profile_image);
        progressDialog = new ProgressDialog(this);

        firebaseAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        postrefrance = FirebaseStorage.getInstance().getReference("userpics");

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });

        bsignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Register();
            }
        });
    }

    void pickImage() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "select image"), 1002);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1002 && data != null) {
            try {
                uri = data.getData();
                Bitmap bm = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                profile_image.setImageBitmap(bm);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "" + e, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void Register() {
        email = edtemail.getText().toString().trim();
        password = edtpassword.getText().toString().trim();
        name = edtname.getText().toString().trim();
        mobile = edtmobile.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || name.isEmpty() || mobile.isEmpty()) {
            Toast.makeText(this, "모든 항목을 입력해주세요", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "비밀번호는 최소 6자 이상이어야 합니다", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Registering User....");
        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            sendUserDetails(uri);
                        } else {
                            Toast.makeText(SignupScreen.this,
                                    "회원가입 실패: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void sendUserDetails(Uri uri) {
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "사용자 인증 정보가 없습니다", Toast.LENGTH_SHORT).show();
            return;
        }

        final String id = currentUser.getUid();
        final ProgressDialog progressDialog = new ProgressDialog(SignupScreen.this);
        progressDialog.setCancelable(true);
        progressDialog.setMessage("저장 중...");
        progressDialog.show();

        if (uri == null) {
            // 사진 없이 바로 유저 정보 저장
            User user = new User(id, name, email, mobile, null);
            mDatabase.child("Users").child(id).setValue(user)
                    .addOnSuccessListener(unused -> {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "User Added", Toast.LENGTH_SHORT).show();
                        finish();
                        startActivity(new Intent(SignupScreen.this, MainActivity.class));
                    });
        } else {
            // 사진 업로드 후 저장
            StorageMetadata metadata = new StorageMetadata.Builder().setContentType("image/jpeg").build();
            UploadTask uploadTask = postrefrance.child("insta_" + id).putFile(uri, metadata);

            uploadTask.addOnSuccessListener(taskSnapshot -> {
                taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    String url = downloadUri.toString();
                    User user = new User(id, name, email, mobile, url);
                    mDatabase.child("Users").child(id).setValue(user)
                            .addOnSuccessListener(unused -> {
                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(), "User Added", Toast.LENGTH_SHORT).show();
                                finish();
                                startActivity(new Intent(SignupScreen.this, MainActivity.class));
                            });
                });
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "사진 업로드 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

}
