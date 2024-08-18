package com.example.easysale;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.easysale.databinding.MainActivityBinding;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements
        UserAdapter.OnDeleteClickListener, UserAdapter.OnItemClickListener {
    private static final String TAG = "MainActivity";
    private UserViewModel userViewModel;
    private UserAdapter userAdapter;
    private MainActivityBinding binding;

    private final ActivityResultLauncher<Intent> editUserLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    userViewModel.loadUsers(1);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = MainActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupRecyclerView();
        setupViewModel();
        setupFab();
        setupToolbar();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle("EasySale");
        }
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
        userViewModel.getTotalPages().observe(this, this::updatePagination);
        userViewModel.loadUsers(1);
    }

    private void updatePagination(int totalPages) {
        binding.paginationLayout.removeAllViews();
        for (int i = 1; i <= totalPages; i++) {
            Button pageButton = new Button(this);
            pageButton.setText(String.valueOf(i));
            pageButton.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            pageButton.setBackgroundResource(R.drawable.pagination_button);

            final int page = i;
            pageButton.setOnClickListener(v -> userViewModel.loadUsers(page));

            if (i == userViewModel.getCurrentPage()) {
                pageButton.setEnabled(false);
                pageButton.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
            }

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dpToPx(40), // Width
                    dpToPx(40)  // Height
            );
            params.setMargins(dpToPx(4), 0, dpToPx(4), 0);
            pageButton.setLayoutParams(params);

            // Center the text in the button
            pageButton.setGravity(Gravity.CENTER);

            // Set text size
            pageButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

            binding.paginationLayout.addView(pageButton);
        }
    }

    // Helper method to convert dp to pixels
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    private void setupFab() {
        binding.addImageView.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EditUserActivity.class);
            intent.putExtra(EditUserActivity.EXTRA_STATE, EditUserActivity.STATE_ADD);
            editUserLauncher.launch(intent);
        });
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
        intent.putExtra(EditUserActivity.EXTRA_STATE, EditUserActivity.STATE_EDIT);
        intent.putExtra("USER", user);
        editUserLauncher.launch(intent);
    }
}