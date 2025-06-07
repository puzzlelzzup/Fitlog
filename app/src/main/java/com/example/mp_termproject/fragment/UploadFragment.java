package com.example.mp_termproject.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import com.example.mp_termproject.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UploadFragment extends Fragment {

    private static final int IMAGE_PICK_CODE = 1000;

    private FrameLayout layoutImageUploadArea;
    private ImageView ivSelectedImage;
    private LinearLayout layoutUploadPlaceholderContent;
    private EditText etPostContent;
    private EditText etPostTags;
    private ChipGroup chipGroupCategory;
    private Spinner spinnerVisibility;
    private Button btnPublishPost;
    private ProgressBar progressBar; // 업로드 중 상태를 표시할 프로그레스바

    private Uri selectedImageUri;

    public UploadFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_upload, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        layoutImageUploadArea = view.findViewById(R.id.layout_image_upload_area);
        ivSelectedImage = view.findViewById(R.id.iv_selected_image);
        layoutUploadPlaceholderContent = view.findViewById(R.id.layout_upload_placeholder_content);
        etPostContent = view.findViewById(R.id.et_post_content);
        etPostTags = view.findViewById(R.id.et_post_tags);
        chipGroupCategory = view.findViewById(R.id.chip_group_category);
        spinnerVisibility = view.findViewById(R.id.spinner_visibility);
        btnPublishPost = view.findViewById(R.id.btn_publish_post);
        // progressBar = view.findViewById(R.id.upload_progress_bar); // XML에 ProgressBar 추가 필요

        setupSpinner();
        layoutImageUploadArea.setOnClickListener(v -> pickImageFromGallery());
        btnPublishPost.setOnClickListener(v -> publishPost());
    }

    private void setupSpinner() {
        if (getContext() != null) {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                    R.array.visibility_options, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerVisibility.setAdapter(adapter);
        }
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            try {
                if (getContext() != null) {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), selectedImageUri);
                    ivSelectedImage.setImageBitmap(bitmap);
                    ivSelectedImage.setVisibility(View.VISIBLE);
                    layoutUploadPlaceholderContent.setVisibility(View.GONE);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "이미지를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void publishPost() {
        String content = etPostContent.getText().toString().trim();
        String tags = etPostTags.getText().toString().trim();
        String visibility = spinnerVisibility.getSelectedItem().toString();
        String selectedCategory = "";

        int selectedChipId = chipGroupCategory.getCheckedChipId();
        if (selectedChipId != View.NO_ID) {
            Chip selectedChip = chipGroupCategory.findViewById(selectedChipId);
            selectedCategory = selectedChip.getText().toString();
        }

        if (selectedImageUri == null) {
            Toast.makeText(getContext(), "이미지를 선택해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (content.isEmpty()) {
            Toast.makeText(getContext(), "내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedCategory.isEmpty()) {
            Toast.makeText(getContext(), "카테고리를 선택해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        setUploadingState(true); // 업로드 시작 상태

        // 1. 이미지 업로드
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        String filename = "posts/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference imageRef = storageRef.child(filename);

        final String finalCategory = selectedCategory;
        imageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // 2. 이미지 URL 가져오기
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        // 3. Firestore에 게시물 데이터 저장
                        uploadPostDataToFirestore(imageUrl, content, tags, finalCategory, visibility);
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "이미지 업로드 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    setUploadingState(false); // 업로드 실패 상태
                });
    }

    private void uploadPostDataToFirestore(String imageUrl, String content, String tags,
                                           String category, String visibility) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(getContext(), "로그인 상태가 아닙니다.", Toast.LENGTH_SHORT).show();
            setUploadingState(false);
            return;
        }

        String uid = currentUser.getUid();
        // 3-1. 작성자 이름 가져오기
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String writerName = documentSnapshot.getString("name");

                        Map<String, Object> post = new HashMap<>();
                        post.put("imageUrl", imageUrl);
                        post.put("content", content);
                        post.put("tags", tags);
                        post.put("category", category);
                        post.put("visibility", visibility);
                        post.put("userId", uid);
                        post.put("writer", writerName); // 가져온 작성자 이름 저장
                        post.put("timestamp", FieldValue.serverTimestamp());

                        // 3-2. 게시글 정보 저장
                        db.collection("posts").add(post)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(getContext(), "게시물 업로드 성공", Toast.LENGTH_SHORT).show();
                                    setUploadingState(false);
                                    // TODO: 업로드 성공 후 화면 초기화 또는 다른 화면으로 이동
                                    // 예: getParentFragmentManager().popBackStack();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "게시물 업로드 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    setUploadingState(false);
                                });
                    } else {
                        Toast.makeText(getContext(), "작성자 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                        setUploadingState(false);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "사용자 정보 로딩 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    setUploadingState(false);
                });
    }

    // 업로드 중/완료 시 UI 상태를 변경하는 헬퍼 메소드
    private void setUploadingState(boolean isUploading) {
        if (isUploading) {
            // progressBar.setVisibility(View.VISIBLE); // XML에 ProgressBar 추가했다면 주석 해제
            btnPublishPost.setEnabled(false);
            btnPublishPost.setText("업로드 중...");
        } else {
            // progressBar.setVisibility(View.GONE); // XML에 ProgressBar 추가했다면 주석 해제
            btnPublishPost.setEnabled(true);
            btnPublishPost.setText("게시하기");
        }
    }
}