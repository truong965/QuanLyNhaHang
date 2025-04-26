package com.example.quanlynhahang.view.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.example.quanlynhahang.adapter.PaymentItemAdapter;
import com.example.quanlynhahang.model.Food;
import com.example.quanlynhahang.model.Order;
import com.example.quanlynhahang.model.UserResponse;
import com.example.quanlynhahang.data.local.ChangeNumberItemsListener;
import com.example.quanlynhahang.data.local.ManageUser;
import com.example.quanlynhahang.data.local.ManagmentCart;
import com.example.quanlynhahang.R;
import com.example.quanlynhahang.utils.CustomToast;
import com.example.quanlynhahang.databinding.FragmentPaymentBinding;
import com.example.quanlynhahang.view.activities.simple_vnpay;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;


public class PaymentFragment extends Fragment {
    private FragmentPaymentBinding binding;
    private RecyclerView.Adapter adapter;
    private CustomToast customToast;
    private ManagmentCart managmentCart;
    private String address="";
    public PaymentFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 📨 Get arguments from bundle
        Bundle bundle = getArguments();
        if (bundle != null) {
            address = bundle.getString("diaChi");
            // Do something with diaChi, like displaying it in a TextView
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPaymentBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (isAdded()) {
            customToast = new CustomToast(getContext());
            managmentCart = new ManagmentCart(getContext());
            calculateTotal();
            initList();
        } else {
            Log.w("FoodListFragment", "Fragment not attached, skipping init.");
        }
    }
    private void initList(){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
        binding.recyclerFood.setLayoutManager(linearLayoutManager);
        adapter = new PaymentItemAdapter(getContext(), managmentCart.getListCart(), new ChangeNumberItemsListener() {
            @Override
            public void change() {
                calculateTotal();
            }
        });

        binding.recyclerFood.setAdapter(adapter);
        binding.back.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item,
                new String[]{"Tiền mặt", "Zalo Pay"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerPaymentMethod.setAdapter(adapter);

        binding.btnPlaceOrder.setOnClickListener(v -> {
            if(binding.textAddress.getText().toString().isEmpty() || binding.textAddress.getText().toString().equals("trống")){
                customToast.showToast("❌ Vui lòng chọn địa chỉ giao hàng", false);
                return;
            }
            String selectedMethod = binding.spinnerPaymentMethod.getSelectedItem().toString();
            Order order = addOrder();
            if (selectedMethod.equals("Tiền mặt")) {
                showDialog(order);
            } else {
//                Log.i("orderId", order.getOrderId());
                Intent intent = new Intent( getContext(), simple_vnpay.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("order", order);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        if(address.isEmpty()){
            UserResponse.User user= ManageUser.getUser();
            if(user ==null){
                address= "trống";
            }else{
                UserResponse.LocationUser locationUser = user.getLocation();
                if(address.isEmpty()){
                    address = locationUser.getStreet() + ", " + locationUser.getWard() + ", " +
                            locationUser.getState() + ", " + locationUser.getCity();
                }
            }
        }
        binding.textAddress.setText(address);
        binding.addAddress.setOnClickListener(v -> {
            DeliveryAddressFragment addressFragment = new DeliveryAddressFragment();
            Fragment currentFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.frame_layout);
            if (currentFragment != null && currentFragment.getClass().equals(addressFragment.getClass())) {
                Log.i("replaceFragment", "Fragment already displayed: " + addressFragment.getClass().getSimpleName());
                return; // 🚀 Skip replacement if it's the same fragment
            }
            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame_layout, addressFragment);
//            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });
    }
    private void calculateTotal(){
        double total = Math.round(managmentCart.getTotalFee());
        binding.textTotalPrice.setText(total + " $");
    }
    private Order addOrder() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();
        DatabaseReference dbRef = database.getReference("Orders").child(userId);

        // Cart list
        ArrayList<Food> foodList = new ArrayList<>();
        int maxTimeForCooking =0;
        for (Food f : managmentCart.getListCart()) {
            foodList.add(new Food(f.getId(), f.getTitle(), f.getPrice(), f.getNumberInCart()));
            if (f.getTimeValue() > maxTimeForCooking) {
                maxTimeForCooking = f.getTimeValue();
            }
        }

        // Times
        long now = System.currentTimeMillis();
        long cookingEnd = now + (long) maxTimeForCooking * 60 * 1000;
        long deliveryEnd = cookingEnd + 10 * 60 * 1000; // 10 minutes

        // User input
        String address = binding.textAddress.getText().toString();
        String paymentMethod = binding.spinnerPaymentMethod.getSelectedItem().toString();

        // Create order object
        Order order = new Order(address, paymentMethod, foodList, now, cookingEnd, deliveryEnd, "Cooking");

        // Push order
        String orderId = dbRef.push().getKey();
        order.setOrderId(orderId);
        return order;
//        if (orderId != null) {
//            dbRef.child(orderId).setValue(order)
//                    .addOnSuccessListener(aVoid -> customToast.showToast("✅ Đặt hàng thành công", true))
//                    .addOnFailureListener(e -> customToast.showToast("❌ Đặt hàng thất bại", false));
//        }
    }
    private void showDialog(Order order){
        new AlertDialog.Builder(getContext())
                .setTitle("Thông báo")
                .setMessage("Xác nhận đơn hàng \n Đơn hàng của bạn sẽ đến vào lúc: " + new SimpleDateFormat("HH:mm:ss").format(order.getDeliveryEndTime()) )
                .setIcon(android.R.drawable.ic_lock_idle_alarm)
                .setPositiveButton("OK", (dialog, which) -> {
                    managmentCart.clearCart();

                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    String userId = auth.getCurrentUser().getUid();
                    DatabaseReference dbRef = database.getReference("Orders").child(userId);

                    dbRef.child(order.getOrderId()).setValue(order)
                            .addOnSuccessListener(aVoid -> {
                                managmentCart.clearCart();
                                customToast.showToast("✅ Đặt hàng thành công", true);
                                HomeFragment homeFragment = new HomeFragment();
                                Fragment currentFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.frame_layout);
                                if (currentFragment != null && currentFragment.getClass().equals(homeFragment.getClass())) {
                                    Log.i("replaceFragment", "Fragment already displayed: " + homeFragment.getClass().getSimpleName());
                                    return; // 🚀 Skip replacement if it's the same fragment
                                }
                                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                                fragmentTransaction.replace(R.id.frame_layout, homeFragment);
                                fragmentTransaction.commit();
                            })
                            .addOnFailureListener(e -> customToast.showToast("❌ Đặt hàng thất bại", false));
                    dialog.dismiss();
                })
                .setNegativeButton("Hủy",(dialog, which)-> dialog.dismiss())
                .show();
    }


}