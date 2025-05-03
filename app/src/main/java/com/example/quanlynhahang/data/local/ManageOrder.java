package com.example.quanlynhahang.data.local;

import android.content.Context;
import android.util.Log;

import com.example.quanlynhahang.model.AnonymousOrderList;
import com.example.quanlynhahang.model.Order;

import java.util.ArrayList;

public class ManageOrder {
    private Context context;
    public  ManageOrder (Context context) {
        this.context = context;
        // Constructor
    }
    public boolean saveUpdatedAnonymousOrder(Order updatedOrder) {
        TinyDB tinyDB = new TinyDB(context);
        try {
            AnonymousOrderList orderList = tinyDB.getObject("AnonymousOrders", AnonymousOrderList.class);
            if (orderList != null) {
                ArrayList<Order> orders = orderList.getOrders();
                for (int i = 0; i < orders.size(); i++) {
                    if (orders.get(i).getOrderId().equals(updatedOrder.getOrderId())) {
                        orders.set(i, updatedOrder);
                        break;
                    }
                }
                tinyDB.putObject("AnonymousOrders", orderList);
                return true;
            }
        } catch (Exception e) {
            Log.e("OrderFragment", "Error updating anonymous order", e);
        }
        return false;
    }
    public boolean updateAllAnonymousOrdersToCompleted() {
        TinyDB tinyDB = new TinyDB(context);
        try {
            // Retrieve the current orders
            AnonymousOrderList orderList = tinyDB.getObject("AnonymousOrders", AnonymousOrderList.class);
            if (orderList != null && orderList.getOrders() != null) {
                ArrayList<Order> orders = orderList.getOrders();

                // Update each order's status
                for (Order order : orders) {
                    order.setStatus("Hoàn thành");
                }

                // Save the updated orders back to SharedPreferences
                tinyDB.putObject("AnonymousOrders", orderList);
                return true;
            }
        } catch (Exception e) {
            Log.e("ManageOrder", "Error updating anonymous orders' status", e);
        }
        return false;
    }
}
