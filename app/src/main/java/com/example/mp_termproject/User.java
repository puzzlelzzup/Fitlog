package com.example.mp_termproject;

public class User {
    private String id;
    private String name;
    private String email;
    // [신규 추가] 팔로워, 팔로잉 수 필드
    private long followerCount = 0;
    private long followingCount = 0;

    // Firestore가 데이터를 객체로 변환할 때 꼭 필요한 빈 생성자!
    public User() {
    }

    // private 변수에 접근하기 위한 public get 함수들 (Getter)
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public long getFollowerCount() {
        return followerCount;
    }

    public long getFollowingCount() {
        return followingCount;
    }
}