package com.example.quanlynhahang.network;

import com.example.quanlynhahang.model.DistrictResponse;
import com.example.quanlynhahang.model.ProvinceResponse;
import com.example.quanlynhahang.model.WardReponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface VietNamApiService {
    @GET("provinces/getAll?limit=-1")
    Call<ProvinceResponse> getProvinces();

    @GET("districts/getByProvince")
    Call<DistrictResponse> getDistrictsByProvince(@Query("provinceCode") String provinceCode, @Query("limit") int limit);

    @GET("wards/getByDistrict")
    Call<WardReponse> getWardsByDistrict(@Query("districtCode") String districtCode, @Query("limit") int limit);
}
