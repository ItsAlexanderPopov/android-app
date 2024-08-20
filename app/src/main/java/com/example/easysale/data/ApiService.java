package com.example.easysale.data;

import com.example.easysale.model.User;
import com.example.easysale.model.UserResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    // Fetch users with pagination
    @GET("users")
    Call<UserResponse> getUsers(@Query("page") int page, @Query("per_page") int perPage);

    // Delete a user by ID
    @DELETE("users/{id}")
    Call<Void> deleteUser(@Path("id") int userId);

    // Update user details by ID
    @PUT("users/{id}")
    Call<User> updateUser(@Path("id") int userId, @Body User user);

    // Create a new user
    @POST("users")
    Call<User> createUser(@Body User user);
}