package com.example.mp_termproject.fragment.profiletabs;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mp_termproject.R;
import com.example.mp_termproject.adapter.PostGridAdapter;
import com.example.mp_termproject.model.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserPostsGridFragment extends Fragment {

    private static final String ARG_USER_ID = "user_id";
    private String userId;

    private RecyclerView rvUserPostsGrid;
    private PostGridAdapter adapter;
    private List<Post> postList;

    // 어떤 사용자의 게시물을 보여줄지 ID를 받는 메소드
    public static UserPostsGridFragment newInstance(String userId) {
        UserPostsGridFragment fragment = new UserPostsGridFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString(ARG_USER_ID);
        } else {
            // ID가 넘어오지 않으면 로그인한 사용자 ID를 사용
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_posts_grid, container, false);
        initViews(view);
        loadUserPosts();
        return view;
    }

    private void initViews(View view) {
        rvUserPostsGrid = view.findViewById(R.id.rv_user_posts_grid);
        postList = new ArrayList<>();
        adapter = new PostGridAdapter(getContext(), postList);
        rvUserPostsGrid.setLayoutManager(new GridLayoutManager(getContext(), 3));
        rvUserPostsGrid.setAdapter(adapter);
    }

    // userId를 사용해 해당 사용자의 게시물을 불러오도록 변경
    private void loadUserPosts() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("posts")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    postList.clear();
                    for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                        Post post = snapshot.toObject(Post.class);
                        postList.add(post);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("UserPostsGridFragment", "게시물 로딩 실패", e);
                });
    }
}