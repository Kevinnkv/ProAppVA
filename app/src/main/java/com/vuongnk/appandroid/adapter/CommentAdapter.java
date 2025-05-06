package com.vuongnk.appandroid.adapter;


import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vuongnk.appandroid.R;
import com.vuongnk.appandroid.model.Comment;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private List<Comment> parentComments;
    private Map<String, List<Comment>> repliesMap;
    private Context context;
    private String currentUserId;

    public CommentAdapter(Context context, String currentUserId) {
        this.context = context;
        this.currentUserId = currentUserId;
        this.parentComments = new ArrayList<>();
        this.repliesMap = new HashMap<>();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = parentComments.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return parentComments.size();
    }

    public void setComments(List<Comment> allComments) {
        parentComments.clear();
        repliesMap.clear();

        // Phân loại comments thành parents và replies
        for (Comment comment : allComments) {
            if (comment.getParentId() == null || comment.getParentId().isEmpty()) {
                parentComments.add(comment);
            } else {
                List<Comment> replies = repliesMap.getOrDefault(comment.getParentId(), new ArrayList<>());
                replies.add(comment);
                repliesMap.put(comment.getParentId(), replies);
            }
        }

        // Sắp xếp theo thời gian mới nhất
        Collections.sort(parentComments, (c1, c2) ->
                Long.compare(c2.getCreatedAt(), c1.getCreatedAt()));

        notifyDataSetChanged();
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgUser;
        private TextView tvUserName, tvContent, tvTime;
        private ImageButton btnLike;
        private TextView tvLikeCount;
        private LinearLayout repliesContainer;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            // Khởi tạo các view
            imgUser = itemView.findViewById(R.id.img_user);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvTime = itemView.findViewById(R.id.tv_time);
            btnLike = itemView.findViewById(R.id.btn_like);
            tvLikeCount = itemView.findViewById(R.id.tv_like_count);
            repliesContainer = itemView.findViewById(R.id.replies_container);
        }

        void bind(Comment comment) {
            // Kiểm tra null trước khi sử dụng
            if (imgUser != null && tvUserName != null && tvContent != null &&
                    tvTime != null && btnLike != null && tvLikeCount != null) {

                // Hiển thị thông tin comment chính
                if (comment.getUserPhotoUrl() != null && !comment.getUserPhotoUrl().isEmpty()) {
                    Glide.with(context)
                            .load(comment.getUserPhotoUrl())
                            .placeholder(R.drawable.baseline_person_24)
                            .error(R.drawable.baseline_person_24)
                            .circleCrop()
                            .into(imgUser);
                } else {
                    imgUser.setImageResource(R.drawable.baseline_person_24);
                }

                tvUserName.setText(comment.getUserDisplayName());
                tvContent.setText(comment.getContent());
                tvTime.setText(formatTime(comment.getCreatedAt()));

                // Hiển thị số lượt thích và trạng thái like
                Map<String, Boolean> likes = comment.getLikes();
                int likeCount = (likes != null) ? likes.size() : 0;
                tvLikeCount.setText(String.valueOf(likeCount));

                boolean isLiked = likes != null && likes.containsKey(currentUserId);
                btnLike.setImageResource(isLiked ? R.drawable.ic_like_filled : R.drawable.ic_like_outline);
                btnLike.setOnClickListener(v -> toggleLike(comment));

                // Hiển thị replies
                if (repliesContainer != null) {
                    repliesContainer.removeAllViews();
                    List<Comment> replies = repliesMap.get(comment.getId());
                    if (replies != null && !replies.isEmpty()) {
                        for (Comment reply : replies) {
                            addReplyView(reply);
                        }
                    }
                }
            }
        }

        private void addReplyView(Comment reply) {
            View replyView = LayoutInflater.from(context)
                    .inflate(R.layout.item_reply, repliesContainer, false);

            // Kiểm tra null cho các view trong reply
            ImageView imgReplyUser = replyView.findViewById(R.id.img_user);
            TextView tvReplyUserName = replyView.findViewById(R.id.tv_user_name);
            TextView tvReplyContent = replyView.findViewById(R.id.tv_content);
            TextView tvReplyTime = replyView.findViewById(R.id.tv_time);
            ImageButton btnReplyLike = replyView.findViewById(R.id.btn_like);
            TextView tvReplyLikeCount = replyView.findViewById(R.id.tv_like_count);

            if (imgReplyUser != null && tvReplyUserName != null && tvReplyContent != null &&
                    tvReplyTime != null && btnReplyLike != null && tvReplyLikeCount != null) {

                // Hiển thị thông tin reply
                if (reply.getUserPhotoUrl() != null && !reply.getUserPhotoUrl().isEmpty()) {
                    Glide.with(context)
                            .load(reply.getUserPhotoUrl())
                            .placeholder(R.drawable.baseline_person_24)
                            .error(R.drawable.baseline_person_24)
                            .circleCrop()
                            .into(imgReplyUser);
                } else {
                    imgReplyUser.setImageResource(R.drawable.baseline_person_24);
                }

                tvReplyUserName.setText(reply.getUserDisplayName());
                tvReplyContent.setText(reply.getContent());
                tvReplyTime.setText(formatTime(reply.getCreatedAt()));

                // Hiển thị số lượt thích và trạng thái like của reply
                Map<String, Boolean> likes = reply.getLikes();
                int likeCount = (likes != null) ? likes.size() : 0;
                tvReplyLikeCount.setText(String.valueOf(likeCount));

                boolean isLiked = likes != null && likes.containsKey(currentUserId);
                btnReplyLike.setImageResource(isLiked ? R.drawable.ic_like_filled : R.drawable.ic_like_outline);
                btnReplyLike.setOnClickListener(v -> toggleLike(reply));

                repliesContainer.addView(replyView);
            }
        }

        private void toggleLike(Comment comment) {
            DatabaseReference commentRef = FirebaseDatabase.getInstance()
                    .getReference("comments")
                    .child(comment.getId())
                    .child("likes")
                    .child(currentUserId);

            commentRef.get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    // Unlike
                    commentRef.removeValue();
                } else {
                    // Like
                    commentRef.setValue(true);
                }
            });
        }

        private String formatTime(long timestamp) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }
    }
}