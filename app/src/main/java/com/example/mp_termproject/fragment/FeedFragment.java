package com.example.mp_termproject.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mp_termproject.AuthRepository;
import com.example.mp_termproject.FriendRequestInfo;
import com.example.mp_termproject.R;
import com.example.mp_termproject.model.User;
import com.example.mp_termproject.adapter.FeedPostAdapter;
import com.example.mp_termproject.adapter.FriendRequestAdapter;
import com.example.mp_termproject.adapter.UserSearchAdapter;
import com.example.mp_termproject.model.Post;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FeedFragment extends Fragment implements FriendRequestAdapter.OnRequestActionListener {

    private EditText etFriendSearch;
    private RecyclerView rvSearchResults, rvFriendRequests;
    private LinearLayout feedContentGroup;
    private RecyclerView rvFeedPosts;

    private FeedPostAdapter feedPostAdapter;
    private List<Post> feedPostList;
    private Set<String> bookmarkedPostIds = new HashSet<>();

    private UserSearchAdapter userSearchAdapter;
    private FriendRequestAdapter friendRequestAdapter;
    private AuthRepository authRepository;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    public FeedFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initFirebase();

        setupSearchRecyclerView();
        setupFriendRequestRecyclerView();
        setupFeedPostsRecyclerView();

        loadBookmarkStatus(); // 북마크 상태 먼저 불러오기
        loadFriendRequests();
        setupSearchFunctionality();
    }

    private void initViews(View view) {
        etFriendSearch = view.findViewById(R.id.et_friend_search);
        rvSearchResults = view.findViewById(R.id.rv_search_results);
        rvFriendRequests = view.findViewById(R.id.rv_friend_requests);
        feedContentGroup = view.findViewById(R.id.feed_content_group);
        rvFeedPosts = view.findViewById(R.id.rv_feed_posts);
    }

    private void initFirebase() {
        authRepository = new AuthRepository();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
    }

    private void setupFeedPostsRecyclerView() {
        feedPostList = new ArrayList<>();
        // 어댑터 생성 시 북마크 목록을 함께 전달
        feedPostAdapter = new FeedPostAdapter(getContext(), feedPostList, bookmarkedPostIds);
        rvFeedPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFeedPosts.setAdapter(feedPostAdapter);
    }

    private void loadBookmarkStatus() {
        if (currentUser == null) {
            loadFriendsFeed();
            return;
        }
        db.collection("users").document(currentUser.getUid()).collection("bookmarks")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    bookmarkedPostIds.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        bookmarkedPostIds.add(doc.getId());
                    }
                    loadFriendsFeed();
                })
                .addOnFailureListener(e -> {
                    Log.e("FeedFragment", "북마크 정보 로딩 실패", e);
                    loadFriendsFeed();
                });
    }

    private void loadFriendsFeed() {
        if (currentUser == null) return;

        String currentUserId = currentUser.getUid();
        Log.d("FeedFragment", "loadFriendsFeed 호출됨. 현재 사용자 UID: " + currentUserId);

        authRepository.getFeedPosts(currentUserId, new AuthRepository.FeedPostsListener() {
            @Override
            public void onPostsLoaded(List<Post> posts) {
                Log.d("FeedFragment", "AuthRepository.getFeedPosts - 게시물 로딩 성공. 개수: " + posts.size());
                feedPostList.clear();
                feedPostList.addAll(posts); // 가져온 게시물로 리스트 업데이트
                feedPostAdapter.notifyDataSetChanged(); // 어댑터에게 데이터 변경 알림
                Log.d("FeedFragment", "피드 어댑터 데이터 업데이트 완료.");
            }

            @Override
            public void onError(String message) {
                Log.e("FeedFragment", "AuthRepository.getFeedPosts - 게시물 로딩 실패: " + message);
                Toast.makeText(getContext(), "피드 게시물을 불러오는 데 실패했습니다: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSearchRecyclerView() {
        rvSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));
        userSearchAdapter = new UserSearchAdapter(getContext(), user -> {
            if (currentUser != null) {
                String fromUid = currentUser.getUid();
                String toUid = user.getId();

                if (fromUid.equals(toUid)) {
                    Toast.makeText(getContext(), "자기 자신에게는 친구 요청을 보낼 수 없습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                db.collection("users").document(fromUid).collection("friends").document(toUid).get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                                Toast.makeText(getContext(), "이미 친구 관계입니다.", Toast.LENGTH_SHORT).show();
                            } else {
                                authRepository.sendFriendRequest(fromUid, toUid, new AuthRepository.FriendRequestListener() {
                                    @Override
                                    public void onSuccess() {
                                        Toast.makeText(getContext(), user.getName() + "님에게 친구 요청을 보냈습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                    @Override
                                    public void onFailure(String message) {
                                        Toast.makeText(getContext(), "요청 실패: " + message, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
            }
        });
        rvSearchResults.setAdapter(userSearchAdapter);
    }

    private void setupFriendRequestRecyclerView() {
        rvFriendRequests.setLayoutManager(new LinearLayoutManager(getContext()));
        friendRequestAdapter = new FriendRequestAdapter(this);
        rvFriendRequests.setAdapter(friendRequestAdapter);
    }

    private void loadFriendRequests() {
        if (currentUser != null) {
            authRepository.getFriendRequestsWithUserInfo(currentUser.getUid(), new AuthRepository.FriendRequestInfoListener() {
                @Override
                public void onListLoaded(List<FriendRequestInfo> requestInfoList) {
                    if (requestInfoList != null && !requestInfoList.isEmpty()) {
                        rvFriendRequests.setVisibility(View.VISIBLE);
                        friendRequestAdapter.setRequests(requestInfoList);
                    } else {
                        rvFriendRequests.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(getContext(), "친구 요청 목록 로딩 오류: " + message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onAccept(FriendRequestInfo requestInfo) {
        String fromUid = requestInfo.getSender().getId();
        String toUid = currentUser.getUid();
        authRepository.acceptFriendRequest(requestInfo.getRequestId(), fromUid, toUid, new AuthRepository.FriendRequestListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(getContext(), requestInfo.getSender().getName() + "님과 친구가 되었습니다.", Toast.LENGTH_SHORT).show();
                loadFriendRequests();
                loadFriendsFeed(); // 친구가 되었으니 피드를 새로고침
            }
            @Override
            public void onFailure(String message) {
                Toast.makeText(getContext(), "수락 실패: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDecline(FriendRequestInfo requestInfo) {
        authRepository.declineFriendRequest(requestInfo.getRequestId(), new AuthRepository.FriendRequestListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(getContext(), "요청을 거절했습니다.", Toast.LENGTH_SHORT).show();
                loadFriendRequests();
            }
            @Override
            public void onFailure(String message) {
                Toast.makeText(getContext(), "거절 실패: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSearchFunctionality() {
        etFriendSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String searchText = s.toString().trim();
                if (searchText.isEmpty()) {
                    rvSearchResults.setVisibility(View.GONE);
                    feedContentGroup.setVisibility(View.VISIBLE);
                    loadFriendRequests();
                } else {
                    rvSearchResults.setVisibility(View.VISIBLE);
                    feedContentGroup.setVisibility(View.GONE);
                    searchForFriends(searchText);
                }
            }
        });
    }

    private void searchForFriends(String searchText) {
        authRepository.searchUsers(searchText, new AuthRepository.UserSearchListener() {
            @Override
            public void onSearchSuccess(List<User> users) {
                if (currentUser != null && users != null) {
                    String myUid = currentUser.getUid();
                    List<User> filteredUsers = users.stream()
                            .filter(user -> !user.getId().equals(myUid))
                            .collect(Collectors.toList());
                    userSearchAdapter.setUsers(filteredUsers);
                }
            }
            @Override
            public void onSearchFailure(String message) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "검색 실패: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}