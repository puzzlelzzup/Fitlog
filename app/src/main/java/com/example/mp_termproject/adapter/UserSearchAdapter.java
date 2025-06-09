package com.example.mp_termproject.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // Glide import 추가
import com.example.mp_termproject.R;
import com.example.mp_termproject.model.User;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.ViewHolder> {

    private List<User> userList;
    private final Context context;
    private final OnAddFriendClickListener listener;

    public interface OnAddFriendClickListener {
        void onAddFriendClick(User user);
    }

    public UserSearchAdapter(Context context, OnAddFriendClickListener listener) {
        this.context = context;
        this.userList = new ArrayList<>();
        this.listener = listener;
    }

    public void setUsers(List<User> users) {
        this.userList = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_search, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);
        holder.name.setText(user.getName());
        holder.email.setText(user.getEmail());

        // Glide를 사용해 실제 프로필 사진을 불러오도록 변경
        if (user.getImageUrl() != null && !user.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(user.getImageUrl())
                    .placeholder(R.mipmap.ic_launcher_round) // 로딩 중 또는 사진이 없을 때 보여줄 기본 이미지
                    .into(holder.profileImage);
        } else {
            // 이미지 URL이 없는 경우 기본 이미지 표시
            holder.profileImage.setImageResource(R.mipmap.ic_launcher_round);
        }

        holder.addFriendButton.setOnClickListener(v -> listener.onAddFriendClick(user));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImage;
        TextView name, email;
        Button addFriendButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.iv_profile);
            name = itemView.findViewById(R.id.tv_name);
            email = itemView.findViewById(R.id.tv_email);
            addFriendButton = itemView.findViewById(R.id.btn_add_friend);
        }
    }
}