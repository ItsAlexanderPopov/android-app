package com.example.easysale.data;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

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
    private Context context;

    public FetchUsers(Context context) {
        this.context = context;
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

    // Fetch all users from the API and local database
    public void getAllUsers(final OnUsersFetchListener listener) {
        if (!initialCheckDone) {
            initialCheckDone = true;
            AsyncTask.execute(() -> checkAndFetchUsers(listener));
        } else {
            AsyncTask.execute(() -> fetchLocalUsers(listener));
        }
    }

    // Check if there are any users in the local database and fetch them from the API if empty
    private void checkAndFetchUsers(OnUsersFetchListener listener) {
        List<User> localUsers = userDao.getAllUsers();
        if (localUsers.isEmpty()) {
            final List<User> allUsers = new ArrayList<>();
            fetchUsersRecursively(1, allUsers, listener);
        } else {
            listener.onUsersFetched(localUsers, localUsers.size());
        }
    }

    // Fetch users from the local database
    private void fetchLocalUsers(OnUsersFetchListener listener) {
        List<User> localUsers = userDao.getAllUsers();
        listener.onUsersFetched(localUsers, localUsers.size());
    }

    // Recursive method to fetch users from the API
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
                    String errorMsg = "Unable to fetch users. Please try again later.";
                    showToast(errorMsg);
                    listener.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable throwable) {
                String errorMsg = "Network error. Please check your connection and try again.";
                showToast(errorMsg);
                listener.onError(errorMsg);
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
                    User createdUser = response.body();
                    AsyncTask.execute(() -> {
                        int newID = userDao.getMaxUserId() + 1;
                        Log.d(TAG, "createUser: Generated new ID: " + newID);
                        createdUser.setId(newID);
                        userDao.insert(createdUser);
                    });
                    listener.onUserCreated(createdUser);
                } else {
                    String errorMsg = "Unable to create user. Please try again.";
                    showToast(errorMsg);
                    listener.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                Log.e(TAG, "Create user failed", t);
                String errorMsg = "Network error. User creation failed. Please try again.";
                showToast(errorMsg);
                listener.onError(errorMsg);
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
                    user.setFirstName(updatedUser.getFirstName());
                    user.setLastName(updatedUser.getLastName());
                    user.setEmail(updatedUser.getEmail());
                    AsyncTask.execute(() -> userDao.update(user));
                    listener.onUserUpdated(user);
                } else {
                    String errorMsg = "Unable to update user. Please try again.";
                    showToast(errorMsg);
                    listener.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                Log.e(TAG, "Update failed", t);
                String errorMsg = "Network error. User update failed. Please try again.";
                showToast(errorMsg);
                listener.onError(errorMsg);
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
                    String errorMsg = "Unable to delete user. Please try again.";
                    showToast(errorMsg);
                    listener.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                String errorMsg = "Network error. User deletion failed. Please try again.";
                showToast(errorMsg);
                listener.onError(errorMsg);
            }
        });
    }

    private void showToast(final String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}