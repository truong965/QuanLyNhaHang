package com.example.quanlynhahang.nhap;

import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import android.view.LayoutInflater;
import android.widget.TextView;
import androidx.annotation.NonNull;

public class adapter extends RecyclerView.Adapter<adapter.ViewHolder> {
    private ArrayList<String> data;

    public adapter(ArrayList<String> data) {
        this.data = new ArrayList<>(data); // Ensure a copy is made to avoid modifying external lists
    }

    public void setData(ArrayList<String> newData) {
        int startPosition = data.size();
        data.addAll(newData); // Append new data instead of replacing the list
        notifyItemRangeInserted(startPosition, newData.size()); // Notify adapter efficiently
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textView.setText(data.get(position) +"   "+ position); // Bind data to the ViewHolder
    }

    @Override
    public int getItemCount() {
        return data != null ? data.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1); // Get TextView reference
        }
    }
}
