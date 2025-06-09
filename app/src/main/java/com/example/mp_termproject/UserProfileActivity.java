package com.example.mp_termproject;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.mp_termproject.fragment.profiletabs.UserPostsGridFragment;
import com.example.mp_termproject.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity {

    private String profileUserId;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    private CircleImageView profileImageView;
    private TextView nameTextView, emailTextView, bioTextView;
    private TextView postCountTextView, followerCountTextView, followingCountTextView;
    private Button followButton;

    private boolean isFollowing = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        profileUserId = getIntent().getStringExtra("user_id");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        if (profileUserId == null || profileUserId.isEmpty() || profileUserId.equals(currentUser.getUid())) {
            finish(); // 자기 자신 프로필은 이 화면에서 보지 않음
            return;
        }

        initViews();
        loadUserProfile();
        loadUserPosts();
        checkIfFollowing();
        followButton.setOnClickListener(v -> toggleFollow());
    }

    // 팔로우/언팔로우 로직
    private void toggleFollow() {
        // 한 번에 여러 데이터 작업을 안전하게 처리하기 위해 WriteBatch 사용
        WriteBatch batch = db.batch();

        // 내 following 목록과 상대방의 followers 목록 경로
        DocumentReference myFollowingRef = db.collection("users").document(currentUser.getUid()).collection("following").document(profileUserId);
        DocumentReference otherFollowerRef = db.collection("users").document(profileUserId).collection("followers").document(currentUser.getUid());

        // 팔로워/팔로잉 수 카운트를 위한 경로
        DocumentReference myUserRef = db.collection("users").document(currentUser.getUid());
        DocumentReference otherUserRef = db.collection("users").document(profileUserId);

        if (isFollowing) {
            // 언팔로우 로직
            batch.delete(myFollowingRef);
            batch.delete(otherFollowerRef);
            batch.update(myUserRef, "followingCount", FieldValue.increment(-1));
            batch.update(otherUserRef, "followerCount", FieldValue.increment(-1));
        } else {
            // 팔로우 로직
            batch.set(myFollowingRef, new HashMap<>());
            batch.set(otherFollowerRef, new HashMap<>());
            batch.update(myUserRef, "followingCount", FieldValue.increment(1));
            batch.update(otherUserRef, "followerCount", FieldValue.increment(1));
        }

        // 모든 작업을 한번에 실행
        batch.commit().addOnSuccessListener(aVoid -> {
            isFollowing = !isFollowing;
            updateFollowButton();
        }).addOnFailureListener(e -> Toast.makeText(this, "작업에 실패했습니다.", Toast.LENGTH_SHORT).show());
    }

    // (이하 다른 메소드들은 이전 답변과 거의 동일)
    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar_user_profile);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        profileImageView = findViewById(R.id.iv_profile_picture_user);
        nameTextView = findViewById(R.id.tv_profile_user_name);
        emailTextView = findViewById(R.id.tv_profile_user_email);
        bioTextView = findViewById(R.id.tv_profile_user_bio);
        postCountTextView = findViewById(R.id.tv_post_count_user);
        followerCountTextView = findViewById(R.id.tv_follower_count_user);
        followingCountTextView = findViewById(R.id.tv_following_count_user);
        followButton = findViewById(R.id.btn_follow_user);
    }

    private void loadUserProfile() {
        db.collection("users").document(profileUserId).addSnapshotListener((snapshot, error) -> {
            if (error != null || snapshot == null || !snapshot.exists()) return;

            User user = snapshot.toObject(User.class);
            if (user != null) {
                getSupportActionBar().setTitle(user.getName());
                nameTextView.setText(user.getName());
                emailTextView.setText(user.getEmail());
                bioTextView.setText(user.getBio());
                followerCountTextView.setText(user.getFollowerCount() + "\n팔로워");
                followingCountTextView.setText(user.getFollowingCount() + "\n팔로잉");
                Glide.with(this).load(user.getImageUrl()).placeholder(R.mipmap.ic_launcher_round).into(profileImageView);
            }
        });

        db.collection("posts").whereEqualTo("userId", profileUserId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    postCountTextView.setText(queryDocumentSnapshots.size() + "\n게시물");
                });
    }

    private void loadUserPosts() {
        UserPostsGridFragment fragment = UserPostsGridFragment.newInstance(profileUserId);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.user_posts_container, fragment)
                .commit();
    }

    private void checkIfFollowing() {
        db.collection("users").document(currentUser.getUid()).collection("following").document(profileUserId)
                .get().addOnSuccessListener(documentSnapshot -> {
                    isFollowing = documentSnapshot.exists();
                    updateFollowButton();
                });
    }

    private void updateFollowButton() {
        if (isFollowing) {
            followButton.setText("팔로잉");
            followButton.setBackgroundColor(ContextCompat.getColor(this, R.color.light_gray));
        } else {
            followButton.setText("팔로우");
            followButton.setBackgroundColor(ContextCompat.getColor(this, R.color.holo_blue_light));
        }
    }
}