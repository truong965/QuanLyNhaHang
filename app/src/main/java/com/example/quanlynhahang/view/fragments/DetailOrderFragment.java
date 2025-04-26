package com.example.quanlynhahang.view.fragments;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.quanlynhahang.adapter.DetailOrderAdapter;
import com.example.quanlynhahang.model.Food;
import com.example.quanlynhahang.model.Order;
import com.example.quanlynhahang.R;
import com.example.quanlynhahang.utils.CustomToast;
import com.example.quanlynhahang.databinding.FragmentDetailOrderBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;


public class DetailOrderFragment extends Fragment {

    private FragmentDetailOrderBinding binding;
    private CustomToast customToast;
    private Order order;
    private String reason;
    public DetailOrderFragment() {

        // Required empty public constructor
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            order = getArguments().getParcelable("order");
        }

    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (isAdded()) {
            customToast = new CustomToast(getContext());
            initList();
        } else {
            Log.w("FoodListFragment", "Fragment not attached, skipping init.");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentDetailOrderBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    private void initList(){
        if(order==null){
            customToast.showToast("Không tìm thấy đơn hàng", false);
            return;
        }
        binding.statusText.setText(order.getStatus());
        if (order.getStatus().equals("Cooking")) {
            binding.statusIcon.setImageResource(R.drawable.food);
        } else if (order.getStatus().equals("Delivering")|| order.getStatus().equals("Delivered")) {
            binding.statusIcon.setImageResource(R.drawable.delivery);
        } else if (order.getStatus().equals("Done")) {
            binding.statusIcon.setImageResource(R.drawable.checked);
        } else if (order.getStatus().equals("Cancelled")) {
            binding.statusIcon.setImageResource(R.drawable.unchecked);
        }
        binding.textAddress.setText(order.getAddress());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
        binding.recyclerFood.setLayoutManager(linearLayoutManager);
        DetailOrderAdapter detailOrderAdapter = new DetailOrderAdapter(getContext(), (ArrayList<Food>) order.getFoodList());
        binding.recyclerFood.setAdapter(detailOrderAdapter);
        double totalPrice = order.getFoodList().stream().mapToDouble(food -> food.getPrice() * food.getNumberInCart()).sum();
        binding.textTotalPrice.setText(totalPrice + " $");
        binding.paymentMethod.setText(order.getPaymentMethod());
        binding.back.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        if(!order.getStatus().equals("Done") && !order.getStatus().equals("Cancelled")) {
            binding.editReason.setVisibility(View.GONE);
            binding.titleReason.setVisibility(View.GONE);
            binding.cancelOrder.setVisibility(View.VISIBLE);
            binding.cancelOrder.setOnClickListener(v -> {
                showDialog();
            });
        }else if(order.getStatus().equals("Delivered")){
            binding.editReason.setVisibility(View.GONE);
            binding.titleReason.setVisibility(View.GONE);
            binding.cancelOrder.setVisibility(View.VISIBLE);
            Button confirmButton = binding.cancelOrder;
            confirmButton.setBackgroundColor(getResources().getColor(R.color.teal_200));
            confirmButton.setText("Xác nhận đã nhận hàng");
            binding.cancelOrder.setOnClickListener(v->{
                if(updateDoneOrder(order)){
                    binding.cancelOrder.setVisibility(View.GONE);
                    binding.statusText.setText(order.getStatus());
                }
            });
        }
        else{
            binding.editReason.setVisibility(View.VISIBLE);
            binding.titleReason.setVisibility(View.VISIBLE);
            binding.editReason.setText(order.getReasonForCancel()!=null?order.getReasonForCancel():"Chưa có lý do");
            binding.cancelOrder.setVisibility(View.GONE);

            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) binding.editReason.getLayoutParams();
            layoutParams.setMargins(0, 0, 0, 200);
            binding.editReason.setLayoutParams(layoutParams);
        }
    }
    private boolean updateDoneOrder(Order order){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();
        DatabaseReference orderRef = database.getReference("Orders")
                .child(userId)
                .child(order.getOrderId());
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "Done");
        updates.put("deliveryEndTime", System.currentTimeMillis()); // ✅ Add cancel time field
        AtomicBoolean isSuccess = new AtomicBoolean(false);
        orderRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    customToast.showToast("✅ Order cancelled with reason", true);
                    isSuccess.set(true);
                })
                .addOnFailureListener(e ->
                        customToast.showToast("❌ Failed to update order", false)
                );
        return isSuccess.get();
    }
    private boolean updateOrderStatusWithReason(Order order, String reason) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();
        DatabaseReference orderRef = database.getReference("Orders")
                .child(userId)
                .child(order.getOrderId());
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "Cancelled");
        updates.put("reasonForCancel", reason); // ✅ Add reason field
        updates.put("deliveryEndTime", System.currentTimeMillis()); // ✅ Add cancel time field
        AtomicBoolean isSuccess = new AtomicBoolean(false);
        orderRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    customToast.showToast("✅ Order cancelled with reason", true);
                    isSuccess.set(true);
                })
                .addOnFailureListener(e ->
                        customToast.showToast("❌ Failed to update order", false)
                );
        return isSuccess.get();
    }
    private void showDialog(){
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.reason_dialog);
        initDialog(dialog);
        dialog.setCanceledOnTouchOutside(true);
        dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT, // or WRAP_CONTENT
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        dialog.show();
    }
    private void initDialog(Dialog view){
        view.findViewById(R.id.buttonCancel).setOnClickListener(v -> {
            view.dismiss();
        });
        view.findViewById(R.id.buttonConfirm).setOnClickListener(v->{
            EditText editReason = view.findViewById(R.id.editReason);
            if(editReason.getText().toString().isEmpty()){
                customToast.showToast("Vui lòng nhập lý do", false);
                return;
            }else{
                reason= editReason.getText().toString();
                if(!updateOrderStatusWithReason(order,reason)){
                    customToast.showToast("Hủy đơn hàng không thành công", false);
                    return;
                }
                order.setStatus("Cancel");
                customToast.showToast("Đã hủy đơn hàng", true);
                binding.statusText.setText(order.getStatus());
                binding.cancelOrder.setVisibility(View.GONE);
                view.dismiss();
            }
        });
    }
}
interface OnInputReasonListener {
    void sendInput(String input);
}