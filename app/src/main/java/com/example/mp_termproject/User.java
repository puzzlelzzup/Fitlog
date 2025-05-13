package com.example.mp_termproject;

public class User {
    public String id, name, email, mobile, imageUrl;

    public User() {} // Firebase용 기본 생성자

    public User(String id, String name, String email, String mobile, String imageUrl) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.imageUrl = imageUrl;
    }
}
