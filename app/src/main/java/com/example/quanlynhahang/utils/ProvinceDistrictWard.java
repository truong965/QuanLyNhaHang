package com.example.quanlynhahang.utils;

import com.example.quanlynhahang.network.VietNamApiService;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProvinceDistrictWard {
    private static final String BASE_URL = "https://vn-public-apis.fpo.vn/";
    private static Retrofit retrofit = null;
    public static VietNamApiService getApiService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(VietNamApiService.class);
    }
}
