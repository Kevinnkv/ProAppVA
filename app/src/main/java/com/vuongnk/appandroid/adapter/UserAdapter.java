package com.vuongnk.appandroid.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.vuongnk.appandroid.R;
import com.vuongnk.appandroid.model.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<User> users;
    private OnUserActionListener listener;

    public interface OnUserActionListener {
        void onEditUser(User user);
        void onDeleteUser(User user);
        void onToggleUserStatus(User user);
    }

    public UserAdapter(List<User> users, OnUserActionListener listener) {
        this.users = users;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvName, tvEmail, tvRole, tvStatus, tv_accountBalance;
        ImageButton btnEdit, btnDelete, btnToggleStatus;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.img_avatar);
            tvName = itemView.findViewById(R.id.tv_name);
            tvEmail = itemView.findViewById(R.id.tv_email);
            tvRole = itemView.findViewById(R.id.tv_role);
            tv_accountBalance = itemView.findViewById(R.id.tv_accountBalance);
            tvStatus = itemView.findViewById(R.id.tv_status);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            btnToggleStatus = itemView.findViewById(R.id.btn_toggle_status);
        }

        void bind(User user) {
            tvName.setText(user.getDisplayName());
            tvEmail.setText(user.getEmail());
            tvRole.setText(user.getRole());
            tv_accountBalance.setText(String.valueOf(user.getAccountBalance()));

            tvStatus.setText(user.isAccountActive() ? "Đang hoạt động" : "Đã khóa");
            tvStatus.setTextColor(user.isAccountActive() ? Color.GREEN : Color.RED);

            btnToggleStatus.setImageResource(user.isAccountActive() ? R.drawable.ic_lock : R.drawable.ic_unlock);
            
            if (user.getPhotoURL() != null && !user.getPhotoURL().isEmpty()) {
                Glide.with(itemView.getContext())
                    .load(user.getPhotoURL())
                    .circleCrop()
                    .into(imgAvatar);
            }

            btnEdit.setOnClickListener(v -> listener.onEditUser(user));
            btnDelete.setOnClickListener(v -> listener.onDeleteUser(user));
            btnToggleStatus.setOnClickListener(v -> listener.onToggleUserStatus(user));
        }
    }
} 