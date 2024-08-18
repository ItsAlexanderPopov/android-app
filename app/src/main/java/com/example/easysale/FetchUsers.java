package com.example.easysale;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import java.util.List;
import java.util.ArrayList;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FetchUsers {
    private final ApiService apiService;
    private final UserDao userDao;
    private static final String TAG = "FetchUsers";

    public FetchUsers(Context context) {
        apiService = RetrofitClient.getClient().create(ApiService.class);
        userDao = UserDatabase.getDatabase(context).userDao();
    }

    public interface OnUsersFetchListener {
        void onUsersFetched(List<User> users);
        void onError(String error);
    }

    public interface OnUserDeleteListener {
        void onUserDeleted();
        void onError(String error);
    }

    public interface OnUserUpdateListener {
        void onUserUpdated(User user);
        void onError(String error);
    }

    public interface OnUserCreateListener {
        void onUserCreated(User user);
        void onError(String error);
    }

    public void getUsers(final OnUsersFetchListener listener) {
        AsyncTask.execute(() -> {
            List<User> localUsers = userDao.getAllUsers();
            if (!localUsers.isEmpty()) {
                listener.onUsersFetched(localUsers);
            } else {
                fetchUsersFromApi(listener);
            }
        });
    }

    private void fetchUsersFromApi(final OnUsersFetchListener listener) {
        final List<User> allUsers = new ArrayList<>();
        fetchUsersRecursively(1, allUsers, listener);
    }

    private void fetchUsersRecursively(final int page, final List<User> allUsers, final OnUsersFetchListener listener) {
        apiService.getUsers(page).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<User> users = response.body().getData();
                    if (users.isEmpty()) {
                        AsyncTask.execute(() -> {
                            userDao.deleteAll();
                            userDao.insertAll(allUsers);
                        });
                        listener.onUsersFetched(allUsers);
                    } else {
                        allUsers.addAll(users);
                        fetchUsersRecursively(page + 1, allUsers, listener);
                    }
                } else {
                    listener.onError("Error fetching page " + page + ": " + response.message());
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable throwable) {
                listener.onError("Error fetching page " + page + ": " + throwable.getMessage());
            }
        });
    }

    public void deleteUser(User user, final OnUserDeleteListener listener) {
        apiService.deleteUser(user.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    AsyncTask.execute(() -> userDao.delete(user));
                    listener.onUserDeleted();
                } else {
                    listener.onError("Error deleting user: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                listener.onError("Error deleting user: " + t.getMessage());
            }
        });
    }

    public void updateUser(User user, final OnUserUpdateListener listener) {
        apiService.updateUser(user.getId(), user).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful()) {
                    AsyncTask.execute(() -> userDao.update(user));
                    listener.onUserUpdated(user);
                } else {
                    listener.onError("Error updating user: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Log.e("FetchUsers", "Update failed", t);
                listener.onError("Error updating user: " + t.getMessage());
            }
        });
    }

    public void createUser(User user, final OnUserCreateListener listener) {
        apiService.createUser(user).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful()) {
                    AsyncTask.execute(() -> userDao.insert(user));
                    listener.onUserCreated(user);
                } else {
                    listener.onError("Error creating user: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                listener.onError("Error creating user: " + t.getMessage());
            }
        });
    }
}