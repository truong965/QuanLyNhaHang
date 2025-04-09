package com.example.quanlynhahang.DAO;

import com.example.quanlynhahang.Entity.UserResponse;

import retrofit2.Call;
import retrofit2.http.GET;

public interface RandomUserAPi {
    @GET("api/")
    Call<UserResponse> getRandomUser();
}
