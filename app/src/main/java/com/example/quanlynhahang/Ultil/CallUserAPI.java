package com.example.quanlynhahang.Ultil;

import com.example.quanlynhahang.DAO.RandomUserAPi;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CallUserAPI {
    public static final String BASE_URL = "https://randomuser.me/";
    private  static Retrofit retrofit = null;
    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
    public  static  RandomUserAPi getApi() {
        return getClient().create(RandomUserAPi.class);
    }
}
