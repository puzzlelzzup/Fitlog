package com.example.mp_termproject.model;

public class User {

    // 모든 필드를 private으로 선언하여 외부에서 직접 수정하는 것을 방지
    private String id;
    private String name;
    private String email;
    private String mobile;
    private String imageUrl;
    private String bio;         // (ProfileFragment 호환을 위해 추가)
    private long followerCount = 0;
    private long followingCount = 0;

    // Firestore가 데이터를 객체로 변환할 때 꼭 필요한 빈 생성자
    public User() {
    }

    // 코드 내에서 User 객체를 쉽게 생성하기 위한 생성자
    public User(String id, String name, String email, String mobile, String imageUrl, String bio) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.imageUrl = imageUrl;
        this.bio = bio;
    }

    // private 필드에 안전하게 접근하기 위한 public get 함수들
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getMobile() {
        return mobile;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getBio() {
        return bio;
    }

    public long getFollowerCount() {
        return followerCount;
    }

    public long getFollowingCount() {
        return followingCount;
    }
}