package com.example.easysale;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    // Calling ApiService.getUsers(1) yields api/users?page=1
    @GET("api/users") Call<UserResponse> getUsers(@Query("page") int page);
}
