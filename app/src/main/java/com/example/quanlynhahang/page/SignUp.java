package com.example.quanlynhahang.page;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.quanlynhahang.DAO.RandomUserAPi;
import com.example.quanlynhahang.Entity.UserResponse;
import com.example.quanlynhahang.ManageState.ManageUser;
import com.example.quanlynhahang.R;
import com.example.quanlynhahang.Ultil.CallUserAPI;
import com.example.quanlynhahang.databinding.ActivitySignUpBinding;
import com.google.gson.annotations.SerializedName;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUp extends BaseActivity {
    ActivitySignUpBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       binding = ActivitySignUpBinding.inflate(getLayoutInflater());
       setContentView(binding.getRoot());
       setVariable();
    }
    private void setVariable() {
         binding.dangNhap.setOnClickListener(v -> {
            startActivity(new Intent(this,Login.class));
         });
         binding.dangKy.setOnClickListener(v -> {
             String email = binding.etEmail.getText().toString();
             String password = binding.etPsw.getText().toString();
             String hoTen = binding.etHoTen.getText().toString();
             boolean isMale = binding.radioGroup.getCheckedRadioButtonId() == R.id.nam;
             if(email.isEmpty() || password.isEmpty() || hoTen.isEmpty()){
                 Toast.makeText(this,"Vui lòng nhập đầy đủ thông tin",Toast.LENGTH_SHORT).show();
                    return;
             }
             if(password.length()<6){
                 Toast.makeText(this,"Mật khẩu phải lớn hơn 6 ký tự",Toast.LENGTH_SHORT).show();
                 return;
             }
             mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
                 if(task.isSuccessful()){
                     String id = mAuth.getCurrentUser().getUid();
                     addUserToDatabase( id,email,hoTen,isMale);
                     Log.i(TAG,"Đăng ký thành công");
                     Toast.makeText(this,"Đăng ký thành công",Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this,Login.class));
                 }else{
                     Log.i(TAG,"Đăng ký thất bại");
                     Toast.makeText(this,"Đăng ký thất bại",Toast.LENGTH_SHORT).show();
                 }
             });

         });
    }
    private void addUserToDatabase(String id,String email, String hoTen, boolean isMale){
        UserResponse.User user = new UserResponse.User();
        user.setEmail(email);
        user.setName(new UserResponse.Name("Mr",hoTen,""));
        user.setGender(isMale?"male":"female");
        database.getReference("User").child(id).setValue(user).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Log.i(TAG,"Thêm người dùng thành công");
            }else{
                Log.i(TAG,"Thêm người dùng thất bại");
            }
        });
    }


}