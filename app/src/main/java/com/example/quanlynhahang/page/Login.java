package com.example.quanlynhahang.page;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.quanlynhahang.Entity.UserResponse;
import com.example.quanlynhahang.ManageState.ManageUser;
import com.example.quanlynhahang.R;
import com.example.quanlynhahang.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends BaseActivity {
    private ManageUser manageUser =new ManageUser();
    ActivityLoginBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setVariable();
        binding.etEmail.setText("ex@gmail.com");
        binding.etPsw.setText("123456789");
    }
    private void setVariable() {
        binding.darkScreen.setVisibility(View.GONE);
        binding.progressBar2.setVisibility(View.GONE);

        binding.dangNhap.setOnClickListener(v -> {
            binding.dangNhap.setClickable(false);
            binding.darkScreen.setVisibility(View.VISIBLE);
            binding.progressBar2.setVisibility(View.VISIBLE);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (isDestroyed() || isFinishing()) return;

                String email = binding.etEmail.getText().toString().trim();
                String password = binding.etPsw.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
                    resetLoginState();
                    return;
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
                    resetLoginState();
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    getUserInfo(user.getUid());
                                    Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(this, Home.class);
//                                    intent.putExtra("targetFragment", "OrderFragment");
                                    startActivity(intent);
                                    finish();
                                }
                            } else {
                                Toast.makeText(this, "Đăng nhập thất bại", Toast.LENGTH_SHORT).show();
                            }
                            resetLoginState();
                        });
            }, 2000);
        });
        binding.dangKy.setOnClickListener(v -> {
            startActivity(new Intent(this, SignUp.class));
        });
    }

    private void resetLoginState() {
        binding.dangNhap.setClickable(true);
        binding.darkScreen.setVisibility(View.GONE);
        binding.progressBar2.setVisibility(View.GONE);
    }
    private void getUserInfo(String uid) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("User").child(uid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                     UserResponse.User user = snapshot.getValue(UserResponse.User.class);
                     manageUser.setUser(user);
                    // Do whatever you want with the data
                    Toast.makeText(Login.this, "Welcome " + user.getName().getFirst(), Toast.LENGTH_LONG).show();
//                    Log.i("user", "User location city : " + user.getLocation().getCity() + " street and name " + user.getLocation().getStreet().getNumber() + " " + user.getLocation().getStreet().getName() + "state :" + user.getLocation().getState() + " ward :" + user.getLocation().getWard());
                } else {
                    Toast.makeText(Login.this, "User data not found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors.
                Toast.makeText(Login.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}