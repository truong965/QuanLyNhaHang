package com.example.quanlynhahang.nhap;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import com.example.quanlynhahang.Entity.UserResponse;
import com.example.quanlynhahang.R;
import com.example.quanlynhahang.DAO.*;
import com.example.quanlynhahang.Ultil.CallUserAPI;

import retrofit2.Call;

public class testApi extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_test_api);
        fetchRandomUser();
    }
    private void fetchRandomUser() {
        RandomUserAPi apiService = CallUserAPI.getApi();
        Call<UserResponse> call = apiService.getRandomUser();

        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse userResponse = response.body();
                    UserResponse.User user = userResponse.getResults().get(0); // Get first user
                    Log.i("User Data", "User fetched successfully");
                    Log.d("User Data", "Name: " + user.toString());}
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Log.e("API Error", t.getMessage());
            }
        });
    }
}