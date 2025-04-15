package kr.test.termprojecttest;

public class User {
    public String id, name, email, mobile, imageUrl, about;

    public User() {} // Firebase용 기본 생성자

    public User(String id, String name, String email, String mobile, String imageUrl, String about) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.imageUrl = imageUrl;
        this.about = about;
    }
}
