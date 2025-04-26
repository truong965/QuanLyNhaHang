package com.example.quanlynhahang.view.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
            initSearch();
            setVariable();
            initSpiner();
            initBestSeller();
            initCategory();
        } else {
            Log.w("HomeFragment", "Fragment not attached, skipping init.");
        }
    }

    private void moveToSeach(String searchValue, boolean isSearch){
        Bundle bundle = new Bundle();
        bundle.putString("searchValue", searchValue);
        bundle.putBoolean("isSearch", isSearch);
       FoodListFragment searchFragment = new FoodListFragment();
        searchFragment.setArguments(bundle);
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, searchFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
    private void initSearch(){
        binding.editTextText.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                // 🎯 Handle "Enter" key press here
                String searchValue = binding.editTextText.getText().toString();
                moveToSeach(searchValue,true);
                return true; // Consume the event
            }
            return false;
        });
        binding.imageButton.setOnClickListener(v -> {
            String searchValue = binding.editTextText.getText().toString();
            moveToSeach(searchValue,true);
        });
    }
    private void initLocation(){
        DatabaseReference databaseReference= database.getReference("Location");
        ArrayList<Location> locations = new ArrayList<>();
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot data: snapshot.getChildren()){
                    Location location = data.getValue(Location.class);
                    locations.add(location);
                }
                if (!isAdded() || getContext() == null) {
                    Log.w("HomeFragment", "Fragment is detached, skipping UI update.");
                    return;
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_item, locations.stream().map(Location::getLoc).toArray(String[]::new));
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.locationSp.setAdapter(adapter);
                final boolean[] isFirstSelection = {true};
                binding.locationSp.setSelection(locations.size()-1);

                spinnerManager.setSelectedPosition("Location",locations.size()-1);
                binding.locationSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                        if (isFirstSelection[0]) {
                            isFirstSelection[0] = false;  // Ignore the first event
                            return;
                        }
                        spinnerManager.setSelectedPosition("Location",position);
                        moveToSeach("",false);
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
                spinnerManager.setSpinnerData("Location",locations);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    private void initTime(){
        DatabaseReference databaseReference = database.getReference("Time");
        ArrayList<Time> times = new ArrayList<>();
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data:snapshot.getChildren()){
                    Time time = data.getValue(Time.class);
                    times.add(time);
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item,times.stream().map(Time::getValue).toArray(String[]::new));
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.timeSp.setAdapter(adapter);
                final boolean[] isFirstSelection = {true};
                binding.timeSp.setSelection(times.size()-1);
                spinnerManager.setSelectedPosition("Time",times.size()-1);
                binding.timeSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                        if (isFirstSelection[0]) {
                            isFirstSelection[0] = false;  // Ignore the first event
                            return;
                        }
                        spinnerManager.setSelectedPosition("Time",position);
                        moveToSeach("",false);
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
                spinnerManager.setSpinnerData("Time",times);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private void initPrice(){
        DatabaseReference databaseReference = database.getReference("Price");
        ArrayList<Price> prices = new ArrayList<>();
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data:snapshot.getChildren()){
                    Price price = data.getValue(Price.class);
                    prices.add(price);
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),R.layout.spinner_item,prices.stream().map(Price::getValue).toArray(String[]::new));
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.priceSp.setAdapter(adapter);
                final boolean[] isFirstSelection = {true};
                binding.priceSp.setSelection(prices.size()-1);
                spinnerManager.setSelectedPosition("Price",prices.size()-1);
                binding.priceSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                        if (isFirstSelection[0]) {
                            isFirstSelection[0] = false;  // Ignore the first event
                            return;
                        }
                        spinnerManager.setSelectedPosition("Price",position);
                        moveToSeach("",false);
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
                spinnerManager.setSpinnerData("Price",prices);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void initSpiner(){
        initLocation();
        initPrice();
        initTime();
    }
    private void initBestSeller() {

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