package com.example.quanlynhahang.page;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.example.quanlynhahang.Adapter.FoodAdapter;
import com.example.quanlynhahang.DAO.GeneralDAO;
import com.example.quanlynhahang.Entity.Category;
import com.example.quanlynhahang.Entity.Food;
import com.example.quanlynhahang.Entity.Location;
import com.example.quanlynhahang.Entity.Price;
import com.example.quanlynhahang.Entity.Time;
import com.example.quanlynhahang.ManageState.SpinnerManager;
import com.example.quanlynhahang.R;
import com.example.quanlynhahang.Ultil.PaginationScrollListener;
import com.example.quanlynhahang.databinding.FragmentFoodListBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class FoodListFragment extends Fragment {
    private FirebaseDatabase database;
    FragmentFoodListBinding binding;
    private String searchValue;
    private boolean isSearch = false;
    private SpinnerManager spinnerManager = SpinnerManager.getInstance();
    private GeneralDAO generalDAO;
    private Set<Integer> filteredFoodsID = new HashSet<>();
    private int currentPage = 1;
    private int itemsPerPage = 10;
    private boolean isLastPage = false;
    private FoodAdapter adapter;
    private ArrayList<Food> foodByFilter = new ArrayList<>();
    private Boolean[] spinnerInit = {false, false, false, false};
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFoodListBinding.inflate(inflater, container, false);
        BaseActivity baseActivity = (BaseActivity) getActivity();
        if (baseActivity != null) {
            database = baseActivity.database;
        } else {
            Log.w(" FoodListFragment ", "BaseActivity is null");
        }
        if (getArguments() != null) {
            searchValue = getArguments().getString("searchValue", "");  // Default empty if null
            isSearch = getArguments().getBoolean("isSearch", false);
        }
        Log.i("searchValue : is", searchValue +" " + isSearch);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (isAdded()) {
            loadAll(foods->{
                Log.i("loadAll", "Size: " + filteredFoodsID.size());
//                updateUI(filteredFoodsID);
            });
            initAdapter();
            initSearch();
            initSpinner();
            handleFirstLoad();
        } else {
            Log.w("FoodListFragment", "Fragment not attached, skipping init.");
        }
    }
    private void handleFirstLoad(){
        if(isSearch){
            handleSearch();
        }else{
            setActionSpinner();
        }
    }
    private void clearPage(){
        currentPage = 1;
        isLastPage = false;
        foodByFilter.clear();
        adapter.clearData();
    }
    private void loadNextPage(){
        binding.loading.setVisibility(View.VISIBLE);
        int totalPage = (int) Math.ceil((double) filteredFoodsID.size() / itemsPerPage);
        if(currentPage < totalPage){
            adapter.addData(new  ArrayList<>(foodByFilter.subList((currentPage - 1) * itemsPerPage, Math.min(currentPage * itemsPerPage, foodByFilter.size()))));
            currentPage++;
        }else{
            isLastPage = true;
        }
        binding.loading.setVisibility(View.GONE);
    }
    private void initAdapter(){
        GridLayoutManager  gridLayoutManager = new GridLayoutManager(getContext(), 2);
        binding.foodList.setLayoutManager( gridLayoutManager);
        adapter = new FoodAdapter(getContext(), new ArrayList<>(),getActivity().getSupportFragmentManager());
        binding.foodList.setAdapter(adapter);
        binding.foodList.addOnScrollListener(new PaginationScrollListener(gridLayoutManager) {
            @Override
            public void loadMoreItems() {
                loadNextPage();
            }
            @Override
            public boolean isLoading() {
                return binding.loading.getVisibility() == View.VISIBLE;
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }
        });
    }
    private void initSearch(){
      binding.editTextText.setText(searchValue);
        // set up
        binding.editTextText.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                // 🎯 Handle "Enter" key press here

                handleSearch();

                return true; // Consume the event
            }
            return false;
        });
        binding.searchButton.setOnClickListener(v -> {
            handleSearch();
        });
        //
//        binding.back.setOnClickListener(v -> {
//            finish();
//        });
    }
    private void initSpinner(){
        ArrayList<Location> locations = spinnerManager.getSpinnerData("Location", Location.class);
        ArrayList<Time> times = spinnerManager.getSpinnerData("Time", Time.class);
        ArrayList<Price> prices = spinnerManager.getSpinnerData("Price", Price.class);
        ArrayList<Category> categories = spinnerManager.getSpinnerData("Category", Category.class);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),R.layout.spinner_item,locations.stream().map(Location::getLoc).toArray(String[]::new));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.locationSp.setAdapter(adapter);

        ArrayAdapter<String> tadapter = new ArrayAdapter<>(getContext(),R.layout.spinner_item,times.stream().map(Time::getValue).toArray(String[]::new));
        tadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.timeSp.setAdapter(tadapter);


        ArrayAdapter<String> padapter = new ArrayAdapter<>(getContext(),R.layout.spinner_item,prices.stream().map(Price::getValue).toArray(String[]::new));
        padapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.priceSp.setAdapter(padapter);

        ArrayAdapter<String> cadapter = new ArrayAdapter<>(getContext(),R.layout.spinner_item,categories.stream().map(Category::getName).toArray(String[]::new));
        cadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.categorySp.setAdapter(cadapter);

//        binding.timeSp.setOnItemSelectedListener(null);
//        binding.priceSp.setOnItemSelectedListener(null);
//        binding.categorySp.setOnItemSelectedListener(null);
//        binding.locationSp.setOnItemSelectedListener(null);

        binding.timeSp.setSelection(spinnerManager.getSelectedPosition("Time"));
        binding.priceSp.setSelection(spinnerManager.getSelectedPosition("Price"));
        binding.categorySp.setSelection(spinnerManager.getSelectedPosition("Category"));
        binding.locationSp.setSelection(spinnerManager.getSelectedPosition("Location"));
    }
    private void setActionSpinner(){
        binding.locationSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                spinnerManager.setSelectedPosition("Location",position);
                spinnerInit[0] = true;
                handleLocationFilter(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        binding.timeSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                spinnerManager.setSelectedPosition("Time",position);
                spinnerInit[1] = true;
                handleTimeFilter(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        binding.priceSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                spinnerManager.setSelectedPosition("Price",position);
                spinnerInit[2] = true;
                handlePriceFilter(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        binding.categorySp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                spinnerManager.setSelectedPosition("Category",position);
                spinnerInit[3] = true;
                handleCategoryFilter(position);

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
    private void handleLocationFilter(int position){
        Location l = spinnerManager.getSpinnerData("Location", Location.class).get(position);
        if (l.getId()==2){
            checkReset();
            return ;
        }
        Set<Integer> filteredFoodsByLocation = new HashSet<>();
        DatabaseReference ref = database.getReference("Foods");
        Query query =ref.orderByChild("LocationId").equalTo(l.getId());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    int foodId = Objects.requireNonNull(data.child("Id").getValue(Integer.class));
                    filteredFoodsByLocation.add(foodId);
                }
                filteredFoodsID.retainAll(filteredFoodsByLocation);
                Log.i("filteredFoodsID location",  filteredFoodsID.stream().map(String::valueOf).reduce((a, b) -> a + ", " + b).orElse("empty"));
                updateUI(filteredFoodsID);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private void handleTimeFilter(int position){
        Time t = spinnerManager.getSpinnerData("Time", Time.class).get(position);
        if (t.getId()==3){
            checkReset();
            return ;
        }
        Set<Integer> filteredFoodsByTime = new HashSet<>();
        DatabaseReference ref = database.getReference("Foods");
        Query query = ref.orderByChild("TimeId").equalTo(t.getId());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    int foodId = Objects.requireNonNull(data.child("Id").getValue(Integer.class));
                    filteredFoodsByTime.add(foodId);
                }
                filteredFoodsID.retainAll(filteredFoodsByTime);
                Log.i("filteredFoodsID time",  filteredFoodsID.stream().map(String::valueOf).reduce((a, b) -> a + ", " + b).orElse("empty"));
                updateUI(filteredFoodsID);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void handlePriceFilter(int position){
        Price  p = spinnerManager.getSpinnerData("Price", Price.class).get(position);
        if (p.getId()==3){
            checkReset();
            return ;
        }
        Set<Integer> filteredFoodsByPrice = new HashSet<>();
        DatabaseReference ref = database.getReference("Foods");
        Query  query=ref.orderByChild("PriceId").equalTo(p.getId());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    int foodId = Objects.requireNonNull(data.child("Id").getValue(Integer.class));
                    filteredFoodsByPrice.add(foodId);
                }
                filteredFoodsID.retainAll(filteredFoodsByPrice);
                Log.i("filteredFoodsID price",  filteredFoodsID.stream().map(String::valueOf).reduce((a, b) -> a + ", " + b).orElse("empty"));
                updateUI(filteredFoodsID);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void handleCategoryFilter(int position){
        Category  c = spinnerManager.getSpinnerData("Category", Category.class).get(position);
        Log.i("category", c.toString());
        if (c.getId()==7){
            checkReset();
            return ;
        }
        Set<Integer> filteredFoodsByCategory = new HashSet<>();
        DatabaseReference ref = database.getReference("Foods");
        Query  query=ref.orderByChild("CategoryId").equalTo(c.getId());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    int foodId = Objects.requireNonNull(data.child("Id").getValue(Integer.class));
                    filteredFoodsByCategory.add(foodId);
                }
                Log.i("position cate", String.valueOf(position));
                filteredFoodsID.retainAll(filteredFoodsByCategory);
                Log.i("filteredFoodsID cate",  filteredFoodsID.stream().map(String::valueOf).reduce((a, b) -> a + ", " + b).orElse("empty"));
                updateUI(filteredFoodsID);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void loadAll(FirebaseCallback callback){
        DatabaseReference ref = database.getReference("Foods");
        Query query = ref.orderByChild("Id");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                int count=5;
                for (DataSnapshot data : snapshot.getChildren()) {
//                    if(count==0) break;
                    int foodId = Objects.requireNonNull(data.child("Id").getValue(Integer.class));
                    filteredFoodsID.add(foodId);
//                    count--;
                }
                callback.onDataLoaded(filteredFoodsID);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error loading data", error.toException());
            }
        });
    }
    private void checkReset(){
        if(spinnerInit[0] && spinnerInit[1] && spinnerInit[2] && spinnerInit[3]){
            if (spinnerManager.getSelectedPosition("Location")==2 && spinnerManager.getSelectedPosition("Time")==3 && spinnerManager.getSelectedPosition("Price")==3 && spinnerManager.getSelectedPosition("Category")==7){
                filteredFoodsID.clear();
                loadAll(this::updateUI);
            }
        }

    }
    private void updateUI(Set<Integer> filteredFoodsID) {
            Log.i("filteredFoodsID", "Size: " + filteredFoodsID.size());
            if (filteredFoodsID.isEmpty()) {
                // them trang notFound
                binding.empty.getRoot().setVisibility(View.VISIBLE);
                binding.foodList.setVisibility(View.GONE);
                binding.loading.setVisibility(View.GONE);
                clearPage();
                return;
            }
            clearPage();
            binding.loading.setVisibility(View.VISIBLE);
            binding.empty.getRoot().setVisibility(View.GONE);
            // Avoid recreating DAO every time
            if (generalDAO == null) {
                generalDAO = new GeneralDAO(database);
            }
            DatabaseReference ref = database.getReference("Foods");
            for (Integer i : filteredFoodsID) {
                Query query = ref.orderByChild("Id").equalTo(i);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Số lượng Food cần tải
                        for (DataSnapshot data : snapshot.getChildren()) {
                            Food food = data.getValue(Food.class);
                            if (food != null) {
                                AtomicInteger count = new AtomicInteger(4); // Đếm số dữ liệu cần tải cho từng Food
                                int lId = Objects.requireNonNull(data.child("LocationId").getValue(Integer.class));
                                int pId = Objects.requireNonNull(data.child("PriceId").getValue(Integer.class));
                                int tId = Objects.requireNonNull(data.child("TimeId").getValue(Integer.class));
                                int cId = Objects.requireNonNull(data.child("CategoryId").getValue(Integer.class));
                                generalDAO.getById(lId, Location.class, location -> {
                                    food.setLocation(location);
                                    checkAndAddFood(food, count);
                                });

                                generalDAO.getById(pId, Price.class, price -> {
                                    food.setPriceObject(price);
                                    checkAndAddFood( food, count);
                                });

                                generalDAO.getById(tId, Time.class, time -> {
                                    food.setTime(time);
                                    checkAndAddFood( food, count);
                                });
                                generalDAO.getById(cId, Category.class, category -> {
                                    food.setCategory(category);
                                    checkAndAddFood( food, count);
                                });
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.loading.setVisibility(View.GONE);
                    }
                });
            }
    }
    private void checkAndAddFood( Food food, AtomicInteger count) {
        if (count.decrementAndGet() == 0) {
            foodByFilter.add(food);
            if (foodByFilter.size() == filteredFoodsID.size()) {
                // Tất cả Food đã tải xong
                loadNextPage();
//                binding.loading.setVisibility(View.GONE);
            }
            // Thêm Food vào danh sách
//            Log.i("foods", "Added food: " + food.toString());
            // Khi tất cả Food đã tải xong, cập nhật UI
        }
    }
    private void handleSearch(){
        String searchValue = binding.editTextText.getText().toString();
        if(searchValue.isEmpty()){
            updateUI(filteredFoodsID);
            return;
        }
        DatabaseReference ref = database.getReference("Foods");
        Set<Integer> filteredFoodsID2 = new HashSet<>();
        Query query = ref.orderByChild("Title").startAt(searchValue).endAt(searchValue + "\uf8ff"); // It ensures all values that start with searchValue are included.
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    int foodId = Objects.requireNonNull(data.child("Id").getValue(Integer.class));
                    filteredFoodsID2.add(foodId);
                }
                updateUI(filteredFoodsID2);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }


}