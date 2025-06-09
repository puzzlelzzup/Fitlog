package com.example.mp_termproject.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.mp_termproject.EditProfileActivity;
import com.example.mp_termproject.FollowListActivity; // 새로 만든 액티비티 import
import com.example.mp_termproject.LoginScreen;
import com.example.mp_termproject.R;
import com.example.mp_termproject.model.User;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;

import com.example.mp_termproject.fragment.profiletabs.UserPostsGridFragment;
import com.example.mp_termproject.fragment.profiletabs.SavedPostsFragment;

public class ProfileFragment extends Fragment {

    private CircleImageView ivProfilePicture;
    private TextView tvProfileName, tvProfileHandle, tvPostCount, tvFollowerCount, tvFollowingCount, tvProfileBio;
    private Button btnEditProfile, btnLogoutProfile;
    private TabLayout tabLayoutProfile;
    private ViewPager2 viewPagerProfile;
    private ProfileTabsAdapter tabsAdapter;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DocumentReference userDocRef;

    private final int[] TAB_ICONS = new int[]{
            R.drawable.ic_profile_grid,
            R.drawable.ic_profile_bookmark
    };

    public ProfileFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initFirebase();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            userDocRef = db.collection("users").document(userId);
            addSnapshotListenerForUserProfile();
            loadUserPostCount(userId);
        } else {
            navigateToLogin();
        }

        setupTabs();
        setupListeners(); // 리스너 설정
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAuth.getCurrentUser() != null) {
            loadUserPostCount(mAuth.getCurrentUser().getUid());
        }
    }

    private void initViews(View view) {
        ivProfilePicture = view.findViewById(R.id.iv_profile_picture_main);
        tvProfileName = view.findViewById(R.id.tv_profile_main_name);
        tvProfileHandle = view.findViewById(R.id.tv_profile_handle);
        tvPostCount = view.findViewById(R.id.tv_post_count);
        tvFollowerCount = view.findViewById(R.id.tv_follower_count);
        tvFollowingCount = view.findViewById(R.id.tv_following_count);
        tvProfileBio = view.findViewById(R.id.tv_profile_bio);
        btnEditProfile = view.findViewById(R.id.btn_edit_profile_main);
        btnLogoutProfile = view.findViewById(R.id.btn_logout_profile_main);
        tabLayoutProfile = view.findViewById(R.id.tab_layout_profile);
        viewPagerProfile = view.findViewById(R.id.view_pager_profile);
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void setupTabs() {
        tabsAdapter = new ProfileTabsAdapter(requireActivity());
        viewPagerProfile.setAdapter(tabsAdapter);

        new TabLayoutMediator(tabLayoutProfile, viewPagerProfile, (tab, position) -> {
            tab.setIcon(TAB_ICONS[position]);
        }).attach();
    }

    // 클릭 리스너를 설정하는 메소드
    private void setupListeners() {
        btnLogoutProfile.setOnClickListener(v -> logoutUser());
        btnEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), EditProfileActivity.class));
        });

        // 팔로워/팔로잉 숫자 TextView에 클릭 리스너 추가
        tvFollowerCount.setOnClickListener(v -> openFollowList(0)); // 0: 팔로워 탭
        tvFollowingCount.setOnClickListener(v -> openFollowList(1)); // 1: 팔로잉 탭
    }

    // FollowListActivity를 여는 메소드
    private void openFollowList(int initialTab) {
        Intent intent = new Intent(getContext(), FollowListActivity.class);
        intent.putExtra("initial_tab", initialTab); // 0 또는 1을 전달하여 시작 탭 지정
        startActivity(intent);
    }

    private void addSnapshotListenerForUserProfile() {
        if (userDocRef == null) return;

        userDocRef.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null || getContext() == null || !isAdded() || documentSnapshot == null || !documentSnapshot.exists()) {
                return;
            }

            User user = documentSnapshot.toObject(User.class);
            if (user != null) {
                tvProfileName.setText(user.getName());
                tvProfileHandle.setText(user.getEmail());
                tvFollowerCount.setText(String.valueOf(user.getFollowerCount()));
                tvFollowingCount.setText(String.valueOf(user.getFollowingCount()));

                if (user.getBio() != null && !user.getBio().isEmpty()) {
                    tvProfileBio.setText(user.getBio());
                } else {
                    tvProfileBio.setText("소개글이 없습니다.");
                }

                if (user.getImageUrl() != null && !user.getImageUrl().isEmpty()) {
                    Glide.with(this).load(user.getImageUrl()).into(ivProfilePicture);
                } else {
                    ivProfilePicture.setImageResource(R.mipmap.ic_launcher_round);
                }
            }
        });
    }

    private void loadUserPostCount(String userId) {
        db.collection("posts")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (getContext() != null && isAdded()) {
                        tvPostCount.setText(String.valueOf(queryDocumentSnapshots.size()));
                    }
                });
    }

    private void logoutUser() {
        mAuth.signOut();
        navigateToLogin();
    }

    private void navigateToLogin() {
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), LoginScreen.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }

    private static class ProfileTabsAdapter extends FragmentStateAdapter {
        public ProfileTabsAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0: return new UserPostsGridFragment();
                case 1: return new SavedPostsFragment();
                default: return new UserPostsGridFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}