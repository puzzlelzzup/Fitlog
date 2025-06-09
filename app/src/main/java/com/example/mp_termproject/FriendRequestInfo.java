package com.example.mp_termproject;

import com.example.mp_termproject.model.User;

public class FriendRequestInfo {
    private String requestId;
    private User sender; // 요청 보낸 사람의 정보

    public FriendRequestInfo(String requestId, User sender) {
        this.requestId = requestId;
        this.sender = sender;
    }

    public String getRequestId() {
        return requestId;
    }

    public User getSender() {
        return sender;
    }
}