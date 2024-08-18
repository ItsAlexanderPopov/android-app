package com.example.easysale;

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
    @GET("api/users")
    Call<UserResponse> getUsers(@Query("page") int page, @Query("per_page") int perPage);

    // Delete a user by ID
    @DELETE("api/users/{id}")
    Call<Void> deleteUser(@Path("id") int userId);

    // Update user details by ID
    @PUT("api/users/{id}")
    Call<UserResponse> updateUser(@Path("id") int userId, @Body User user);

    // New endpoint for creating a user
    @POST("api/users")
    Call<UserResponse> createUser(@Body User user);
}