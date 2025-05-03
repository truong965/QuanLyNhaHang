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
import com.example.quanlynhahang.data.local.ManagmentCart;
import com.example.quanlynhahang.R;
import com.example.quanlynhahang.view.fragments.DetailFoodFragment;

import java.util.List;

public class BestFoodAdapter extends RecyclerView.Adapter<BestFoodAdapter.ViewHolder> {

    private Context context;
    private List<Food> foodList;
    private FragmentManager fragmentManager;
    private ManagmentCart managmentCart;
    public BestFoodAdapter(Context context, List<Food> foodList,FragmentManager fragmentManager) {
        this.context = context;
        this.foodList = foodList;
        this.fragmentManager = fragmentManager;
        managmentCart = new ManagmentCart(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.viewholder_best_deal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Bind data to the views here
        holder.title.setText(foodList.get(position).getTitle());
        holder.price.setText(foodList.get(position).getPrice()+"k VND");
        holder.time.setText(foodList.get(position).getTimeValue()+" phút");
        holder.star.setText(String.valueOf(foodList.get(position).getStar()));
        String imagePath = "file:///android_asset/food/" + foodList.get(position).getTitle() + ".jpg";
        Log.d("ImagePath", "Loading image from: " + imagePath);

        Glide.with(context)
                .load(Uri.parse(imagePath))
                .transform(new CenterCrop(), new RoundedCorners(16))
                .into(holder.image);
        holder.image.setOnClickListener(v -> {
            // Handle image click event
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

        });
        holder.addToCart.setOnClickListener(v->{
            managmentCart.insertFood(foodList.get(position));
        });
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, price, time, star,addToCart;
        ImageView image;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize views here
            title = itemView.findViewById(R.id.title);
            price = itemView.findViewById(R.id.price);
            image = itemView.findViewById(R.id.pic);
            time = itemView.findViewById(R.id.time);
            star = itemView.findViewById(R.id.star);
            addToCart = itemView.findViewById(R.id.addToCart);
        }
    }
}
