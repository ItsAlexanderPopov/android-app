package com.example.easysale;

import android.util.Log;
import java.util.List;
import java.util.ArrayList;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FetchUsers {
    private final ApiService apiService;
    private static final String TAG = "FetchUsers";

    public FetchUsers() {
        apiService = RetrofitClient.getClient().create(ApiService.class);
    }

    public interface OnUsersFetchListener {
        void onUsersFetched(List<User> users);
        void onError(String error);
    }

    public void getUsers(final OnUsersFetchListener listener) {
        final List<User> allUsers = new ArrayList<>();
        fetchUsersRecursively(1, allUsers, listener);
    }

    // There are 6 users per page, by default 12. (18/08/2024)
    private void fetchUsersRecursively(final int page, final List<User> allUsers, final OnUsersFetchListener listener) {
        apiService.getUsers(page).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<User> users = response.body().getData();
                    if (users.isEmpty()) {
                        // No more users to fetch
                        Log.d(TAG, "All users fetched successfully");
                        listener.onUsersFetched(allUsers);
                    } else {
                        allUsers.addAll(users);
                        // Fetch the next page
                        fetchUsersRecursively(page + 1, allUsers, listener);
                    }
                } else {
                    Log.e(TAG, "Error fetching page " + page + ": " + response.message());
                    listener.onError("Error fetching page " + page + ": " + response.message());
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable throwable) {
                Log.e(TAG, "Error fetching page " + page, throwable);
                listener.onError("Error fetching page " + page + ": " + throwable.getMessage());
            }
        });
    }
}
