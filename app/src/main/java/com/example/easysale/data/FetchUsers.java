package com.example.easysale.data;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import java.util.List;
import java.util.ArrayList;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.easysale.model.User;
import com.example.easysale.model.UserResponse;

public class FetchUsers {
    private final ApiService apiService;
    private final UserDao userDao;
    private static final String TAG = "FetchUsers";
    boolean initialCheckDone = false;

    public FetchUsers(Context context) {
        apiService = RetrofitClient.getClient().create(ApiService.class);
        userDao = UserDatabase.getDatabase(context).userDao();
    }

    public interface OnUsersFetchListener {
        void onUsersFetched(List<User> users, int total);
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

    public void getAllUsers(final OnUsersFetchListener listener) {
        if (!initialCheckDone) {
            initialCheckDone = true;
            AsyncTask.execute(() -> {
                List<User> localUsers = userDao.getAllUsers();
                if (localUsers.isEmpty()) {
                    fetchAllUsersFromApi(listener);
                } else {
                    listener.onUsersFetched(localUsers, localUsers.size());
                }
            });
        } else {
            AsyncTask.execute(() -> {
                List<User> localUsers = userDao.getAllUsers();
                listener.onUsersFetched(localUsers, localUsers.size());
            });
        }
    }

    private void fetchAllUsersFromApi(final OnUsersFetchListener listener) {
        final List<User> allUsers = new ArrayList<>();
        fetchUsersRecursively(1, allUsers, listener);
    }

    private void fetchUsersRecursively(final int page, final List<User> allUsers, final OnUsersFetchListener listener) {
        apiService.getUsers(page, 100).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<User> users = response.body().getData();
                    allUsers.addAll(users);
                    if (users.size() == 100) {
                        fetchUsersRecursively(page + 1, allUsers, listener);
                    } else {
                        AsyncTask.execute(() -> {
                            userDao.deleteAll();
                            userDao.insertAll(allUsers);
                        });
                        listener.onUsersFetched(allUsers, allUsers.size());
                    }
                } else {
                    listener.onError("Error fetching users: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable throwable) {
                listener.onError("Error fetching users: " + throwable.getMessage());
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
        apiService.updateUser(user.getId(), user).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User updatedUser = response.body();
                    // Update only the fields that the API returns
                    user.setFirstName(updatedUser.getFirstName());
                    user.setLastName(updatedUser.getLastName());
                    user.setEmail(updatedUser.getEmail());
                    AsyncTask.execute(() -> userDao.update(user));
                    listener.onUserUpdated(user);
                } else {
                    listener.onError("Error updating user: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e(TAG, "Update failed", t);
                listener.onError("Error updating user: " + t.getMessage());
            }
        });
    }

    public void createUser(User user, final OnUserCreateListener listener) {
        apiService.createUser(user).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User createdUser = response.body();
                    // Use the data returned from the API
                    AsyncTask.execute(() -> {
                        // The API doesn't return ID so we create it ourselves
                        int newID = userDao.getMaxUserId() + 1;
                        Log.d(TAG, "createUser: Generated new ID: " + newID);
                        createdUser.setId(newID);
                        userDao.insert(createdUser);
                    });
                    listener.onUserCreated(createdUser);
                } else {
                    listener.onError("Error creating user: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e(TAG, "Create user failed", t);
                listener.onError("Error creating user: " + t.getMessage());
            }
        });
    }
}