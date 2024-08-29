package com.example.easysale.utils;

import android.os.Handler;
import android.os.Looper;

public class ClickDebounce<T> {
    private static final long DEFAULT_DEBOUNCE_TIME = 1000;
    private final long debounceTime;
    private boolean isClickable = true;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final DebounceClickListener<T> listener;

    public interface DebounceClickListener<T> {
        void onDebounceClick(T param);
    }

    private ClickDebounce(DebounceClickListener<T> listener, long debounceTime) {
        this.listener = listener;
        this.debounceTime = debounceTime;
    }

    public void onClick(T param) {
        if (isClickable) {
            isClickable = false;
            listener.onDebounceClick(param);
            handler.postDelayed(this::reset, debounceTime);
        }
    }

    private void reset() {
        isClickable = true;
    }

    public static <T> ClickDebounce<T> wrap(DebounceClickListener<T> listener) {
        return new ClickDebounce<>(listener, DEFAULT_DEBOUNCE_TIME);
    }

    // In case we want to use custom time for debounce
    public static <T> ClickDebounce<T> wrap(DebounceClickListener<T> listener, long customDebounceTime) {
        return new ClickDebounce<>(listener, customDebounceTime);
    }
}