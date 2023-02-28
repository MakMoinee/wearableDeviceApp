package com.example.wearabldeviceapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.wearabldeviceapp.adapters.SelectDeviceAdapter;
import com.example.wearabldeviceapp.databinding.ActivityTrackBinding;
import com.example.wearabldeviceapp.databinding.DialogSelectDeviceBinding;
import com.example.wearabldeviceapp.interfaces.SimpleRequestListener;
import com.example.wearabldeviceapp.models.D;
import com.example.wearabldeviceapp.models.DangerZone;
import com.example.wearabldeviceapp.models.Dependents;
import com.example.wearabldeviceapp.models.Devices;
import com.example.wearabldeviceapp.models.LocalGPS;
import com.example.wearabldeviceapp.models.SafeZone;
import com.example.wearabldeviceapp.models.Users;
import com.example.wearabldeviceapp.preference.UserPref;
import com.example.wearabldeviceapp.services.LocalFirestore;
import com.example.wearabldeviceapp.services.LocalRequest;
import com.example.wearabldeviceapp.services.NotifReceiver;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TrackActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int M_MAX_ENTRIES = 5;
    ActivityTrackBinding binding;
    private GoogleMap mMap;
    AlertDialog alertChoose;
    DialogSelectDeviceBinding sBinding;

    List<LocalGPS> gpsList = new ArrayList<>();
    List<Dependents> dependentsList = new ArrayList<>();
    Polygon safeZonePolygon = null;
    LocalFirestore fs;

    SelectDeviceAdapter adapter;

    String deviceID = "";
    LocalGPS selectedDevice = new LocalGPS();
    Dependents selectedDependent = new Dependents();

    LocalRequest req;

    Devices deviceLocation;
    Marker markerYourLocation;
    Marker markerDeviceLocation;
    PlacesClient placesClient;
    FusedLocationProviderClient fusedLocationProviderClient;
    Boolean locationPermissionGranted = false;
    Location lastKnownLocation;
    Location currentLocation;

    List<LatLng> safeZoneLatLng = new ArrayList<>();
    DangerZone selectDangerZone;
    Polygon dangerZonePolygon;
    List<LatLng> dangerZoneLatLng = new ArrayList<>();


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
        getLocationPermission();

        fetchLocation();

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera


        loadDevices();
        chooseDevice();
        initListeners();

    }

    private void loadDangerZone() {
        if (selectedDependent != null) {
            fs.getDangerZones(selectedDependent.getUserID(), selectedDependent.getDeviceID(), new SimpleRequestListener() {
                @Override
                public void onSuccess(DangerZone dangerZone) {
                    selectDangerZone = dangerZone;
                    if (dangerZonePolygon != null) dangerZonePolygon.remove();
                    List<LatLng> list = new Gson().fromJson(selectDangerZone.getLatLngRaw(), new TypeToken<List<LatLng>>() {
                    }.getType());

                    if (list != null) {
                        dangerZoneLatLng = list;
                        PolygonOptions options = new PolygonOptions().addAll(list)
                                .clickable(true);
                        dangerZonePolygon = mMap.addPolygon(options);
                        dangerZonePolygon.setStrokeColor(Color.rgb(200, 0, 0));
                        dangerZonePolygon.setFillColor(Color.rgb(200, 0, 0));
                    }


                }

                @Override
                public void onError() {
                    SimpleRequestListener.super.onError();
                }
            });
        }
    }

    private void loadSafeZone() {
        if (selectedDependent != null) {
            fs.getSafeZones(selectedDependent.getUserID(), selectedDependent.getDeviceID(), new SimpleRequestListener() {
                @Override
                public void onSuccess(SafeZone safeZone) {
                    if (safeZonePolygon != null) safeZonePolygon.remove();
                    List<LatLng> latLngList = new Gson().fromJson(safeZone.getLatLngRaw(), new TypeToken<List<LatLng>>() {
                    }.getType());

                    if (latLngList != null) {
                        safeZoneLatLng = latLngList;
                        PolygonOptions options = new PolygonOptions().addAll(latLngList)
                                .clickable(true);
                        safeZonePolygon = mMap.addPolygon(options);
                        safeZonePolygon.setStrokeColor(Color.BLACK);
                        safeZonePolygon.setFillColor(Color.rgb(0, 200, 0));
                    }


                }

                @Override
                public void onError() {
                    SimpleRequestListener.super.onError();
                }
            });
        }
    }

    private void runMyThread() {
        Runnable helloRunnable = new Runnable() {
            public void run() {
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
                            fetchLocation();
//                            if (currentLocation != null) {
//                                LatLng sydney = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
//                                markerYourLocation = mMap.addMarker(new MarkerOptions()
//                                        .position(sydney)
//                                        .title("Your Location"));
//                                markerYourLocation.setTag(0);
//                            }
                            //show notif
                            Boolean isAtSafeZone = false;
                            Boolean isAtDangerZone = false;
                            if (safeZoneLatLng.size() > 0) {
                                isAtSafeZone = PolyUtil.containsLocation(new LatLng(deviceLocation.getBaiduLat(), deviceLocation.getBaiduLng()), safeZoneLatLng, false);
                            }

                            if (dangerZoneLatLng.size() > 0) {
                                isAtDangerZone = PolyUtil.containsLocation(new LatLng(deviceLocation.getBaiduLat(), deviceLocation.getBaiduLng()), safeZoneLatLng, false);
                            }


                            if (isAtDangerZone && dangerZoneLatLng.size() > 0) {
                                showDangerNotif();
                            } else {
                                if (!isAtSafeZone && safeZoneLatLng.size() > 0) {
                                    showNotif();
                                }
                            }


                            LatLng dLocation = new LatLng(deviceLocation.getBaiduLat(), deviceLocation.getBaiduLng());

                            markerDeviceLocation = mMap.addMarker(new MarkerOptions()
                                    .position(dLocation)
                                    .title(String.format("%s Location", selectedDependent.getName())));
                            markerDeviceLocation.setTag(0);
//                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(dLocation, 15));
                        }
                    }

                    @Override
                    public void onError() {
                        SimpleRequestListener.super.onError();
                    }
                });
            }
        };
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(helloRunnable, 0, 10, TimeUnit.SECONDS);
    }

    private void showDangerNotif() {
        Intent intent = new Intent("com.example.TRIGGER_NOTIF");
        intent.putExtra("msg", "Dependent is in danger zone. Please act as soon as possible");
        sendBroadcast(intent);
    }

    private void showNotif() {
        Intent intent = new Intent("com.example.TRIGGER_NOTIF");
        intent.putExtra("msg", "Dependent is not in safe zone. Please act as soon as possible");
        sendBroadcast(intent);
    }

    private BitmapDescriptor BitmapFromVector(Context context, int vectorResId) {
        // below line is use to generate a drawable.
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);

        // below line is use to set bounds to our vector drawable.
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());

        // below line is use to create a bitmap for our
        // drawable which we have added.
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

        // below line is use to add bitmap in our canvas.
        Canvas canvas = new Canvas(bitmap);

        // below line is use to draw our
        // vector drawable in canvas.
        vectorDrawable.draw(canvas);

        // after generating our bitmap we are returning our bitmap.
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void initListeners() {
        sBinding.btnSelectDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertChoose.dismiss();
                if (selectedDependent != null) {
                    selectedDevice = new LocalGPS();
                    selectedDevice.setDeviceID(selectedDependent.getDeviceID());
                    selectedDevice.setUserID(selectedDependent.getDeviceUserID());
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
                                fetchLocation();
//                                LatLng sydney = new LatLng(7.91173, 125.09199);
                                LatLng dLocation = new LatLng(deviceLocation.getBaiduLat(), deviceLocation.getBaiduLng());
//                                markerYourLocation = mMap.addMarker(new MarkerOptions()
//                                        .position(sydney)
//                                        .title("Your Location"));
//                                markerYourLocation.setTag(0);
                                markerDeviceLocation = mMap.addMarker(new MarkerOptions()
                                        .position(dLocation)
                                        .title(String.format("%s Location", selectedDependent.getName())));
                                markerDeviceLocation.setTag(0);
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(dLocation, 15));
                                runMyThread();
                            }
                        }

                        @Override
                        public void onError() {
                            SimpleRequestListener.super.onError();
                        }
                    });

                    loadSafeZone();
                    loadDangerZone();
                }
            }
        });
    }

    private void loadDevices() {

        Users users = new UserPref(TrackActivity.this).getUser();
        if (users != null) {
            fs.getAllDependents(users.getUserID(), new SimpleRequestListener() {

                @Override
                public void onSuccessDependent(List<Dependents> d) {
                    dependentsList = d;
                    List<String> str = new ArrayList<>();
                    if (dependentsList != null) {
                        for (Dependents dep : dependentsList) {
                            str.add(dep.getName());
                        }
                    }
                    adapter = new SelectDeviceAdapter(TrackActivity.this, str);
                    sBinding.spinMe.setAdapter(adapter);
                    sBinding.spinMe.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedDependent = dependentsList.get(position);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            selectedDependent = null;
                        }
                    });
                    sBinding.btnSelectDevice.setEnabled(true);
                }

                @Override
                public void onError() {
                    Toast.makeText(TrackActivity.this, "There are no active dependents associated", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }

    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        if (requestCode
                == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

        updateLocationUI();
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()), 15));
                            }
                        } else {
//                            Log.d(TAG, "Current location is null. Using defaults.");
//                            Log.e(TAG, "Exception: %s", task.getException());
                            lastKnownLocation.setLatitude(7.91173);
                            lastKnownLocation.setLongitude(125.09199);
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(new LatLng(7.91173, 125.09199), 15));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    private void chooseDevice() {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(TrackActivity.this);
        sBinding = DialogSelectDeviceBinding.inflate(getLayoutInflater(), null, false);
        View mView = sBinding.getRoot();
        mBuilder.setView(mView);
        sBinding.btnSelectDevice.setEnabled(false);
        alertChoose = mBuilder.create();
        alertChoose.setCancelable(false);
        alertChoose.show();
    }

    private void fetchLocation() {

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                5000,
                5000, new LocationListener() {
                    @Override
                    public void onLocationChanged(@NonNull Location location) {
                        Log.e("LOCATION:", location.toString());
                    }

                    @Override
                    public void onProviderEnabled(@NonNull String provider) {

                    }

                    @Override
                    public void onProviderDisabled(@NonNull String provider) {

                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {

                    }
                });
        boolean isavailable = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (isavailable) {

            Location loc = lm.getLastKnownLocation("gps");

            if (loc != null) {
                double latitude = loc.getLatitude();
                double longitude = loc.getLongitude();

              //  Toast.makeText(TrackActivity.this, "Longitude is  " + longitude + "   Latitude is   " + latitude, Toast.LENGTH_LONG).show();

            }
        }
//        if (location != null) {
//            currentLocation = location;
//            LatLng sydney = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
//            markerYourLocation = mMap.addMarker(new MarkerOptions()
//                    .position(sydney)
//                    .title("Your Location"));
//            markerYourLocation.setTag(0);
//        }
    }

}
