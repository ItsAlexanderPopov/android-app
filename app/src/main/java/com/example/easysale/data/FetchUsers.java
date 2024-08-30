package com.example.easysale.data;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

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

    public interface OnUserCreateListener {
        void onUserCreated(User user);
        void onError(String error);
    }

    public interface OnUserUpdateListener {
        void onUserUpdated(User user);
        void onError(String error);
    }

    public interface OnUserDeleteListener {
        void onUserDeleted();
        void onError(String error);
    }

    public void getAllUsers(final OnUsersFetchListener listener) {
        if (!initialCheckDone) {
            initialCheckDone = true;
            AsyncTask.execute(() -> checkAndFetchUsers(listener));
        } else {
            AsyncTask.execute(() -> fetchLocalUsers(listener));
        }
    }

    private void checkAndFetchUsers(OnUsersFetchListener listener) {
        List<User> localUsers = userDao.getAllUsers();
        if (localUsers.isEmpty()) {
            final List<User> allUsers = new ArrayList<>();
            fetchUsersRecursively(1, allUsers, listener);
        } else {
            listener.onUsersFetched(localUsers, localUsers.size());
        }
    }

    private void fetchLocalUsers(OnUsersFetchListener listener) {
        List<User> localUsers = userDao.getAllUsers();
        listener.onUsersFetched(localUsers, localUsers.size());
    }

    // By API's default per_page is 6 but it can be changed to whatever with this function and it will work with our GET implementation
    private void fetchUsersRecursively(final int page, final List<User> allUsers, final OnUsersFetchListener listener) {
        apiService.getUsers(page, 6).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserResponse> call, @NonNull Response<UserResponse> response) {
                Log.d(TAG, "fetchUsersRecursively: Response code: " + response.code());
                Log.d(TAG, "fetchUsersRecursively: Response body: " + response.body().getData());
                if (response.isSuccessful() && response.body() != null) {
                    List<User> users = response.body().getData();
                    allUsers.addAll(users);
                    if (users.size() == 6) {
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
            public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable throwable) {
                listener.onError("Error fetching users: " + throwable.getMessage());
            }
        });
    }

    public void createUser(User user, final OnUserCreateListener listener) {
        apiService.createUser(user).enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                Log.d(TAG, "createUser: Response code: " + response.code());
                Log.d(TAG, "createUser: " + response.body().getFirstName() + " " + response.body().getLastName() + ", email: " + response.body().getEmail());
                if (response.isSuccessful() && response.body() != null) {
                    // Use the data returned from the API
                    User createdUser = response.body();
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
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                Log.e(TAG, "Create user failed", t);
                listener.onError("Error creating user: " + t.getMessage());
            }
        });
    }

    public void updateUser(User user, final OnUserUpdateListener listener) {
        apiService.updateUser(user.getId(), user).enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                Log.d(TAG, "updateUser: Response code: " + response.code());
                Log.d(TAG, "updateUser: name: " + response.body().getFirstName() + " " + response.body().getLastName() + ", email: " + response.body().getEmail() + "");
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
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                Log.e(TAG, "Update failed", t);
                listener.onError("Error updating user: " + t.getMessage());
            }
        });
    }

    public void deleteUser(User user, final OnUserDeleteListener listener) {
        apiService.deleteUser(user.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                Log.d(TAG, "deleteUser: Response code: " + response.code());
                Log.d(TAG, "deleteUser: " + user.getFirstName() + " " + user.getLastName());

                if (response.isSuccessful()) {
                    AsyncTask.execute(() -> userDao.delete(user));
                    listener.onUserDeleted();
                } else {
                    listener.onError("Error deleting user: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                listener.onError("Error deleting user: " + t.getMessage());
            }
        });
    }
}