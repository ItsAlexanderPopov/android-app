package com.example.easysale;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;

public class UserViewModel extends ViewModel {
    private static final String TAG = "UserViewModel";
    private FetchUsers repository;
    private MutableLiveData<List<User>> users = new MutableLiveData<>();

    public UserViewModel() {
        repository = new FetchUsers();
    }

    public LiveData<List<User>> getUsers() {
        return users;
    }

    public void loadUsers() {
        repository.getUsers(new FetchUsers.OnUsersFetchListener() {
            @Override
            public void onUsersFetched(List<User> fetchedUsers) {
                users.setValue(fetchedUsers);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error fetching users: " + error);
            }
        });
    }
    public void deleteUser(User user) {
        repository.deleteUser(user, new FetchUsers.OnUserDeleteListener() {
            @Override
            public void onUserDeleted() {
                List<User> currentUsers = users.getValue();
                if (currentUsers != null) {
                    currentUsers.remove(user);
                    users.setValue(currentUsers);
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error deleting user: " + error);
                // You might want to add some error handling here, e.g., showing a toast
            }
        });
    }

    public void updateUser(User user, OnUserUpdateListener listener) {
        repository.updateUser(user, new FetchUsers.OnUserUpdateListener() {
            @Override
            public void onUserUpdated(User updatedUser) {
                List<User> currentUsers = users.getValue();
                if (currentUsers != null) {
                    int index = currentUsers.indexOf(user);
                    if (index != -1) {
                        currentUsers.set(index, updatedUser);
                        users.setValue(currentUsers);
                    }
                }
                listener.onUserUpdated();
            }

            @Override
            public void onError(String error) {
                listener.onError(error);
            }
        });
    }

    public void createUser(User user, OnUserCreateListener listener) {
        repository.createUser(user, new FetchUsers.OnUserCreateListener() {
            @Override
            public void onUserCreated(User createdUser) {
                List<User> currentUsers = users.getValue();
                if (currentUsers != null) {
                    currentUsers.add(createdUser);
                    users.setValue(currentUsers);
                }
                listener.onUserCreated();
            }

            @Override
            public void onError(String error) {
                listener.onError(error);
            }
        });
    }

    public interface OnUserUpdateListener {
        void onUserUpdated();
        void onError(String error);
    }

    public interface OnUserCreateListener {
        void onUserCreated();
        void onError(String error);
    }
}
