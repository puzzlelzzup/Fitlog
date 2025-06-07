package com.example.mp_termproject;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mp_termproject.fragment.FollowerListFragment;
import com.example.mp_termproject.fragment.FollowingListFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class FollowListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_list);

        Toolbar toolbar = findViewById(R.id.toolbar_follow_list);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        // 툴바 제목을 현재 사용자 닉네임으로 설정
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            FirebaseFirestore.getInstance().collection("users").document(currentUser.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            if (name != null) {
                                getSupportActionBar().setTitle(name);
                            }
                        }
                    });
        } else {
            getSupportActionBar().setTitle(""); // 사용자 이름이 없으면 비워두기
        }

        ViewPager2 viewPager = findViewById(R.id.view_pager_follow);
        TabLayout tabLayout = findViewById(R.id.tab_layout_follow);

        FollowPagerAdapter adapter = new FollowPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // 탭 레이아웃과 뷰페이저 연결
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("팔로워");
            } else {
                tab.setText("팔로잉");
            }
        }).attach();

        // 프로필 화면에서 "팔로잉"을 눌렀을 경우, 두 번째 탭이 먼저 보이도록 설정
        int initialTab = getIntent().getIntExtra("initial_tab", 0);
        viewPager.setCurrentItem(initialTab, false);
    }

    // ViewPager2를 위한 어댑터 (Activity 내부에 private static class로 정의)
    private static class FollowPagerAdapter extends FragmentStateAdapter {
        public FollowPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return new FollowerListFragment(); // 팔로워 탭
            } else {
                return new FollowingListFragment(); // 팔로잉 탭
            }
        }

        @Override
        public int getItemCount() {
            return 2; // 탭 개수
        }
    }
}