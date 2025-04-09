package com.example.quanlynhahang.Entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import lombok.Getter;


@Getter
public class ProvinceResponse {
    @SerializedName("data")
    private ProvinceDataResponse data;
    @Getter
    public class ProvinceDataResponse {
        @SerializedName("data") // Đây là danh sách các tỉnh
        private List<Province> data;
    }
}


