package com.example.quanlynhahang.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlynhahang.Entity.UserResponse;
import com.example.quanlynhahang.R;

import java.util.ArrayList;

import lombok.Getter;

@Getter
public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.ViewHolder> {
    private Context context;
    private ArrayList<UserResponse.LocationUser> location;
    private int selectedPosition = 0;
    public String getSelectedAddress() {
        UserResponse.LocationUser selectedLocation = location.get(selectedPosition);
        return selectedLocation.getStreet().getName() + ", " +
                selectedLocation.getWard() + ", " +
                selectedLocation.getState() + ", " +
                selectedLocation.getCity();
    }
    public AddressAdapter(Context context, ArrayList<UserResponse.LocationUser> location) {
        this.context = context;
        this.location = location;
    }
    public void addLocation(UserResponse.LocationUser locationUser) {
        location.add(locationUser);
        notifyItemInserted(location.size() - 1);
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tinh,huyen,xa,diaChi;
        CardView cardView;
        public ViewHolder(View itemView) {
            super(itemView);
            tinh = itemView.findViewById(R.id.text_province);
            huyen = itemView.findViewById(R.id.text_district);
            xa = itemView.findViewById(R.id.text_ward);
            diaChi = itemView.findViewById(R.id.text_detail);
            cardView = itemView.findViewById(R.id.card_address);
        }
    }

    @NonNull
    @Override
    public AddressAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.address_item, parent, false);
        return new AddressAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserResponse.LocationUser l = location.get(position);
        holder.tinh.setText("Tỉnh: " + l.getCity());
        holder.huyen.setText("Huyện: " + l.getState());
        holder.xa.setText("Xã: " + l.getWard());
        holder.diaChi.setText("Địa chỉ: " + l.getStreet().getName());

        // Set background based on current selection
        if (position == selectedPosition) {
            holder.cardView.setBackgroundResource(R.drawable.success_border);
        } else {
            holder.cardView.setBackground(null);
        }

        holder.itemView.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return;

            // Only update if different
            if (selectedPosition != currentPos) {
                int previous = selectedPosition;
                selectedPosition = currentPos;
                notifyItemChanged(previous);
                notifyItemChanged(selectedPosition);
            }
        });
    }


    @Override
    public int getItemCount() {
        return location.size();
    }
}
