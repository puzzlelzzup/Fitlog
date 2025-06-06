package com.example.mp_termproject.fragment.profiletabs; // 패키지 경로 예시

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.mp_termproject.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
// Firebase 관련 import

import com.example.mp_termproject.adapter.PostGridAdapter;
import com.example.mp_termproject.model.Post;
import java.util.ArrayList;
import java.util.List;

public class UserPostsGridFragment extends Fragment {
    private RecyclerView rvUserPostsGrid;
    private PostGridAdapter adapter;
    private List<Post> postList;
    // Firebase 관련 변수

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_posts_grid, container, false);
        rvUserPostsGrid = view.findViewById(R.id.rv_user_posts_grid);
        rvUserPostsGrid.setLayoutManager(new GridLayoutManager(getContext(), 3));
        // setupRecyclerView();
        // loadUserPosts();
        postList = new ArrayList<>();
        adapter = new PostGridAdapter(getContext(), postList);
        rvUserPostsGrid.setAdapter(adapter);

        loadUserPosts();
        return view;
    }

    // private void setupRecyclerView() { ... }
    private void loadUserPosts() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (currentUser != null) {
            String uid = currentUser.getUid();

            db.collection("users").document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            if (name != null) {
                                db.collection("posts")
                                        .whereEqualTo("writer", name) // ✅ writer에 저장된 이름으로 쿼리
                                        .orderBy("timestamp", Query.Direction.DESCENDING)
                                        .get()
                                        .addOnSuccessListener(queryDocumentSnapshots -> {
                                            postList.clear();
                                            for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                                                String imageUrl = snapshot.getString("imageUrl");
                                                String writer = snapshot.getString("writer");
                                                String content = snapshot.getString("content") != null ? snapshot.getString("content") : "";
                                                String tags = snapshot.getString("tags");
                                                String category = snapshot.getString("category");
                                                String visibility = snapshot.getString("visibility");
                                                String userId = snapshot.getString("userId");
                                                Timestamp timestamp = snapshot.getTimestamp("timestamp");

                                                if (imageUrl != null) {
                                                    Post post = new Post(imageUrl, writer, content, tags, category, visibility, userId, timestamp);
                                                    postList.add(post);
                                                }
                                            }
                                            adapter.notifyDataSetChanged();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(getContext(), "게시물 불러오기 실패", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "사용자 정보 로딩 실패", Toast.LENGTH_SHORT).show();
                    });
        }

        /*
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("posts")
                .whereEqualTo("userId", currentUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    postList.clear();
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        String imageUrl = snapshot.getString("imageUrl");
                        String writer = snapshot.getString("writer");
                        String content = snapshot.getString("content") != null ? snapshot.getString("content") : "";
                        String tags = snapshot.getString("tags");
                        String category = snapshot.getString("category");
                        String visibility = snapshot.getString("visibility");
                        String userId = snapshot.getString("userId");
                        Timestamp timestamp = snapshot.getTimestamp("timestamp");

                        if (imageUrl != null) {
                            Post post = new Post(imageUrl, writer, content, tags, category, visibility, userId, timestamp);
                            postList.add(post);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "게시물 불러오기 실패", Toast.LENGTH_SHORT).show());

         */
    }
}