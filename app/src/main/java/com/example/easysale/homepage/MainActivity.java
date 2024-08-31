package com.example.easysale.homepage;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
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
    private RecyclerGestureHandler gestureHandler;
    private ClickDebounce<Void> fabClickDebounce;
    private ClickDebounce<User> itemClickDebounce;
    private ClickDebounce<User> deleteClickDebounce;
    private int currentPage = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = MainActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupComponents();
    }

    private void setupComponents() {
        setupRecyclerView();
        setupGestureHandler();
        setupViewModel();
        setupToolbar();
        setupFab();
        setupItemClick();
        searchBarManager = new SearchBarManager(this, binding, userViewModel);
        paginationManager = new PaginationManager(this, binding, userViewModel);
        Log.d(TAG, "setupComponents: All components set up successfully");
    }

    private void setupRecyclerView() {
        userAdapter = new UserAdapter(new ArrayList<>(), this, this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(userAdapter);
        binding.recyclerView.setNestedScrollingEnabled(false);
    }

    private void setupGestureHandler() {
        if (gestureHandler != null) {
            binding.recyclerView.removeOnItemTouchListener(gestureHandler);
        }

        gestureHandler = new RecyclerGestureHandler(
                new RecyclerGestureHandler.OnSwipeListener() {
                    @Override
                    public void onSwipeLeft() {
                        Log.d(TAG, "onSwipeLeft: Swiped left, going to next page");
                        goToNextPageWithAnimation();
                    }

                    @Override
                    public void onSwipeRight() {
                        Log.d(TAG, "onSwipeRight: Swiped right, going to previous page");
                        goToPreviousPageWithAnimation();
                    }
                },
                (view, position) -> {
                    User user = userAdapter.getUsers().get(position);
                    Log.d(TAG, "onItemClick: Clicked on user: " + user.getFirstName() + " " + user.getLastName());
                    itemClickDebounce.onClick(user);
                },
                position -> {
                    User user = userAdapter.getUsers().get(position);
                    Log.d(TAG, "onDeleteClick: Delete clicked for user: " + user.getFirstName() + " " + user.getLastName());
                    deleteClickDebounce.onClick(user);
                }
        );

        binding.recyclerView.addOnItemTouchListener(gestureHandler);
    }

    private void goToNextPageWithAnimation() {
        if (currentPage < userViewModel.getTotalPages().getValue()) {
            currentPage++;
            Log.d(TAG, "goToNextPageWithAnimation: Moving to page " + currentPage);
            animatePageTransition(true);
            paginationManager.goToNextPage();
        } else {
            Log.d(TAG, "goToNextPageWithAnimation: Already at last page, cannot go forward");
        }
    }

    private void goToPreviousPageWithAnimation() {
        if (currentPage > 1) {
            currentPage--;
            Log.d(TAG, "goToPreviousPageWithAnimation: Moving to page " + currentPage);
            animatePageTransition(false);
            paginationManager.goToPreviousPage();
        } else {
            Log.d(TAG, "goToPreviousPageWithAnimation: Already at first page, cannot go back");
        }
    }

    private void animatePageTransition(boolean goingForward) {
        int animationResource = goingForward ? R.anim.slide_left : R.anim.slide_right;
        Animation animation = AnimationUtils.loadAnimation(this, animationResource);
        binding.recyclerView.startAnimation(animation);
    }

    private void setupViewModel() {
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        userViewModel.getUsers().observe(this, users -> {
            if (users != null) {
                Log.d(TAG, "setupViewModel: Received updated user list. Size: " + users.size());
                userAdapter.setUsers(users);
                setupGestureHandler(); // Reset gesture handler when user list updates
            } else {
                Log.e(TAG, "setupViewModel: Received null user list");
            }
        });
        userViewModel.getTotalUsers().observe(this, this::updateUserCount);
        userViewModel.loadAllUsers();
    }

    @SuppressLint("SetTextI18n")
    private void updateUserCount(int count) {
        Log.d(TAG, "updateUserCount: Updating user count to " + count);
        binding.userCountTextView.setText("Found " + count + " users");
    }

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
            Log.d(TAG, "setupToolbar: Toolbar setup complete");
        } else {
            Log.e(TAG, "setupToolbar: SupportActionBar is null");
        }
    }

    private void showDeleteConfirmationDialog(User user) {
        DeleteDialog dialog = new DeleteDialog(this, user, user1 -> {
            Log.d(TAG, "showDeleteConfirmationDialog: User confirmed deletion. Deleting user: " + user1.getFirstName() + " " + user1.getLastName());
            userViewModel.deleteUserAndReload(user1);
        });
        dialog.show();
    }

    @Override
    public void onDeleteClick(User user) {
        deleteClickDebounce.onClick(user);
    }

    private void setupFab() {
        fabClickDebounce = ClickDebounce.wrap(param -> {
            Log.d(TAG, "setupFab: FAB clicked");
            KeyboardUtils.hideKeyboard(this);
            Intent intent = new Intent(MainActivity.this, EditUserActivity.class);
            intent.putExtra(EditUserActivity.EXTRA_STATE, EditUserActivity.STATE_ADD);
            editUserLauncher.launch(intent);
        });
        binding.addImageView.setOnClickListener(v -> fabClickDebounce.onClick(null));
    }

    private void setupItemClick() {
        itemClickDebounce = ClickDebounce.wrap(this::handleItemClick);
        deleteClickDebounce = ClickDebounce.wrap(this::showDeleteConfirmationDialog);
    }

    @Override
    public void onItemClick(User user) {
        Log.d(TAG, "onItemClick: Item clicked for user: " + user.getFirstName() + " " + user.getLastName());
        itemClickDebounce.onClick(user);
    }

    private void handleItemClick(User user) {
        Log.d(TAG, "handleItemClick: Handling click for user: " + user.getFirstName() + " " + user.getLastName());
        KeyboardUtils.hideKeyboard(this);
        Intent intent = new Intent(this, EditUserActivity.class);
        intent.putExtra(EditUserActivity.EXTRA_STATE, EditUserActivity.STATE_EDIT);
        intent.putExtra("USER", user);
        editUserLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> editUserLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Log.d(TAG, "editUserLauncher: result was " + (result.getResultCode() == RESULT_OK ? "successful" : "unsuccessful"));
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null && data.hasExtra("UPDATED_USER")) {
                        User updatedUser = (User) data.getSerializableExtra("UPDATED_USER");
                        Log.d(TAG, "editUserLauncher: Received updated user - " + updatedUser.getFirstName() + " " + updatedUser.getLastName());
                        userViewModel.updateLocalUser(updatedUser);
                    } else {
                        Log.d(TAG, "editUserLauncher: No updated user data received. Reloading all users.");
                        userViewModel.loadAllUsers();
                    }
                    searchBarManager.clearSearchBar();
                } else {
                    Log.d(TAG, "editUserLauncher: EditUserActivity did not return RESULT_OK");
                }
            });

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: MainActivity is being destroyed");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (binding.recyclerView != null) {
            setupGestureHandler();
        }
        Log.d(TAG, "onResume: Gesture handler reset");
    }
}