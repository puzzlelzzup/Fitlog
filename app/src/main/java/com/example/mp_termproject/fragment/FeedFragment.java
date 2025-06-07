package com.example.mp_termproject.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.mp_termproject.R;
// import com.example.mp_termproject.adapter.FriendListAdapter; // TODO: FriendListAdapter 생성 필요
// import com.example.mp_termproject.adapter.FeedPostAdapter; // TODO: FeedPostAdapter 생성 필요
// import com.example.mp_termproject.model.User; // TODO: User 또는 Friend 모델 생성/활용 필요
// import com.example.mp_termproject.model.Post; // TODO: Post 모델 생성 필요

import java.util.ArrayList; // 임시 데이터용

public class FeedFragment extends Fragment {

    private EditText etFriendSearch;
    private RecyclerView rvFriendList;
    private RecyclerView rvFeedPosts;

    // TODO: 어댑터 및 데이터 리스트 선언
    // private FriendListAdapter friendListAdapter;
    // private FeedPostAdapter feedPostAdapter;
    // private ArrayList<User> friendDataList; // 또는 Friend 모델
    // private ArrayList<Post> postDataList;

    public FeedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_feed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etFriendSearch = view.findViewById(R.id.et_friend_search);
        rvFriendList = view.findViewById(R.id.rv_friend_list);
        rvFeedPosts = view.findViewById(R.id.rv_feed_posts);

        setupFriendListRecyclerView();
        setupFeedPostsRecyclerView();

        // TODO: 검색창 리스너 설정
        // TODO: Firebase에서 데이터 로드 및 어댑터에 연결
        loadPlaceholderData(); // 임시 데이터 로드 (실제로는 Firebase에서 로드)
    }

    private void setupFriendListRecyclerView() {
        rvFriendList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        // friendDataList = new ArrayList<>();
        // friendListAdapter = new FriendListAdapter(getContext(), friendDataList); // TODO: 어댑터 생성
        // rvFriendList.setAdapter(friendListAdapter);
        // TODO: '친구 추가' 버튼 아이템 처리 로직은 어댑터 내에서 구현
    }

    private void setupFeedPostsRecyclerView() {
        rvFeedPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        // postDataList = new ArrayList<>();
        // feedPostAdapter = new FeedPostAdapter(getContext(), postDataList); // TODO: 어댑터 생성
        // rvFeedPosts.setAdapter(feedPostAdapter);
    }

    // 실제 앱에서는 이 부분을 Firebase 데이터 로딩으로 대체해야 합니다.
    private void loadPlaceholderData() {
        // 임시 친구 목록 데이터 (실제로는 User 모델 사용)
        // if (friendListAdapter != null) {
        //     ArrayList<User> tempFriends = new ArrayList<>();
        //     // tempFriends.add(new User("friend1_id", "김지민", "email", "mobile", "profile_url_1"));
        //     // tempFriends.add(new User("friend2_id", "박준호", "email", "mobile", "profile_url_2"));
        //     // friendListAdapter.updateData(tempFriends); // 어댑터에 데이터 업데이트 메서드 필요
        // }

        // 임시 피드 게시물 데이터 (실제로는 Post 모델 사용)
        // if (feedPostAdapter != null) {
        //     ArrayList<Post> tempPosts = new ArrayList<>();
        //     // tempPosts.add(new Post("post1_id", "user1_id", "김지연", "profile_url_user1", "image_url_post1", "오늘의 점심 식단! ...", System.currentTimeMillis() - 7200000, 24));
        //     // feedPostAdapter.updateData(tempPosts); // 어댑터에 데이터 업데이트 메서드 필요
        // }
    }
}