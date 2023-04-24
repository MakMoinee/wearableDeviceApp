package com.example.wearabldeviceapp.services;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.wearabldeviceapp.common.CommonMaps;
import com.example.wearabldeviceapp.interfaces.SimpleRequestListener;
import com.example.wearabldeviceapp.models.DangerZone;
import com.example.wearabldeviceapp.models.Dependents;
import com.example.wearabldeviceapp.models.FiretoreGPS;
import com.example.wearabldeviceapp.models.History;
import com.example.wearabldeviceapp.models.LocalGPS;
import com.example.wearabldeviceapp.models.SafeZone;
import com.example.wearabldeviceapp.models.Users;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalFirestore {

    Context mContext;
    FirebaseFirestore db;
    int successCount = 0;

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

    public void addSafeZone(SafeZone safeZone, SimpleRequestListener listener) {
        Map<String, Object> finalMap = CommonMaps.getSafeZoneMap(safeZone);
        getSafeZones(safeZone.getUserID(), safeZone.getDependentDeviceID(), new SimpleRequestListener() {
            @Override
            public void onSuccess(SafeZone safeZone) {
                listener.onError();
            }

            @Override
            public void onError() {
                db.collection("safeZones")
                        .document()
                        .set(finalMap)
                        .addOnSuccessListener(unused -> listener.onSuccess())
                        .addOnFailureListener(e -> listener.onError());
            }
        });

    }

    public void getSafeZones(String userID, int deviceID, SimpleRequestListener listener) {
        db.collection("safeZones")
                .whereEqualTo("userID", userID)
                .whereEqualTo("dependentDeviceID", deviceID)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        listener.onError();
                    } else {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            if (documentSnapshot.exists()) {
                                SafeZone safeZone = documentSnapshot.toObject(SafeZone.class);
                                safeZone.setDocID(documentSnapshot.getId());
                                listener.onSuccess(safeZone);
                                break;
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> listener.onError());
    }

    public void deleteSafeZone(String docID, SimpleRequestListener listener) {
        db.collection("safeZones")
                .document(docID)
                .delete()
                .addOnSuccessListener(unused -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onError());
    }

    public void addDangerZone(DangerZone dangerZone, SimpleRequestListener listener) {
        Map<String, Object> finalMap = CommonMaps.getDangerZoneMap(dangerZone);
        getDangerZones(dangerZone.getUserID(), dangerZone.getDependentDeviceID(), new SimpleRequestListener() {
            @Override
            public void onSuccess(DangerZone safeZone) {
                listener.onError();
            }

            @Override
            public void onError() {
                db.collection("dangerZones")
                        .document()
                        .set(finalMap)
                        .addOnSuccessListener(unused -> listener.onSuccess())
                        .addOnFailureListener(e -> listener.onError());
            }
        });

    }

    public void getDangerZones(String userID, int deviceID, SimpleRequestListener listener) {
        db.collection("dangerZones")
                .whereEqualTo("userID", userID)
                .whereEqualTo("dependentDeviceID", deviceID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.isEmpty()) {
                            listener.onError();
                        } else {
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                if (documentSnapshot.exists()) {
                                    DangerZone dangerZone = documentSnapshot.toObject(DangerZone.class);
                                    dangerZone.setDocID(documentSnapshot.getId());
                                    listener.onSuccess(dangerZone);
                                    break;
                                }
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> listener.onError());
    }

    public void deleteDangerZone(String docID, SimpleRequestListener listener) {
        db.collection("dangerZones")
                .document(docID)
                .delete()
                .addOnSuccessListener(unused -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onError());
    }


    public void addHistoryLogger(History history, SimpleRequestListener listener) {
        Map<String, Object> params = new HashMap<>();
        params.put("dependentName", history.getDependentName());
        params.put("zoneType", history.getZoneType());
        params.put("timestamp", history.getTimestamp());
        params.put("latitude", history.getLatitude());
        params.put("longitude", history.getLongitude());
        params.put("userID", history.getUserID());

        db.collection("history")
                .document()
                .set(params)
                .addOnSuccessListener(unused -> listener.onSuccess())
                .addOnFailureListener(e -> {
                    Log.e("ERROR_ADD_HISTORY", e.getMessage());
                    listener.onError();
                });
    }

    public void getAllHistory(String userID, SimpleRequestListener listener) {
        db.collection("history")
                .whereEqualTo("userID", userID)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        listener.onError();
                    } else {
                        List<History> historyList = new ArrayList<>();
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            if (documentSnapshot.exists()) {
                                History history = documentSnapshot.toObject(History.class);
                                if (history != null) {
                                    history.setDocID(documentSnapshot.getId());
                                    historyList.add(history);
                                }
                            }
                        }
                        if (historyList.size() > 0) {
                            listener.onSuccessHistory(historyList);
                        } else {
                            listener.onError();
                        }
                    }

                })
                .addOnFailureListener(e -> listener.onError());
    }

    public void deleteHistory(String userID, SimpleRequestListener listener) {
        successCount = 0;
        this.getAllHistory(userID, new SimpleRequestListener() {
            @Override
            public void onSuccessHistory(List<History> historyList) {
                for (History history : historyList) {
                    db.collection("history").document(history.getDocID()).delete()
                            .addOnSuccessListener(unused -> {
                                successCount++;
                                if (successCount == historyList.size()) {
                                    listener.onSuccess();
                                }
                            })
                            .addOnFailureListener(e -> listener.onError());
                }


            }

            @Override
            public void onError() {
                SimpleRequestListener.super.onError();
            }
        });
    }


}
