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

    private final MutableLiveData<List<User>> users = new MutableLiveData<>();
    private final MutableLiveData<Integer> totalPages = new MutableLiveData<>();
    private final MutableLiveData<Integer> totalUsers = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentPage = new MutableLiveData<>(1);
    private final MutableLiveData<Integer> selectedPage = new MutableLiveData<>(1);
    private final MutableLiveData<Boolean> paginationUpdated = new MutableLiveData<>();

    private final List<User> allUsers = new ArrayList<>();
    private List<User> filteredUsers = new ArrayList<>();
    private String currentSearchQuery = "";
    private int lastKnownPage = 1;


    public UserViewModel(Application application) {
        super(application);
        repository = new FetchUsers(application);
        userDao = UserDatabase.getDatabase(application).userDao();
        executor = Executors.newSingleThreadExecutor();
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
        currentSearchQuery = "";
        repository.getAllUsers(new FetchUsers.OnUsersFetchListener() {
            @Override
            public void onUsersFetched(List<User> fetchedUsers, int total) {
                allUsers.clear();
                allUsers.addAll(fetchedUsers);
                Collections.reverse(allUsers);
                filteredUsers = new ArrayList<>(allUsers);
                totalUsers.postValue(total);
                updatePagination();
                loadPage(1);  // Always reset to page 1 when loading all users
                paginationUpdated.postValue(true);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error fetching users: " + error);
            }
        });
    }

    public void updateLocalUser(User updatedUser) {
        Log.d(TAG, "Updating local user: " + updatedUser);
        updateUserInList(allUsers, updatedUser);
        updateUserInList(filteredUsers, updatedUser);
        loadPage(currentPage.getValue() != null ? currentPage.getValue() : 1);
        paginationUpdated.postValue(true);
    }

    private void updateUserInList(List<User> userList, User updatedUser) {
        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getId() == updatedUser.getId()) {
                userList.set(i, updatedUser);
                Log.d(TAG, "Updated user in list at index: " + i);
                break;
            }
        }
    }

    public void searchUsers(String query) {
        currentSearchQuery = query;
        String lowercaseQuery = query.toLowerCase().trim();
        Log.d(TAG, "Searching for: " + lowercaseQuery);

        try {
            if (lowercaseQuery.isEmpty()) {
                filteredUsers = new ArrayList<>(allUsers);
            } else {
                filteredUsers = allUsers.stream()
                        .filter(user -> userMatchesSearch(user, lowercaseQuery))
                        .collect(Collectors.toList());
            }

            Log.d(TAG, "Found " + filteredUsers.size() + " matching users");
            updatePagination();
            loadPage(1);
            paginationUpdated.postValue(true);
        } catch (Exception e) {
            Log.e(TAG, "Error during search: ", e);
            filteredUsers = new ArrayList<>(allUsers);
            updatePagination();
            loadPage(1);
            paginationUpdated.postValue(true);
        }
    }

    private void updatePagination() {
        int total = filteredUsers.size();
        totalUsers.postValue(total);
        int pages = Math.max(1, (total + USERS_PER_PAGE - 1) / USERS_PER_PAGE);
        totalPages.postValue(pages);
        Log.d(TAG, "Updated pagination: total users = " + total + ", total pages = " + pages);
    }

    private void checkAndAdjustPage() {
        int currentPageValue = currentPage.getValue() != null ? currentPage.getValue() : 1;
        int totalPagesValue = totalPages.getValue() != null ? totalPages.getValue() : 1;

        // Calculate the start index for the current page
        int start = (currentPageValue - 1) * USERS_PER_PAGE;

        // If the start index is beyond the size of filteredUsers, adjust to the last page
        if (start >= filteredUsers.size() && totalPagesValue > 0) {
            loadPage(totalPagesValue);
        } else if (filteredUsers.isEmpty()) {
            // If filteredUsers is empty, stay on page 1
            loadPage(1);
        } else {
            // Otherwise, reload the current page
            loadPage(currentPageValue);
        }
        paginationUpdated.postValue(true);
    }

    public void loadPage(int page) {
        Log.d(TAG, "Loading page: " + page);
        Integer totalPagesValue = totalPages.getValue();
        if (totalPagesValue == null) {
            totalPagesValue = 1;
        }

        page = Math.max(1, Math.min(page, totalPagesValue));

        selectedPage.postValue(page);
        currentPage.postValue(page);
        int start = (page - 1) * USERS_PER_PAGE;
        int end = Math.min(start + USERS_PER_PAGE, filteredUsers.size());

        // Ensure start is not greater than the list size
        start = Math.min(start, filteredUsers.size());

        List<User> pageUsers = new ArrayList<>(filteredUsers.subList(start, end));
        users.postValue(pageUsers);
        Log.d(TAG, "Loaded " + pageUsers.size() + " users for page " + page);
    }

    public void deleteUserAndReload(User user) {
        repository.deleteUser(user, new FetchUsers.OnUserDeleteListener() {
            @Override
            public void onUserDeleted() {
                allUsers.remove(user);
                filteredUsers.remove(user);
                updatePagination();
                checkAndAdjustPage();
                totalUsers.postValue(allUsers.size());
                paginationUpdated.postValue(true);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error deleting user: " + error);
            }
        });
    }

    public void updateUser(User user, OnUserUpdateListener listener) {
        lastKnownPage = currentPage.getValue() != null ? currentPage.getValue() : 1;
        repository.updateUser(user, new FetchUsers.OnUserUpdateListener() {
            @Override
            public void onUserUpdated(User updatedUser) {
                int indexInAll = -1;
                for (int i = 0; i < allUsers.size(); i++) {
                    if (allUsers.get(i).getId() == updatedUser.getId()) {
                        indexInAll = i;
                        break;
                    }
                }
                if (indexInAll != -1) {
                    allUsers.set(indexInAll, updatedUser);
                }

                int indexInFiltered = -1;
                for (int i = 0; i < filteredUsers.size(); i++) {
                    if (filteredUsers.get(i).getId() == updatedUser.getId()) {
                        indexInFiltered = i;
                        break;
                    }
                }
                if (indexInFiltered != -1) {
                    filteredUsers.set(indexInFiltered, updatedUser);
                }

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
                allUsers.add(0, createdUser);
                if (currentSearchQuery.isEmpty() || userMatchesSearch(createdUser, currentSearchQuery.toLowerCase())) {
                    filteredUsers.add(0, createdUser);
                }
                updatePagination();
                checkAndAdjustPage();
                paginationUpdated.postValue(true);
                listener.onUserCreated();
            }

            @Override
            public void onError(String error) {
                listener.onError(error);
            }
        });
    }

    private boolean userMatchesSearch(User user, String lowercaseQuery) {
        return user.getFirstName().toLowerCase().contains(lowercaseQuery) ||
                user.getLastName().toLowerCase().contains(lowercaseQuery) ||
                user.getEmail().toLowerCase().contains(lowercaseQuery);
    }

    public void isEmailUnique(String email, int userId, EmailUniqueCallback callback) {
        executor.execute(() -> {
            int count = userDao.countUsersWithEmail(email, userId);
            boolean isUnique = count == 0;
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
    }
}