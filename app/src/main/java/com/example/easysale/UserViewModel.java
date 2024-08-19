package com.example.easysale;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class UserViewModel extends AndroidViewModel {
    private static final String TAG = "UserViewModel";
    private FetchUsers repository;
    private MutableLiveData<List<User>> users = new MutableLiveData<>();
    private MutableLiveData<Integer> totalPages = new MutableLiveData<>();
    private MutableLiveData<Integer> totalUsers = new MutableLiveData<>();
    private MutableLiveData<Integer> currentPage = new MutableLiveData<>();
    private static final int USERS_PER_PAGE = 6;
    private List<User> allUsers = new ArrayList<>();
    private List<User> filteredUsers = new ArrayList<>();

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
                filteredUsers = new ArrayList<>(allUsers);
                totalUsers.postValue(total);
                updatePagination();
                loadPage(1);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error fetching users: " + error);
            }
        });
    }

    public void searchUsers(String query) {
        String lowercaseQuery = query.toLowerCase().trim();
        Log.d(TAG, "Searching for: " + lowercaseQuery);
        filteredUsers = allUsers.stream()
                .filter(user -> {
                    boolean matches = user.getFirstName().toLowerCase().contains(lowercaseQuery) ||
                            user.getLastName().toLowerCase().contains(lowercaseQuery) ||
                            user.getEmail().toLowerCase().contains(lowercaseQuery);
                    if (matches) {
                        Log.d(TAG, "Matched user: " + user.getFirstName() + " " + user.getLastName() + " (" + user.getEmail() + ")");
                    }
                    return matches;
                })
                .collect(Collectors.toList());

        Log.d(TAG, "Found " + filteredUsers.size() + " matching users");

        updatePagination();
        loadPage(1);
    }

    private void updatePagination() {
        int total = filteredUsers.size();
        totalUsers.postValue(total);
        int pages = (total + USERS_PER_PAGE - 1) / USERS_PER_PAGE;
        totalPages.postValue(pages);

        // Ensure current page is valid
        int currentPageValue = currentPage.getValue() != null ? currentPage.getValue() : 1;
        if (currentPageValue > pages) {
            currentPage.postValue(pages);
        }
    }

    public void loadPage(int page) {
        currentPage.postValue(page);
        int start = (page - 1) * USERS_PER_PAGE;
        int end = Math.min(start + USERS_PER_PAGE, filteredUsers.size());
        List<User> pageUsers = filteredUsers.subList(start, end);
        users.postValue(new ArrayList<>(pageUsers));
    }

    public void deleteUserAndReload(User user) {
        repository.deleteUser(user, new FetchUsers.OnUserDeleteListener() {
            @Override
            public void onUserDeleted() {
                // Remove the user from allUsers and filteredUsers
                allUsers.remove(user);
                filteredUsers.remove(user);

                // Update pagination
                updatePagination();

                // Reload the current page
                int currentPageValue = currentPage.getValue() != null ? currentPage.getValue() : 1;
                loadPage(currentPageValue);

                // Update total users count
                totalUsers.postValue(allUsers.size());
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
                int indexInAll = allUsers.indexOf(user);
                if (indexInAll != -1) {
                    allUsers.set(indexInAll, updatedUser);
                }

                int indexInFiltered = filteredUsers.indexOf(user);
                if (indexInFiltered != -1) {
                    filteredUsers.set(indexInFiltered, updatedUser);
                }

                updatePagination();
                loadPage(currentPage.getValue() != null ? currentPage.getValue() : 1);

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
                allUsers.add(0, createdUser);  // Add to the beginning of the list
                filteredUsers.add(0, createdUser);

                updatePagination();
                loadPage(1);  // Load the first page to show the newly created user

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