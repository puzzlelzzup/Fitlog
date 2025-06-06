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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import com.example.mp_termproject.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.io.IOException;

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

    private Uri selectedImageUri;

    public UploadFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
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
                    layoutUploadPlaceholderContent.setVisibility(View.GONE); // 플레이스홀더 숨기기
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "이미지를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void publishPost() {
        String content = etPostContent.getText().toString().trim();
        String tags = etPostTags.getText().toString().trim(); // TODO: 태그 파싱 (쉼표 또는 공백 기준)
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

        // TODO: Firebase에 데이터 업로드 로직 구현
        // 1. Firebase Storage에 selectedImageUri를 사용하여 이미지 업로드
        // 2. 업로드 성공 후 이미지 다운로드 URL 가져오기
        // 3. Firebase Realtime Database 또는 Firestore에 게시물 정보 (이미지 URL, content, tags, category, visibility, userId, timestamp 등) 저장

        Toast.makeText(getContext(), "게시물 정보: \n내용: " + content + "\n태그: " + tags + "\n카테고리: " + selectedCategory + "\n공개범위: " + visibility, Toast.LENGTH_LONG).show();
        // 업로드 성공 후 현재 Fragment를 닫거나 다른 화면으로 이동하는 로직 추가 가능
    }
}