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
        // Set the content view to the layout defined in main_activity.xml
        setContentView(R.layout.main_activity);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(new ArrayList<>());
        recyclerView.setAdapter(userAdapter);
        userViewModel = new UserViewModel();
        // Observe the users LiveData from the ViewModel and update the adapter when data changes
        userViewModel.getUsers().observeForever(users -> userAdapter.setUsers(users));
        userViewModel.loadUsers();
    }

    @Override
    // Remove the observer to prevent memory leaks
    protected void onDestroy() {
        super.onDestroy();
        userViewModel.getUsers().removeObservers((LifecycleOwner) this);
    }
}