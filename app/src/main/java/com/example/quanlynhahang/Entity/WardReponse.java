package com.example.quanlynhahang.Entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import lombok.Getter;
@Getter
public class WardReponse {
    @SerializedName("data")
    private WardDataReponse data;
    @Getter
    public class WardDataReponse {
        @SerializedName("data")
        private List<Province.Ward> data;}
}


