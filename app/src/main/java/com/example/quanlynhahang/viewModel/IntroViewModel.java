package com.example.quanlynhahang.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

// File: viewmodel/IntroViewModel.java
public class IntroViewModel extends ViewModel {
    private final MutableLiveData<Boolean> _navigateToLogin = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _navigateToSignUp = new MutableLiveData<>();

    public LiveData<Boolean> navigateToLogin() { return _navigateToLogin; }
    public LiveData<Boolean> navigateToSignUp() { return _navigateToSignUp; }

    public void onLoginClicked() {
        _navigateToLogin.setValue(true);
    }

    public void onSignUpClicked() {
        _navigateToSignUp.setValue(true);
    }
    // Thêm phương thức reset sau khi điều hướng
    public void onNavigationComplete() {
        _navigateToLogin.setValue(false);
        _navigateToSignUp.setValue(false);
    }
}