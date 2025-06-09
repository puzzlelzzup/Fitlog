package com.example.mp_termproject.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

public class Comment {
    @DocumentId
    private String commentId;
    private String postId;
    private String userId;
    private String userName;
    private String userProfileImageUrl;
    private String text;
    private Timestamp timestamp;

    public Comment() {} // Firestore용 빈 생성자

    public Comment(String postId, String userId, String userName, String userProfileImageUrl, String text, Timestamp timestamp) {
        this.postId = postId;
        this.userId = userId;
        this.userName = userName;
        this.userProfileImageUrl = userProfileImageUrl;
        this.text = text;
        this.timestamp = timestamp;
    }

    // Getter 메소드들
    public String getCommentId() { return commentId; }
    public String getPostId() { return postId; }
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getUserProfileImageUrl() { return userProfileImageUrl; }
    public String getText() { return text; }
    public Timestamp getTimestamp() { return timestamp; }
}