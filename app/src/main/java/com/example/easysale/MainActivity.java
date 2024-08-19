package com.example.easysale;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
                    // Empty the searchbar after editing/adding a user
                    binding.searchEditText.setText("");
                    // Clear focus from the search EditText
                    binding.searchEditText.clearFocus();
                    // Hide the keyboard if it's open
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(binding.searchEditText.getWindowToken(), 0);
                    }
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
        setupSearchBar();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle("EasySale");
        }
    }

    private void setupSearchBar() {
        // Initially hide the clear icon
        updateClearIconVisibility(binding.searchEditText.getText());

        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateClearIconVisibility(s);
                if (s.length() > 0) {
                    userViewModel.searchUsers(s.toString());
                } else {
                    userViewModel.loadAllUsers();
                }
            }
        });

        binding.searchEditText.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if(event.getAction() == MotionEvent.ACTION_UP) {
                if(binding.searchEditText.getCompoundDrawables()[DRAWABLE_RIGHT] != null &&
                        event.getRawX() >= (binding.searchEditText.getRight() - binding.searchEditText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    binding.searchEditText.setText("");
                    return true;
                }
            }
            return false;
        });
    }

    private void updateClearIconVisibility(CharSequence s) {
        if (s.length() > 0) {
            binding.searchEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.ic_delete, 0);
        } else {
            binding.searchEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
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
            params.setMargins(dpToPx(12), 0, dpToPx(12), 0);
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
        binding.paginationScrollView.postDelayed(() ->
                binding.paginationScrollView.smoothScrollTo(finalTargetScrollX, 0), 100);
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