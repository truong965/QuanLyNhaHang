package com.example.quanlynhahang.Entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Order implements Parcelable {
    private String orderId;
    private String address;
    private String paymentMethod;
    private List<Food> foodList;
    private long createTime;         // Now
    private long cookingEndTime;     // max time of food
    private long deliveryEndTime;    // +10 mins after cooking
    private String status; // Cooking, Delivering, Done, Cancelled
    private String reasonForCancel; // Optional
    public Order(String address, String paymentMethod, List<Food> foodList, long createTime, long cookingEndTime, long deliveryEndTime, String status) {
        this.address = address;
        this.paymentMethod = paymentMethod;
        this.foodList = foodList;
        this.createTime = createTime;
        this.cookingEndTime = cookingEndTime;
        this.deliveryEndTime = deliveryEndTime;
        this.status = status;
    }
    // Add getters and setters here
    protected Order(Parcel in) {
        orderId = in.readString();
        address = in.readString();
        paymentMethod = in.readString();
        foodList = in.createTypedArrayList(Food.CREATOR);
        createTime = in.readLong();
        cookingEndTime = in.readLong();
        deliveryEndTime = in.readLong();
        status = in.readString();
    }
    public static final Creator<Order> CREATOR = new Creator<Order>() {
        @Override
        public Order createFromParcel(Parcel in) {
            return new Order(in);
        }

        @Override
        public Order[] newArray(int size) {
            return new Order[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(orderId);
        parcel.writeString(address);
        parcel.writeString(paymentMethod);
        parcel.writeTypedList(foodList);
        parcel.writeLong(createTime);
        parcel.writeLong(cookingEndTime);
        parcel.writeLong(deliveryEndTime);
        parcel.writeString(status);
    }
}
