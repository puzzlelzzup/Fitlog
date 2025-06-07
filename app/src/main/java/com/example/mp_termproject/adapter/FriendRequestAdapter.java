package com.example.mp_termproject.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mp_termproject.FriendRequestInfo;
import com.example.mp_termproject.R;

import java.util.ArrayList;
import java.util.List;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.ViewHolder> {

    private List<FriendRequestInfo> requestInfoList;
    private OnRequestActionListener listener;

    public interface OnRequestActionListener {
        void onAccept(FriendRequestInfo requestInfo);
        void onDecline(FriendRequestInfo requestInfo);
    }

    public FriendRequestAdapter(OnRequestActionListener listener) {
        this.requestInfoList = new ArrayList<>();
        this.listener = listener;
    }

    public void setRequests(List<FriendRequestInfo> requests) {
        this.requestInfoList = requests;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FriendRequestInfo requestInfo = requestInfoList.get(position);
        String senderName = requestInfo.getSender().getName();
        holder.requesterName.setText(senderName + "님이 친구 요청을 보냈습니다.");

        holder.acceptButton.setOnClickListener(v -> listener.onAccept(requestInfo));
        holder.declineButton.setOnClickListener(v -> listener.onDecline(requestInfo));
    }

    @Override
    public int getItemCount() {
        return requestInfoList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView requesterName;
        Button acceptButton, declineButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            requesterName = itemView.findViewById(R.id.tv_requester_name);
            acceptButton = itemView.findViewById(R.id.btn_accept);
            declineButton = itemView.findViewById(R.id.btn_decline);
        }
    }
}