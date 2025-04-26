package com.example.quanlynhahang.view.base;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quanlynhahang.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
//
//public class BaseActivity extends AppCompatActivity {
//    public final String TAG = "FastBite";
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        getWindow().setStatusBarColor(getResources().getColor(R.color.white, getTheme()));
//    }
//}
public class BaseActivity extends AppCompatActivity {
public FirebaseAuth mAuth;
public FirebaseDatabase database;
public  final  String TAG ="FastBite";
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    database = FirebaseDatabase.getInstance();
    mAuth = FirebaseAuth.getInstance();
    getWindow().setStatusBarColor(getResources().getColor(R.color.white, getTheme()));
}
}