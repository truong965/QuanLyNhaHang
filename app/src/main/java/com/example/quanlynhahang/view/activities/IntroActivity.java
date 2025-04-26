package com.example.quanlynhahang.view.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.lifecycle.ViewModelProvider;

import com.example.quanlynhahang.databinding.ActivityIntroBinding;
import com.example.quanlynhahang.view.base.BaseActivity;
import com.example.quanlynhahang.viewModel.IntroViewModel;

// File: view/activities/IntroActivity.java
public class IntroActivity extends BaseActivity {
    private ActivityIntroBinding binding;
    private IntroViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIntroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(IntroViewModel.class);
        setupObservers();
        getWindow().setStatusBarColor(Color.parseColor("#FFE4B5"));
    }

    private void setupObservers() {
        viewModel.navigateToLogin().observe(this, shouldNavigate -> {
            if (shouldNavigate) {
                startActivity(new Intent(this, LoginActivity.class));
                viewModel.onNavigationComplete();
            }
        });

        viewModel.navigateToSignUp().observe(this, shouldNavigate -> {
            if (shouldNavigate) {
                startActivity(new Intent(this, SignUpActivity.class));
                viewModel.onNavigationComplete();
            }
        });

        binding.dangNhap.setOnClickListener(v -> viewModel.onLoginClicked());
        binding.dangKy.setOnClickListener(v -> viewModel.onSignUpClicked());
    }
}