package com.example.wearabldeviceapp.services;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.wearabldeviceapp.common.CommonMaps;
import com.example.wearabldeviceapp.interfaces.SimpleRequestListener;
import com.example.wearabldeviceapp.models.Users;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;

import java.util.Map;

public class LocalFirestore {

    Context mContext;
    FirebaseFirestore db;

    public LocalFirestore(Context mContext) {
        this.mContext = mContext;
        db = FirebaseFirestore.getInstance();
    }

    public void createAccount(Users users, SimpleRequestListener listener) {
        Map<String, Object> map = CommonMaps.getUserMap(users);
        db.collection("users")
                .document(users.getUserID())
                .set(map)
                .addOnSuccessListener(unused -> listener.onSuccess())
                .addOnFailureListener(e -> {
                    listener.onError();
                });
    }

    public void getUser(Users users, SimpleRequestListener listener) {
        db.collection("users")
                .whereEqualTo("name", users.getName())
                .whereEqualTo("password", users.getPassword())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        listener.onError();
                    } else {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            if (documentSnapshot.exists()) {
                                Users mUsers = documentSnapshot.toObject(Users.class);
                                listener.onSuccessWithUserData(mUsers);
                                break;
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (e != null) {
                        Log.e("ERROR_GETTING_USER", e.getMessage());
                    }
                    listener.onError();
                });
    }
}
