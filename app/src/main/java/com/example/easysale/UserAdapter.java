package com.example.easysale;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<User> users;

    public UserAdapter(List<User> users) {
        this.users = users;
    }

    @NonNull
    @Override
    // Inflate the layout for each item in the RecyclerView
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        // Get the user at the current position
        User user = users.get(position);

        // Bind user data to the views in the ViewHolder
        holder.nameTextView.setText(user.getFirstName() + " " + user.getLastName());
        holder.emailTextView.setText(user.getEmail());

        // Load the user's avatar image using Glide
        Glide.with(holder.itemView.getContext())
                .load(user.getAvatar())
                .circleCrop() // Crop the image into a circle
                .into(holder.avatarImageView); // Set the image to the ImageView
    }

    @Override
    public int getItemCount() {
        // Return the total number of items in the list
        return users.size();
    }

    public void setUsers(List<User> users) {
        this.users = users;
        // Notify the adapter that the data has changed
        notifyDataSetChanged();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView avatarImageView;
        TextView nameTextView;
        TextView emailTextView;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            // Find views by their IDs
            avatarImageView = itemView.findViewById(R.id.avatarImageView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            emailTextView = itemView.findViewById(R.id.emailTextView);
        }
    }
}