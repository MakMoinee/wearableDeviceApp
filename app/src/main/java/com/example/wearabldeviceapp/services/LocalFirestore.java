package com.example.wearabldeviceapp.services;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.wearabldeviceapp.common.CommonMaps;
import com.example.wearabldeviceapp.interfaces.SimpleRequestListener;
import com.example.wearabldeviceapp.models.Dependents;
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
                .whereEqualTo("deviceUserID", gps.getUserID())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.isEmpty()) {
                            db.collection("devices")
                                    .document()
                                    .set(fMap)
                                    .addOnSuccessListener(unused -> listener.onSuccess())
                                    .addOnFailureListener(e1 -> listener.onError());
                        } else {
                            listener.onError();
                        }
                    }
                })
                .addOnFailureListener(e -> db.collection("devices")
                        .document()
                        .set(fMap)
                        .addOnSuccessListener(unused -> listener.onSuccess())
                        .addOnFailureListener(e1 -> listener.onError()));

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
                                gpss.setDocument(documentSnapshot.getId());
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
                    Log.e("ERROR_GET_DEVICE", e.getMessage());
                    listener.onError();
                });
    }

    public void deleteDevice(String docID, SimpleRequestListener listener) {
        db.collection("devices")
                .document(docID)
                .delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listener.onSuccess();
                    } else {
                        listener.onError();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ERROR_DELETE_DIVICE", e.getMessage());
                    listener.onError();
                });
    }

    public void addDependent(Dependents dependents, SimpleRequestListener listener) {
        Map<String, Object> map = CommonMaps.getDependentMap(dependents);
        db.collection("dependents")
                .document()
                .set(map)
                .addOnSuccessListener(unused -> listener.onSuccess())
                .addOnFailureListener(e -> {
                    if (e != null) {
                        Log.e("ERROR_ADD_DEPENDENTS", e.getMessage());
                    }
                    listener.onError();
                });
    }

    public void getAllDependents(String userID, SimpleRequestListener listener) {
        db.collection("dependents")
                .whereEqualTo("userID", userID)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        listener.onError();
                    } else {
                        List<Dependents> dependentsList = new ArrayList<>();
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            Dependents dependents = documentSnapshot.toObject(Dependents.class);
                            if (dependents != null) {
                                dependents.setDocID(documentSnapshot.getId());
                                dependentsList.add(dependents);
                            }

                        }
                        listener.onSuccessDependent(dependentsList);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ERROR_GET_DEPENDENTS", e.getMessage());
                    listener.onError();
                });
    }

    public void deleteDependent(String docID, SimpleRequestListener listener) {
        db.collection("dependents")
                .document(docID)
                .delete()
                .addOnSuccessListener(unused -> listener.onSuccess())
                .addOnFailureListener(e -> {
                    Log.e("FAILED_TO_DELETE", e.getMessage());
                    listener.onError();
                });
    }
}
