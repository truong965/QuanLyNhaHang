package com.example.quanlynhahang.page;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.quanlynhahang.R;
import com.example.quanlynhahang.databinding.ActivityHomeBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
public class Home extends BaseActivity {
    FloatingActionButton fab;
    DrawerLayout drawerLayout;
    BottomNavigationView bottomNavigationView;
    ActivityHomeBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init(savedInstanceState);
        initCart();
    }
    private boolean getTargetFragment(){
        String targetFragment = getIntent().getStringExtra("targetFragment");
        if (targetFragment != null && targetFragment.equals("OrderFragment")) {
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new OrderFragment()).commit();
            NavigationView navigationView = findViewById(R.id.nav_view);
            navigationView.setCheckedItem(R.id.nav_order);
            return true;
        }
        return false;
    }
    private void init(Bundle savedInstanceState){
        bottomNavigationView= binding.bottomNavigationView;
        drawerLayout = binding.drawerLayout;
        fab = binding.fab;
        NavigationView navigationView = binding.navView;
        navigationView.setNavigationItemSelectedListener(item -> {
            Fragment fragment = null;
            if(item.getItemId() == R.id.nav_home) {
                fragment = new HomeFragment();
            } else if (item.getItemId() == R.id.nav_settings) {
                fragment = new SettingFragment();
            } else if (item.getItemId() == R.id.nav_order) {
                fragment = new OrderFragment();
            } else if (item.getItemId() == R.id.nav_about) {
                fragment = new IntroduceFragment();
            }else if(item.getItemId() == R.id.nav_logout){
                new AlertDialog.Builder(this)
                        .setTitle("Đăng xuất")
                        .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                        .setPositiveButton("Có", (dialog, which) -> {
                            // Xử lý đăng xuất ở đây
                            Intent intent = new Intent(Home.this, Login.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        })
                        .setNegativeButton("Không", (null))
                        .show();
                return true;
            }

            if(fragment != null){
                replaceFragment(fragment);
                item.setChecked(true);
            }
            // Đóng NavigationView sau khi chọn item
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle drawerToggle= new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        if(savedInstanceState==null){
            if(!getTargetFragment()){
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new HomeFragment()).commit();
                navigationView.setCheckedItem(R.id.nav_home);
            }
        }
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            if(item.getItemId() == R.id.home) {
                fragment = new HomeFragment();
            } else if (item.getItemId() == R.id.foodList) {
                fragment = new FoodListFragment();
            } else if (item.getItemId() == R.id.order) {
                fragment = new OrderFragment();
            } else if (item.getItemId() == R.id.account) {
                fragment = new ProfileFragment();
            }
            if (fragment != null) {
                replaceFragment(fragment);
            }
            return true;
        });

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomDialog();
            }
        });
    }
    private void replaceFragment(Fragment fragment) {
        if (!isFinishing() && !isDestroyed()) {
            FragmentManager fragmentManager = getSupportFragmentManager();

            Fragment currentFragment = fragmentManager.findFragmentById(R.id.frame_layout);
            if (currentFragment != null && currentFragment.getClass().equals(fragment.getClass())) {
                Log.i("replaceFragment", "Fragment already displayed: " + fragment.getClass().getSimpleName());
                return;
            }
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frame_layout, fragment);
            fragmentTransaction.commitAllowingStateLoss();  // 🔥 Fix crash khi fragment chưa sẵn sàng
        } else {
            Log.w("replaceFragment", "Activity is finishing or destroyed, skipping fragment replacement.");
        }
    }
    private void showBottomDialog() {
        if (isFinishing()) {
            Log.w("showBottomDialog", "Activity is finishing, skipping dialog.");
            return;
        }

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottomsheetlayout);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            dialog.getWindow().setGravity(Gravity.BOTTOM);
        }

        dialog.show();
    }
    private void initCart(){
        binding.cart.setOnClickListener(v -> {
            replaceFragment(new CartFragment());
        });
    }
}