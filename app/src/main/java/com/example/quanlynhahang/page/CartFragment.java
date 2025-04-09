package com.example.quanlynhahang.page;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.quanlynhahang.Adapter.CartAdapter;
import com.example.quanlynhahang.Entity.Food;
import com.example.quanlynhahang.ManageState.ChangeNumberItemsListener;
import com.example.quanlynhahang.ManageState.ManagmentCart;
import com.example.quanlynhahang.R;
import com.example.quanlynhahang.Ultil.CustomToast;
import com.example.quanlynhahang.databinding.FragmentCartBinding;
import com.example.quanlynhahang.databinding.FragmentFoodListBinding;

public class CartFragment extends Fragment {
    private FragmentCartBinding binding;
    private RecyclerView.Adapter adapter;
    private ManagmentCart managmentCart;
    private CustomToast customToast;
    public CartFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
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
            init();
        } else {
            Log.w("FoodListFragment", "Fragment not attached, skipping init.");
        }
    }
    private void initList(){
        if(managmentCart.getListCart().isEmpty()){
            binding.empty.getRoot().setVisibility(View.VISIBLE);
            binding.foodInCart.setVisibility(View.GONE);
            binding.loading.setVisibility(View.GONE);


            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) binding.divider.getLayoutParams();
            layoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
            layoutParams.topToBottom =R.id.emptyLinear;
            binding.divider.setLayoutParams(layoutParams);

        }else{
            binding.empty.getRoot().setVisibility(View.GONE);
            binding.foodInCart.setVisibility(View.VISIBLE);

            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) binding.divider.getLayoutParams();
            layoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
            layoutParams.topToBottom =R.id.foodInCart;
            binding.divider.setLayoutParams(layoutParams);
        }
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
        binding.foodInCart.setLayoutManager(linearLayoutManager);
        adapter = new CartAdapter(getContext(), managmentCart.getListCart(), new ChangeNumberItemsListener() {
            @Override
            public void change() {
                calculateTotal();
            }
        });

        binding.foodInCart.setAdapter(adapter);
        binding.loading.setVisibility(View.GONE);
        binding.back.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
    };
    private void calculateTotal(){
        double total = Math.round(managmentCart.getTotalFee());
        binding.totalPrice.setText(total + " $");
        int quantity = managmentCart.getListCart().stream()
                .mapToInt(Food::getNumberInCart)
                .sum();
        binding.quantity.setText(String.valueOf( quantity));
    }
    private void init() {
        binding.thanhToan.setOnClickListener(v -> {
            PaymentFragment paymentFragment = new PaymentFragment();
            Fragment currentFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.frame_layout);
            if (currentFragment != null && currentFragment.getClass().equals(paymentFragment.getClass())) {
                Log.i("replaceFragment", "Fragment already displayed: " + paymentFragment.getClass().getSimpleName());
                return; // 🚀 Skip replacement if it's the same fragment
            }
            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frame_layout, paymentFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });
    }
}