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
}