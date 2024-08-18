package com.example.easysale;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.example.easysale.databinding.ActivityEditUserBinding;

public class EditUserActivity extends AppCompatActivity {
    private ActivityEditUserBinding binding;
    private UserViewModel userViewModel;
    private User currentUser;
    private String state;
    public static final String EXTRA_STATE = "EXTRA_STATE";
    public static final String STATE_EDIT = "Edit";
    public static final String STATE_ADD = "Add";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        if (intent != null) {
            state = intent.getStringExtra(EXTRA_STATE);
            if (STATE_EDIT.equals(state)) {
                currentUser = (User) intent.getSerializableExtra("USER");
            } else if (STATE_ADD.equals(state)) {
                currentUser = new User(); // Create a new User object for adding
            }
        }

        setupToolbar();
        setupViewModel();
        loadUserData();
        setupSaveButton();
        setupBackNavigation();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(state + " User");
        }
    }

    private void setupViewModel() {
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
    }

    private void loadUserData() {
        if (STATE_EDIT.equals(state) && currentUser != null) {
            binding.editTextFirstName.setText(currentUser.getFirstName());
            binding.editTextLastName.setText(currentUser.getLastName());
            binding.editTextEmail.setText(currentUser.getEmail());
            // Load avatar image using Glide
            Glide.with(this)
                    .load(currentUser.getAvatar())
                    .circleCrop()
                    .into(binding.imageViewAvatar);
        } else if (STATE_ADD.equals(state)) {
            // Clear fields for adding a new user
            binding.editTextFirstName.setText("");
            binding.editTextLastName.setText("");
            binding.editTextEmail.setText("");
            //binding.imageViewAvatar.setImageResource(R.drawable.default_avatar); // Set a default avatar
        } else {
            // Handle the error case
            showError("Invalid state or user data not found!");
        }
    }

    private void setupSaveButton() {
        binding.buttonSave.setOnClickListener(v -> saveUser());
    }

    private void setupBackNavigation() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    private void saveUser() {
        String firstName = binding.editTextFirstName.getText().toString();
        String lastName = binding.editTextLastName.getText().toString();
        String email = binding.editTextEmail.getText().toString();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
            showError("All fields are required");
            return;
        }

        currentUser.setFirstName(firstName);
        currentUser.setLastName(lastName);
        currentUser.setEmail(email);

        if (STATE_EDIT.equals(state)) {
            updateUser();
        } else if (STATE_ADD.equals(state)) {
            createUser();
        }
    }

    private void updateUser() {
        userViewModel.updateUser(currentUser, new UserViewModel.OnUserUpdateListener() {
            @Override
            public void onUserUpdated() {
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onError(String error) {
                showError(error);
            }
        });
    }

    private void createUser() {
        userViewModel.createUser(currentUser, new UserViewModel.OnUserCreateListener() {
            @Override
            public void onUserCreated() {
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onError(String error) {
                showError(error);
            }
        });
    }

    private void showError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}