package com.example.quanlynhahang.adapter;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.quanlynhahang.model.Food;
import com.example.quanlynhahang.R;
import com.example.quanlynhahang.view.fragments.DetailFoodFragment;

import java.util.ArrayList;
import java.util.List;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Food> foodList;
    private FragmentManager fragmentManager;

    public void addData(ArrayList<Food> newFoods) {
        int startPosition = foodList.size();
        foodList.addAll(newFoods); // Append new data instead of replacing the list
        notifyItemRangeInserted(startPosition, newFoods.size()); // Notify adapter efficiently
        notifyDataSetChanged();
    }
    public void clearData() {
        foodList.clear();
        notifyDataSetChanged();
    }
    public FoodAdapter(Context context, ArrayList<Food> foodList, FragmentManager fragmentManager) {
        this.context = context;
        this.foodList = foodList;
        this.fragmentManager = fragmentManager;
    }
    public void updateData(List<Food> newFoods) {
        this.foodList.clear();
        this.foodList.addAll(newFoods);
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public FoodAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.viewholder_food_item, parent, false);
        return new FoodAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodAdapter.ViewHolder holder, int position) {
        holder.title.setText(foodList.get(position).getTitle());
        holder.price.setText(foodList.get(position).getPrice()+"$");
        holder.time.setText(foodList.get(position).getTimeValue()+" min");
        holder.star.setText(String.valueOf(foodList.get(position).getStar()));
        String imagePath = "file:///android_asset/food/" + foodList.get(position).getTitle() + ".jpg";
        Glide.with(context)
                .load(Uri.parse(imagePath))
                .transform(new CenterCrop(), new RoundedCorners(16))
                .into(holder.pic);
        holder.itemView.setOnClickListener(v -> {
            DetailFoodFragment fragment = new DetailFoodFragment();
            Bundle args = new Bundle();
            args.putParcelable("food", foodList.get(position)); // Pass food object
            fragment.setArguments(args);

            // 🔍 Check if the current fragment is the same as the new one
            Fragment currentFragment = fragmentManager.findFragmentById(R.id.frame_layout);
            if (currentFragment != null && currentFragment.getClass().equals(fragment.getClass())) {
                Log.i("replaceFragment", "Fragment already displayed: " + fragment.getClass().getSimpleName());
                return; // 🚀 Skip replacement if it's the same fragment
            }

            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frame_layout, fragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
//            Intent intent = new Intent(context, DetailFood.class);
//            intent.putExtra("food",foodList.get(position)); // Pass category name as search value
//            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, price, time, star;
        ImageView pic;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize views here
            title = itemView.findViewById(R.id.title);
            price = itemView.findViewById(R.id.price);
            pic = itemView.findViewById(R.id.pic);
            time = itemView.findViewById(R.id.time);
            star = itemView.findViewById(R.id.star);
        }
    }
}
