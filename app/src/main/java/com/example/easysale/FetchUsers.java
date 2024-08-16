package com.example.easysale;

import android.util.Log;

import java.util.List;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FetchUsers {
    private ApiService apiService;
    private static final String TAG = "FetchUsers";

    public FetchUsers(){
        apiService = RetrofitClient.getClient().create(ApiService.class);
    }

    public interface OnUsersFetchListener {
        void onUsersFetched(List<User> users);
        void onError(String error);
    }

    public void getUsers(final OnUsersFetchListener listener){
        final List<User> allUsers = new ArrayList<>();

        // Fetch the first page
        apiService.getUsers(1).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response){
                if(response.isSuccessful() && response.body() != null){
                    allUsers.addAll(response.body().getData());
                    // Fetch the second page
                    fetchSecondPage(listener, allUsers);
                } else{
                    Log.e(TAG, "Error fetching page 1: " + response.message());
                    listener.onError("Error fetching page 1: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable throwable) {
                Log.e(TAG, "Error fetching page 1", throwable);
                listener.onError("Error fetching page 1: " + throwable.getMessage());
            }
        });
    }

    private void fetchSecondPage(final OnUsersFetchListener listener, final List<User> allUsers) {
        apiService.getUsers(2).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if(response.isSuccessful() && response.body() != null){
                    allUsers.addAll(response.body().getData());
                    Log.d(TAG, "Users fetched successfully");
                    listener.onUsersFetched(allUsers);
                } else{
                    Log.e(TAG, "Error fetching page 2: " + response.message());
                    listener.onError("Error fetching page 2: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable throwable) {
                Log.e(TAG, "Error fetching page 2", throwable);
                listener.onError("Error fetching page 2: " + throwable.getMessage());
            }
        });
    }
}
