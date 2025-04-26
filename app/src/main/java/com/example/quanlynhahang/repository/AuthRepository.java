package com.example.quanlynhahang.repository;

import com.example.quanlynhahang.data.firebase.FirebaseAuthManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthRepository {
    private final FirebaseAuth mAuth = FirebaseAuthManager.getAuth();

    public interface LoginCallback {
        void onSuccess(FirebaseUser user);
        void onFailure(String error);
    }

    public void loginWithEmail(String email, String password, LoginCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(mAuth.getCurrentUser());
                    } else {
                        callback.onFailure(task.getException() != null
                                ? task.getException().getMessage()
                                : "Đăng nhập thất bại");
                    }
                });
    }

    public void loginAnonymously(LoginCallback callback) {
        mAuth.signInAnonymously()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(mAuth.getCurrentUser());
                    } else {
                        callback.onFailure("Đăng nhập ẩn danh thất bại");
                    }
                });
    }
}