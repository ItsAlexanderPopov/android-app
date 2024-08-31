package com.example.easysale.homepage;

import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.os.Handler;
import android.os.Looper;

public class RecyclerGestureHandler implements RecyclerView.OnItemTouchListener {
    private static final String TAG = "RecyclerGestureHandler";
    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;
    private static final int CLICK_THRESHOLD = 10; // pixels
    private static final long SWIPE_RESET_DELAY = 300; // milliseconds

    private final OnSwipeListener swipeListener;
    private final OnItemClickListener clickListener;
    private float startX;
    private float startY;
    private long startTime;
    private boolean isSwiping = false;
    private boolean isWaitingForReset = false;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public RecyclerGestureHandler(OnSwipeListener swipeListener, OnItemClickListener clickListener) {
        this.swipeListener = swipeListener;
        this.clickListener = clickListener;
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = e.getX();
                startY = e.getY();
                startTime = System.currentTimeMillis();
                isSwiping = false;
                isWaitingForReset = false;
                Log.d(TAG, "ACTION_DOWN: x=" + startX + ", y=" + startY);
                break;

            case MotionEvent.ACTION_MOVE:
                if (!isSwiping && !isWaitingForReset) {
                    float diffX = e.getX() - startX;
                    float diffY = e.getY() - startY;
                    if (Math.abs(diffX) > Math.abs(diffY) && Math.abs(diffX) > SWIPE_THRESHOLD) {
                        isSwiping = true;
                        Log.d(TAG, "Swipe detected, disallowing intercept");
                        rv.getParent().requestDisallowInterceptTouchEvent(true);
                        return true; // Intercept the touch event
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                if (!isWaitingForReset) {
                    handleActionUp(rv, e);
                }
                break;
        }
        return false; // Don't intercept the touch event
    }

    private void handleActionUp(RecyclerView rv, MotionEvent e) {
        float endX = e.getX();
        float endY = e.getY();
        long endTime = System.currentTimeMillis();
        float diffX = endX - startX;
        float diffY = endY - startY;
        float velocity = Math.abs(diffX) / (endTime - startTime) * 1000;

        Log.d(TAG, "ACTION_UP: endX=" + endX + ", endY=" + endY + ", diffX=" + diffX + ", velocity=" + velocity);

        if (isSwiping && Math.abs(diffX) > SWIPE_THRESHOLD && velocity > SWIPE_VELOCITY_THRESHOLD) {
            if (diffX > 0) {
                Log.d(TAG, "Swipe Right detected");
                swipeListener.onSwipeRight();
            } else {
                Log.d(TAG, "Swipe Left detected");
                swipeListener.onSwipeLeft();
            }
            isWaitingForReset = true;
            handler.postDelayed(this::resetSwipeState, SWIPE_RESET_DELAY);
        } else if (!isSwiping && Math.abs(diffX) < CLICK_THRESHOLD && Math.abs(diffY) < CLICK_THRESHOLD) {
            View childView = rv.findChildViewUnder(e.getX(), e.getY());
            if (childView != null) {
                int position = rv.getChildAdapterPosition(childView);
                Log.d(TAG, "Item click at position " + position);
                clickListener.onItemClick(childView, position);
            }
        }
    }

    private void resetSwipeState() {
        isSwiping = false;
        isWaitingForReset = false;
        Log.d(TAG, "Swipe state reset");
    }

    @Override
    public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        // This method will be called if we return true from onInterceptTouchEvent
        if (e.getAction() == MotionEvent.ACTION_UP && isSwiping) {
            handleActionUp(rv, e);
        }
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        // Not needed for this implementation
    }

    public interface OnSwipeListener {
        void onSwipeLeft();
        void onSwipeRight();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
}