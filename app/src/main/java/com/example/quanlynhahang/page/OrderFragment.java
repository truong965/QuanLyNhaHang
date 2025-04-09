package com.example.quanlynhahang.page;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.quanlynhahang.Adapter.AddressAdapter;
import com.example.quanlynhahang.Adapter.OrderAdapter;
import com.example.quanlynhahang.Adapter.PaymentItemAdapter;
import com.example.quanlynhahang.Entity.Order;
import com.example.quanlynhahang.Entity.UserResponse;
import com.example.quanlynhahang.ManageState.ChangeNumberItemsListener;
import com.example.quanlynhahang.ManageState.ManageUser;
import com.example.quanlynhahang.ManageState.ManagmentCart;
import com.example.quanlynhahang.R;
import com.example.quanlynhahang.Ultil.CustomToast;
import com.example.quanlynhahang.databinding.FragmentOrderBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.stream.Collectors;


public class OrderFragment extends Fragment {
    private FragmentOrderBinding binding;
    private CustomToast customToast;
    private ArrayList<Order> orderList;
    private OrderAdapter orderAdapter;
    public OrderFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (isAdded()) {
            customToast = new CustomToast(getContext());
            getOrder(orders -> {
                orderList = orders;
                initList();
            });
        } else {
            Log.w("FoodListFragment", "Fragment not attached, skipping init.");
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentOrderBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    private void initList(){
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, new String[]{"Cooking", "Delivering","Delivered", "Done", "Cancelled"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.statusSpinner.setAdapter(adapter);
        binding.statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String selectedStatus = ((String) parent.getItemAtPosition(position)).trim();
                // Handle the selected status here
                if (selectedStatus.equals("Cooking")) {
                    binding.statusIcon.setImageResource(R.drawable.food);
                } else if (selectedStatus.equals("Delivering") || selectedStatus.equals("Delivered")) {
                    binding.statusIcon.setImageResource(R.drawable.delivery);
                } else if (selectedStatus.equals("Done")) {
                    binding.statusIcon.setImageResource(R.drawable.checked);
                } else if (selectedStatus.equals("Cancelled")) {
                    binding.statusIcon.setImageResource(R.drawable.unchecked);
                }
                for (Order order : orderList) {
                    Log.i("order", order.getOrderId() + "   " + order.getStatus());
                }
                orderAdapter.updateData(orderList.stream()
                        .filter(order -> order.getStatus().equals(selectedStatus))
                        .collect(Collectors.toList()));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle case when nothing is selected if needed
            }
        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
        binding.recyclerFood.setLayoutManager(linearLayoutManager);

        orderAdapter = new OrderAdapter(getContext(), orderList.stream()
                .filter(order -> order.getStatus().equals(binding.statusSpinner.getSelectedItem().toString()))
                .collect(Collectors.toList()),getActivity().getSupportFragmentManager());

        binding.recyclerFood.setAdapter(orderAdapter);
    }
    private void getOrder(OnOrderLoadedListener listener) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();
        DatabaseReference dbRef = database.getReference("Orders").child(userId);

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {ArrayList<Order> orderList = new ArrayList<>();

                for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                    Order order = orderSnapshot.getValue(Order.class);
                    if (order != null) {
                        order.setOrderId(orderSnapshot.getKey());
                    }
                    if (order != null) {
//                        Order o= updateStatusOrder(order, userId);
                        updateStatusOrder(order, userId);
                        orderList.add(order);
                    }
                }
                if (orderList.isEmpty()) {
                    customToast.showToast("No orders found", false);
                } else {
                    listener.onAddressLoaded(orderList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), " Failed to retrieve orders", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void updateStatusOrder(Order order, String userID){
//        Log.i("orderId   : ", order.getOrderId() + "   "+ order.getStatus());
        if(order.getStatus().equals("Cancelled")){
            return ;
        }
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference orderRef = database.getReference("Orders")
                .child(userID)
                .child(order.getOrderId());
        boolean change = false;
        long currentTime = System.currentTimeMillis();
        if(currentTime > order.getCookingEndTime() && order.getStatus().equals("Cooking")) {
            order.setStatus("Delivering");
            change = true;
        }
        if(currentTime > order.getDeliveryEndTime() && order.getStatus().equals("Delivering")){
            order.setStatus("Delivered");
            change = true;
        }
        long endTimePlus5minutes = order.getDeliveryEndTime() + 5 * 60 * 1000;
        if(currentTime> endTimePlus5minutes && order.getStatus().equals("Delivering")){
            order.setStatus("Done");
            change = true;
        }
        if(change){
            orderRef.setValue(order)
                    .addOnSuccessListener(aVoid -> {
                        customToast.showToast("Update status successfully", true);
                    })
                    .addOnFailureListener(e -> {
                        customToast.showToast("Failed to update status", false);
                    });
        }
//        return order;
    }
}
interface OnOrderLoadedListener {
    void onAddressLoaded(ArrayList<Order> orders);
}
