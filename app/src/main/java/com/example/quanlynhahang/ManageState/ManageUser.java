package com.example.quanlynhahang.ManageState;

import com.example.quanlynhahang.Entity.Province;
import com.example.quanlynhahang.Entity.UserResponse;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class ManageUser {
    // Method to get user
    @Getter
    @Setter
    private static UserResponse.User user;
    @Getter
    @Setter
    private static List<Province> provinces;
    @Getter
    @Setter
    private static List<Province.District> districts;
    @Getter
    @Setter
    private static List<Province.Ward> wards;
    private int positionProvince=-1;
    private int positionDistrict=-1;
    private int positionWard=-1;


}
