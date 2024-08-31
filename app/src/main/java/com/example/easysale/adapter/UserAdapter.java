package com.example.easysale.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.easysale.R;
import com.example.easysale.model.User;

import java.util.List;
import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<User> users;
    private OnDeleteClickListener deleteListener;
    private OnItemClickListener itemClickListener;

    public List<User> getUsers() {
        return users;
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(User user);
    }

    public interface OnItemClickListener {
        void onItemClick(User user);
    }

    public UserAdapter(List<User> users, OnDeleteClickListener deleteListener, OnItemClickListener itemClickListener) {
        this.users = new ArrayList<>(users);
        this.deleteListener = deleteListener;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.nameTextView.setText(user.getFirstName() + " " + user.getLastName());
        holder.emailTextView.setText(user.getEmail());
        holder.deleteImageView.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(user);
            }
        });
        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(user);
            }
        });
        Glide.with(holder.itemView.getContext())
                .load(user.getAvatar())
                .circleCrop()
                .into(holder.avatarImageView);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void setUsers(List<User> users) {
        this.users = new ArrayList<>(users);
        notifyDataSetChanged();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView avatarImageView;
        TextView nameTextView;
        TextView emailTextView;
        ImageView deleteImageView;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarImageView = itemView.findViewById(R.id.avatarImageView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            emailTextView = itemView.findViewById(R.id.emailTextView);
            deleteImageView = itemView.findViewById(R.id.deleteImageView);
        }
    }
}