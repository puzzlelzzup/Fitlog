package com.example.mp_termproject.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mp_termproject.R;
import com.example.mp_termproject.UserProfileActivity;
import com.example.mp_termproject.adapter.UserListAdapter;
import com.example.mp_termproject.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FollowingListFragment extends Fragment implements UserListAdapter.OnUserClickListener {

    private RecyclerView recyclerView;
    private UserListAdapter adapter;
    private List<User> userList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_follow_list, container, false);
        recyclerView = view.findViewById(R.id.rv_follow_list);
        userList = new ArrayList<>();
        adapter = new UserListAdapter(getContext(), userList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        loadFollowingUsers();
        return view;
    }

    private void loadFollowingUsers() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // "following" -> "friends" 경로로 변경
        db.collection("users").document(currentUserId).collection("following").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> friendIds = new ArrayList<>();
                    for(QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        friendIds.add(doc.getId());
                    }

                    if (!friendIds.isEmpty()) {
                        db.collection("users").whereIn(FieldPath.documentId(), friendIds).get()
                                .addOnSuccessListener(userSnapshots -> {
                                    userList.clear();
                                    userList.addAll(userSnapshots.toObjects(User.class));
                                    adapter.notifyDataSetChanged();
                                });
                    } else {
                        userList.clear();
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public void onUserClick(User user) {
        Intent intent = new Intent(getContext(), UserProfileActivity.class);
        intent.putExtra("user_id", user.getId());
        startActivity(intent);
    }
}