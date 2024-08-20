package com.example.easysale.utils;

import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;

import com.example.easysale.MainActivity;
import com.example.easysale.R;
import com.example.easysale.databinding.MainActivityBinding;
import com.example.easysale.viewmodel.UserViewModel;

public class PaginationManager {
    private static final String TAG = "PaginationManager";
    private final MainActivity activity;
    private final MainActivityBinding binding;
    private final UserViewModel userViewModel;

    public PaginationManager(MainActivity activity, MainActivityBinding binding, UserViewModel userViewModel) {
        this.activity = activity;
        this.binding = binding;
        this.userViewModel = userViewModel;
        setupPagination();
    }

    // Setup pagination observers and initial state
    private void setupPagination() {
        userViewModel.getTotalPages().observe(activity, this::updatePagination);
        userViewModel.getSelectedPage().observe(activity, this::updatePaginationButtonStates);
        userViewModel.getPaginationUpdated().observe(activity, updated -> {
            if (updated) {
                updatePagination(userViewModel.getTotalPages().getValue());
                updatePaginationButtonStates(userViewModel.getSelectedPage().getValue());
            }
        });
    }

    // Update pagination buttons based on total pages
    private void updatePagination(Integer totalPages) {
        if (totalPages == null) return;
        binding.paginationLayout.removeAllViews();
        for (int i = 1; i <= totalPages; i++) {
            Button pageButton = createPageButton(i);
            binding.paginationLayout.addView(pageButton);
        }
        updatePaginationButtonStates(userViewModel.getSelectedPage().getValue());
    }

    // Create a single page button
    private Button createPageButton(int page) {
        Button pageButton = new Button(activity);
        pageButton.setText(String.valueOf(page));
        pageButton.setTextColor(ContextCompat.getColor(activity, android.R.color.black));
        pageButton.setBackgroundResource(R.drawable.pagination_button);
        pageButton.setOnClickListener(v -> {
            userViewModel.loadPage(page);
            Log.d(TAG, "Page button clicked: " + page);
        });

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                dpToPx(36), dpToPx(36));
        params.setMargins(dpToPx(4), 0, dpToPx(4), 0);
        pageButton.setLayoutParams(params);

        pageButton.setGravity(Gravity.CENTER);
        pageButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        pageButton.setIncludeFontPadding(false);
        pageButton.setPadding(0, 0, 0, 0);

        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                pageButton, 8, 14, 1, TypedValue.COMPLEX_UNIT_SP);

        return pageButton;
    }

    // Update pagination button states based on current page
    private void updatePaginationButtonStates(Integer currentPage) {
        if (currentPage == null) return;
        Log.d(TAG, "Updating pagination button states. Current page: " + currentPage);

        int totalPages = binding.paginationLayout.getChildCount();
        if (currentPage < 1 || currentPage > totalPages) {
            Log.e(TAG, "Invalid current page: " + currentPage + ". Total pages: " + totalPages);
            userViewModel.loadPage(1);
            return;
        }

        int totalWidth = 0;
        int targetScrollX = 0;
        int buttonWidth = dpToPx(36 + 8);

        for (int i = 0; i < binding.paginationLayout.getChildCount(); i++) {
            View view = binding.paginationLayout.getChildAt(i);
            if (view instanceof Button) {
                Button pageButton = (Button) view;
                int page = Integer.parseInt(pageButton.getText().toString());
                updateButtonState(pageButton, page == currentPage);
                if (page == currentPage) {
                    targetScrollX = totalWidth - (binding.paginationScrollView.getWidth() - buttonWidth) / 2;
                }
                totalWidth += buttonWidth;
            }
        }

        scrollToSelectedButton(targetScrollX, totalWidth);
    }

    // Update individual button state
    private void updateButtonState(Button button, boolean isSelected) {
        button.setEnabled(!isSelected);
        button.setTextColor(ContextCompat.getColor(activity, isSelected ? R.color.light : android.R.color.black));
        button.setBackgroundResource(isSelected ? R.drawable.pagination_button_selected : R.drawable.pagination_button);
    }

    // Scroll to the selected button
    private void scrollToSelectedButton(int targetScrollX, int totalWidth) {
        final int finalTargetScrollX = Math.max(0, Math.min(targetScrollX, totalWidth - binding.paginationScrollView.getWidth()));
        binding.paginationScrollView.post(() -> binding.paginationScrollView.smoothScrollTo(finalTargetScrollX, 0));
    }

    // Convert dp to pixels
    private int dpToPx(int dp) {
        float density = activity.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    // Go to the next page
    public void goToNextPage() {
        Integer currentPage = userViewModel.getCurrentPage().getValue();
        Integer totalPages = userViewModel.getTotalPages().getValue();
        if (currentPage != null && totalPages != null && currentPage < totalPages) {
            userViewModel.loadPage(currentPage + 1);
        }
    }

    // Go to the previous page
    public void goToPreviousPage() {
        Integer currentPage = userViewModel.getCurrentPage().getValue();
        if (currentPage != null && currentPage > 1) {
            userViewModel.loadPage(currentPage - 1);
        }
    }
}