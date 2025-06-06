package com.example.mp_termproject.fragment;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mp_termproject.LoginScreen;
import com.example.mp_termproject.R;
import com.example.mp_termproject.User;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import de.hdodenhof.circleimageview.CircleImageView;

import com.example.mp_termproject.fragment.profiletabs.UserPostsGridFragment;
import com.example.mp_termproject.fragment.profiletabs.SavedPostsFragment;
import com.example.mp_termproject.fragment.profiletabs.LikedPostsFragment;

public class ProfileFragment extends Fragment {

    private CircleImageView ivProfilePicture;
    private TextView tvProfileName, tvProfileHandle, tvPostCount, tvFollowerCount, tvFollowingCount, tvProfileBio;
    private Button btnEditProfile, btnLogoutProfile;
    private TabLayout tabLayoutProfile;
    private ViewPager2 viewPagerProfile;
    private ProfileTabsAdapter tabsAdapter;

    private FirebaseAuth mAuth;
    private DocumentReference userDocRef;
    private FirebaseFirestore db;

    private final int[] TAB_ICONS = new int[]{
            R.drawable.ic_profile_grid,
            R.drawable.ic_profile_bookmark,
            R.drawable.ic_profile_liked
    };


    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // UI 초기화
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

        // Firebase 서비스 초기화
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            userDocRef = db.collection("users").document(userId);
            loadUserProfileData();
        } else {
            navigateToLogin();
        }

        tabsAdapter = new ProfileTabsAdapter(requireActivity());
        viewPagerProfile.setAdapter(tabsAdapter);

        new TabLayoutMediator(tabLayoutProfile, viewPagerProfile, (tab, position) -> {
            tab.setIcon(TAB_ICONS[position]);
        }).attach();

        btnLogoutProfile.setOnClickListener(v -> logoutUser());
        btnEditProfile.setOnClickListener(v -> {
            Toast.makeText(getContext(), "프로필 수정 기능 구현 예정", Toast.LENGTH_SHORT).show();
        });
    }

    // [수정] loadUserProfileData 메소드
    private void loadUserProfileData() {
        userDocRef.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                if (getContext() != null && isAdded()) {
                    Toast.makeText(getContext(), "데이터 로딩 중 오류 발생", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists() && getContext() != null && isAdded()) {
                User user = documentSnapshot.toObject(User.class);
                if (user != null) {
                    tvProfileName.setText(user.getName());
                    tvProfileHandle.setText(user.getEmail());

                    tvFollowerCount.setText(String.valueOf(user.getFollowerCount()));
                    tvFollowingCount.setText(String.valueOf(user.getFollowingCount()));

                    tvPostCount.setText("0"); // TODO: 게시물 수 로직 필요

                    tvProfileBio.setText("30대 직장인 | 다이어트 6개월 차\n건강한 식단과 꾸준한 운동으로 -12kg 감량 중!");
                    ivProfilePicture.setImageResource(R.mipmap.ic_launcher_round);
                }
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
                case 2: return new LikedPostsFragment();
                default: return new UserPostsGridFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }
}