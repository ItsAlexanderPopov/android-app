package com.example.easysale.utils;

import android.view.View;

public class ClickDebounce implements View.OnClickListener {
    private boolean isClickable = true;
    private final View.OnClickListener wrappedListener;

    public ClickDebounce(View.OnClickListener listener) {
        this.wrappedListener = listener;
    }

    @Override
    public void onClick(View v) {
        if (isClickable) {
            isClickable = false;
            wrappedListener.onClick(v);
        }
    }

    public void reset() {
        isClickable = true;
    }

    public static ClickDebounce wrap(View.OnClickListener listener) {
        return new ClickDebounce(listener);
    }
}