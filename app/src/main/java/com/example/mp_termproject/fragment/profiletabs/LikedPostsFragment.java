package com.example.mp_termproject.fragment.profiletabs;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mp_termproject.R;
import com.example.mp_termproject.adapter.PostGridAdapter;
import com.example.mp_termproject.model.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class LikedPostsFragment extends Fragment {

    private RecyclerView rvLikedPosts;
    private PostGridAdapter adapter;
    private List<Post> postList;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_posts_grid, container, false);

        initFirebase();
        initViews(view);
        loadLikedPosts();

        return view;
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    private void initViews(View view) {
        rvLikedPosts = view.findViewById(R.id.rv_user_posts_grid);
        postList = new ArrayList<>();
        adapter = new PostGridAdapter(getContext(), postList);
        rvLikedPosts.setAdapter(adapter);
    }

    private void loadLikedPosts() {
        if (currentUser == null) return;

        db.collection("posts")
                .whereArrayContains("likedBy", currentUser.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    postList.clear();
                    postList.addAll(queryDocumentSnapshots.toObjects(Post.class));
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("LikedPostsFragment", "좋아요한 게시물 로딩 실패", e));
    }
}