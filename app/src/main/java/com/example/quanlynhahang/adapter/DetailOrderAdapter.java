package com.example.quanlynhahang.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.quanlynhahang.model.Food;
import com.example.quanlynhahang.R;

import java.util.ArrayList;

public class DetailOrderAdapter extends RecyclerView.Adapter<DetailOrderAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Food> foodList;
    public DetailOrderAdapter(Context context, ArrayList<Food> foodList) {
        this.context = context;
        this.foodList = foodList;

    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Define your views here
        TextView title, price,totalPrice,quantity ;
        ImageView image;
        public ViewHolder(View itemView) {
            super(itemView);
            // Initialize your views here
            title = itemView.findViewById(R.id.text_food_name);
            price = itemView.findViewById(R.id.text_price);
            quantity = itemView.findViewById(R.id.text_quantity);
            totalPrice = itemView.findViewById(R.id.text_total_price);
            image = itemView.findViewById(R.id.img_food);
        }
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.detail_order_item, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Bind data to your views here
        Food food = foodList.get(position);
        holder.title.setText(food.getTitle());
        holder.price.setText("Giá: " +food.getPrice() );
        holder.quantity.setText("Số lượng: "+ food.getNumberInCart() );
        holder.totalPrice.setText("Tổng: "+ (food.getPrice() * food.getNumberInCart()) +"k VND");
        String imagePath = "file:///android_asset/food/" + food.getTitle() + ".jpg";
        Glide.with(context)
                .load(imagePath)
                .into(holder.image);
    }
    @Override
    public int getItemCount() {
        return foodList.size();
    }
}
