package com.example.quanlynhahang.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.quanlynhahang.model.Food;
import com.example.quanlynhahang.data.local.ChangeNumberItemsListener;
import com.example.quanlynhahang.data.local.ManagmentCart;
import com.example.quanlynhahang.R;

import java.util.ArrayList;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Food> foodList;
    private ManagmentCart  managmentCart;
    ChangeNumberItemsListener changeNumberItemsListener;
    public  CartAdapter(Context context, ArrayList<Food> foodList, ChangeNumberItemsListener changeNumberItemsListener){
        this.context = context;
        managmentCart =new ManagmentCart(context);
        this.foodList = foodList;
        this.changeNumberItemsListener = changeNumberItemsListener;
    }
    // Define your ViewHolder class here
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Define your views here
        TextView title, price,totalPrice ;
        EditText quantity;
        ImageView image,minusBtn,plusBtn, deleteBtn;
        public ViewHolder(View itemView) {
            super(itemView);
            // Initialize your views here
            title = itemView.findViewById(R.id.title);
            price = itemView.findViewById(R.id.price);
            quantity = itemView.findViewById(R.id.quantity);
            totalPrice = itemView.findViewById(R.id.totalPrice);
            image = itemView.findViewById(R.id.pic);
            minusBtn = itemView.findViewById(R.id.btn_minus);
            plusBtn = itemView.findViewById(R.id.btn_plus);
            deleteBtn= itemView.findViewById(R.id.delete);
        }
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.viewholder_cart_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // Bind data to your views here
        Food food = foodList.get(position);
        holder.title.setText(food.getTitle());
        holder.price.setText(food.getPrice() + "k VND");
        holder.quantity.setText(food.getNumberInCart() + "");
        holder.totalPrice.setText((food.getPrice() * food.getNumberInCart()) +"k VND");
        String imagePath = "file:///android_asset/food/" + food.getTitle() + ".jpg";
        Glide.with(context)
                .load(imagePath)
                .into(holder.image);
        holder.minusBtn.setOnClickListener(v -> {
            managmentCart.minusNumberItem(foodList, position, new ChangeNumberItemsListener() {
                @Override
                public void change() {
                    notifyDataSetChanged();
                    changeNumberItemsListener.change();
                }
            });
        });
        holder.plusBtn.setOnClickListener(v -> {
            managmentCart.plusNumberItem(foodList, position, new ChangeNumberItemsListener() {
                @Override
                public void change() {
                    notifyDataSetChanged();
                    changeNumberItemsListener.change();
                }
            });
        });
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
