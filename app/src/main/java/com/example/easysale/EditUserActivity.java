package com.example.easysale;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.example.easysale.databinding.ActivityEditUserBinding;
import com.example.easysale.model.User;
import com.example.easysale.utils.ClickDebounce;
import com.example.easysale.utils.KeyboardUtils;
import com.example.easysale.viewmodel.UserViewModel;

public class EditUserActivity extends AppCompatActivity {
    private static final String TAG = "EditUserActivity";
    private ActivityEditUserBinding binding;
    private ClickDebounce saveButtonDebounce;
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
                Log.d(TAG, "onCreate: Editing user - " + (currentUser != null ? currentUser.toString() : "null"));
            } else if (STATE_ADD.equals(state)) {
                currentUser = new User();
                currentUser.setAvatar(DEFAULT_AVATAR);
                Log.d(TAG, "onCreate: Adding new user");
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
        Log.d(TAG, "loadUserData: State = " + state + ", CurrentUser = " + (currentUser != null ? currentUser.toString() : "null"));
        if (STATE_EDIT.equals(state) && currentUser != null) {
            binding.editTextFirstName.setText(currentUser.getFirstName());
            binding.editTextLastName.setText(currentUser.getLastName());
            binding.editTextEmail.setText(currentUser.getEmail());
            loadAvatarImage(currentUser.getAvatar());
            Log.d(TAG, "loadUserData: Loaded user data for editing - " + currentUser.toString());
        } else if (STATE_ADD.equals(state)) {
            binding.editTextFirstName.setText("");
            binding.editTextLastName.setText("");
            binding.editTextEmail.setText("");
            loadAvatarImage(DEFAULT_AVATAR);
            Log.d(TAG, "loadUserData: Prepared for adding new user");
        } else {
            showError("Invalid state or user data not found!");
            Log.e(TAG, "loadUserData: Invalid state or user data not found!");
        }
    }

    private void loadAvatarImage(String avatarUri) {
        Glide.with(this)
                .load(avatarUri)
                .circleCrop()
                .into(binding.imageViewAvatar);
    }

    private void setupSaveButton() {
        saveButtonDebounce = ClickDebounce.wrap(param -> saveUser());
        binding.buttonSave.setOnClickListener(v -> saveButtonDebounce.onClick(null));
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
        binding.imageViewAvatar.setOnClickListener(v -> handleAvatarClick());
    }

    private void handleAvatarClick() {
        if (currentUser.getAvatar().equals(DEFAULT_AVATAR)) {
            openGallery();
        } else {
            showAvatarOptionsDialog();
        }
    }

    private void showAvatarOptionsDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.avatar_dialog);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        View closeIcon = dialog.findViewById(R.id.close_icon);
        View deletePictureButton = dialog.findViewById(R.id.delete_picture_button);
        View uploadPictureButton = dialog.findViewById(R.id.upload_picture_button);

        closeIcon.setOnClickListener(v -> dialog.dismiss());

        deletePictureButton.setOnClickListener(v -> {
            currentUser.setAvatar(DEFAULT_AVATAR);
            loadAvatarImage(DEFAULT_AVATAR);
            dialog.dismiss();
        });

        uploadPictureButton.setOnClickListener(v -> {
            openGallery();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private String cleanName(String name) {
        // Remove leading and trailing spaces, then replace multiple spaces with a single space
        return name.trim().replaceAll("\\s+", " ");
    }

    private void validateInputs(String firstName, String lastName, String email, ValidationCallback callback) {
        boolean isValid = validateName(firstName, binding.editTextFirstName, "First name") &&
                validateName(lastName, binding.editTextLastName, "Last name") &&
                validateEmail(email);

        if (isValid) {
            int currentUserId = (currentUser != null) ? currentUser.getId() : -1;
            userViewModel.isEmailUnique(email, currentUserId, isUnique -> {
                runOnUiThread(() -> {
                    if (!isUnique) {
                        binding.editTextEmail.setError("Email is already taken");
                        callback.onValidationComplete(false);
                    } else {
                        binding.editTextEmail.setError(null);
                        callback.onValidationComplete(true);
                    }
                });
            });
        } else {
            callback.onValidationComplete(false);
        }
    }

    private boolean validateName(String name, android.widget.EditText editText, String fieldName) {
        if (name.isEmpty()) {
            editText.setError(fieldName + " is required");
            return false;
        } else if (name.length() < 2) {
            editText.setError(fieldName + " must be at least 2 characters long");
            return false;
        } else if (name.length() > 35) {
            editText.setError(fieldName + " must not exceed 35 characters");
            return false;
        } else {
            editText.setError(null);
            editText.setText(name);
            return true;
        }
    }

    private boolean validateEmail(String email) {
        if (email.isEmpty()) {
            binding.editTextEmail.setError("Email is required");
            return false;
        } else if (!isValidEmail(email)) {
            binding.editTextEmail.setError("Invalid email format");
            return false;
        } else {
            binding.editTextEmail.setError(null);
            return true;
        }
    }

    private boolean isValidEmail(String email) {
        String emailPattern = "(?i)^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$";
        return email.matches(emailPattern);
    }

    private void saveUser() {
        String firstName = cleanName(binding.editTextFirstName.getText().toString());
        String lastName = cleanName(binding.editTextLastName.getText().toString());
        String email = binding.editTextEmail.getText().toString().trim();

        validateInputs(firstName, lastName, email, isValid -> {
            if (isValid) {
                KeyboardUtils.hideKeyboard(this);

                currentUser.setFirstName(firstName);
                currentUser.setLastName(lastName);
                currentUser.setEmail(email);

                binding.buttonSave.setEnabled(false);

                if (STATE_EDIT.equals(state)) {
                    updateUser();
                } else if (STATE_ADD.equals(state)) {
                    createUser();
                }
            } else {
                binding.buttonSave.setEnabled(true);
            }
        });
    }


    private void updateUser() {
        Log.d(TAG, "updateUser: Updating user - " + currentUser.toString());
        userViewModel.updateUser(currentUser, new UserViewModel.OnUserUpdateListener() {
            @Override
            public void onUserUpdated() {
                runOnUiThread(() -> {
                    Log.d(TAG, "onUserUpdated: User updated successfully - " + currentUser.toString());
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
                    Log.e(TAG, "onError: Update failed - " + error);
                    showError("Update failed: " + error);
                    binding.buttonSave.setEnabled(true);
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
                    binding.buttonSave.setEnabled(true);
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
            KeyboardUtils.hideKeyboard(this);
            // Delay the back navigation slightly to ensure keyboard is hidden
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                getOnBackPressedDispatcher().onBackPressed();
            }, 300);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private interface ValidationCallback {
        void onValidationComplete(boolean isValid);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Activity is being destroyed");
    }
}