package com.example.easysale.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;

import com.example.easysale.MainActivity;
import com.example.easysale.databinding.MainActivityBinding;
import com.example.easysale.viewmodel.UserViewModel;

public class SearchBarManager {
    private final MainActivity activity;
    private final MainActivityBinding binding;
    private final UserViewModel userViewModel;

    public SearchBarManager(MainActivity activity, MainActivityBinding binding, UserViewModel userViewModel) {
        this.activity = activity;
        this.binding = binding;
        this.userViewModel = userViewModel;
        setupSearchBar();
    }

    // Setup search bar functionality
    private void setupSearchBar() {
        updateClearIconVisibility(binding.searchEditText.getText());

        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateClearIconVisibility(s);
                if (s.toString().isEmpty()) {
                    userViewModel.loadAllUsers();
                } else {
                    userViewModel.searchUsers(s.toString());
                }
            }
        });

        binding.searchEditText.setOnTouchListener(this::handleSearchBarTouch);
    }

    // Update clear icon visibility based on text content
    private void updateClearIconVisibility(CharSequence s) {
        if (s.length() > 0) {
            binding.searchEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.ic_delete, 0);
        } else {
            binding.searchEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
    }

    // Handle touch events on search bar
    private boolean handleSearchBarTouch(android.view.View v, MotionEvent event) {
        final int DRAWABLE_RIGHT = 2;
        if(event.getAction() == MotionEvent.ACTION_UP) {
            if(binding.searchEditText.getCompoundDrawables()[DRAWABLE_RIGHT] != null &&
                    event.getRawX() >= (binding.searchEditText.getRight() - binding.searchEditText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                clearSearchBar();
                return true;
            }
        }
        return false;
    }

    // Clear the search bar and reload all users
    public void clearSearchBar() {
        binding.searchEditText.setText("");
        userViewModel.loadAllUsers();
    }
}