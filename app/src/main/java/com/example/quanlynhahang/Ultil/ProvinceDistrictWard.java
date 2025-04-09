package com.example.quanlynhahang.Ultil;

import com.example.quanlynhahang.DAO.VietNamApiService;
import com.example.quanlynhahang.Entity.Province;

import java.util.ArrayList;

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
