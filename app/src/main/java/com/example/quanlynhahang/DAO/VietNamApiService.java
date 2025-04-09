package com.example.quanlynhahang.DAO;

import com.example.quanlynhahang.Entity.DistrictResponse;
import com.example.quanlynhahang.Entity.ProvinceResponse;
import com.example.quanlynhahang.Entity.WardReponse;

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
