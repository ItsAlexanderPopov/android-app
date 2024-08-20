package com.example.easysale;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.example.easysale.databinding.ActivityEditUserBinding;
import com.example.easysale.model.User;
import com.example.easysale.utils.KeyboardUtils;
import com.example.easysale.viewmodel.UserViewModel;

public class EditUserActivity extends AppCompatActivity {
    private static final String TAG = "EditUserActivity";
    private ActivityEditUserBinding binding;
    private UserViewModel userViewModel;
    private User currentUser;
    private String state;
    public static final String EXTRA_STATE = "EXTRA_STATE";
    public static final String STATE_EDIT = "Edit";
    public static final String STATE_ADD = "Add";
    private static final String DEFAULT_AVATAR = "android.resource://com.example.easysale/drawable/placeholder";

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        currentUser.setAvatar(selectedImageUri.toString());
                        loadAvatarImage(selectedImageUri.toString());
                    }
                }
            }
    );

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
                currentUser = new User();
                currentUser.setAvatar(DEFAULT_AVATAR);
            }
        }

        setupToolbar();
        setupViewModel();
        loadUserData();
        setupSaveButton();
        setupBackNavigation();
        setupAvatarClick();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(state + " User");
            binding.toolbar.setNavigationContentDescription("Go Back");
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
            loadAvatarImage(currentUser.getAvatar());
        } else if (STATE_ADD.equals(state)) {
            binding.editTextFirstName.setText("");
            binding.editTextLastName.setText("");
            binding.editTextEmail.setText("");
            loadAvatarImage(DEFAULT_AVATAR);
        } else {
            showError("Invalid state or user data not found!");
        }
    }

    private void loadAvatarImage(String avatarUri) {
        Glide.with(this)
                .load(avatarUri)
                .circleCrop()
                .into(binding.imageViewAvatar);
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

    private void setupAvatarClick() {
        binding.imageViewAvatar.setOnClickListener(v -> openGallery());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private void saveUser() {
        // Cleans strings from common mistakes and then validates
        String firstName = cleanName(binding.editTextFirstName.getText().toString());
        String lastName = cleanName(binding.editTextLastName.getText().toString());
        String email = binding.editTextEmail.getText().toString().trim();
        if (!validateInputs(firstName, lastName, email)) {
            return;
        }

        // Hide the keyboard
        KeyboardUtils.hideKeyboard(this);

        currentUser.setFirstName(firstName);
        currentUser.setLastName(lastName);
        currentUser.setEmail(email);

        if (STATE_EDIT.equals(state)) {
            updateUser();
        } else if (STATE_ADD.equals(state)) {
            createUser();
        }
    }

    private String cleanName(String name) {
        // Remove leading and trailing spaces, then replace multiple spaces with a single space
        return name.trim().replaceAll("\\s+", " ");
    }

    private boolean validateInputs(String firstName, String lastName, String email) {
        boolean isValid = true;
        // Disclaimer: after research, people can have special characters and numbers in their name.
        if (firstName.isEmpty()) {
            binding.editTextFirstName.setError("First name is required");
            isValid = false;
        } else if (firstName.length() < 2) {
            binding.editTextFirstName.setError("First name must be at least 2 characters long");
            isValid = false;
        } else if (firstName.length() > 35) {
            binding.editTextFirstName.setError("First name must not exceed 35 characters");
            isValid = false;
        } else {
            binding.editTextFirstName.setError(null);
            binding.editTextFirstName.setText(firstName);
        }

        if (lastName.isEmpty()) {
            binding.editTextLastName.setError("Last name is required");
            isValid = false;
        } else if (lastName.length() < 2) {
            binding.editTextLastName.setError("Last name must be at least 2 characters long");
            isValid = false;
        } else if (lastName.length() > 35) {
            binding.editTextLastName.setError("Last name must not exceed 35 characters");
            isValid = false;
        } else {
            binding.editTextLastName.setError(null);
            binding.editTextLastName.setText(lastName);
        }

        if (email.isEmpty()) {
            binding.editTextEmail.setError("Email is required");
            isValid = false;
        } else if (!isValidEmail(email)) {
            binding.editTextEmail.setError("Invalid email format");
            isValid = false;
        } else {
            binding.editTextEmail.setError(null);
        }

        return isValid;
    }

    private boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return email.matches(emailPattern);
    }

    private void updateUser() {
        Log.d(TAG, "Updating user: " + currentUser.toString());
        userViewModel.updateUser(currentUser, new UserViewModel.OnUserUpdateListener() {
            @Override
            public void onUserUpdated() {
                runOnUiThread(() -> {
                    Log.d(TAG, "User updated successfully. Sending result back.");
                    Toast.makeText(EditUserActivity.this, "User updated successfully", Toast.LENGTH_SHORT).show();
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("UPDATED_USER", currentUser);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Update error: " + error);
                    showError("Update failed: " + error);
                });
            }
        });
    }

    private void createUser() {
        userViewModel.createUser(currentUser, new UserViewModel.OnUserCreateListener() {
            @Override
            public void onUserCreated() {
                runOnUiThread(() -> {
                    Toast.makeText(EditUserActivity.this, "User created successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showError("Creation failed: " + error);
                    Log.e("EditUserActivity", "Creation error: " + error);
                });
            }
        });
    }


    private void showError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Hide the keyboard when the back button is pressed
            KeyboardUtils.hideKeyboard(this);  // Delay the back navigation slightly to ensure keyboard is hidden
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                getOnBackPressedDispatcher().onBackPressed();
            }, 300);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}