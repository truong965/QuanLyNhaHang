package com.example.quanlynhahang.data.firebase;

import com.google.firebase.database.FirebaseDatabase;
public class FirebaseDatabaseManager {
    private static FirebaseDatabase databaseInstance;

    public static FirebaseDatabase getDatabase() {
        if (databaseInstance == null) {
            databaseInstance = FirebaseDatabase.getInstance();
        }
        return databaseInstance;
    }
}
