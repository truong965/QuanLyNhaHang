package com.example.quanlynhahang.model;

import java.util.ArrayList;

public class AnonymousOrderList {
    private ArrayList<Order> orders;

    public AnonymousOrderList() {
        this.orders = new ArrayList<>();
    }

    public AnonymousOrderList(ArrayList<Order> orders) {
        this.orders = orders;
    }

    public ArrayList<Order> getOrders() {
        return orders;
    }

    public void setOrders(ArrayList<Order> orders) {
        this.orders = orders;
    }
}