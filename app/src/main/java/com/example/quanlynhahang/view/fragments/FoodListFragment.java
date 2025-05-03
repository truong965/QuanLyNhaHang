package com.example.quanlynhahang.view.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.quanlynhahang.R;
import com.example.quanlynhahang.adapter.FoodAdapter;
import com.example.quanlynhahang.data.dao.GeneralDAO;
import com.example.quanlynhahang.data.firebase.FirebaseDatabaseManager;
import com.example.quanlynhahang.data.local.SpinnerManager;
import com.example.quanlynhahang.databinding.FragmentFoodListBinding;
import com.example.quanlynhahang.model.Category;
import com.example.quanlynhahang.model.Food;
import com.example.quanlynhahang.model.Location;
import com.example.quanlynhahang.model.Price;
import com.example.quanlynhahang.model.Time;
import com.example.quanlynhahang.utils.PaginationScrollListener;
import com.example.quanlynhahang.view.FirebaseCallback;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FoodListFragment extends Fragment {
    private static final String TAG = "FoodListFragment";
    private FirebaseDatabase database;
    FragmentFoodListBinding binding;
    private SpinnerManager spinnerManager = SpinnerManager.getInstance();
    private GeneralDAO generalDAO;

    // Bộ nhớ cache cho dữ liệu
    private final Map<Integer, Food> foodsCache = new HashMap<>();
    private final Map<Integer, Location> locationsCache = new HashMap<>();
    private final Map<Integer, Time> timesCache = new HashMap<>();
    private final Map<Integer, Price> pricesCache = new HashMap<>();
    private final Map<Integer, Category> categoriesCache = new HashMap<>();

    private Set<Integer> filteredFoodsID = new HashSet<>();
    private int currentPage = 1;
    private int itemsPerPage = 10;
    private boolean isLastPage = false;
    private boolean isLoading = false;
    private FoodAdapter adapter;
    private ArrayList<Food> foodByFilter = new ArrayList<>();
    private Boolean[] spinnerInit = {false, false, false, false};
    private boolean isSpinnerProgrammaticChange = false;

    // Executor để thực hiện tác vụ nặng ở background
    private final Executor backgroundExecutor = Executors.newFixedThreadPool(2);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFoodListBinding.inflate(inflater, container, false);
        database = FirebaseDatabaseManager.getDatabase();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initAdapter();
        loadAllDataOptimized();
    }
    private void handleBundleArguments() {
        if (getArguments() != null && getArguments().containsKey("category")) {
            int categoryId = getArguments().getInt("category");

            // Find the position in spinner data that matches this category ID
            ArrayList<Category> categories = spinnerManager.getSpinnerData("Category", Category.class);
            for (int i = 0; i < categories.size(); i++) {
                if (categories.get(i).getId() == categoryId) {
                    // Set the spinner selection programmatically
                    isSpinnerProgrammaticChange = true;
                    binding.categorySp.setSelection(i);
                    spinnerManager.setSelectedPosition("Category", i);
                    isSpinnerProgrammaticChange = false;

                    // Mark this spinner as initialized
                    spinnerInit[3] = true;

                    // Trigger search with the selected category
                    handleSearch();
                    break;
                }
            }
        }
    }
//    private void loadAllDataOptimized() {
//        binding.loading.setVisibility(View.VISIBLE);
//
//        // Tải tất cả dữ liệu cần thiết song song
//        backgroundExecutor.execute(() -> {
//            // Tải danh sách địa điểm, thời gian, giá, và danh mục
//            loadLocationData(() -> {
//                loadTimeData(() -> {
//                    loadPriceData(() -> {
//                        loadCategoryData(() -> {
//                            // Sau khi tải xong tất cả dữ liệu phụ, tải danh sách món ăn
//                            loadAllFoodsData(foods -> {
//                                filteredFoodsID = foods;
//                                requireActivity().runOnUiThread(() -> {
//                                    initSpinner();
//                                    setActionSpinner();
//                                    updateUIOptimized(filteredFoodsID);
//                                    initSearch();
//                                    binding.loading.setVisibility(View.GONE);
//                                });
//                            });
//                        });
//                    });
//                });
//            });
//        });
//    }
    private void loadAllDataOptimized() {
        binding.loading.setVisibility(View.VISIBLE);

        // Tải tất cả dữ liệu cần thiết song song
        backgroundExecutor.execute(() -> {
            // Tải danh sách địa điểm, thời gian, giá, và danh mục
            loadLocationData(() -> {
                loadTimeData(() -> {
                    loadPriceData(() -> {
                        loadCategoryData(() -> {
                            // Sau khi tải xong tất cả dữ liệu phụ, tải danh sách món ăn
                            loadAllFoodsData(foods -> {
                                filteredFoodsID = foods;
                                requireActivity().runOnUiThread(() -> {
                                    initSpinner();
                                    setActionSpinner();
                                    // Handle any category filter from bundle arguments
                                    handleBundleArguments();
                                    // Only update UI if no arguments were processed
                                    if (getArguments() == null || !getArguments().containsKey("category")) {
                                        updateUIOptimized(filteredFoodsID);
                                    }
                                    initSearch();
                                    binding.loading.setVisibility(View.GONE);
                                });
                            });
                        });
                    });
                });
            });
        });
    }
    private void loadAllFoodsData(FirebaseCallback callback) {
        DatabaseReference ref = database.getReference("Foods");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Set<Integer> foodIds = new HashSet<>();

                // Tải và cache tất cả món ăn
                for (DataSnapshot data : snapshot.getChildren()) {
                    try {
                        Food food = data.getValue(Food.class);
                        if (food != null) {
                            foodIds.add(food.getId());
                            foodsCache.put(food.getId(), food);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing food data", e);
                    }
                }
                callback.onDataLoaded(foodIds);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading food data", error.toException());
                callback.onDataLoaded(new HashSet<>());
            }
        });
    }

    private void loadLocationData(Runnable onComplete) {
        DatabaseReference ref = database.getReference("Location");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Location> locations = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Location location = data.getValue(Location.class);
                    if (location != null) {
                        locations.add(location);
                        locationsCache.put(location.getId(), location);
                    }
                }
                spinnerManager.setSelectedPosition("Location", 2);
                spinnerManager.setSpinnerData("Location", locations);
                onComplete.run();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading location data", error.toException());
                onComplete.run();
            }
        });
    }

    private void loadTimeData(Runnable onComplete) {
        DatabaseReference ref = database.getReference("Time");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Time> times = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Time time = data.getValue(Time.class);
                    if (time != null) {
                        times.add(time);
                        timesCache.put(time.getId(), time);
                    }
                }
                spinnerManager.setSelectedPosition("Time", 3);
                spinnerManager.setSpinnerData("Time", times);
                onComplete.run();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading time data", error.toException());
                onComplete.run();
            }
        });
    }

    private void loadPriceData(Runnable onComplete) {
        DatabaseReference ref = database.getReference("Price");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Price> prices = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Price price = data.getValue(Price.class);
                    if (price != null) {
                        prices.add(price);
                        pricesCache.put(price.getId(), price);
                    }
                }
                spinnerManager.setSelectedPosition("Price", 3);
                spinnerManager.setSpinnerData("Price", prices);
                onComplete.run();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading price data", error.toException());
                onComplete.run();
            }
        });
    }

    private void loadCategoryData(Runnable onComplete) {
        DatabaseReference ref = database.getReference("Category");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Category> categories = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Category category = data.getValue(Category.class);
                    if (category != null) {
                        categories.add(category);
                        categoriesCache.put(category.getId(), category);
                    }
                }
                spinnerManager.setSelectedPosition("Category", 7);
                spinnerManager.setSpinnerData("Category", categories);
                onComplete.run();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading category data", error.toException());
                onComplete.run();
            }
        });
    }

    private void initAdapter() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        binding.foodList.setLayoutManager(gridLayoutManager);
        adapter = new FoodAdapter(getContext(), new ArrayList<>(), getActivity().getSupportFragmentManager());
        binding.foodList.setAdapter(adapter);
        binding.foodList.addOnScrollListener(new PaginationScrollListener(gridLayoutManager) {
            @Override
            public void loadMoreItems() {
                loadNextPage();
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }
        });
    }

    private void initSpinner() {
        isSpinnerProgrammaticChange = true;
        ArrayList<Location> locations = spinnerManager.getSpinnerData("Location", Location.class);
        ArrayList<Time> times = spinnerManager.getSpinnerData("Time", Time.class);
        ArrayList<Price> prices = spinnerManager.getSpinnerData("Price", Price.class);
        ArrayList<Category> categories = spinnerManager.getSpinnerData("Category", Category.class);

        if (getContext() == null) return;

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item,
                locations.stream().map(Location::getLoc).toArray(String[]::new));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.locationSp.setAdapter(adapter);

        ArrayAdapter<String> tadapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item,
                times.stream().map(Time::getValue).toArray(String[]::new));
        tadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.timeSp.setAdapter(tadapter);

        ArrayAdapter<String> padapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item,
                prices.stream().map(Price::getValue).toArray(String[]::new));
        padapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.priceSp.setAdapter(padapter);

        ArrayAdapter<String> cadapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item,
                categories.stream().map(Category::getName).toArray(String[]::new));
        cadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.categorySp.setAdapter(cadapter);

        // Completing the initSpinner() method
        binding.locationSp.setSelection(spinnerManager.getSelectedPosition("Location"));
        binding.timeSp.setSelection(spinnerManager.getSelectedPosition("Time"));
        binding.priceSp.setSelection(spinnerManager.getSelectedPosition("Price"));
        binding.categorySp.setSelection(spinnerManager.getSelectedPosition("Category"));
        isSpinnerProgrammaticChange = false;
    }

    private void setActionSpinner() {
        binding.locationSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                if (isSpinnerProgrammaticChange) return;
                spinnerManager.setSelectedPosition("Location", position);
                spinnerInit[0] = true;
                if (checkReset()) {
                    handleSearch();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.timeSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                if (isSpinnerProgrammaticChange) return;
                spinnerManager.setSelectedPosition("Time", position);
                spinnerInit[1] = true;
                if (checkReset()) {
                    handleSearch();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.priceSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                if (isSpinnerProgrammaticChange) return;
                spinnerManager.setSelectedPosition("Price", position);
                spinnerInit[2] = true;
                if (checkReset()) {
                    handleSearch();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.categorySp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                if (isSpinnerProgrammaticChange) return;
                spinnerManager.setSelectedPosition("Category", position);
                spinnerInit[3] = true;
                if (checkReset()) {
                    handleSearch();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private boolean checkReset() {
        for (Boolean init : spinnerInit) {
            if (!init) return false;
        }
        return true;
    }

    private void handleSearch() {
        binding.loading.setVisibility(View.VISIBLE);
        backgroundExecutor.execute(() -> {
            // Get selected values from spinners
            int locationId = spinnerManager.getSpinnerData("Location", Location.class)
                    .get(spinnerManager.getSelectedPosition("Location")).getId();
            int timeId = spinnerManager.getSpinnerData("Time", Time.class)
                    .get(spinnerManager.getSelectedPosition("Time")).getId();
            int priceId = spinnerManager.getSpinnerData("Price", Price.class)
                    .get(spinnerManager.getSelectedPosition("Price")).getId();
            int categoryId = spinnerManager.getSpinnerData("Category", Category.class)
                    .get(spinnerManager.getSelectedPosition("Category")).getId();

            String searchText = binding.editTextText.getText().toString().toLowerCase().trim();

            // Filter food items locally using cached data
            Set<Integer> filteredIds = new HashSet<>();

            for (Food food : foodsCache.values()) {
                // Filter by search text if provided
                if (!searchText.isEmpty() && !food.getTitle().toLowerCase().contains(searchText)) {
                    continue;
                }

                // Apply spinner filters if not "All" (assuming ID 0 is "All")
                if (locationId != 2 && food.getLocationId() != locationId) continue;
                if (timeId != 3 && food.getTimeId() != timeId) continue;
                if (priceId != 3 && food.getPriceId() != priceId) continue;
                if (categoryId != 7 && food.getCategoryId() != categoryId) continue;

                filteredIds.add(food.getId());
            }

            // Update UI with filtered results
            requireActivity().runOnUiThread(() -> {
                updateUIOptimized(filteredIds);
            });
        });
    }

    private void updateUIOptimized(Set<Integer> filteredIds) {
        if (filteredIds.isEmpty()) {
            binding.empty.getRoot().setVisibility(View.VISIBLE);
            binding.foodList.setVisibility(View.GONE);
            binding.loading.setVisibility(View.GONE);
            clearPage();
            return;
        }

        clearPage();
        binding.foodList.setVisibility(View.VISIBLE);
        binding.empty.getRoot().setVisibility(View.GONE);

        // Prepare filtered food list from cache
        foodByFilter.clear();
        for (Integer id : filteredIds) {
            Food food = foodsCache.get(id);
            if (food != null) {
                foodByFilter.add(food);
            }
        }

        // Load first page
        loadNextPage();
        binding.loading.setVisibility(View.GONE);
    }

    private void loadNextPage() {
        if (isLastPage || isLoading) return;

        isLoading = true;
        int start = (currentPage - 1) * itemsPerPage;
        int end = Math.min(currentPage * itemsPerPage, foodByFilter.size());

        if (start < end) {
            ArrayList<Food> pageItems = new ArrayList<>(foodByFilter.subList(start, end));
            adapter.addData(pageItems);
            currentPage++;
            isLastPage = end >= foodByFilter.size();
        } else {
            isLastPage = true;
        }

        isLoading = false;
    }

    private void clearPage() {
        currentPage = 1;
        isLastPage = false;
        adapter.clearData();
    }

    private void initSearch() {
        binding.editTextText.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                handleSearch();
                return true;
            }
            return false;
        });

        binding.refeshButton.setOnClickListener(v -> {
            isSpinnerProgrammaticChange = true;
            binding.locationSp.setSelection(2);
            binding.timeSp.setSelection(3);
            binding.priceSp.setSelection(3);
            binding.categorySp.setSelection(7);
            binding.editTextText.setText("");
            isSpinnerProgrammaticChange = false;

            spinnerManager.setSelectedPosition("Location", 2);
            spinnerManager.setSelectedPosition("Time", 3);
            spinnerManager.setSelectedPosition("Price", 3);
            spinnerManager.setSelectedPosition("Category", 7);

            handleSearch();
        });
        binding.searchButton.setOnClickListener(v -> {
            handleSearch();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}