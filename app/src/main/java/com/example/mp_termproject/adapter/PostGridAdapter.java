package com.example.mp_termproject.adapter;

import androidx.recyclerview.widget.RecyclerView;

import com.example.mp_termproject.R;
import com.example.mp_termproject.model.Post;
import com.example.mp_termproject.PostDetailActivity;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import com.bumptech.glide.Glide;


public class PostGridAdapter extends RecyclerView.Adapter<PostGridAdapter.PostViewHolder> {
    private List<Post> posts;
    private Context context;

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

        // 클릭 이벤트 처리 (상세 페이지로 이동 등)
        holder.imageView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PostDetailActivity.class);
            intent.putExtra("imageUrl", post.getImageUrl());
            intent.putExtra("writer", post.getWriter());
            intent.putExtra("content", post.getContent());
            intent.putExtra("tags", post.getTags());
            intent.putExtra("category", post.getCategory());
            intent.putExtra("timestamp", post.getTimestamp() != null ? post.getTimestamp().toDate().toString() : "");
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

