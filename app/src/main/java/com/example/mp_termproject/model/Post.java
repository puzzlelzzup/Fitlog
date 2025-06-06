package com.example.mp_termproject.model;

public class Post {
    public String imageUrl;
    private String writer;
    private String content;
    private String tags;
    private String category;
    private String visibility;
    private String userId;
    private com.google.firebase.Timestamp timestamp;

    // Firestore 디시리얼라이즈를 위한 빈 생성자
    public Post() {}

    // 생성자 (필요에 따라 추가 가능)

    public Post(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Post(String imageUrl, String writer, String content, String tags, String category,
                String visibility, String userId, com.google.firebase.Timestamp timestamp) {
        this.imageUrl = imageUrl;
        this.writer = writer;
        this.content = content;
        this.tags = tags;
        this.category = category;
        this.visibility = visibility;
        this.userId = userId;
        this.timestamp = timestamp;
    }

    // Getter & Setter
    public String getWriter() {
        return writer;
    }
    public void setWriter(String writer) {
        this.writer = writer;
    }
    public String getImageUrl() { return imageUrl; }
    public String getContent() { return content; }
    public String getTags() { return tags; }
    public String getCategory() { return category; }
    public String getVisibility() { return visibility; }
    public String getUserId() { return userId; }
    public com.google.firebase.Timestamp getTimestamp() { return timestamp; }
}