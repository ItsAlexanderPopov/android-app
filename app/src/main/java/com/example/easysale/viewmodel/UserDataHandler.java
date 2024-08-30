package com.example.easysale.viewmodel;

import com.example.easysale.model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class UserDataHandler {
    private List<User> allUsers = new ArrayList<>();
    private List<User> filteredUsers = new ArrayList<>();
    private String currentSearchQuery = "";

    public void setAllUsers(List<User> users) {
        allUsers.clear();
        allUsers.addAll(users);
        Collections.reverse(allUsers);
        filteredUsers = new ArrayList<>(allUsers);
    }

    public void resetSearchQuery() {
        currentSearchQuery = "";
    }

    public void setSearchQuery(String query) {
        currentSearchQuery = query.toLowerCase().trim();
    }

    public void performSearch() {
        if (currentSearchQuery.isEmpty()) {
            filteredUsers = new ArrayList<>(allUsers);
        } else {
            filteredUsers = allUsers.stream()
                    .filter(user -> userMatchesSearch(user, currentSearchQuery))
                    .collect(Collectors.toList());
        }
    }

    public void updateUser(User updatedUser) {
        updateUserInList(allUsers, updatedUser);
        updateUserInList(filteredUsers, updatedUser);
    }

    private void updateUserInList(List<User> userList, User updatedUser) {
        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getId() == updatedUser.getId()) {
                userList.set(i, updatedUser);
                break;
            }
        }
    }

    public void removeUser(User user) {
        allUsers.remove(user);
        filteredUsers.remove(user);
    }

    public void addUser(User user) {
        allUsers.add(0, user);
        if (currentSearchQuery.isEmpty() || userMatchesSearch(user, currentSearchQuery)) {
            filteredUsers.add(0, user);
        }
    }

    private boolean userMatchesSearch(User user, String lowercaseQuery) {
        return user.getFirstName().toLowerCase().contains(lowercaseQuery) ||
                user.getLastName().toLowerCase().contains(lowercaseQuery) ||
                user.getEmail().toLowerCase().contains(lowercaseQuery);
    }

    public int getFilteredUsersSize() {
        return filteredUsers.size();
    }

    public int getAllUsersSize() {
        return allUsers.size();
    }

    public List<User> getFilteredUsersSubList(int start, int end) {
        return new ArrayList<>(filteredUsers.subList(start, end));
    }
}