package com.example.mp_termproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.mp_termproject.model.Post;
import com.example.mp_termproject.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SinglePostActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private CircleImageView writerProfile;
    private TextView writerName;
    private ImageView postImage;
    private TextView postContent, tvPostTags;

    private ImageView likeButton, commentButton;
    private TextView likeCountText, commentCountText;

    private Post post;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_post);

        initFirebase();
        initViews();

        post = getIntent().getParcelableExtra(PostDetailActivity.POST_DATA);
        if (post == null || post.getPostId() == null) {
            Toast.makeText(this, "게시물을 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        displayPostContent();
        fetchWriterInfo();
        setupListeners();
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar_single_post);
        writerProfile = findViewById(R.id.iv_writer_profile);
        writerName = findViewById(R.id.tv_writer_name);
        postImage = findViewById(R.id.iv_single_post_image);
        postContent = findViewById(R.id.tv_single_post_content);
        likeButton = findViewById(R.id.iv_like_single);
        commentButton = findViewById(R.id.iv_comment_single);
        likeCountText = findViewById(R.id.tv_like_count_single);
        commentCountText = findViewById(R.id.tv_comment_count_single);
        tvPostTags = findViewById(R.id.tv_post_tags);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void displayPostContent() {
        writerName.setText(post.getWriter());
        postContent.setText(post.getContent());
        tvPostTags.setText(post.getTags());
        likeCountText.setText("좋아요 " + post.getLikeCount() + "개");
        commentCountText.setText("댓글 " + post.getCommentCount() + "개");
        Glide.with(this).load(post.getImageUrl()).into(postImage);
        updateLikeButtonUI();
    }

    private void fetchWriterInfo() {
        if (post.getUserId() == null) return;

        db.collection("users").document(post.getUserId()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if(documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null && user.getImageUrl() != null && !user.getImageUrl().isEmpty()) {
                            Glide.with(this)
                                    .load(user.getImageUrl())
                                    .placeholder(R.mipmap.ic_launcher_round)
                                    .into(writerProfile);
                        }
                    } else {
                        Log.d("SinglePostActivity", "작성자 정보가 존재하지 않음: " + post.getUserId());
                    }
                }).addOnFailureListener(e -> Log.e("SinglePostActivity", "작성자 정보 로딩 실패", e));
    }

    private void setupListeners() {
        likeButton.setOnClickListener(v -> toggleLikeStatus());
        commentButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, CommentActivity.class);
            intent.putExtra(PostDetailActivity.POST_DATA, post);
            startActivity(intent);
        });
    }

    private void updateLikeButtonUI() {
        if (currentUser != null && post.getLikedBy() != null && post.getLikedBy().contains(currentUser.getUid())) {
            likeButton.setImageResource(R.drawable.ic_like_filled);
        } else {
            likeButton.setImageResource(R.drawable.ic_like_outline);
        }
    }

    private void toggleLikeStatus() {
        if (currentUser == null) {
            Toast.makeText(this, "로그인 후 이용해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DocumentReference postRef = db.collection("posts").document(post.getPostId());

        boolean isCurrentlyLiked = post.getLikedBy().contains(userId);

        if (isCurrentlyLiked) {
            post.getLikedBy().remove(userId);
        } else {
            post.getLikedBy().add(userId);
        }
        likeCountText.setText("좋아요 " + post.getLikedBy().size() + "개");
        updateLikeButtonUI();

        db.runTransaction((Transaction.Function<Void>) transaction -> {
            Post snapshot = transaction.get(postRef).toObject(Post.class);
            if (snapshot == null) return null;

            List<String> newLikedBy = new ArrayList<>(snapshot.getLikedBy());
            if (isCurrentlyLiked) {
                newLikedBy.remove(userId);
            } else {
                newLikedBy.add(userId);
            }
            transaction.update(postRef, "likedBy", newLikedBy);
            transaction.update(postRef, "likeCount", newLikedBy.size());
            return null;
        }).addOnFailureListener(e -> {
            Log.e("SinglePostActivity", "좋아요 트랜잭션 실패", e);
            if (isCurrentlyLiked) {
                post.getLikedBy().add(userId);
            } else {
                post.getLikedBy().remove(userId);
            }
            likeCountText.setText("좋아요 " + post.getLikedBy().size() + "개");
            updateLikeButtonUI();
        });
    }
}