package com.example.mp_termproject.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mp_termproject.PostDetailActivity; // SinglePostActivity로 변경 필요
import com.example.mp_termproject.R;
import com.example.mp_termproject.SinglePostActivity; // 새로 만들 액티비티 import
import com.example.mp_termproject.model.Post;

import java.util.List;

public class PostGridAdapter extends RecyclerView.Adapter<PostGridAdapter.PostViewHolder> {

    private final Context context;
    private final List<Post> posts;

    public PostGridAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post_grid, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        Glide.with(context).load(post.getImageUrl()).into(holder.imageView);

        // 이미지 클릭 시 SinglePostActivity를 열도록 리스너 설정
        holder.imageView.setOnClickListener(v -> {
            Intent intent = new Intent(context, SinglePostActivity.class);
            // PostDetailActivity에서 사용하던 키를 재사용하거나 새 키 정의
            intent.putExtra(PostDetailActivity.POST_DATA, post);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_grid_post_image);
        }
    }
}