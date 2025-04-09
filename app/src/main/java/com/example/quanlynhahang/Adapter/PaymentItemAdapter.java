package com.example.quanlynhahang.Adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.quanlynhahang.Entity.Food;
import com.example.quanlynhahang.ManageState.ChangeNumberItemsListener;
import com.example.quanlynhahang.ManageState.ManagmentCart;
import com.example.quanlynhahang.R;

import java.util.ArrayList;

public class PaymentItemAdapter  extends RecyclerView.Adapter<PaymentItemAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Food> foodList;
    private ManagmentCart managmentCart;
    ChangeNumberItemsListener changeNumberItemsListener;

    public PaymentItemAdapter(Context context, ArrayList<Food> foodList, ChangeNumberItemsListener changeNumberItemsListener) {
        this.context = context;
        managmentCart = new ManagmentCart(context);
        this.foodList = foodList;
        this.changeNumberItemsListener = changeNumberItemsListener;
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Define your views here
        TextView title, price,totalPrice,quantity ;
        ImageView image, deleteBtn;
        public ViewHolder(View itemView) {
            super(itemView);
            // Initialize your views here
            title = itemView.findViewById(R.id.text_food_name);
            price = itemView.findViewById(R.id.text_price);
            quantity = itemView.findViewById(R.id.text_quantity);
            totalPrice = itemView.findViewById(R.id.text_total_price);
            image = itemView.findViewById(R.id.img_food);
            deleteBtn= itemView.findViewById(R.id.btn_remove);
        }
    }


    @NonNull
    @Override
    public PaymentItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_payment, parent, false);
        return new PaymentItemAdapter.ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Bind data to your views here
        Food food = foodList.get(position);
        holder.title.setText(food.getTitle());
        holder.price.setText("Giá: " +food.getPrice() );
        holder.quantity.setText("Số lượng: "+ food.getNumberInCart() );
        holder.totalPrice.setText("Tổng: "+ (food.getPrice() * food.getNumberInCart()) +"$");
        String imagePath = "file:///android_asset/food/" + food.getTitle() + ".jpg";
        Glide.with(context)
                .load(imagePath)
                .into(holder.image);
        holder.deleteBtn.setOnClickListener(v -> {
            managmentCart.removeItem(foodList, position, new ChangeNumberItemsListener() {
                @Override
                public void change() {
                    notifyDataSetChanged();
                    changeNumberItemsListener.change();
                }
            });
        });
    }
    @Override
    public int getItemCount() {
        return foodList.size();
    }
}
