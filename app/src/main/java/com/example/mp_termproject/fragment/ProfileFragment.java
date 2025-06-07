package com.example.mp_termproject.fragment;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity; // ViewPager2 어댑터용
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mp_termproject.LoginScreen;
import com.example.mp_termproject.R;
import com.example.mp_termproject.User;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
    private DatabaseReference mUserDatabase;

    // 탭 아이콘 (Vector Asset으로 준비)
    private final int[] TAB_ICONS = new int[]{
            R.drawable.ic_profile_grid,      // 내 게시물 (그리드)
            R.drawable.ic_profile_bookmark,  // 저장한 게시물
            R.drawable.ic_profile_liked      // 좋아요 한 게시물
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

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            mUserDatabase = FirebaseDatabase.getInstance().getReference("Users").child(userId);
            loadUserProfileData();
        } else {
            navigateToLogin();
        }

        // ViewPager2와 Adapter 설정
        tabsAdapter = new ProfileTabsAdapter(getActivity()); // getActivity() 또는 requireActivity()
        viewPagerProfile.setAdapter(tabsAdapter);

        // TabLayout과 ViewPager2 연결
        new TabLayoutMediator(tabLayoutProfile, viewPagerProfile, (tab, position) -> {
            // 탭 아이콘 설정 (또는 텍스트 설정 가능)
            tab.setIcon(TAB_ICONS[position]);
            // if (position == 0) tab.setText("게시물");
            // else if (position == 1) tab.setText("저장됨");
            // else if (position == 2) tab.setText("좋아요");
        }).attach();


        btnLogoutProfile.setOnClickListener(v -> logoutUser());
        btnEditProfile.setOnClickListener(v -> {
            Toast.makeText(getContext(), "프로필 수정 기능 구현 예정", Toast.LENGTH_SHORT).show();
            // TODO: 프로필 수정 화면 (새 Activity 또는 Fragment)으로 이동
        });
    }

    private void loadUserProfileData() {
        mUserDatabase.addValueEventListener(new ValueEventListener() { // addListenerForSingleValueEvent 대신 addValueEventListener로 실시간 업데이트 (선택사항)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null && getContext() != null && isAdded()) { // isAdded() 추가
                    tvProfileName.setText(user.name);
                    // 핸들은 User 모델에 추가하거나, 이메일에서 생성하는 로직 필요
                    // tvProfileHandle.setText("@" + (user.email != null ? user.email.split("@")[0] : "username"));
                    if (user.email != null) { // 간단히 이메일 표시 (또는 핸들 필드 추가)
                        tvProfileHandle.setText(user.email);
                    }

                    // TODO: 게시물, 팔로워, 팔로잉 수 가져오는 로직 필요
                    // tvPostCount.setText("0"); // Firebase에서 가져와야 함
                    // tvFollowerCount.setText("0");
                    // tvFollowingCount.setText("0");

                    // TODO: Bio (소개글) 필드가 User 모델에 있다면 표시
                    // tvProfileBio.setText(user.bio != null ? user.bio : "소개글이 없습니다.");
                    tvProfileBio.setText("30대 직장인 | 다이어트 6개월 차\n건강한 식단과 꾸준한 운동으로 -12kg 감량 중!"); // 임시 Bio

                    if (user.imageUrl != null && !user.imageUrl.isEmpty()) {
                        Glide.with(ProfileFragment.this) // Fragment 컨텍스트 사용
                                .load(user.imageUrl)
                                .placeholder(R.mipmap.ic_launcher_round)
                                .error(R.mipmap.ic_launcher_round)
                                .into(ivProfilePicture);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (getContext() != null && isAdded()){
                    Toast.makeText(getContext(), "사용자 정보를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
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

    // ViewPager2를 위한 Adapter 클래스 (ProfileFragment 내부에 private static class로 정의)
    private static class ProfileTabsAdapter extends FragmentStateAdapter {
        public ProfileTabsAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new UserPostsGridFragment(); // 내 게시물 그리드
                case 1:
                    return new SavedPostsFragment();    // 저장된 게시물 (생성 필요)
                case 2:
                    return new LikedPostsFragment();    // 좋아요 한 게시물 (생성 필요)
                default:
                    return new UserPostsGridFragment(); // 기본값
            }
        }

        @Override
        public int getItemCount() {
            return 3; // 탭 개수
        }
    }
}