package com.example.quanlynhahang.view.fragments;


import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.quanlynhahang.R;
import com.example.quanlynhahang.data.firebase.FirebaseDatabaseManager;
import com.example.quanlynhahang.data.local.ManageUser;
import com.example.quanlynhahang.databinding.FragmentProfileBinding;
import com.example.quanlynhahang.model.DistrictResponse;
import com.example.quanlynhahang.model.Province;
import com.example.quanlynhahang.model.ProvinceResponse;
import com.example.quanlynhahang.model.UserResponse;
import com.example.quanlynhahang.model.WardReponse;
import com.example.quanlynhahang.network.VietNamApiService;
import com.example.quanlynhahang.utils.ProvinceDistrictWard;
import com.example.quanlynhahang.view.base.BaseActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private boolean isUpdate = false;
    private ArrayAdapter<String> tinhAdapter,huyen_quanAdapter,xa_phuongAdapter;
    private  ManageUser manageUser =new ManageUser();
    public FirebaseAuth mAuth;
    public FirebaseDatabase database;

    public ProfileFragment() {
        // Required empty public constructor
    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        fetchRandomUser();
        BaseActivity baseActivity = (BaseActivity) getActivity();
        if (baseActivity != null) {
            mAuth = baseActivity.mAuth;
            database = baseActivity.database;
            if(mAuth.getCurrentUser().isAnonymous()){
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                Fragment emptyProfileFragment = new EmptyProfileFragment();
                Fragment currentFragment = fragmentManager.findFragmentById(R.id.frame_layout);
                if (currentFragment != null && currentFragment.getClass().equals(emptyProfileFragment.getClass())) {
                    Log.i("replaceFragment", "Fragment already displayed: " + emptyProfileFragment.getClass().getSimpleName());
                    return;
                }
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.frame_layout, emptyProfileFragment);
                fragmentTransaction.commitAllowingStateLoss();
                return;
            }else{
                init();
                initSpinner();
                setUpdating();
            }
        }

    }
//    private void fetchRandomUser() {
//        RandomUserAPi apiService = CallUserAPI.getApi();
//        Call<UserResponse> call = apiService.getRandomUser();
//
//        call.enqueue(new Callback<UserResponse>() {
//            @Override
//            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    if(ManageUser.getUser() == null) {
//                        UserResponse userResponse = response.body();
//                        UserResponse.User user = userResponse.getResults().get(0); // Get first user
//                        ManageUser.setUser(user);
//                    }
//                    init();
//                    initSpinner();
//                }
//            }
//            @Override
//            public void onFailure(Call<UserResponse> call, Throwable t) {
//                Log.e("API Error", t.getMessage());
//            }
//        });
//    }
    private void initSpinner(){
        tinhAdapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_spinner_item, new ArrayList<String>());
        tinhAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.tinh.setAdapter(tinhAdapter);

        huyen_quanAdapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_spinner_item, new ArrayList<String>());
        huyen_quanAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.huyenQuan.setAdapter(huyen_quanAdapter);

        xa_phuongAdapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_spinner_item, new ArrayList<String>());
        xa_phuongAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.xaPhuong.setAdapter(xa_phuongAdapter);

        binding.tinh.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedProvince = (String) parent.getItemAtPosition(position);
                for (Province province : ManageUser.getProvinces()) {
                    if (province.getName().equals(selectedProvince)) {
                        manageUser.setPositionProvince(position);
                        if(tinhAdapter.getCount()>1){
                            UserResponse.LocationUser  locationUser= ManageUser.getUser().getLocation();
                            if(locationUser==null)
                                locationUser = new UserResponse.LocationUser();
                            locationUser.setCity(province.getName());
                            ManageUser.getUser().setLocation(locationUser);
                            loadDistricts(province);
                        }
                        break;
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        binding.huyenQuan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedDistrict = (String) parent.getItemAtPosition(position);
                for (Province.District district : ManageUser.getDistricts()) {
                    if (district.getName().equals(selectedDistrict)) {
                        manageUser.setPositionDistrict(position);
                        if(huyen_quanAdapter.getCount()>1){
                            UserResponse.LocationUser locationUser = ManageUser.getUser().getLocation();
                            locationUser.setState(district.getName());
                            ManageUser.getUser().setLocation(locationUser);
                            loadWards(district);
                        }
                        break;
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        binding.xaPhuong.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                manageUser.setPositionWard(position);
                if(xa_phuongAdapter.getCount()>1){
                    UserResponse.LocationUser locationUser = ManageUser.getUser().getLocation();
                    locationUser.setWard(ManageUser.getWards().get(position).getName());
                    ManageUser.getUser().setLocation(locationUser);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        loadProvince();
    }
    private void loadProvince(){
        VietNamApiService vietNamApiService = ProvinceDistrictWard.getApiService();
        vietNamApiService.getProvinces().enqueue(new Callback<ProvinceResponse>() {
            @Override
            public void onResponse(Call<ProvinceResponse> call, Response<ProvinceResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if(ManageUser.getProvinces()==null) {
                        List<Province> l = new ArrayList<>();
                        l.add(new Province("Chọn tỉnh thành phố",""));
                        l.addAll(response.body().getData().getData());
                        ManageUser.setProvinces( l);
                    }
                    UserResponse.User user = ManageUser.getUser();
                    Log.i("user", "User location city : " + user.getLocation().getCity() + " street and name " + user.getLocation().getStreet().getNumber() + " " + user.getLocation().getStreet().getName() + "state :" + user.getLocation().getState() + " ward :" + user.getLocation().getWard());
                    if(user.getLocation()!=null){
                        if(user.getLocation().getCity()!=null){
                            for (Province p: ManageUser.getProvinces()) {
                                if(p.getName().equals(user.getLocation().getCity())){
                                    manageUser.setPositionProvince(ManageUser.getProvinces().indexOf(p));
                                    break;
                                }
                            }
                        }
                    }
                    tinhAdapter.addAll(ManageUser.getProvinces().stream()
                            .map(Province::getName)
                            .collect(Collectors.toList()));
                    tinhAdapter.notifyDataSetChanged();
                    if(manageUser.getPositionProvince()==-1){
                        binding.tinh.setSelection(0);
                    }else{
                        binding.tinh.setSelection(manageUser.getPositionProvince());
                    }
                }
            }
            @Override
            public void onFailure(Call<ProvinceResponse> call, Throwable t) {
                Log.e("API province Error", t.getMessage());
            }
        });
    }
    private void loadDistricts(Province tinh){
    VietNamApiService vietNamApiService = ProvinceDistrictWard.getApiService();
        vietNamApiService.getDistrictsByProvince(tinh.getCode(),-1).enqueue(new Callback<DistrictResponse>() {
        @Override
        public void onResponse(Call<DistrictResponse> call, Response<DistrictResponse> response) {
            if (response.isSuccessful() && response.body() != null) {
                if(ManageUser.getUser().getLocation()==null || ManageUser.getUser().getLocation().getState()==null) {
                        List<Province.District> l = new ArrayList<>();
                        l.add(new Province.District("Chọn huyện quận",""));
                        l.addAll(response.body().getData().getData());
                        ManageUser.setDistricts(l);
                }else{
                    ManageUser.setDistricts(response.body().getData().getData());
                    for (Province.District d: ManageUser.getDistricts()) {
                        if(d.getName().equals(ManageUser.getUser().getLocation().getState())){
                            manageUser.setPositionDistrict(ManageUser.getDistricts().indexOf(d));
                            break;
                        }
                    }
                }
                huyen_quanAdapter.clear();
                for (Province.District district : ManageUser.getDistricts()) {
                    huyen_quanAdapter.add(district.getName());
                }
                huyen_quanAdapter.notifyDataSetChanged();
                if(manageUser.getPositionDistrict()==-1){
                    binding.huyenQuan.setSelection(0);
                }else{
                    binding.huyenQuan.setSelection(manageUser.getPositionDistrict());
                }
            }
        }
        @Override
        public void onFailure(Call<DistrictResponse> call, Throwable t) {
            Log.e("API district Error", t.getMessage());
        }
    });
    }
    private void loadWards(Province.District huyen_quan){
        VietNamApiService vietNamApiService = ProvinceDistrictWard.getApiService();
        vietNamApiService.getWardsByDistrict(huyen_quan.getCode(),-1).enqueue(new Callback<WardReponse>() {
            @Override
            public void onResponse(Call<WardReponse> call, Response<WardReponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if(ManageUser.getUser().getLocation()==null || ManageUser.getUser().getLocation().getWard()==null) {
                        List<Province.Ward> l = new ArrayList<>();
                        l.add(new Province.Ward("Chọn xã phường", ""));
                        l.addAll(response.body().getData().getData());
                        ManageUser.setWards(l);
                    }else{
                        ManageUser.setWards(response.body().getData().getData());
                        for (Province.Ward w: ManageUser.getWards()) {
                            if(w.getName().equals(ManageUser.getUser().getLocation().getWard())){
                                manageUser.setPositionWard(ManageUser.getWards().indexOf(w));
                                break;
                            }
                        }
                    }
                    xa_phuongAdapter.clear();
                    for (Province.Ward ward : ManageUser.getWards()) {
                        xa_phuongAdapter.add(ward.getName());
                    }
                    xa_phuongAdapter.notifyDataSetChanged();
                    if(manageUser.getPositionWard()==-1) {
                        binding.xaPhuong.setSelection(0);
                    }else {
                            binding.xaPhuong.setSelection(manageUser.getPositionWard());
                    }
                }
            }
            @Override
            public void onFailure(Call<WardReponse> call, Throwable t) {
                Log.e("API ward Error", t.getMessage());
            }
        });
    }
    private void init(){
        UserResponse.User user = ManageUser.getUser();
        if(user.getPicture()!=null){
            Glide.with(requireContext())
                    .load(user.getPicture().getLarge())
                    .placeholder(new ColorDrawable(Color.TRANSPARENT))
                    .circleCrop()
                    .into(binding.imageView8);
        }else{
            Glide.with(requireContext())
                    .load(R.drawable.user)
                    .placeholder(new ColorDrawable(Color.TRANSPARENT))
                    .circleCrop()
                    .into(binding.imageView8);
        }
        String name = user.getName().toString();
        binding.textView12.setText(name);
        if(user.getGender().equals("female")){
            binding.nu.setChecked(true);
        }else{
            binding.nam.setChecked(true);
        }

        binding.email.setText(user.getEmail());

        if(user.getLocation()!=null) {
            if (user.getLocation().getStreet()!= null) {
                binding.diaChi.setText(user.getLocation().getStreet().getName());
            } else {
                binding.diaChi.setText("");
            }
        }
        binding.chinhSua.setOnClickListener(v ->{
            if(isUpdate){
                new AlertDialog.Builder(getContext())
                        .setTitle("Xác nhận")
                        .setMessage("Bạn có muốn thay đổi thông tin không?")
                        .setPositiveButton("Có", (dialog,which)-> {updateUser();})
                        .setNegativeButton("Không", (dialog,which)-> dialog.dismiss())
                        .show();
            }else{
                isUpdate= true;
                setUpdating();
                binding.huy.setVisibility(View.VISIBLE);
                binding.chinhSua.setText("xác nhận");
            }

        });
        binding.huy.setOnClickListener(v->{
            new AlertDialog.Builder(getContext())
                    .setTitle("Xác nhận")
                    .setMessage("Bạn có muốn hủy thay đổi không?")
                    .setPositiveButton("Có", (dialog,which)-> {isUpdate=false; setUpdating(); binding.huy.setVisibility(View.GONE); binding.chinhSua.setText("Chỉnh sửa");})
                    .setNegativeButton("Không", (dialog,which)-> dialog.dismiss())
                    .show();
        });
    }
    private void setUpdating(){
            binding.textView12.setFocusableInTouchMode(isUpdate);
            binding.textView12.setClickable(isUpdate);
            binding.textView12.setFocusable(isUpdate);

            binding.nam.setClickable(isUpdate);
            binding.nu.setClickable(isUpdate);

//            binding.email.setFocusableInTouchMode(isUpdate);
//            binding.email.setClickable(isUpdate);
//            binding.email.setFocusable(isUpdate);

            binding.tinh.setEnabled(isUpdate);

            binding.huyenQuan.setEnabled(isUpdate);
            binding.xaPhuong.setEnabled(isUpdate);

            binding.diaChi.setFocusableInTouchMode(isUpdate);
            binding.diaChi.setClickable(isUpdate);
            binding.diaChi.setFocusable(isUpdate);
    }
    private void updateUser(){
        String id = mAuth.getCurrentUser().getUid();
        Province p = ManageUser.getProvinces().get(manageUser.getPositionProvince());
        Province.District d = ManageUser.getDistricts().get(manageUser.getPositionDistrict());
        Province.Ward w = ManageUser.getWards().get(manageUser.getPositionWard());
        UserResponse.User user = new UserResponse.User();
        user.setName(new UserResponse.Name("",binding.textView12.getText().toString(),""));
        user.setGender(binding.nam.isChecked()?"male":"female");
        user.setLocation(new UserResponse.LocationUser(
                p.getName(),
                w.getName(),
                d.getName(),
                new UserResponse.Street(1,binding.diaChi.getText().toString())
        ));
        database.getReference("User").child(id).setValue(user).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Log.i("TAG","Thêm người dùng thành công");
                binding.huy.setVisibility(View.GONE);
                binding.chinhSua.setText("Chỉnh sửa");
                isUpdate=false;
                setUpdating();
                getLatestInfo(id);
            }else{
                Log.i("TAG","Thêm người dùng thất bại");
            }
        });

    }
    private void getLatestInfo(String uid){
        DatabaseReference userRef = FirebaseDatabaseManager.getDatabase().getReference("User").child(uid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    UserResponse.User user = snapshot.getValue(UserResponse.User.class);
                    user.setEmail(mAuth.getCurrentUser().getEmail());
                    manageUser.setUser(user);
               }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}