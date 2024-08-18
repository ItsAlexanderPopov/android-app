package com.example.easysale;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.easysale.databinding.MainActivityBinding;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements UserAdapter.OnDeleteClickListener, UserAdapter.OnItemClickListener {
    private static final String TAG = "MainActivity";
    private UserViewModel userViewModel;
    private UserAdapter userAdapter;
    private MainActivityBinding binding;

    private final ActivityResultLauncher<Intent> editUserLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    userViewModel.loadUsers();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = MainActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupRecyclerView();
        setupViewModel();
    }

    private void setupRecyclerView() {
        userAdapter = new UserAdapter(new ArrayList<>(), this, this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(userAdapter);
    }

    private void setupViewModel() {
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        userViewModel.getUsers().observe(this, users -> {
            if (users != null) {
                userAdapter.setUsers(users);
                updateUserCount(users.size());
            } else {
                Log.e(TAG, "Received null user list");
            }
        });
        userViewModel.loadUsers();
    }

    private void updateUserCount(int count) {
        binding.userCountTextView.setText("Found " + count + " users");
    }

    @Override
    public void onDeleteClick(User user) {
        userViewModel.deleteUser(user);
    }

    @Override
    public void onItemClick(User user) {
        Intent intent = new Intent(this, EditUserActivity.class);
        intent.putExtra("USER", user);
        editUserLauncher.launch(intent);
    }
}