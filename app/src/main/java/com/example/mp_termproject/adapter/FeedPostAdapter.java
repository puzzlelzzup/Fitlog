package com.example.mp_termproject.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mp_termproject.CommentActivity;
import com.example.mp_termproject.PostDetailActivity;
import com.example.mp_termproject.R;
import com.example.mp_termproject.SinglePostActivity;
import com.example.mp_termproject.model.Post;
import com.example.mp_termproject.model.User; // [수정] 올바른 경로로 변경
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class FeedPostAdapter extends RecyclerView.Adapter<FeedPostAdapter.PostViewHolder> {

    private final Context context;
    private final List<Post> postList;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private final Set<String> bookmarkedPostIds;

    public FeedPostAdapter(Context context, List<Post> postList, Set<String> bookmarkedPostIds) {
        this.context = context;
        this.postList = postList;
        this.bookmarkedPostIds = bookmarkedPostIds;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_feed_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);
        if (post == null || post.getPostId() == null) return;

        holder.userName.setText(post.getWriter());
        holder.caption.setText(post.getContent());
        holder.timestamp.setText(PostDetailActivity.formatTimestamp(post.getTimestamp()));
        holder.likeCount.setText("좋아요 " + post.getLikeCount() + "개");
        holder.commentCount.setText("댓글 " + post.getCommentCount() + "개 보기");
        Glide.with(context).load(post.getImageUrl()).into(holder.postImage);

        // 작성자 프로필 사진 불러오기
        fetchWriterProfileImage(post.getUserId(), holder.userProfile);

        // 버튼 상태 업데이트 및 리스너 설정
        updateLikeButtonUI(holder.likeButton, post);
        updateBookmarkButtonUI(holder.bookmarkButton, post.getPostId());
        setupClickListeners(holder, post);
    }

    // 프로필 사진을 User 객체를 통해 가져오도록 변경
    private void fetchWriterProfileImage(String userId, CircleImageView imageView) {
        if (userId == null) return;
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null && user.getImageUrl() != null && !user.getImageUrl().isEmpty()) {
                            Glide.with(context)
                                    .load(user.getImageUrl())
                                    .placeholder(R.mipmap.ic_launcher_round)
                                    .into(imageView);
                        }
                    }
                });
    }

    // 클릭 리스너들을 모아서 관리하는 메소드
    private void setupClickListeners(@NonNull PostViewHolder holder, Post post) {
        holder.likeButton.setOnClickListener(v -> {
            if (currentUser != null) {
                toggleLikeStatus(post, holder);
            } else {
                Toast.makeText(context, "로그인 후 이용해주세요.", Toast.LENGTH_SHORT).show();
            }
        });

        View.OnClickListener commentClickListener = v -> {
            Intent intent = new Intent(context, CommentActivity.class);
            intent.putExtra(PostDetailActivity.POST_DATA, post);
            context.startActivity(intent);
        };
        holder.commentButton.setOnClickListener(commentClickListener);
        holder.commentCount.setOnClickListener(commentClickListener);

        holder.bookmarkButton.setOnClickListener(v -> {
            if (currentUser != null) {
                toggleBookmarkStatus(post, holder.bookmarkButton);
            } else {
                Toast.makeText(context, "로그인 후 이용해주세요.", Toast.LENGTH_SHORT).show();
            }
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, SinglePostActivity.class);
            intent.putExtra(PostDetailActivity.POST_DATA, post);
            context.startActivity(intent);
        });
    }
    // 좋아요 버튼 UI 업데이트
    private void updateLikeButtonUI(ImageView likeButton, Post post) {
        if (currentUser != null && post.getLikedBy() != null && post.getLikedBy().contains(currentUser.getUid())) {
            likeButton.setImageResource(R.drawable.ic_like_filled);
        } else {
            likeButton.setImageResource(R.drawable.ic_like_outline);
        }
    }

    // 북마크 버튼 UI 업데이트
    private void updateBookmarkButtonUI(ImageView bookmarkButton, String postId) {
        if (bookmarkedPostIds != null && bookmarkedPostIds.contains(postId)) {
            bookmarkButton.setImageResource(R.drawable.ic_bookmark_filled);
        } else {
            bookmarkButton.setImageResource(R.drawable.ic_bookmark_outline);
        }
    }

    // 좋아요 상태 토클
    private void toggleLikeStatus(Post post, PostViewHolder holder) {
        if (currentUser == null) return;
        String userId = currentUser.getUid();
        DocumentReference postRef = db.collection("posts").document(post.getPostId());

        boolean isCurrentlyLiked = post.getLikedBy().contains(userId);

        // UI 반영
        if (isCurrentlyLiked) {
            post.getLikedBy().remove(userId);
        } else {
            post.getLikedBy().add(userId);
        }
        holder.likeCount.setText("좋아요 " + post.getLikedBy().size() + "개");
        updateLikeButtonUI(holder.likeButton, post);

        db.runTransaction((Transaction.Function<Void>) transaction -> {
            Post snapshot = transaction.get(postRef).toObject(Post.class);
            if (snapshot == null) return null;
            List<String> newLikedBy = new ArrayList<>(snapshot.getLikedBy());
            if (isCurrentlyLiked) {
                newLikedBy.remove(userId);
            } else {
                newLikedBy.add(userId);
            }
            transaction.update(postRef, "likedBy", newLikedBy);
            transaction.update(postRef, "likeCount", newLikedBy.size());
            return null;
        }).addOnFailureListener(e -> {
            Log.e("FeedAdapter", "좋아요 트랜잭션 실패", e);
            if (isCurrentlyLiked) {
                post.getLikedBy().add(userId);
            } else {
                post.getLikedBy().remove(userId);
            }
            holder.likeCount.setText("좋아요 " + post.getLikedBy().size() + "개");
            updateLikeButtonUI(holder.likeButton, post);
        });
    }
    // 북마크 상태
    private void toggleBookmarkStatus(Post post, ImageView bookmarkButton) {
        if (currentUser == null) return;
        String userId = currentUser.getUid();
        String postId = post.getPostId();
        DocumentReference bookmarkRef = db.collection("users").document(userId)
                .collection("bookmarks").document(postId);

        boolean isCurrentlyBookmarked = bookmarkedPostIds.contains(postId);

        // UI 반영
        if (isCurrentlyBookmarked) {
            bookmarkedPostIds.remove(postId);
        } else {
            bookmarkedPostIds.add(postId);
        }
        updateBookmarkButtonUI(bookmarkButton, postId);

        if (isCurrentlyBookmarked) {
            bookmarkRef.delete();
        } else {
            Map<String, Object> bookmarkData = new HashMap<>();
            bookmarkData.put("bookmarkedAt", com.google.firebase.Timestamp.now());
            bookmarkRef.set(bookmarkData);
        }
    }

    // 포스트 개수 반환
    @Override
    public int getItemCount() {
        return postList.size();
    }

    // Viewholder 정의
    public static class PostViewHolder extends RecyclerView.ViewHolder {
        CircleImageView userProfile;
        ImageView postImage, likeButton, commentButton, bookmarkButton;
        TextView userName, timestamp, likeCount, caption, commentCount;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            userProfile = itemView.findViewById(R.id.iv_post_user_profile);
            postImage = itemView.findViewById(R.id.iv_post_main_image);
            likeButton = itemView.findViewById(R.id.iv_post_like);
            commentButton = itemView.findViewById(R.id.iv_post_comment);
            bookmarkButton = itemView.findViewById(R.id.iv_post_bookmark);
            userName = itemView.findViewById(R.id.tv_post_user_name);
            timestamp = itemView.findViewById(R.id.tv_post_time);
            likeCount = itemView.findViewById(R.id.tv_post_like_count);
            caption = itemView.findViewById(R.id.tv_post_caption);
            commentCount = itemView.findViewById(R.id.tv_post_view_comments);
        }
    }
}