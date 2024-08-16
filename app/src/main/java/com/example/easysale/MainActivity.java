package com.example.easysale;

import android.app.Activity;
import android.os.Bundle;

import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class MainActivity extends Activity {
    private UserViewModel userViewModel;
    private UserAdapter userAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(new ArrayList<>());
        recyclerView.setAdapter(userAdapter);

        userViewModel = new UserViewModel();

        userViewModel.getUsers().observeForever(users -> userAdapter.setUsers(users));

        userViewModel.loadUsers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove the observer to prevent memory leaks
        userViewModel.getUsers().removeObservers((LifecycleOwner) this);
    }
}