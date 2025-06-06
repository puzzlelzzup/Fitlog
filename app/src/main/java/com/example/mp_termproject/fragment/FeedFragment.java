package com.example.mp_termproject.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.mp_termproject.AuthRepository;
import com.example.mp_termproject.FriendRequestInfo;
import com.example.mp_termproject.R;
import com.example.mp_termproject.adapter.FriendRequestAdapter;
import com.example.mp_termproject.adapter.UserSearchAdapter;
import com.example.mp_termproject.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FeedFragment extends Fragment implements FriendRequestAdapter.OnRequestActionListener {

    private EditText etFriendSearch;
    private RecyclerView rvSearchResults;
    private RecyclerView rvFriendRequests;
    private LinearLayout feedContentGroup;

    private UserSearchAdapter userSearchAdapter;
    private FriendRequestAdapter friendRequestAdapter;
    private AuthRepository authRepository;
    private FirebaseUser currentUser;

    public FeedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etFriendSearch = view.findViewById(R.id.et_friend_search);
        rvSearchResults = view.findViewById(R.id.rv_search_results);
        rvFriendRequests = view.findViewById(R.id.rv_friend_requests);
        feedContentGroup = view.findViewById(R.id.feed_content_group);

        authRepository = new AuthRepository();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        setupSearchRecyclerView();
        setupFriendRequestRecyclerView();
        loadFriendRequests();
        setupSearchFunctionality();
    }

    // 이 메소드가 없어서 에러가 발생한 것!
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
                    if (rvFriendRequests.getVisibility() == View.GONE) {
                        feedContentGroup.setVisibility(View.VISIBLE);
                    }
                    if (userSearchAdapter != null) {
                        userSearchAdapter.setUsers(new ArrayList<>());
                    }
                } else {
                    rvSearchResults.setVisibility(View.VISIBLE);
                    feedContentGroup.setVisibility(View.GONE);
                    rvFriendRequests.setVisibility(View.GONE);
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