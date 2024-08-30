package com.example.easysale.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.easysale.data.FetchUsers;
import com.example.easysale.data.UserDao;
import com.example.easysale.data.UserDatabase;
import com.example.easysale.model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class UserViewModel extends AndroidViewModel {
    private static final String TAG = "UserViewModel";
    private static final int USERS_PER_PAGE = 6;

    private final FetchUsers repository;
    private final UserDao userDao;
    private final ExecutorService executor;
    private final UserDataHandler dataHandler;

    private final MutableLiveData<List<User>> users = new MutableLiveData<>();
    private final MutableLiveData<Integer> totalPages = new MutableLiveData<>();
    private final MutableLiveData<Integer> totalUsers = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentPage = new MutableLiveData<>(1);
    private final MutableLiveData<Integer> selectedPage = new MutableLiveData<>(1);
    private final MutableLiveData<Boolean> paginationUpdated = new MutableLiveData<>();

    public UserViewModel(Application application) {
        super(application);
        repository = new FetchUsers(application);
        userDao = UserDatabase.getDatabase(application).userDao();
        executor = Executors.newSingleThreadExecutor();
        dataHandler = new UserDataHandler();
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

    public LiveData<Integer> getSelectedPage() {
        return selectedPage;
    }

    public LiveData<Boolean> getPaginationUpdated() {
        return paginationUpdated;
    }

    public void loadAllUsers() {
        dataHandler.resetSearchQuery();
        repository.getAllUsers(new FetchUsers.OnUsersFetchListener() {
            @Override
            public void onUsersFetched(List<User> fetchedUsers, int total) {
                Log.i(TAG, "Fetched " + fetchedUsers.size() + " users out of " + total + " total users");
                dataHandler.setAllUsers(fetchedUsers);
                totalUsers.postValue(total);
                updatePagination();
                loadPage(1);
                paginationUpdated.postValue(true);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error fetching users: " + error);
            }
        });
    }

    public void updateLocalUser(User updatedUser) {
        Log.d(TAG, "Updating local user: " + updatedUser.getFirstName() + " " + updatedUser.getLastName());
        dataHandler.updateUser(updatedUser);
        loadPage(currentPage.getValue() != null ? currentPage.getValue() : 1);
        paginationUpdated.postValue(true);
    }

    public void searchUsers(String query) {
        dataHandler.setSearchQuery(query);
        Log.d(TAG, "Searching for: " + query);
        dataHandler.performSearch();
        updatePagination();
        loadPage(1);
        paginationUpdated.postValue(true);
    }

    private void updatePagination() {
        int total = dataHandler.getFilteredUsersSize();
        totalUsers.postValue(total);
        int pages = Math.max(1, (total + USERS_PER_PAGE - 1) / USERS_PER_PAGE);
        totalPages.postValue(pages);
        Log.d(TAG, "Updated pagination: total users = " + total + ", total pages = " + pages);
    }

    private void checkAndAdjustPage() {
        int currentPageValue = currentPage.getValue() != null ? currentPage.getValue() : 1;
        int totalPagesValue = totalPages.getValue() != null ? totalPages.getValue() : 1;
        int start = (currentPageValue - 1) * USERS_PER_PAGE;

        if (start >= dataHandler.getFilteredUsersSize() && totalPagesValue > 0) {
            Log.d(TAG, "Adjusting to last page: " + totalPagesValue);
            loadPage(totalPagesValue);
        } else if (dataHandler.getFilteredUsersSize() == 0) {
            Log.d(TAG, "No filtered users, staying on page 1");
            loadPage(1);
        } else {
            Log.d(TAG, "Reloading current page: " + currentPageValue);
            loadPage(currentPageValue);
        }
        paginationUpdated.postValue(true);
    }

    public void loadPage(int page) {
        Integer totalPagesValue = totalPages.getValue();
        if (totalPagesValue == null) {
            totalPagesValue = 1;
        }
        page = Math.max(1, Math.min(page, totalPagesValue));
        selectedPage.postValue(page);
        currentPage.postValue(page);
        int start = (page - 1) * USERS_PER_PAGE;
        int end = Math.min(start + USERS_PER_PAGE, dataHandler.getFilteredUsersSize());

        start = Math.min(start, dataHandler.getFilteredUsersSize());
        List<User> pageUsers = new ArrayList<>(dataHandler.getFilteredUsersSubList(start, end));
        users.postValue(pageUsers);
        Log.d(TAG, "Loaded " + pageUsers.size() + " users for page " + page);
    }

    public void deleteUserAndReload(User user) {
        repository.deleteUser(user, new FetchUsers.OnUserDeleteListener() {
            @Override
            public void onUserDeleted() {
                Log.d(TAG, "User deleted successfully: " + user.getId());
                dataHandler.removeUser(user);
                updatePagination();
                checkAndAdjustPage();
                totalUsers.postValue(dataHandler.getAllUsersSize());
                paginationUpdated.postValue(true);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error deleting user: " + error);
            }
        });
    }

    public void updateUser(User user, OnUserUpdateListener listener) {
        int lastKnownPage = currentPage.getValue() != null ? currentPage.getValue() : 1;
        repository.updateUser(user, new FetchUsers.OnUserUpdateListener() {
            @Override
            public void onUserUpdated(User updatedUser) {
                Log.d(TAG, "User updated successfully: " + updatedUser.getId());
                dataHandler.updateUser(updatedUser);
                updatePagination();
                loadPage(Math.min(lastKnownPage, totalPages.getValue() != null ? totalPages.getValue() : 1));
                paginationUpdated.postValue(true);
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
                Log.d(TAG, "User created successfully: " + createdUser.getId());
                dataHandler.addUser(createdUser);
                updatePagination();
                checkAndAdjustPage();
                paginationUpdated.postValue(true);
                listener.onUserCreated();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error creating user: " + error);
                listener.onError(error);
            }
        });
    }

    public void isEmailUnique(String email, int userId, EmailUniqueCallback callback) {
        executor.execute(() -> {
            int count = userDao.countUsersWithEmail(email, userId);
            boolean isUnique = count == 0;
            Log.d(TAG, "Email uniqueness check result: " + isUnique);
            callback.onResult(isUnique);
        });
    }

    public interface EmailUniqueCallback {
        void onResult(boolean isUnique);
    }

    public interface OnUserUpdateListener {
        void onUserUpdated();
        void onError(String error);
    }

    public interface OnUserCreateListener {
        void onUserCreated();
        void onError(String error);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
        Log.d(TAG, "UserViewModel cleared, executor shut down");
    }
}