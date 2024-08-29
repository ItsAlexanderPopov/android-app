package com.example.easysale.utils;

import android.view.MotionEvent;
import android.view.View;

public class OnSwipeTouchListener implements View.OnTouchListener {
    private float startX;
    private float startY;
    private static final int SWIPE_THRESHOLD = 100;
    private static final float MAX_VERTICAL_RATIO = 2.0f;
    private boolean isSwipeDetected = false;
    private float lastTouchX;
    private float lastTouchY;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = lastTouchX = event.getX();
                startY = lastTouchY = event.getY();
                isSwipeDetected = false;
                return false;
            case MotionEvent.ACTION_MOVE:
                float currentX = event.getX();
                float currentY = event.getY();
                float diffX = currentX - startX;
                float diffY = currentY - startY;
                lastTouchX = currentX;
                lastTouchY = currentY;

                if (!isSwipeDetected) {
                    float absX = Math.abs(diffX);
                    float absY = Math.abs(diffY);
                    if (absX > SWIPE_THRESHOLD && absY / absX <= MAX_VERTICAL_RATIO) {
                        isSwipeDetected = true;
                        if (diffX > 0) {
                            onSwipeRight();
                        } else {
                            onSwipeLeft();
                        }
                        return true;
                    }
                }
                return isSwipeDetected;
            case MotionEvent.ACTION_UP:
                if (isSwipeDetected) {
                    isSwipeDetected = false;
                    return true;
                }
                return false;
        }
        return false;
    }

    public void onSwipeRight() {
        // Override this method in your implementation
    }

    public void onSwipeLeft() {
        // Override this method in your implementation
    }
}