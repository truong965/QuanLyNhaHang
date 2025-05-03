package com.example.quanlynhahang.view.fragments;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.quanlynhahang.R;
import com.example.quanlynhahang.adapter.OrderAdapter;
import com.example.quanlynhahang.data.local.TinyDB;
import com.example.quanlynhahang.databinding.FragmentOrderBinding;
import com.example.quanlynhahang.model.AnonymousOrderList;
import com.example.quanlynhahang.model.Food;
import com.example.quanlynhahang.model.Order;
import com.example.quanlynhahang.utils.CustomToast;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;


public class OrderFragment extends Fragment {
    private FragmentOrderBinding binding;
    private CustomToast customToast;
    private ArrayList<Order> orderList;
    private OrderAdapter orderAdapter;
    private Long startDate = null;
    private Long endDate = null;
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
                setupDatePickers();
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
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, new String[]{"Đang nấu", "Đang vận chuyển","Hoàn thành", "Đã hủy"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.statusSpinner.setAdapter(adapter);
        binding.statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String selectedStatus = ((String) parent.getItemAtPosition(position)).trim();
                // Handle the selected status here
                if (selectedStatus.equals("Đang nấu")) {
                    binding.statusIcon.setImageResource(R.drawable.food);
                } else if (selectedStatus.equals("Đang vận chuyển") || selectedStatus.equals("Delivered")) {
                    binding.statusIcon.setImageResource(R.drawable.delivery);
                } else if (selectedStatus.equals("Hoàn thành")) {
                    binding.statusIcon.setImageResource(R.drawable.checked);
                } else if (selectedStatus.equals("Đã hủy")) {
                    binding.statusIcon.setImageResource(R.drawable.unchecked);
                }
                for (Order order : orderList) {
                    Log.i("order", order.getOrderId() + "   " + order.getStatus());
                }
                orderAdapter.updateData(orderList.stream()
                        .filter(order -> order.getStatus().equals(selectedStatus))
                        .collect(Collectors.toList()));
                if(orderAdapter.getItemCount()==0){
//            customToast.showToast("No orders found", false);
                    binding.khongTimThay.setVisibility(View.VISIBLE);
                }else{
                    binding.khongTimThay.setVisibility(View.GONE);
                }
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
        if(orderList.isEmpty()){
//            customToast.showToast("No orders found", false);
            binding.khongTimThay.setVisibility(View.VISIBLE);
        }else{
            binding.khongTimThay.setVisibility(View.GONE);
        }
    }
    private void getOrder(OnOrderLoadedListener listener) {
        binding.loading.setVisibility(View.VISIBLE);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        ArrayList<Order> orderList = new ArrayList<>();

        // Check if user is anonymous
        if (auth.getCurrentUser() == null || auth.getCurrentUser().isAnonymous()) {
            // Load anonymous orders from SharedPreferences
            TinyDB tinyDB = new TinyDB(getContext());
            try {
                AnonymousOrderList anonymousOrderList = tinyDB.getObject("AnonymousOrders", AnonymousOrderList.class);
                if (anonymousOrderList != null && anonymousOrderList.getOrders() != null) {
                    orderList.addAll(anonymousOrderList.getOrders());

                    // Update status for each order based on time
                    for (Order order : orderList) {
                        updateLocalOrderStatus(order);
                    }

                    // Save updated statuses
                    tinyDB.putObject("AnonymousOrders", anonymousOrderList);
                }
            } catch (Exception e) {
                Log.e("OrderFragment", "Error loading anonymous orders", e);
            }

            customToast.showToast("Bạn đang xem đơn hàng đã lưu cục bộ", true);
            binding.loading.setVisibility(View.GONE);
            listener.onAddressLoaded(orderList);
            return;
        }

        // Regular user - get orders from Firebase
        String userId = auth.getCurrentUser().getUid();
        DatabaseReference dbRef = database.getReference("Orders").child(userId);

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                    // Lấy toàn bộ dữ liệu đơn hàng
                    Order order = orderSnapshot.getValue(Order.class);
                    if (order != null) {
                        order.setOrderId(orderSnapshot.getKey());

                        // Lấy danh sách món ăn từ orderSnapshot
                        List<Food> foodList = new ArrayList<>();
                        DataSnapshot foodsSnapshot = orderSnapshot.child("foodList");
                        for (DataSnapshot foodSnapshot : foodsSnapshot.getChildren()) {
                            Food food = foodSnapshot.getValue(Food.class);
                            if (food != null) {
                                foodList.add(food);
                            }
                        }
                        order.setFoodList(foodList);

                        orderList.add(order);
                    }
                }
                binding.loading.setVisibility(View.GONE);
                listener.onAddressLoaded(orderList);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.loading.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Failed to retrieve orders", Toast.LENGTH_SHORT).show();
                listener.onAddressLoaded(new ArrayList<>());
            }
        });
    }
    private void updateLocalOrderStatus(Order order) {
        if (order.getStatus().equals("Đã hủy")|| order.getStatus().equals("Hoàn thành")) return;
        TinyDB tinyDB = new TinyDB(getContext());
        try {
            AnonymousOrderList orderList = tinyDB.getObject("AnonymousOrders", AnonymousOrderList.class);
            if (orderList != null && orderList.getOrders() != null) {
                ArrayList<Order> orders = orderList.getOrders();
                boolean found = false;

                // Find the matching order by ID
                for (int i = 0; i < orders.size(); i++) {
                    if (orders.get(i).getOrderId().equals(order.getOrderId())) {
                        orders.set(i, order); // Replace with updated order
                        found = true;
                        break;
                    }
                }

                if (found) {
                    // Save updated order list back to SharedPreferences
                    orderList.setOrders(orders);
                    tinyDB.putObject("AnonymousOrders", orderList);
                    Log.d("OrderFragment", "Updated order: " + order.getOrderId() + " to status: " + order.getStatus());
                }
            }
        } catch (Exception e) {
            Log.e("OrderFragment", "Error updating local order: " + e.getMessage(), e);
        }
    }

//    private void getOrder(OnOrderLoadedListener listener) {
//        FirebaseDatabase database = FirebaseDatabase.getInstance();
//        FirebaseAuth auth = FirebaseAuth.getInstance();
//
//        // Handle anonymous users by passing an empty list but still initializing UI
//        if(auth.getCurrentUser() == null || auth.getCurrentUser().isAnonymous()) {
//            customToast.showToast("Bạn nên đăng nhập để tránh mất dữ liệu", false);
//            listener.onAddressLoaded(new ArrayList<>()); // Pass empty list to initialize UI
//            return;
//        }
//        String userId = auth.getCurrentUser().getUid();
//        DatabaseReference dbRef = database.getReference("Orders").child(userId);
//
//        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                ArrayList<Order> orderList = new ArrayList<>();
//
//                for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
//                    Order order = orderSnapshot.getValue(Order.class);
//                    if (order != null) {
//                        order.setOrderId(orderSnapshot.getKey());
//                        updateStatusOrder(order, userId);
//                        orderList.add(order);
//                    }
//                }
//
//                listener.onAddressLoaded(orderList);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(getContext(), "Failed to retrieve orders", Toast.LENGTH_SHORT).show();
//                listener.onAddressLoaded(new ArrayList<>()); // Still initialize UI on error
//            }
//        });
//    }
//    private void updateStatusOrder(Order order, String userID){
////        Log.i("orderId   : ", order.getOrderId() + "   "+ order.getStatus());
//        if(order.getStatus().equals("Cancelled")){
//            return ;
//        }
//        FirebaseDatabase database = FirebaseDatabase.getInstance();
//        DatabaseReference orderRef = database.getReference("Orders")
//                .child(userID)
//                .child(order.getOrderId());
//        boolean change = false;
//        long currentTime = System.currentTimeMillis();
//        if(currentTime > order.getCookingEndTime() && order.getStatus().equals("Cooking")) {
//            order.setStatus("Delivering");
//            change = true;
//        }
//        if(currentTime > order.getDeliveryEndTime() && order.getStatus().equals("Delivering")){
//            order.setStatus("Delivered");
//            change = true;
//        }
//        long endTimePlus5minutes = order.getDeliveryEndTime() + 5 * 60 * 1000;
//        if(currentTime> endTimePlus5minutes && order.getStatus().equals("Delivering")){
//            order.setStatus("Done");
//            change = true;
//        }
//        if(change){
//            orderRef.setValue(order)
//                    .addOnSuccessListener(aVoid -> {
//                        customToast.showToast("Update status successfully", true);
//                    })
//                    .addOnFailureListener(e -> {
//                        customToast.showToast("Failed to update status", false);
//                    });
//        }
////        return order;
//    }
        private void setupDatePickers() {

            setupConfirmButton();
            // Xử lý chọbn ngày bắt đầu
            binding.startDateEditText.setOnClickListener(v -> showDatePicker(true));

            // Xử lý chọn ngày kết thúc
            binding.endDateEditText.setOnClickListener(v -> showDatePicker(false));
        }
        private void showDatePicker(boolean isStartDate) {
            // Tạo constraints để ngày kết thúc không thể trước ngày bắt đầu
            CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();

            if (isStartDate && endDate != null) {
                // Nếu đang chọn ngày bắt đầu và đã có ngày kết thúc
                constraintsBuilder.setEnd(endDate);
            } else if (!isStartDate && startDate != null) {
                // Nếu đang chọn ngày kết thúc và đã có ngày bắt đầu
                constraintsBuilder.setStart(startDate);
                constraintsBuilder.setValidator(DateValidatorPointForward.from(startDate));
            }

            MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
            builder.setTitleText(isStartDate ? "Chọn ngày bắt đầu" : "Chọn ngày kết thúc");
            builder.setCalendarConstraints(constraintsBuilder.build());

            MaterialDatePicker<Long> picker = builder.build();
            picker.addOnPositiveButtonClickListener(selection -> {
                // Chuyển đổi từ UTC về timezone local
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                calendar.setTimeInMillis(selection);

                String formattedDate = DateFormat.format("dd/MM/yyyy", calendar).toString();

                if (isStartDate) {
                    startDate = selection;
                    binding.startDateEditText.setText(formattedDate);

                    // Reset ngày kết thúc nếu trước ngày bắt đầu mới
                    if (endDate != null && endDate < startDate) {
                        endDate = null;
                        binding.endDateEditText.setText("");
                    }
                } else {
                    endDate = selection;
                    binding.endDateEditText.setText(formattedDate);
                }

                // Kích hoạt nút xác nhận khi có cả 2 ngày
                binding.confirmButton.setEnabled(startDate != null && endDate != null);
            });
            picker.show(getParentFragmentManager(), picker.toString());
        }
    private void setupConfirmButton() {
        binding.confirmButton.setOnClickListener(v -> {
            // Get current selected status
            String selectedStatus = binding.statusSpinner.getSelectedItem().toString();

            // Start with base filter by status
            List<Order> filteredOrders = orderList.stream()
                    .filter(order -> order.getStatus().equals(selectedStatus))
                    .collect(Collectors.toList());

            // Apply title search if provided
            String searchTitle = binding.searchTitleEditText.getText().toString().trim().toLowerCase();
            if (!searchTitle.isEmpty()) {
                filteredOrders = filteredOrders.stream()
                        .filter(order -> order.getFoodList().stream()
                                .anyMatch(food -> food.getTitle().toLowerCase().contains(searchTitle)))
                        .collect(Collectors.toList());
            }

            // Apply date filtering based on available dates
            if (startDate != null || endDate != null) {
                final long currentTime = System.currentTimeMillis();

                if (startDate != null && endDate != null) {
                    // Both dates provided - filter within range
                    final long adjustedEndDate = endDate + (24 * 60 * 60 * 1000) - 1; // Include full day

                    filteredOrders = filteredOrders.stream()
                            .filter(order -> {
                                long orderTime = order.getCreateTime();
                                return orderTime >= startDate && orderTime <= adjustedEndDate;
                            })
                            .collect(Collectors.toList());

                    // Show date range confirmation
                    showDateRangeToast(startDate, endDate);
                }
                else if (startDate != null) {
                    // Only start date - filter from start date forward
                    filteredOrders = filteredOrders.stream()
                            .filter(order -> order.getCreateTime() >= startDate)
                            .collect(Collectors.toList());

                    customToast.showToast("Tìm kiếm từ " +
                            DateFormat.format("dd/MM/yyyy", startDate).toString() + " đến nay", true);
                }
                else {
                    // Only end date - filter backward from end date
                    final long adjustedEndDate = endDate + (24 * 60 * 60 * 1000) - 1; // Include full day

                    filteredOrders = filteredOrders.stream()
                            .filter(order -> order.getCreateTime() <= adjustedEndDate)
                            .collect(Collectors.toList());

                    customToast.showToast("Tìm kiếm đến " +
                            DateFormat.format("dd/MM/yyyy", endDate).toString(), true);
                }
            }

            // Update the adapter with filtered results
            orderAdapter.updateData(filteredOrders);

            // Show/hide "No results found" message
            binding.khongTimThay.setVisibility(filteredOrders.isEmpty() ?
                    View.VISIBLE : View.GONE);
        });
    }

    private void showDateRangeToast(long startDate, long endDate) {
        Calendar startCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        startCal.setTimeInMillis(startDate);

        Calendar endCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        endCal.setTimeInMillis(endDate);

        String message = String.format("Tìm kiếm từ %s đến %s",
                DateFormat.format("dd/MM/yyyy", startCal).toString(),
                DateFormat.format("dd/MM/yyyy", endCal).toString());
        customToast.showToast(message, true);
    }

}
interface OnOrderLoadedListener {
    void onAddressLoaded(ArrayList<Order> orders);
}
