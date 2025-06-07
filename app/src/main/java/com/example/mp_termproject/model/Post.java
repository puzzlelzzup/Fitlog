package com.example.mp_termproject.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import java.util.ArrayList;
import java.util.List;

public class Post implements Parcelable {

    @DocumentId
    private String postId;
    private String imageUrl;
    private String writer;
    private String content;
    private String tags;
    private String category;
    private String visibility;
    private String userId;
    private Timestamp timestamp;
    private long likeCount = 0;
    private long commentCount = 0;
    private List<String> likedBy = new ArrayList<>();

    // 1. Firestore가 사용하는 빈 생성자
    public Post() {}

    // 2. UserPostsGridFragment에서 직접 객체를 만들 때 사용하는 생성자 (에러 해결!)
    public Post(String imageUrl, String writer, String content, String tags, String category, String visibility, String userId, Timestamp timestamp) {
        this.imageUrl = imageUrl;
        this.writer = writer;
        this.content = content;
        this.tags = tags;
        this.category = category;
        this.visibility = visibility;
        this.userId = userId;
        this.timestamp = timestamp;
    }

    // 3. Parcelable이 사용하는 생성자
    protected Post(Parcel in) {
        postId = in.readString();
        imageUrl = in.readString();
        writer = in.readString();
        content = in.readString();
        tags = in.readString();
        category = in.readString();
        visibility = in.readString();
        userId = in.readString();
        timestamp = in.readParcelable(Timestamp.class.getClassLoader());
        likeCount = in.readLong();
        commentCount = in.readLong();
        likedBy = in.createStringArrayList();
    }

    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }
        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(postId);
        dest.writeString(imageUrl);
        dest.writeString(writer);
        dest.writeString(content);
        dest.writeString(tags);
        dest.writeString(category);
        dest.writeString(visibility);
        dest.writeString(userId);
        dest.writeParcelable(timestamp, flags);
        dest.writeLong(likeCount);
        dest.writeLong(commentCount);
        dest.writeStringList(likedBy);
    }

    // --- Getter 메소드들 ---
    public String getPostId() { return postId; }
    public String getImageUrl() { return imageUrl; }
    public String getWriter() { return writer; }
    public String getContent() { return content; }
    public String getTags() { return tags; }
    public String getCategory() { return category; }
    public String getVisibility() { return visibility; }
    public String getUserId() { return userId; }
    public Timestamp getTimestamp() { return timestamp; }
    public long getLikeCount() { return likeCount; }
    public long getCommentCount() { return commentCount; }
    public List<String> getLikedBy() {
        if (likedBy == null) {
            return new ArrayList<>();
        }
        return likedBy;
    }
}