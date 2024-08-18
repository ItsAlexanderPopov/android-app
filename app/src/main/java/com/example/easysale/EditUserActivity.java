package com.example.easysale;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

public class EditUserActivity extends AppCompatActivity {
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
                runOnUiThread(() -> {
                    Toast.makeText(EditUserActivity.this, "User updated successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showError("Update failed: " + error);
                    Log.e("EditUserActivity", "Update error: " + error);
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
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}