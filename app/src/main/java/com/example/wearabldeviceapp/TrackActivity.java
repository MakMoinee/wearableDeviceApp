package com.example.wearabldeviceapp;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.wearabldeviceapp.adapters.SelectDeviceAdapter;
import com.example.wearabldeviceapp.databinding.ActivityTrackBinding;
import com.example.wearabldeviceapp.databinding.DialogSelectDeviceBinding;
import com.example.wearabldeviceapp.interfaces.SimpleRequestListener;
import com.example.wearabldeviceapp.models.D;
import com.example.wearabldeviceapp.models.DeviceCoordinates;
import com.example.wearabldeviceapp.models.Devices;
import com.example.wearabldeviceapp.models.LocalGPS;
import com.example.wearabldeviceapp.models.Users;
import com.example.wearabldeviceapp.preference.UserPref;
import com.example.wearabldeviceapp.services.LocalFirestore;
import com.example.wearabldeviceapp.services.LocalRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class TrackActivity extends AppCompatActivity implements OnMapReadyCallback {

    ActivityTrackBinding binding;
    private GoogleMap mMap;
    AlertDialog alertChoose;
    DialogSelectDeviceBinding sBinding;

    List<LocalGPS> gpsList = new ArrayList<>();

    LocalFirestore fs;

    SelectDeviceAdapter adapter;

    String deviceID = "";
    LocalGPS selectedDevice = new LocalGPS();

    LocalRequest req;

    Devices deviceLocation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTrackBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        fs = new LocalFirestore(TrackActivity.this);
        req = new LocalRequest(TrackActivity.this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(7.91173, 125.09199);
        mMap.addMarker(new MarkerOptions()
                .position(sydney)
                .title("Your Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        enableMyLocation();
        loadDevices();
        chooseDevice();
        initListeners();
        runMyThread();
    }

    private void runMyThread() {

    }

    private void initListeners() {
        sBinding.btnSelectDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertChoose.dismiss();
                if (selectedDevice != null) {
                    req.getCoordinates(selectedDevice, new SimpleRequestListener() {
                        @Override
                        public void onSuccessWithStr(String uuid) {
                            uuid = uuid.replace("\"{", "{\"");
                            uuid = uuid.replace("}\"", "}");
                            uuid = uuid.replace(":[", "\":\"[");
                            uuid = uuid.replace("}]", "}]\"");
                            Log.e("RAW", uuid);
                            D coordinates = new Gson().fromJson(uuid, new TypeToken<D>() {
                            }.getType());
                            String data = coordinates.getD().getDevices();
                            data = data.replace("[{id", "[{\"id\"");
                            data = data.replace(":\"", "\":\"");
                            data = data.replace(",", ",\"");
                            data = data.replace("groupID:", "groupID\":");
                            data = data.replace("stopTimeMinute:", "stopTimeMinute\":");
                            data = data.replace("isStop:", "isStop\":");
                            List<Devices> devicesList = new Gson().fromJson(data, new TypeToken<List<Devices>>() {
                            }.getType());
                            if (devicesList.size() > 0) {
                                deviceLocation = devicesList.get(0);

                                LatLng sydney = new LatLng(7.91173, 125.09199);
                                LatLng dLocation = new LatLng(deviceLocation.getBaiduLat(), deviceLocation.getBaiduLng());
                                mMap.addMarker(new MarkerOptions()
                                        .position(sydney)
                                        .title("Your Location"));
                                mMap.addMarker(new MarkerOptions()
                                        .position(dLocation)
                                        .title(String.format("Device:{0} Location", deviceLocation.getId())));
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(dLocation, 15));
                            }
                        }

                        @Override
                        public void onError() {
                            SimpleRequestListener.super.onError();
                        }
                    });
                }
            }
        });
    }

    private void loadDevices() {
        Users users = new UserPref(TrackActivity.this).getUser();
        if (users != null) {
            fs.getAllDevice(users.getUserID(), new SimpleRequestListener() {
                @Override
                public void onSuccess(List<LocalGPS> l) {
                    gpsList = l;
                    List<String> str = new ArrayList<>();
                    if (gpsList != null) {
                        for (LocalGPS gpss : gpsList) {
                            str.add(Integer.toString(gpss.getDeviceID()));
                        }
                    }
                    adapter = new SelectDeviceAdapter(TrackActivity.this, str);
                    sBinding.spinMe.setAdapter(adapter);
                    sBinding.spinMe.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            deviceID = adapter.getItem(position).toString();
                            selectedDevice = gpsList.get(position);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            selectedDevice = null;
                        }
                    });
                }

                @Override
                public void onError() {
                    SimpleRequestListener.super.onError();
                }
            });
        }

    }

    @SuppressLint("MissingPermission")
    private void enableMyLocation() {
        // 1. Check if permissions are granted, if so, enable the my location layer
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            return;
        }

        // 2. Otherwise, request location permissions from the user.
//        PermissionUtils.requestLocationPermissions(this, LOCATION_PERMISSION_REQUEST_CODE, true);
    }

    private void chooseDevice() {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(TrackActivity.this);
        sBinding = DialogSelectDeviceBinding.inflate(getLayoutInflater(), null, false);
//        View mView = getLayoutInflater().inflate(R.layout.dialog_select_device, null, false);
        View mView = sBinding.getRoot();
        mBuilder.setView(mView);
        alertChoose = mBuilder.create();
        alertChoose.setCancelable(false);
        alertChoose.show();
    }
}
