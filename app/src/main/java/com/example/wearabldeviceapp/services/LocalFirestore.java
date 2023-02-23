package com.example.wearabldeviceapp.services;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.wearabldeviceapp.common.CommonMaps;
import com.example.wearabldeviceapp.interfaces.SimpleRequestListener;
import com.example.wearabldeviceapp.models.FiretoreGPS;
import com.example.wearabldeviceapp.models.LocalGPS;
import com.example.wearabldeviceapp.models.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
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

    public void addDevice(String userID, LocalGPS gps, SimpleRequestListener listener) {
        Map<String, Object> fMap = CommonMaps.getGPSMap(userID, gps);
        db.collection("devices")
                .document()
                .set(fMap)
                .addOnSuccessListener(unused -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onError());
    }

    public void getAllDevice(String userID, SimpleRequestListener listener) {
        List<LocalGPS> gpsList = new ArrayList<>();
        db.collection("devices")
                .whereEqualTo("userID", userID)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        listener.onError();
                    } else {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            FiretoreGPS fgps = documentSnapshot.toObject(FiretoreGPS.class);
                            if (fgps != null) {
                                LocalGPS gpss = new LocalGPS();
                                gpss.setDeviceID(fgps.getDeviceID());
                                gpss.setUserID(fgps.getDeviceUserID());
                                gpsList.add(gpss);
                            }
                        }
                        if (gpsList.size() > 0) {
                            listener.onSuccess(gpsList);
                        } else {
                            listener.onError();
                        }

                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ERROR_GET_DEVICE",e.getMessage());
                    listener.onError();
                });
    }
}
