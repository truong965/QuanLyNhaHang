package com.example.quanlynhahang.Entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import lombok.Getter;
@Getter
public class DistrictResponse {
        @SerializedName("data")
        private DistrictDataResponse data;
        @Getter
        public class DistrictDataResponse {
                @SerializedName("data")
                private List<Province.District> data;
        }
}



