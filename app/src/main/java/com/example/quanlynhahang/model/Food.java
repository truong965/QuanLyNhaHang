package com.example.quanlynhahang.model;

import android.os.Parcel;
import android.os.Parcelable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString

public class Food implements Parcelable {
    private boolean BestFood;
    private int CategoryId;
    private Category Category;
    private String Description;
    private int Id;
    private String ImagePath;
    private int LocationId;
    private  Location Location;
    private double Price;
    private int PriceId;
    private Price PriceObject;
    private  double Star;
    private int TimeId;
    private  Time Time;
    private  int TimeValue;
    private String Title;
    private  int numberInCart;

    protected Food(Parcel in) {
        BestFood = in.readByte() != 0;
        Description = in.readString();
        Id = in.readInt();
        ImagePath = in.readString();
        Price = in.readDouble();
        Star = in.readDouble();
        TimeValue = in.readInt();
        Title = in.readString();
        numberInCart = in.readInt();

    }

    public Food(int id, String title, double price, int numberInCart) {
        this.Id = id;
        this.Title = title;
        this.Price = price;
        this.numberInCart = numberInCart;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (BestFood ? 1 : 0));
        dest.writeString(Description);
        dest.writeInt(Id);
        dest.writeString(ImagePath);
        dest.writeDouble(Price);
        dest.writeDouble(Star);
        dest.writeInt(TimeValue);
        dest.writeString(Title);
        dest.writeInt(numberInCart);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Food> CREATOR = new Creator<Food>() {
        @Override
        public Food createFromParcel(Parcel in) {
            return new Food(in);
        }

        @Override
        public Food[] newArray(int size) {
            return new Food[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Food food = (Food) o;
        return Id == food.Id;
    }
}
