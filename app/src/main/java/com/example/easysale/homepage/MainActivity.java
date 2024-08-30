package com.example.easysale.homepage;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.easysale.userpage.EditUserActivity;
import com.example.easysale.R;
import com.example.easysale.adapter.UserAdapter;
import com.example.easysale.databinding.MainActivityBinding;
import com.example.easysale.model.User;
import com.example.easysale.utils.ClickDebounce;
import com.example.easysale.utils.KeyboardUtils;
import com.example.easysale.viewmodel.UserViewModel;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements
        UserAdapter.OnDeleteClickListener, UserAdapter.OnItemClickListener {
    private static final String TAG = "MainActivity";
    private UserViewModel userViewModel;
    private UserAdapter userAdapter;
    private MainActivityBinding binding;
    private SearchBarManager searchBarManager;
    private PaginationManager paginationManager;
    private ClickDebounce fabClickDebounce;
    private ClickDebounce itemClickDebounce;
    private int currentPage = 1;
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = MainActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupComponents();
    }

    // Initialize and setup all components
    private void setupComponents() {
        setupRecyclerView();
        setupGestureDetector();
        setupViewModel();
        setupToolbar();
        setupFab();
        setupItemClick();
        searchBarManager = new SearchBarManager(this, binding, userViewModel);
        paginationManager = new PaginationManager(this, binding, userViewModel);
    }

    // Setup RecyclerView with adapter
    @SuppressLint("ClickableViewAccessibility")
    private void setupRecyclerView() {
        userAdapter = new UserAdapter(new ArrayList<>(), this, this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(userAdapter);
    }

    private void goToNextPageWithAnimation() {
        if (currentPage < userViewModel.getTotalPages().getValue()) {
            currentPage++;
            animatePageTransition(true);
            paginationManager.goToNextPage();
        }
    }

    private void goToPreviousPageWithAnimation() {
        if (currentPage > 1) {
            currentPage--;
            animatePageTransition(false);
            paginationManager.goToPreviousPage();
        }
    }

    private void animatePageTransition(boolean goingForward) {
        Log.d(TAG, "animatePageTransition: Going " + (goingForward ? "forward" : "backward"));
        int animationResource = goingForward ? R.anim.slide_left : R.anim.slide_right;
        binding.recyclerView.startAnimation(AnimationUtils.loadAnimation(this, animationResource));
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupGestureDetector() {
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null) {
                    return false; // One of the events is null, so we can't process this fling
                }
                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();
                if (Math.abs(diffX) > Math.abs(diffY) &&
                        Math.abs(diffX) > SWIPE_THRESHOLD &&
                        Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        runOnUiThread(() -> goToPreviousPageWithAnimation());
                    } else {
                        runOnUiThread(() -> goToNextPageWithAnimation());
                    }
                    return true;
                }
                return false;
            }
        });

        binding.recyclerView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return false; // Allow the event to be processed by the RecyclerView as well
        });
    }

    // Initialize ViewModel and observe LiveData
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
        userViewModel.loadAllUsers();
    }

    // Update user count display
    @SuppressLint("SetTextI18n")
    private void updateUserCount(int count) {
        binding.userCountTextView.setText("Found " + count + " users");
    }

    // Setup the toolbar with logo
    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowTitleEnabled(false);

            ImageView logo = new ImageView(this);
            logo.setImageResource(R.drawable.logo);
            Toolbar.LayoutParams params = new Toolbar.LayoutParams(
                    Toolbar.LayoutParams.WRAP_CONTENT,
                    Toolbar.LayoutParams.WRAP_CONTENT
            );
            logo.setLayoutParams(params);

            binding.toolbar.addView(logo);
        }
    }

    // Show delete confirmation dialog
    private void showDeleteConfirmationDialog(User user) {
        DeleteDialog dialog = new DeleteDialog(this, user, user1 -> userViewModel.deleteUserAndReload(user1));
        dialog.show();
    }

    // Handle delete click from adapter
    @Override
    public void onDeleteClick(User user) {
        showDeleteConfirmationDialog(user);
    }

    // Setup FAB for adding new user
    private void setupFab() {
        fabClickDebounce = ClickDebounce.wrap(param -> {
            KeyboardUtils.hideKeyboard(this);
            Intent intent = new Intent(MainActivity.this, EditUserActivity.class);
            intent.putExtra(EditUserActivity.EXTRA_STATE, EditUserActivity.STATE_ADD);
            editUserLauncher.launch(intent);
        });
        binding.addImageView.setOnClickListener(v -> fabClickDebounce.onClick(null));
    }

    private void setupItemClick() {
        itemClickDebounce = ClickDebounce.wrap(this::handleItemClick);
    }

    // Handle item click from adapter
    @Override
    public void onItemClick(User user) {
        itemClickDebounce.onClick(user);
    }

    // Handle the actual item click logic
    private void handleItemClick(User user) {
        Log.d(TAG, "handleItemClick: User selected - " + user.toString());
        KeyboardUtils.hideKeyboard(this);
        Intent intent = new Intent(this, EditUserActivity.class);
        intent.putExtra(EditUserActivity.EXTRA_STATE, EditUserActivity.STATE_EDIT);
        intent.putExtra("USER", user);
        editUserLauncher.launch(intent);
    }

    // ActivityResultLauncher for handling EditUserActivity results
    private final ActivityResultLauncher<Intent> editUserLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Log.d(TAG, "editUserLauncher: Received result from EditUserActivity. Result code: " + result.getResultCode());
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null && data.hasExtra("UPDATED_USER")) {
                        User updatedUser = (User) data.getSerializableExtra("UPDATED_USER");
                        Log.d(TAG, "editUserLauncher: Received updated user - " + updatedUser.toString());
                        userViewModel.updateLocalUser(updatedUser);
                    } else {
                        Log.d(TAG, "editUserLauncher: No updated user data received. Reloading all users.");
                        userViewModel.loadAllUsers();
                    }
                    // Clear the search bar when returning from EditUserActivity
                    searchBarManager.clearSearchBar();
                } else {
                    Log.d(TAG, "editUserLauncher: EditUserActivity did not return RESULT_OK");
                }
            });
}