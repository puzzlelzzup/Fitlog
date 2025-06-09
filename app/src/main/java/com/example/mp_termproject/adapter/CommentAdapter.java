package com.example.mp_termproject.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.mp_termproject.PostDetailActivity;
import com.example.mp_termproject.R;
import com.example.mp_termproject.model.Comment;
import com.example.mp_termproject.model.User;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private final Context context;
    private final List<Comment> commentList;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public CommentAdapter(Context context, List<Comment> commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);

        holder.userName.setText(comment.getUserName());
        holder.commentText.setText(comment.getText());
        holder.timestamp.setText(PostDetailActivity.formatTimestamp(comment.getTimestamp()));

        // 작성자 ID로 프로필 이미지 불러오기
        fetchUserProfileImage(comment.getUserId(), holder.profileImage);
    }

    // 작성자 ID로 프로필 이미지를 불러오는 메소드
    private void fetchUserProfileImage(String userId, CircleImageView imageView) {
        if (userId == null || userId.isEmpty()) {
            imageView.setImageResource(R.mipmap.ic_launcher_round); // 기본 이미지 설정
            return;
        }

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if(documentSnapshot.exists()) {
                        String imageUrl = documentSnapshot.getString("imageUrl");
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(context)
                                    .load(imageUrl)
                                    .placeholder(R.mipmap.ic_launcher_round)
                                    .error(R.mipmap.ic_launcher_round) // 에러 시 기본 이미지
                                    .into(imageView);
                        } else {
                            imageView.setImageResource(R.mipmap.ic_launcher_round);
                        }
                    } else {
                        imageView.setImageResource(R.mipmap.ic_launcher_round);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("CommentAdapter", "프로필 이미지 로딩 실패", e);
                    imageView.setImageResource(R.mipmap.ic_launcher_round);
                });
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    // ViewHolder에 프로필 이미지(CircleImageView) 추가
    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImage;
        TextView userName, commentText, timestamp;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.iv_comment_profile);
            userName = itemView.findViewById(R.id.tv_comment_user_name);
            commentText = itemView.findViewById(R.id.tv_comment_text);
            timestamp = itemView.findViewById(R.id.tv_comment_timestamp);
        }
    }
}