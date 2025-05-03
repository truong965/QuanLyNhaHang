package com.example.quanlynhahang.view.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.quanlynhahang.R;
import com.example.quanlynhahang.adapter.BannerAdapter;
import com.example.quanlynhahang.adapter.BestFoodAdapter;
import com.example.quanlynhahang.adapter.CategoryAdapter;
import com.example.quanlynhahang.data.dao.GeneralDAO;
import com.example.quanlynhahang.data.local.SpinnerManager;
import com.example.quanlynhahang.databinding.FragmentHomeBinding;
import com.example.quanlynhahang.model.Category;
import com.example.quanlynhahang.model.Food;
import com.example.quanlynhahang.model.Location;
import com.example.quanlynhahang.model.Price;
import com.example.quanlynhahang.model.Time;
import com.example.quanlynhahang.view.base.BaseActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class HomeFragment extends Fragment {
    private FirebaseDatabase database;
    FragmentHomeBinding binding;
    private ViewPager2 viewPager;
    private BannerAdapter adapter;
    private List<Integer> images;
    private LinearLayout dotsLayout;
    private ImageView[] dots;
    private Handler handler = new Handler();
    private int currentPage = 0;
    private GeneralDAO generalDAO;
    private SpinnerManager spinnerManager = SpinnerManager.getInstance();

    public HomeFragment() {}
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        BaseActivity baseActivity = (BaseActivity) getActivity();
        if (baseActivity != null) {
            database = baseActivity.database;
        } else {
            Log.w("HomeFragment", "BaseActivity is null");
        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (isAdded()) {
            setVariable();
            initBestSeller();
            initCategory();
        } else {
            Log.w("HomeFragment", "Fragment not attached, skipping init.");
        }
    }

    private void initBestSeller() {
        binding.textView5.setOnClickListener(v -> {
            // move to FoodListFragment
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FoodListFragment foodListFragment = new FoodListFragment();
            // 🔍 Check if the current fragment is the same as the new one
            Fragment currentFragment = fragmentManager.findFragmentById(R.id.frame_layout);
            if (currentFragment != null && currentFragment.getClass().equals(foodListFragment.getClass())) {
                return; // 🚀 Skip replacement if it's the same fragment
            }

            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frame_layout, foodListFragment);
            fragmentTransaction.commit();

        });
        ArrayList<Food> foods = new ArrayList<>();
        binding.progressBarBanChay.setVisibility(View.VISIBLE);
        DatabaseReference databaseReference = database.getReference("Foods");
        Query query = databaseReference.orderByChild("BestFood").equalTo(true);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    binding.progressBarBanChay.setVisibility(View.GONE);
                    return;
                }

                AtomicInteger totalCount = new AtomicInteger((int) snapshot.getChildrenCount()); // Số lượng Food cần tải

                for (DataSnapshot data : snapshot.getChildren()) {
                    Food food = data.getValue(Food.class);

                    if (food != null) {
                        AtomicInteger count = new AtomicInteger(3); // Đếm số dữ liệu cần tải cho từng Food

                        int lId = Objects.requireNonNull(data.child("LocationId").getValue(Integer.class));
                        int pId = Objects.requireNonNull(data.child("PriceId").getValue(Integer.class));
                        int tId = Objects.requireNonNull(data.child("TimeId").getValue(Integer.class));
                        generalDAO = new GeneralDAO(database);
                        generalDAO.getById(lId, Location.class, location -> {
                            food.setLocation(location);
                            checkAndAddFood(foods, food, count, totalCount);
                        });

                        generalDAO.getById(pId, Price.class, price -> {
                            food.setPriceObject(price);
                            checkAndAddFood(foods, food, count, totalCount);
                        });

                        generalDAO.getById(tId, Time.class, time -> {
                            food.setTime(time);
                            checkAndAddFood(foods, food, count, totalCount);
                        });
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBarBanChay.setVisibility(View.GONE);
            }
        });
    }
    //  Hàm kiểm tra dữ liệu của từng Food và cập nhật UI khi tất cả đã xong
    private void checkAndAddFood(ArrayList<Food> foods, Food food, AtomicInteger count, AtomicInteger totalCount) {
        if (count.decrementAndGet() == 0) {
            foods.add(food);
            Log.i("foods", "Added food: " + food.toString());
            // Khi tất cả Food đã tải xong, cập nhật UI
            if (totalCount.decrementAndGet() == 0) {
                updateUI(foods);
            }
        }
    }
    //  Hàm cập nhật UI
    private void updateUI(ArrayList<Food> foods) {
        if (!foods.isEmpty()) {
            binding.banChayNhatLv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            RecyclerView.Adapter adapter = new BestFoodAdapter(getContext(), foods, getActivity().getSupportFragmentManager());
            binding.banChayNhatLv.setAdapter(adapter);
        }
        binding.progressBarBanChay.setVisibility(View.GONE);
    }
    // banner
    private void setVariable(){
        viewPager = binding.viewPager;
        dotsLayout = binding.dotsLayout;

        // Add Images to Banner
        images = new ArrayList<>();
        images.add(R.drawable.banner1);
        images.add(R.drawable.banner2);
        images.add(R.drawable.banner3);

        adapter = new BannerAdapter(images);
        viewPager.setAdapter(adapter);

        // Create Dots for Navigation
        setupDots();

        // Auto Slide Handler
        startAutoSlide();

        // Change Dots When Swiping
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentPage = position;
                updateDots();
            }
        });
    }
    // ✅ Create Dots for Each Image
    private void setupDots() {
        dots = new ImageView[images.size()];
        for (int i = 0; i < images.size(); i++) {
            dots[i] = new ImageView(getContext());
            dots[i].setImageResource(R.drawable.dot_inactive);
            dots[i].setPadding(10, 0, 10, 0);
            int finalI = i;
            dots[i].setOnClickListener(v -> {
                viewPager.setCurrentItem(finalI, true);
                updateDots();}
            );
            dotsLayout.addView(dots[i]);
        }
        dots[0].setImageResource(R.drawable.dot_active); // Highlight First Dot
    }

    // Update Dots When Changing Slide
    private void updateDots() {
        for (int i = 0; i < dots.length; i++) {
            if (i == currentPage) {
                dots[i].setImageResource(R.drawable.dot_active);
            } else {
                dots[i].setImageResource(R.drawable.dot_inactive);
            }
        }
    }
    //  Auto-Slide Every 5 Seconds
    private void startAutoSlide() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (currentPage == images.size() - 1) {
                    currentPage = 0;
                } else {
                    currentPage++;
                }
                viewPager.setCurrentItem(currentPage, true);
                updateDots();
                handler.postDelayed(this, 5000); // Slide every 5s
            }
        };
        handler.postDelayed(runnable, 5000);
    }
    // category
    private void initCategory(){
        DatabaseReference databaseReference = database.getReference("Category");
        ArrayList<Category> categories = new ArrayList<>();
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                binding.progressBar1.setVisibility(View.VISIBLE);
                for (DataSnapshot data:snapshot.getChildren()){
                    Category category = data.getValue(Category.class);
                    categories.add(category);
                    Log.i("category",category.toString());
                }
                binding.loaiMonAnRv.setLayoutManager(new GridLayoutManager(getContext(), 3));
                CategoryAdapter adapter = new CategoryAdapter(getContext(),categories , getActivity(). getSupportFragmentManager());
                binding.loaiMonAnRv.setAdapter(adapter);
                binding.progressBar1.setVisibility(View.GONE);
                spinnerManager.setSpinnerData("Category",categories);
                spinnerManager.setSelectedPosition("Category",categories.size()-1);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

}