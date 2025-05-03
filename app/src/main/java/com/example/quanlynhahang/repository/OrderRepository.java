package com.example.quanlynhahang.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.quanlynhahang.data.firebase.FirebaseDatabaseManager;
import com.example.quanlynhahang.model.Order;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class OrderRepository {

    private final DatabaseReference orderRef;
    public OrderRepository() {
        orderRef = FirebaseDatabaseManager.getDatabase().getReference("Orders");
    }
    public LiveData<Order> getOrderById(String orderId) {
        MutableLiveData<Order> result = new MutableLiveData<>();
        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    if (userSnapshot.hasChild(orderId)) {
                        Order order = userSnapshot.child(orderId).getValue(Order.class);
                        if (order != null) {
                            order.setOrderId(orderId);
                            result.setValue(order);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                result.setValue(null);
            }
        });
        return result;
    }
}
