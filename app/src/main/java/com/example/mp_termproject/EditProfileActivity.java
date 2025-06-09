package com.example.mp_termproject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.mp_termproject.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private CircleImageView profileImageView;
    private TextView changePhotoTextView;
    private EditText bioEditText;
    private ImageView closeButton;
    private TextView saveButton;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private StorageReference storageRef;

    private Uri imageUri;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initFirebase();
        initViews();
        loadUserData();
        setupListeners();
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        storageRef = FirebaseStorage.getInstance().getReference();
    }

    private void initViews() {
        profileImageView = findViewById(R.id.iv_profile_picture_edit);
        changePhotoTextView = findViewById(R.id.tv_change_photo);
        bioEditText = findViewById(R.id.et_bio_edit);
        closeButton = findViewById(R.id.btn_close);
        saveButton = findViewById(R.id.btn_save);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("저장 중...");
        progressDialog.setCancelable(false);
    }

    private void loadUserData() {
        if (currentUser != null) {
            db.collection("users").document(currentUser.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            if (user != null) {
                                bioEditText.setText(user.getBio());
                                Glide.with(this)
                                        .load(user.getImageUrl())
                                        .placeholder(R.mipmap.ic_launcher_round)
                                        .into(profileImageView);
                            }
                        }
                    });
        }
    }

    private void setupListeners() {
        closeButton.setOnClickListener(v -> finish());
        saveButton.setOnClickListener(v -> saveProfileChanges());
        changePhotoTextView.setOnClickListener(v -> openGallery());
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImageView.setImageURI(imageUri);
        }
    }

    private void saveProfileChanges() {
        progressDialog.show();
        String bio = bioEditText.getText().toString().trim();

        if (imageUri != null) {
            // 새 이미지가 선택된 경우: 이미지를 먼저 업로드
            StorageReference fileRef = storageRef.child("profile_images/" + UUID.randomUUID().toString());
            fileRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                String imageUrl = uri.toString();
                                updateUserProfile(imageUrl, bio);
                            }))
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "이미지 업로드 실패", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // 이미지를 변경하지 않은 경우: bio만 업데이트
            updateUserProfile(null, bio);
        }
    }

    private void updateUserProfile(String imageUrl, String bio) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("bio", bio);
        if (imageUrl != null) {
            updates.put("imageUrl", imageUrl);
        }

        db.collection("users").document(currentUser.getUid()).update(updates)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "프로필이 업데이트되었습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "업데이트 실패", Toast.LENGTH_SHORT).show();
                });
    }
}