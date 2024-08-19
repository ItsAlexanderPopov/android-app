package com.example.easysale;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;

public class DeleteDialog extends Dialog {
    private final User user;
    private final OnDeleteConfirmedListener listener;

    public interface OnDeleteConfirmedListener {
        void onDeleteConfirmed(User user);
    }

    public DeleteDialog(@NonNull Context context, User user, OnDeleteConfirmedListener listener) {
        super(context, R.style.CustomDialog);
        this.user = user;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delete_confirmation_dialog);
        TextView messageTextView = findViewById(R.id.message_text_view);
        messageTextView.setText
                ("Are you sure you want to delete " + user.getFirstName() + " " + user.getLastName() + "?");
        Button yesButton = findViewById(R.id.yes_button);
        yesButton.setOnClickListener(v -> {
            listener.onDeleteConfirmed(user);
            dismiss();
        });
        Button noButton = findViewById(R.id.no_button);
        noButton.setOnClickListener(v -> dismiss());
        ImageView closeIcon = findViewById(R.id.close_icon);
        closeIcon.setOnClickListener(v -> dismiss());
        setCanceledOnTouchOutside(true);
    }

    @Override
    public void show() {
        super.show();
        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(window.getAttributes());
            int screenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
            int screenHeight = getContext().getResources().getDisplayMetrics().heightPixels;
            layoutParams.width = (int) (screenWidth * 0.9); // dialog width as 90% of screen width
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            // Position the dialog below the top of the screen with a margin
            layoutParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            int topMargin = (int) (screenHeight * 0.15); // 15% of screen height as top margin
            layoutParams.y = topMargin;
            window.setAttributes(layoutParams);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }
}