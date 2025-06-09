package com.example.mp_termproject;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView; // ImageView import
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mp_termproject.adapter.CommentAdapter;
import com.example.mp_termproject.model.Comment;
import com.example.mp_termproject.model.Post;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

public class CommentActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CommentAdapter adapter;
    private List<Comment> commentList;
    private EditText etCommentInput;
    private ImageView btnPostComment;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private String postId;
    private String currentUserName;
    private String currentUserProfileUrl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        Post post = getIntent().getParcelableExtra(PostDetailActivity.POST_DATA);
        if (post == null || post.getPostId() == null) {
            Toast.makeText(this, "게시물 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        postId = post.getPostId();

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        initViews();
        setupRecyclerView();

        fetchCurrentUserInfo();
        listenForComments();

        btnPostComment.setOnClickListener(v -> postComment());
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar_comment);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.rv_comments);
        etCommentInput = findViewById(R.id.et_comment_input);
        btnPostComment = findViewById(R.id.btn_post_comment);
    }

    private void setupRecyclerView() {
        commentList = new ArrayList<>();
        adapter = new CommentAdapter(this, commentList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void fetchCurrentUserInfo() {
        if (currentUser != null) {
            db.collection("users").document(currentUser.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            currentUserName = documentSnapshot.getString("name");
                            currentUserProfileUrl = documentSnapshot.getString("imageUrl");
                        }
                    });
        }
    }

    private void listenForComments() {
        db.collection("posts").document(postId).collection("comments")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w("CommentActivity", "Listen failed.", e);
                        return;
                    }
                    if(snapshots != null) {
                        commentList.clear();
                        commentList.addAll(snapshots.toObjects(Comment.class));
                        adapter.notifyDataSetChanged();
                        if (!commentList.isEmpty()) {
                            recyclerView.scrollToPosition(commentList.size() - 1);
                        }
                    }
                });
    }

    private void postComment() {
        String commentText = etCommentInput.getText().toString().trim();
        if (TextUtils.isEmpty(commentText)) {
            Toast.makeText(this, "댓글을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentUser == null) {
            Toast.makeText(this, "로그인 후 이용해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        Comment newComment = new Comment(
                postId,
                currentUser.getUid(),
                currentUserName,
                currentUserProfileUrl,
                commentText,
                Timestamp.now()
        );

        WriteBatch batch = db.batch();
        DocumentReference newCommentRef = db.collection("posts").document(postId)
                .collection("comments").document();
        batch.set(newCommentRef, newComment);

        DocumentReference postRef = db.collection("posts").document(postId);
        batch.update(postRef, "commentCount", FieldValue.increment(1));

        batch.commit().addOnSuccessListener(aVoid -> {
            etCommentInput.setText("");
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "댓글 작성에 실패했습니다.", Toast.LENGTH_SHORT).show();
        });
    }
}