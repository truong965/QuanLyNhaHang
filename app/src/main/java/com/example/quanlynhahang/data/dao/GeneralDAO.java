package com.example.quanlynhahang.data.dao;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.function.Consumer;

public class GeneralDAO{
    private FirebaseDatabase database;
    public  GeneralDAO(FirebaseDatabase database){
        this.database = database;
    }
    public <T> void getById(int id, Class<T> tClass, Consumer<T> callback) {
        Query query = database.getReference(tClass.getSimpleName()).orderByKey().equalTo(String.valueOf(id));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    T result = data.getValue(tClass);
                    callback.accept(result);
                    return;
                }
                callback.accept(null);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.accept(null);
            }
        });
    }

}
