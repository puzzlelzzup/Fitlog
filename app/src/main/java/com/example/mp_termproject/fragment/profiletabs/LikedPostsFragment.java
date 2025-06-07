package com.example.mp_termproject.fragment.profiletabs;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.mp_termproject.R;

public class LikedPostsFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        TextView textView = new TextView(getContext());
        textView.setText("좋아요 한 게시물 (구현 예정)");
        textView.setGravity(android.view.Gravity.CENTER);
        return textView;
        // return inflater.inflate(R.layout.fragment_liked_posts, container, false); // 별도 레이아웃 파일 사용 시
    }
}