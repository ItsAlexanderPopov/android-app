package com.example.easysale;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
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
                    userViewModel.loadAllUsers();
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
            } else {
                Log.e(TAG, "Received null user list");
            }
        });
        userViewModel.getTotalUsers().observe(this, this::updateUserCount);
        userViewModel.getTotalPages().observe(this, this::updatePagination);
        userViewModel.getCurrentPage().observe(this, this::updatePaginationButtonStates);
        userViewModel.loadAllUsers();
    }

    // Helper method to convert dp to pixels
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    private void updatePagination(int totalPages) {
        binding.paginationLayout.removeAllViews();
        for (int i = 1; i <= totalPages; i++) {
            Button pageButton = new Button(this);
            pageButton.setText(String.valueOf(i));
            pageButton.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            pageButton.setBackgroundResource(R.drawable.pagination_button);

            final int page = i;
            pageButton.setOnClickListener(v -> {
                userViewModel.loadPage(page);
                Log.d(TAG, "Page button clicked: " + page);
            });

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dpToPx(44), // Width
                    dpToPx(44)  // Height
            );
            params.setMargins(dpToPx(8), 0, dpToPx(8), 0);
            pageButton.setLayoutParams(params);

            pageButton.setGravity(Gravity.CENTER);
            pageButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);

            binding.paginationLayout.addView(pageButton);
        }
    }

    // Controls pagination buttons colors and scroll movement based on current page
    private void updatePaginationButtonStates(int currentPage) {
        Log.d(TAG, "Updating pagination button states. Current page: " + currentPage);
        int totalWidth = 0;
        int targetScrollX = 0;
        int buttonWidth = dpToPx(44 + 16); // button width + margins

        for (int i = 0; i < binding.paginationLayout.getChildCount(); i++) {
            View view = binding.paginationLayout.getChildAt(i);
            if (view instanceof Button) {
                Button pageButton = (Button) view;
                int page = Integer.parseInt(pageButton.getText().toString());
                if (page == currentPage) {
                    pageButton.setEnabled(false);
                    pageButton.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
                    targetScrollX = totalWidth - (binding.paginationScrollView.getWidth() - buttonWidth) / 2;
                } else {
                    pageButton.setEnabled(true);
                    pageButton.setTextColor(ContextCompat.getColor(this, android.R.color.black));
                }
                totalWidth += buttonWidth;
            }
        }

        // Ensure targetScrollX is within bounds
        final int finalTargetScrollX = Math.max(0, Math.min(targetScrollX, binding.paginationLayout.getWidth() - binding.paginationScrollView.getWidth()));

        // Smooth scroll to the target position
        binding.paginationScrollView.post(() -> binding.paginationScrollView.smoothScrollTo(finalTargetScrollX, 0));
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