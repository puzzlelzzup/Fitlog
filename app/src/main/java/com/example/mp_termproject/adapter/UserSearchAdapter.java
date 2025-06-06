package com.example.mp_termproject.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mp_termproject.R;
import com.example.mp_termproject.User;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.ViewHolder> {

    private List<User> userList;
    private Context context;
    private OnAddFriendClickListener listener;

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

        // Glide 로딩 로직 제거, 항상 기본 이미지를 보여줌
        holder.profileImage.setImageResource(R.mipmap.ic_launcher);

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