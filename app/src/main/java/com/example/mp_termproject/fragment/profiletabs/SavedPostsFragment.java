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
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SavedPostsFragment extends Fragment {

    private RecyclerView rvSavedPosts;
    private PostGridAdapter adapter;
    private List<Post> postList;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // UserPostsGridFragment와 동일한 레이아웃 재사용
        View view = inflater.inflate(R.layout.fragment_user_posts_grid, container, false);

        initFirebase();
        initViews(view);
        loadSavedPosts();

        return view;
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    private void initViews(View view) {
        rvSavedPosts = view.findViewById(R.id.rv_user_posts_grid);
        postList = new ArrayList<>();
        adapter = new PostGridAdapter(getContext(), postList);
        rvSavedPosts.setAdapter(adapter);
        // 레이아웃 매니저는 XML에서 설정했으므로 여기서 또 설정할 필요 없음
    }

    private void loadSavedPosts() {
        if (currentUser == null) return;

        // 1. 내가 북마크한 게시물의 ID 목록을 가져옴
        db.collection("users").document(currentUser.getUid()).collection("bookmarks")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> bookmarkedPostIds = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        bookmarkedPostIds.add(doc.getId());
                    }

                    if (!bookmarkedPostIds.isEmpty()) {
                        // 2. ID 목록으로 실제 게시물 정보를 가져옴
                        fetchPostsByIds(bookmarkedPostIds);
                    }
                })
                .addOnFailureListener(e -> Log.e("SavedPostsFragment", "북마크 ID 로딩 실패", e));
    }

    private void fetchPostsByIds(List<String> postIds) {
        // 참고: whereIn 쿼리는 ID가 30개가 넘어가면 여러번 나눠서 호출해야 함
        db.collection("posts").whereIn(FieldPath.documentId(), postIds)
                .get()
                .addOnSuccessListener(postSnapshots -> {
                    postList.clear();
                    postList.addAll(postSnapshots.toObjects(Post.class));
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("SavedPostsFragment", "북마크한 게시물 로딩 실패", e));
    }
}