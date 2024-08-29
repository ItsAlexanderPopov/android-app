package com.example.easysale.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class CustomRecyclerView extends RecyclerView {
    private float startX = 0f;
    private float startY = 0f;
    private boolean isVerticalScrolling = false;
    private static final float SCROLL_THRESHOLD = 10f;

    private OnSwipeListener onSwipeListener;

    public CustomRecyclerView(@NonNull Context context) {
        super(context);
    }

    public CustomRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setOnSwipeListener(OnSwipeListener listener) {
        this.onSwipeListener = listener;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = e.getX();
                startY = e.getY();
                isVerticalScrolling = false;
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaX = e.getX() - startX;
                float deltaY = e.getY() - startY;
                if (!isVerticalScrolling && Math.abs(deltaY) > SCROLL_THRESHOLD) {
                    isVerticalScrolling = true;
                }
                if (!isVerticalScrolling && Math.abs(deltaX) > SCROLL_THRESHOLD) {
                    return false; // Don't intercept horizontal swipes
                }
                break;
        }
        return super.onInterceptTouchEvent(e);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (!isVerticalScrolling) {
            switch (e.getAction()) {
                case MotionEvent.ACTION_UP:
                    float endX = e.getX();
                    float deltaX = endX - startX;
                    if (Math.abs(deltaX) > SCROLL_THRESHOLD) {
                        if (deltaX > 0) {
                            if (onSwipeListener != null) onSwipeListener.onSwipeRight();
                        } else {
                            if (onSwipeListener != null) onSwipeListener.onSwipeLeft();
                        }
                        return true;
                    }
                    break;
            }
        }
        return super.onTouchEvent(e);
    }

    public interface OnSwipeListener {
        void onSwipeLeft();
        void onSwipeRight();
    }
}