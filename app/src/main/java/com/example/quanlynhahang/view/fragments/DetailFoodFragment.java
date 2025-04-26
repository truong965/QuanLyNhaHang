package com.example.quanlynhahang.view.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.quanlynhahang.model.Food;
import com.example.quanlynhahang.data.local.ManagmentCart;
import com.example.quanlynhahang.databinding.FragmentDetailFoodBinding;

public class DetailFoodFragment extends Fragment {

    private FragmentDetailFoodBinding binding;
    private int numberInCart = 1;
    private Food food;
    private ManagmentCart managmentCart;


    public DetailFoodFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            food = getArguments().getParcelable("food");
        }
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
        incAndDecQuantity();
        addToCart();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDetailFoodBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    private void init() {
        managmentCart = new ManagmentCart(requireContext());

        // Retrieve food object from arguments

        if (food != null) {
            String imagePath = "file:///android_asset/food/" + food.getTitle() + ".jpg";
            Glide.with(requireContext())
                    .load(imagePath)
                    .into(binding.pic);

            binding.title.setText(food.getTitle());
            binding.price.setText(food.getPrice() + " $");
            binding.detail.setText(food.getDescription());
            binding.ratingBar.setRating((float) food.getStar());
            binding.rating.setText(String.valueOf(food.getStar()));
            binding.time.setText(food.getTimeValue() + " phút");
            binding.totalPrice.setText(food.getPrice() + " $");
        }

        binding.back.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
    }
    private void incAndDecQuantity() {
        binding.btnMinus.setOnClickListener(v -> {
            numberInCart = Integer.parseInt(binding.quantity.getText().toString());
            if (numberInCart > 1) {
                numberInCart--;
                binding.quantity.setText(String.valueOf(numberInCart));
                binding.totalPrice.setText(food.getPrice() * numberInCart + "$");
            }
        });

        binding.btnPlus.setOnClickListener(v -> {
            numberInCart = Integer.parseInt(binding.quantity.getText().toString());
            numberInCart++;
            binding.quantity.setText(String.valueOf(numberInCart));
            binding.totalPrice.setText(food.getPrice() * numberInCart + " $");
        });
    }

    private void addToCart() {
        binding.addToCart.setOnClickListener(v -> {
            food.setNumberInCart(numberInCart);
            managmentCart.insertFood(food);
            // Navigate to Cart Fragment instead of starting new Activity
//            CartFragment cartFragment = new CartFragment();
//            requireActivity().getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.fragment_container, cartFragment)
//                    .addToBackStack(null)
//                    .commit();
        });
    }
}