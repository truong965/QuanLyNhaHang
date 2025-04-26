package com.example.quanlynhahang.network;

import com.example.quanlynhahang.model.UserResponse;

import retrofit2.Call;
import retrofit2.http.GET;

public interface RandomUserAPi {
    @GET("api/")
    Call<UserResponse> getRandomUser();
}
