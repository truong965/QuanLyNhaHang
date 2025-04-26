package com.example.quanlynhahang.adapter;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.quanlynhahang.model.Category;
import com.example.quanlynhahang.data.local.SpinnerManager;
import com.example.quanlynhahang.R;
import com.example.quanlynhahang.view.fragments.FoodListFragment;

import java.util.List;



public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder>{
    private Context context;
    private List<Category> categoryList;
    private FragmentManager fragmentManager;
    private SpinnerManager spinnerManager = SpinnerManager.getInstance();
    public  CategoryAdapter(Context context, List<Category> categoryList, FragmentManager fragmentManager){
        this.context = context;
        this.categoryList = categoryList;
        this.fragmentManager = fragmentManager;

    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.viewholder_category,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.name.setText(categoryList.get(position).getName());
        String imagePath = "file:///android_asset/category/" + categoryList.get(position).getImagePath() + ".png";
        Glide.with(context)
                .load(Uri.parse(imagePath))
                .transform(new CenterCrop(), new RoundedCorners(16))
                .into(holder.pic);
        // Handle item click event
        holder.itemView.setOnClickListener(v -> {
            spinnerManager.setSelectedPosition("Category",position);
            Bundle bundle = new Bundle();
            bundle.putString("searchValue", "");
            bundle.putBoolean("isSearch", false);
            FoodListFragment searchFragment = new FoodListFragment();
            searchFragment.setArguments(bundle);
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.frame_layout, searchFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });
    }
    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView pic;
        TextView name;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            pic = itemView.findViewById(R.id.pic);
            name = itemView.findViewById(R.id.title);
        }

    }
}
