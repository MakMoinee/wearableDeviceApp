package com.example.wearabldeviceapp.services;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.example.wearabldeviceapp.interfaces.SimpleRequestListener;
import com.example.wearabldeviceapp.models.D;
import com.example.wearabldeviceapp.models.Dependents;
import com.example.wearabldeviceapp.models.Devices;
import com.example.wearabldeviceapp.models.History;
import com.example.wearabldeviceapp.models.LocalGPS;
import com.example.wearabldeviceapp.models.Users;
import com.example.wearabldeviceapp.preference.UserPref;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.firebase.Timestamp;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class HistoryWorker extends ListenableWorker {

    LocalRequest request;
    Context mContext;
    boolean isError = false;
    LocalFirestore fs;
    String userID = "";

    public HistoryWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        request = new LocalRequest(getApplicationContext());
        fs = new LocalFirestore(getApplicationContext());
        this.mContext = context;
        Users users = new UserPref(getApplicationContext()).getUser();
        userID = users.getUserID();
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        final SettableFuture<Result> future = SettableFuture.create();

        try {
            Users users = new UserPref(getApplicationContext()).getUser();
            userID = users.getUserID();
            fs.getAllDependents(userID, new SimpleRequestListener() {
                @Override
                public void onSuccessDependent(List<Dependents> dependentsList) {
                    for (Dependents dependents : dependentsList) {
                        LocalGPS gps = new LocalGPS();
                        if (dependents.getDeviceUserID() == 0) {
                            future.set(Result.failure());
                            break;
                        }
                        gps.setUserID(dependents.getDeviceUserID());
                        gps.setDeviceID(dependents.getDeviceID());
                        request.getCoordinates(gps, new SimpleRequestListener() {
                            @Override
                            public void onSuccessWithStr(String uuid) {
                                uuid = uuid.replace("\"{", "{\"");
                                uuid = uuid.replace("}\"", "}");
                                uuid = uuid.replace(":[", "\":\"[");
                                uuid = uuid.replace("}]", "}]\"");
                                Log.e("RAW_WORKER", uuid);
                                try {
                                    D coordinates = new Gson().fromJson(uuid, new TypeToken<D>() {
                                    }.getType());
                                    String data = coordinates.getD().getDevices();
                                    data = data.replace("[{id", "[{\"id\"");
                                    data = data.replace(":\"", "\":\"");
                                    data = data.replace(",", ",\"");
                                    data = data.replace("groupID:", "groupID\":");
                                    data = data.replace("stopTimeMinute:", "stopTimeMinute\":");
                                    data = data.replace("isStop:", "isStop\":");
                                    try {
                                        List<Devices> devicesList = new Gson().fromJson(data, new TypeToken<List<Devices>>() {
                                        }.getType());
                                        if (devicesList.size() > 0) {
                                            Devices devices = devicesList.get(0);
                                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
                                            devices.setDeviceUtcDate(dateFormat.format(new Date()));
                                            History history = new History();
                                            history.setDependentName(dependents.getName());
                                            history.setTimestamp(Timestamp.now());
                                            history.setLatitude(devices.getBaiduLat());
                                            history.setLongitude(devices.getBaiduLng());
                                            history.setUserID(userID);

                                            fs.addHistoryLogger(history, new SimpleRequestListener() {
                                                @Override
                                                public void onSuccess() {
                                                    future.set(Result.success());
                                                }

                                                @Override
                                                public void onError() {
                                                    Log.e("ERROR_ADD_HISTORY", "yes");
                                                    future.set(Result.failure());
                                                }
                                            });


                                        } else {
                                            future.set(Result.failure());
                                        }
                                    } catch (Exception ee) {
                                        if (ee.getMessage() != null) {
                                            Log.e("ERROR_MALFORMED", ee.getMessage());
                                        }

                                        future.set(Result.failure());
                                    }

                                } catch (Exception dd) {
                                    if (dd.getMessage() != null) {
                                        Log.e("ERROR_MALFORMED_TOP", dd.getMessage());
                                    }
                                    future.set(Result.failure());
                                }


                            }

                            @Override
                            public void onError() {
                                Log.e("ERROR_GET_COORDINATES", "yes");

                                future.set(Result.failure());
                            }
                        });
                    }
                }

                @Override
                public void onError() {
                    Log.e("NO_DEPENDENTS", "empty dependents");
                    future.set(Result.failure());
                }
            });


        } catch (Exception e) {
            Log.e("STARTWORK_EXCEPTION", e.getMessage());
            future.set(Result.failure());
        }

        return future;
    }
}
