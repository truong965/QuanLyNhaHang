package com.example.quanlynhahang.adapter;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlynhahang.model.Order;
import com.example.quanlynhahang.R;
import com.example.quanlynhahang.view.fragments.DetailOrderFragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {
    private Context context;
    private List<Order> orderList;
    private FragmentManager fragmentManager;
    public OrderAdapter(Context context, List<Order> orderList, FragmentManager fragmentManager) {
        this.context = context;
        this.orderList = orderList;
        this.fragmentManager = fragmentManager;
    }
    public void updateData(List<Order> newOrders) {
        this.orderList.clear();
        this.orderList.addAll(newOrders);
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.order_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.bind(order);
        holder.itemView.setOnClickListener(v-> {
                // Handle item click if needed
                DetailOrderFragment fragment = new DetailOrderFragment();
                Bundle args = new Bundle();
                args.putParcelable("order", orderList.get(position)); // Pass food object
                fragment.setArguments(args);

                // 🔍 Check if the current fragment is the same as the new one
                Fragment currentFragment = fragmentManager.findFragmentById(R.id.frame_layout);
                if (currentFragment != null && currentFragment.getClass().equals(fragment.getClass())) {
                    Log.i("replaceFragment", "Fragment already displayed: " + fragment.getClass().getSimpleName());
                    return; // 🚀 Skip replacement if it's the same fragment
                }

                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.frame_layout, fragment);
                fragmentTransaction.addToBackStack(null); // Add to back stack if you want to allow back navigation
                fragmentTransaction.commit();
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView textNumItems,textTotalPrice,textCreationDate,textCompletionDate;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textNumItems = itemView.findViewById(R.id.textNumItems);
            textTotalPrice = itemView.findViewById(R.id.textTotalPrice);
            textCreationDate = itemView.findViewById(R.id.textCreationDate);
            textCompletionDate = itemView.findViewById(R.id.textCompletionDate);
        }

        public void bind(Order order) {
            textNumItems.setText("Số lượng món ăn: " + order.getFoodList().size());
            textTotalPrice.setText("Tổng tiền: " + order.getFoodList().stream()
                    .mapToDouble(food -> food.getPrice() * food.getNumberInCart())
                    .sum()+"k VND");
            textCreationDate.setText("Ngày đặt hàng: " + formatTimestamp(order.getCreateTime()));
            textCompletionDate.setText("Ngày hoàn thành: " + formatTimestamp(order.getDeliveryEndTime()));
        }
        private String formatTimestamp(long timestamp) {
            if(timestamp == 0) {
                return "Chưa xác định";
            }
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss", Locale.getDefault());
            Date date = new Date(timestamp);
            return sdf.format(date);
        }

    }
}
