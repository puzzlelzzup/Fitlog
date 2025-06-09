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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
    private ProgressBar progressBar;

    private Uri selectedImageUri;

    public UploadFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_upload, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // UI 요소 바인딩
        layoutImageUploadArea = view.findViewById(R.id.layout_image_upload_area);
        ivSelectedImage = view.findViewById(R.id.iv_selected_image);
        layoutUploadPlaceholderContent = view.findViewById(R.id.layout_upload_placeholder_content);
        etPostContent = view.findViewById(R.id.et_post_content);
        etPostTags = view.findViewById(R.id.et_post_tags);
        chipGroupCategory = view.findViewById(R.id.chip_group_category);
        spinnerVisibility = view.findViewById(R.id.spinner_visibility);
        btnPublishPost = view.findViewById(R.id.btn_publish_post);

        setupSpinner();
        layoutImageUploadArea.setOnClickListener(v -> pickImageFromGallery());
        btnPublishPost.setOnClickListener(v -> publishPost());

        // CalendarFragment로부터 받은 데이터 처리
        handleArguments();
    }

    // 전달받은 인자(데이터)를 처리하는 메소드
    private void handleArguments() {
        Bundle args = getArguments();
        if (args != null) {
            String exercises = args.getString("ARG_EXERCISES", "");
            String weight = args.getString("ARG_WEIGHT", "");
            int calories = args.getInt("ARG_CALORIES", 0);

            // 1. 내용(Content) 자동 채우기
            StringBuilder contentBuilder = new StringBuilder();
            if (!exercises.isEmpty()) {
                contentBuilder.append("운동: ").append(exercises).append("\n");
            }
            if (!weight.isEmpty()) {
                contentBuilder.append("몸무게: ").append(weight).append("\n");
            }
            if (calories > 0) {
                contentBuilder.append("총 섭취 칼로리: ").append(calories).append(" kcal");
            }
            etPostContent.setText(contentBuilder.toString());

            // 2. 스마트 태그 생성 및 자동 채우기
            if (!exercises.isEmpty()) {
                String smartTags = generateTagsFromExercise(exercises);
                etPostTags.setText(smartTags);
            }
            // 3. 카테고리 '운동'으로 자동 선택
            selectCategoryChip("운동");
        }
    }

    // 운동 문자열로부터 스마트 태그를 생성하는 메소드
    private String generateTagsFromExercise(String exercises) {
        // 규칙 사전 (계속 추가 가능)
        Map<String, String> ruleBook = new HashMap<>();
        ruleBook.put("달리기", "#러닝 #유산소");
        ruleBook.put("running", "#러닝 #유산소");
        ruleBook.put("조깅", "#조깅 #유산소");
        ruleBook.put("스쿼트", "#스쿼트 #하체운동 #근력");
        ruleBook.put("squat", "#스쿼트 #하체운동 #근력");
        ruleBook.put("벤치프레스", "#벤치프레스 #가슴운동 #헬스");
        ruleBook.put("데드리프트", "#데드리프트 #등운동 #3대");
        ruleBook.put("자전거", "#사이클 #유산소");
        ruleBook.put("수영", "#수영 #전신운동");
        ruleBook.put("걷기", "#걷기 #산책");

        Set<String> tags = new HashSet<>();
        String[] exerciseArray = exercises.toLowerCase().split(",\\s*"); // "달리기, 스쿼트" -> ["달리기", "스쿼트"]

        for (String exercise : exerciseArray) {
            for (Map.Entry<String, String> entry : ruleBook.entrySet()) {
                if (exercise.contains(entry.getKey())) {
                    // entry.getValue()는 "#태그1 #태그2" 형태이므로 분리해서 Set에 추가
                    tags.addAll(Arrays.asList(entry.getValue().split("\\s+")));
                }
            }
        }

        // 중복이 제거된 태그들을 다시 하나의 문자열로 합침
        return String.join(" ", tags);
    }

    // 텍스트를 기반으로 카테고리 칩을 선택하는 메소드
    private void selectCategoryChip(String categoryText) {
        for (int i = 0; i < chipGroupCategory.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupCategory.getChildAt(i);
            if (chip.getText().toString().equalsIgnoreCase(categoryText)) {
                chip.setChecked(true);
                break;
            }
        }
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

        setUploadingState(true);

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        String filename = "posts/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference imageRef = storageRef.child(filename);

        final String finalCategory = selectedCategory;
        imageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        uploadPostDataToFirestore(imageUrl, content, tags, finalCategory, visibility);
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "이미지 업로드 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    setUploadingState(false);
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
                        post.put("writer", writerName);
                        post.put("timestamp", FieldValue.serverTimestamp());

                        db.collection("posts").add(post)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(getContext(), "게시물 업로드 성공", Toast.LENGTH_SHORT).show();
                                    setUploadingState(false);
                                    // 업로드 성공 후 캘린더 화면으로 돌아가기
                                    if(getParentFragmentManager() != null){
                                        getParentFragmentManager().popBackStack();
                                    }
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

    private void setUploadingState(boolean isUploading) {
        if (isUploading) {
            // progressBar.setVisibility(View.VISIBLE);
            btnPublishPost.setEnabled(false);
            btnPublishPost.setText("업로드 중...");
        } else {
            // progressBar.setVisibility(View.GONE);
            btnPublishPost.setEnabled(true);
            btnPublishPost.setText("게시하기");
        }
    }
}