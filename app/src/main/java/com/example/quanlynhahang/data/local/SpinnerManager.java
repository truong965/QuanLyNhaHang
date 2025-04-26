package com.example.quanlynhahang.data.local;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpinnerManager{
    private static SpinnerManager instance;
    private Map<String, Integer> spinnerPosition;
    private Map<String, ArrayList<?>> spinnerData;
    private SpinnerManager() {
        spinnerPosition = new HashMap<>();
        spinnerData = new HashMap<>();
    }
    public  static  SpinnerManager getInstance(){
        if(instance == null){
            instance = new SpinnerManager();
        }
        return instance;
    }
    public  void setSelectedPosition(String key, int position){
        spinnerPosition.put(key, position);
    }
    public  int getSelectedPosition(String key){
        return spinnerPosition.getOrDefault(key,0);
    }
    public<T> void setSpinnerData(String key, ArrayList<T> data){
        spinnerData.put(key, new ArrayList<>(data));
    }
    public <T> ArrayList<T> getSpinnerData(String key, Class<T> clazz) {
        List<?> rawList = spinnerData.getOrDefault(key, null);
        if(rawList==null){
            return new ArrayList<>();
        }
        ArrayList<T> typedList = new ArrayList<>();
        for (Object obj : rawList) {
            if (clazz.isInstance(obj)) { // Kiểm tra kiểu trước khi ép
                typedList.add(clazz.cast(obj));
            }
        }
        return typedList;
    }
}
