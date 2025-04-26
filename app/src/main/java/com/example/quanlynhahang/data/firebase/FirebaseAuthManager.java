package com.example.quanlynhahang.data.firebase;

import com.google.firebase.auth.FirebaseAuth;

public class FirebaseAuthManager {
    private static FirebaseAuth authInstance;

    public static FirebaseAuth getAuth() {
        if (authInstance == null) {
            authInstance = FirebaseAuth.getInstance();
        }
        return authInstance;
    }
}
