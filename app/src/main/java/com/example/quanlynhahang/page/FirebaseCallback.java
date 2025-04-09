package com.example.quanlynhahang.page;

import com.example.quanlynhahang.Entity.Food;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public interface FirebaseCallback {
    void onDataLoaded(Set<Integer> foodIdSet);
}