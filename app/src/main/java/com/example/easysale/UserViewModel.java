package com.example.easysale;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserViewModel extends AndroidViewModel {
    private static final String TAG = "UserViewModel";
    private FetchUsers repository;
    private MutableLiveData<List<User>> users = new MutableLiveData<>();
    private MutableLiveData<Integer> totalPages = new MutableLiveData<>();
    private MutableLiveData<Integer> totalUsers = new MutableLiveData<>();
    private MutableLiveData<Integer> currentPage = new MutableLiveData<>();
    private static final int USERS_PER_PAGE = 6;
    private List<User> allUsers = new ArrayList<>();

    public UserViewModel(Application application) {
        super(application);
        repository = new FetchUsers(application);
    }

    public LiveData<List<User>> getUsers() {
        return users;
    }

    public LiveData<Integer> getTotalPages() {
        return totalPages;
    }

    public LiveData<Integer> getTotalUsers() {
        return totalUsers;
    }

    public LiveData<Integer> getCurrentPage() {
        return currentPage;
    }

    public void loadAllUsers() {
        repository.getAllUsers(new FetchUsers.OnUsersFetchListener() {
            @Override
            public void onUsersFetched(List<User> fetchedUsers, int total) {
                allUsers.clear();
                allUsers.addAll(fetchedUsers);
                Collections.reverse(allUsers);
                totalUsers.postValue(total);
                int pages = (total + USERS_PER_PAGE - 1) / USERS_PER_PAGE;
                totalPages.postValue(pages);
                loadPage(1);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error fetching users: " + error);
            }
        });
    }

    public void loadPage(int page) {
        Log.d(TAG, "Loading page: " + page);
        int start = (page - 1) * USERS_PER_PAGE;
        int end = Math.min(start + USERS_PER_PAGE, allUsers.size());
        List<User> pageUsers = allUsers.subList(start, end);
        users.postValue(new ArrayList<>(pageUsers));
        currentPage.postValue(page);
    }

    public void deleteUser(User user) {
        repository.deleteUser(user, new FetchUsers.OnUserDeleteListener() {
            @Override
            public void onUserDeleted() {
                List<User> currentUsers = users.getValue();
                if (currentUsers != null) {
                    currentUsers.remove(user);
                    users.postValue(new ArrayList<>(currentUsers));
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error deleting user: " + error);
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
                        users.postValue(new ArrayList<>(currentUsers));
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
                if (currentUsers == null) {
                    currentUsers = new ArrayList<>();
                }
                currentUsers.add(createdUser);
                users.postValue(new ArrayList<>(currentUsers));
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