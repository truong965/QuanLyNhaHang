package com.example.quanlynhahang.page;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.quanlynhahang.R;
import com.example.quanlynhahang.databinding.ActivityIntroBinding;

public class Intro extends BaseActivity {
    ActivityIntroBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIntroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setVariable();
        getWindow().setStatusBarColor(Color.parseColor("#FFE4B5"));
    }
    private void setVariable() {
        binding.dangNhap.setOnClickListener(v -> {
            startActivity(new Intent(this,Login.class));
        });
        binding.dangKy.setOnClickListener(v -> {
            startActivity(new Intent(this,SignUp.class));
        });
    }
}