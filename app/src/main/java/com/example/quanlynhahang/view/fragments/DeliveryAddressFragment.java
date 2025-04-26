package com.example.quanlynhahang.view.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.quanlynhahang.R;
import com.example.quanlynhahang.adapter.AddressAdapter;
import com.example.quanlynhahang.databinding.FragmentDeliveryAddressBinding;
import com.example.quanlynhahang.model.DistrictResponse;
import com.example.quanlynhahang.model.Province;
import com.example.quanlynhahang.model.ProvinceResponse;
import com.example.quanlynhahang.model.UserResponse;
import com.example.quanlynhahang.model.WardReponse;
import com.example.quanlynhahang.network.VietNamApiService;
import com.example.quanlynhahang.utils.ProvinceDistrictWard;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class DeliveryAddressFragment extends Fragment {


    private FragmentDeliveryAddressBinding binding;
    private FragmentManager fragmentManager;

    private AddressAdapter addressAdapter;
    public FirebaseAuth mAuth;
    public FirebaseDatabase database;
    private ArrayAdapter<String> tinhAdapter,huyen_quanAdapter,xa_phuongAdapter;
    private Spinner tinhSpinner,huyenQuanSpinner,xaPhuongSpinner;
    private AddressSpinner addressSpinner = AddressSpinner.getInstance();
    public DeliveryAddressFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentDeliveryAddressBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (isAdded()) {
            mAuth = FirebaseAuth.getInstance();
            database = FirebaseDatabase.getInstance();
            init();
            fragmentManager =getActivity().getSupportFragmentManager();
        }
    }
    private void init(){
        binding.back.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
//        ArrayList<UserResponse.LocationUser> a = getLocation();
//        for (   UserResponse.LocationUser locationUser : a) {
//            Log.d("Firebase", "Address: " + locationUser.getStreet().getName() + ", " +
//                    locationUser.getWard() + ", " +
//                    locationUser.getState() + ", " +
//                    locationUser.getCity());
//        }
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL,false));
        loadAddresses(locations -> {
           addressAdapter = new AddressAdapter(getContext(), locations);
            binding.recyclerView.setAdapter(addressAdapter);
        });
//        binding.recyclerView.setAdapter(addressAdapter);
        binding.confirmButton.setOnClickListener(v -> {
            // mặc định địa chỉ là địa chỉ đầu tiên
            PaymentFragment fragment = new PaymentFragment();
            Bundle args = new Bundle();
            args.putString("diaChi",addressAdapter.getSelectedAddress()); // Pass food object
            fragment.setArguments(args);

            // 🔍 Check if the current fragment is the same as the new one
            Fragment currentFragment = fragmentManager.findFragmentById(R.id.frame_layout);
            if (currentFragment != null && currentFragment.getClass().equals(fragment.getClass())) {
                Log.i("replaceFragment", "Fragment already displayed: " + fragment.getClass().getSimpleName());
                return; // 🚀 Skip replacement if it's the same fragment
            }

            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frame_layout, fragment);
            fragmentTransaction.commit();
        });
        binding.addAddress.setOnClickListener(v -> {
            showDiaChiDialog();
        });
    }
    public void loadAddresses(OnAddressLoadedListener listener) {
        ArrayList<UserResponse.LocationUser> locationList = new ArrayList<>();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("DeliverAddress");

        dbRef.child(uid).child("addresses").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    UserResponse.LocationUser address = itemSnapshot.getValue(UserResponse.LocationUser.class);
                    Log.d("Firebase", "Address: " + address.getStreet().getName() + ", " +
                            address.getWard() + ", " +
                            address.getState() + ", " +
                            address.getCity());
                    locationList.add(address);
                }
                listener.onAddressLoaded(locationList); // return data here
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Read failed", error.toException());
            }
        });
    }

    private void showDiaChiDialog() {
        Dialog  dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_delivery_address);
        initSpinner(dialog);
        initButton(dialog);
        dialog.setCanceledOnTouchOutside(true);
        dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT, // or WRAP_CONTENT
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        dialog.show();
    }
    private void initButton(Dialog view) {
        view.findViewById(R.id.add_button).setOnClickListener(v -> {
            EditText diaChi = view.findViewById(R.id.diaChi);
            if(addressSpinner.getPositionProvince()==0 || addressSpinner.getPositionDistrict()==0 || addressSpinner.getPositionWard()==0 || diaChi.getText().toString().isEmpty()){
                Toast.makeText(getContext(), "Vui lòng chọn đầy đủ địa chỉ", Toast.LENGTH_SHORT).show();
                return;
            }
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String uid = user.getUid();
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("DeliverAddress");
            UserResponse.Street street = new UserResponse.Street(1, diaChi.getText().toString());
            UserResponse.LocationUser locationUser = new UserResponse.LocationUser(
                    addressSpinner.getSelectedProvince(),
                    addressSpinner.getSelectedWard(),
                    addressSpinner.getSelectedDistrict(),
                    street
            );
            dbRef.child(uid).child("addresses").push().setValue(locationUser)
                    .addOnSuccessListener(aVoid -> Log.d("Firebase", "Address list saved!"))
                    .addOnFailureListener(e -> Log.e("Firebase", "Failed to save: ", e));
            Toast.makeText(getContext(), "Thêm địa chỉ thành công", Toast.LENGTH_SHORT).show();

            addressAdapter.addLocation(locationUser);
            view.dismiss();
        });
        view.findViewById(R.id.cancel_button).setOnClickListener(v -> {
            view.dismiss();
        });
    }
    private void initSpinner(Dialog view) {
        tinhAdapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_spinner_item, new ArrayList<String>());
        tinhAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
          tinhSpinner= view.findViewById(R.id.tinh);
        tinhSpinner.setAdapter(tinhAdapter);

        huyen_quanAdapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_spinner_item, new ArrayList<String>());
         huyen_quanAdapter.add("Chọn huyện quận");
        huyen_quanAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

         huyenQuanSpinner= view.findViewById(R.id.huyen_quan);
        huyenQuanSpinner.setAdapter(huyen_quanAdapter);

        xa_phuongAdapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_spinner_item,    new ArrayList<String>());
        xa_phuongAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         xaPhuongSpinner= view.findViewById(R.id.xa_phuong);
        xaPhuongSpinner.setAdapter(xa_phuongAdapter);

        tinhSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedProvince = (String) parent.getItemAtPosition(position);
                for (Province province : addressSpinner.getProvinces()) {
                    if (province.getName().equals(selectedProvince)) {
                        if(position!=0){
                            addressSpinner.setPositionProvince(position);
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
        huyenQuanSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedDistrict = (String) parent.getItemAtPosition(position);
                for (Province.District district : addressSpinner.getDistricts()) {
                    if (district.getName().equals(selectedDistrict)) {
                        if (position!=0) addressSpinner.setPositionDistrict(position);
                        loadWards(district);
                        break;
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        xaPhuongSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position!=0){
                    addressSpinner.setPositionWard(position);
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
                    if(addressSpinner.getProvinces().isEmpty()) {
                        ArrayList<Province> l = new ArrayList<>();
                        l.add(new Province("Chọn tỉnh thành phố",""));
                        l.addAll(response.body().getData().getData());
                        addressSpinner.setProvinces(l);
                    }
                    tinhAdapter.addAll(
                            addressSpinner.getProvinces().stream()
                                    .map(Province::getName)
                                    .collect(Collectors.toList())
                    );
                    tinhAdapter.notifyDataSetChanged();
                    if(addressSpinner.getPositionProvince()==-1){
                        tinhSpinner.setSelection(0);
                    }else{
                        tinhSpinner.setSelection(addressSpinner.getPositionProvince());
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
                    if(addressSpinner.getPositionProvince()==-1 || addressSpinner.getPositionProvince()==0) {
                        ArrayList<Province.District> l = new ArrayList<>();
                        l.add(new Province.District("Chọn huyện quận",""));
                        l.addAll(response.body().getData().getData());
                       addressSpinner.setDistricts(l);
                    }else{
                        addressSpinner.setDistricts((ArrayList<Province.District>) response.body().getData().getData());
                    }
                    huyen_quanAdapter.clear();
                    for (Province.District district : addressSpinner.getDistricts()) {
                        huyen_quanAdapter.add(district.getName());
                    }
                    huyen_quanAdapter.notifyDataSetChanged();
                    if(addressSpinner.getPositionDistrict()==-1){
                        huyenQuanSpinner.setSelection(0);
                    }else{
                        huyenQuanSpinner.setSelection(addressSpinner.getPositionDistrict());
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
                    if(addressSpinner.getPositionDistrict()==-1 || addressSpinner.getPositionDistrict()==0) {
                        ArrayList<Province.Ward> l = new ArrayList<>();
                        l.add(new Province.Ward("Chọn xã phường", ""));
                        l.addAll(response.body().getData().getData());
                        addressSpinner.setWards(l);
                    }else{
                        addressSpinner.setWards((ArrayList<Province.Ward>) response.body().getData().getData());
                    }
                    xa_phuongAdapter.clear();
                    for (Province.Ward ward : addressSpinner.getWards()) {
                        xa_phuongAdapter.add(ward.getName());
                    }
                    xa_phuongAdapter.notifyDataSetChanged();
                    if(addressSpinner.getPositionWard()==-1) {
                        xaPhuongSpinner.setSelection(0);
                    }else {
                        xaPhuongSpinner.setSelection(addressSpinner.getPositionWard());
                    }
                }
            }
            @Override
            public void onFailure(Call<WardReponse> call, Throwable t) {
                Log.e("API ward Error", t.getMessage());
            }
        });
    }
}
@Setter
@Getter
class AddressSpinner {
    private static  AddressSpinner instance;
    private int positionProvince=-1;
    private int positionDistrict=-1;
    private int positionWard=-1;
    ArrayList<Province> provinces;
    ArrayList<Province.District> districts;
    ArrayList<Province.Ward> wards;
    public AddressSpinner() {
        provinces = new ArrayList<>();
        districts = new ArrayList<>();
        wards = new ArrayList<>();
    }
    public static AddressSpinner getInstance(){
        if(instance == null){
            instance = new AddressSpinner();
        }
        return instance;
    }
    public String getSelectedProvince(){
        return provinces.get(positionProvince).getName();
    }
    public String getSelectedDistrict(){
        return districts.get(positionDistrict).getName();
    }
    public String getSelectedWard(){
        return wards.get(positionWard).getName();
    }
}
interface OnAddressLoadedListener {
    void onAddressLoaded(ArrayList<UserResponse.LocationUser> locations);
}
