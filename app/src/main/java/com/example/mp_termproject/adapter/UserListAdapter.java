package com.example.mp_termproject.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.mp_termproject.R;
import com.example.mp_termproject.UserProfileActivity;
import com.example.mp_termproject.model.User;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserViewHolder> {

    private final Context context;
    private final List<User> userList;
    private final OnUserClickListener clickListener;

    // 클릭 리스너를 위한 인터페이스
    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    public UserListAdapter(Context context, List<User> userList, OnUserClickListener clickListener) {
        this.context = context;
        this.userList = userList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_follow, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        if (user == null) return;

        holder.userName.setText(user.getName());
        Glide.with(context)
                .load(user.getImageUrl())
                .placeholder(R.mipmap.ic_launcher_round)
                .into(holder.profileImage);

        // 아이템 뷰에 클릭 리스너 설정
        holder.itemView.setOnClickListener(v -> clickListener.onUserClick(user));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImage;
        TextView userName;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.iv_user_profile);
            userName = itemView.findViewById(R.id.tv_user_name);
        }
    }
}