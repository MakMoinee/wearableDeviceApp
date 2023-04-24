package com.example.wearabldeviceapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wearabldeviceapp.adapters.SelectDeviceAdapter;
import com.example.wearabldeviceapp.databinding.ActivityZoneBinding;
import com.example.wearabldeviceapp.databinding.DialogAddSafeZoneBinding;
import com.example.wearabldeviceapp.databinding.DialogSelectDeviceBinding;
import com.example.wearabldeviceapp.interfaces.SimpleRequestListener;
import com.example.wearabldeviceapp.models.DangerZone;
import com.example.wearabldeviceapp.models.Dependents;
import com.example.wearabldeviceapp.models.SafeZone;
import com.example.wearabldeviceapp.models.Users;
import com.example.wearabldeviceapp.preference.UserPref;
import com.example.wearabldeviceapp.services.LocalFirestore;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ZoneActivity extends AppCompatActivity implements OnMapReadyCallback {

    ActivityZoneBinding binding;
    DialogSelectDeviceBinding selectDependentBinding;
    GoogleMap mMap;
    private static final int PATTERN_GAP_LENGTH_PX = 20;
    private static final PatternItem DOT = new Dot();
    private static final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);
    private static final List<PatternItem> PATTERN_POLYGON_ALPHA = Arrays.asList(GAP, DOT);
    private static final int COLOR_WHITE_ARGB = 0xffffffff;
    private static final int COLOR_DARK_GREEN_ARGB = 0xff388E3C;
    private static final int COLOR_LIGHT_GREEN_ARGB = 0xff81C784;
    private static final int COLOR_DARK_ORANGE_ARGB = 0xffF57F17;
    private static final int COLOR_LIGHT_ORANGE_ARGB = 0xffF9A825;

    private static final int POLYGON_STROKE_WIDTH_PX = 8;
    private static final int PATTERN_DASH_LENGTH_PX = 20;

    private static final int COLOR_BLACK_ARGB = 0xff000000;
    private static final int POLYLINE_STROKE_WIDTH_PX = 12;
    private static final List<PatternItem> PATTERN_POLYGON_BETA =
            Arrays.asList(DOT, GAP, DOT, GAP);

    Polygon localPolygon = null, safeZonePolygon = null;
    List<LatLng> latLngList = new ArrayList<>();
    List<Marker> markerList = new ArrayList<>();
    int red = 200, green = 200, blue = 0;
    DialogAddSafeZoneBinding sBinding;
    AlertDialog alertAddSafeZone;
    Dependents selectedDependents;
    SafeZone selectSafeZone;
    DangerZone selectDangerZone;
    LocalFirestore fs;
    SelectDeviceAdapter dAdapter;
    List<Dependents> dependentsList;
    ProgressDialog pd;
    Polygon dangerZonePolygon;

    AlertDialog alertLoadZones, alertClearZones;
    List<LatLng> safeZoneLatLng = new ArrayList<>();
    List<LatLng> dangerZoneLatLng = new ArrayList<>();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityZoneBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        fs = new LocalFirestore(ZoneActivity.this);
        pd = new ProgressDialog(ZoneActivity.this);
        pd.setMessage("Sending Request ...");
        pd.setCancelable(false);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        setListeners();
        setTitle("Zones");
    }

    private void setListeners() {
        binding.btnDrawPolygon.setEnabled(false);
        binding.cbFillSafeZone.setChecked(false);
        binding.cbFillDangerZone.setChecked(false);
        binding.cbFillSafeZone.setEnabled(false);
        binding.cbFillDangerZone.setEnabled(false);

        binding.cbFillSafeZone.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (localPolygon == null) return;
                binding.cbFillDangerZone.setChecked(false);
                localPolygon.setFillColor(Color.rgb(0, green, 0));
            } else {
                localPolygon.setFillColor(Color.TRANSPARENT);
            }
        });

        binding.cbFillDangerZone.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (localPolygon == null) return;
                binding.cbFillSafeZone.setChecked(false);
                localPolygon.setFillColor(Color.rgb(red, 0, 0));
            } else {
                localPolygon.setFillColor(Color.TRANSPARENT);
            }
        });

        binding.btnDrawPolygon.setOnClickListener(v -> {
            if (localPolygon != null) localPolygon.remove();
            PolygonOptions options = new PolygonOptions().addAll(latLngList)
                    .clickable(true);
            localPolygon = mMap.addPolygon(options);
            localPolygon.setStrokeColor(Color.BLACK);
            binding.cbFillSafeZone.setEnabled(true);
            binding.cbFillDangerZone.setEnabled(true);
            if (binding.cbFillSafeZone.isChecked())
                localPolygon.setFillColor(Color.rgb(0, green, 0));
            if (binding.cbFillDangerZone.isChecked())
                localPolygon.setFillColor(Color.rgb(0, green, 0));
        });

        binding.btnClear.setOnClickListener(v -> {
            if (localPolygon != null) localPolygon.remove();
            for (Marker marker : markerList) marker.remove();
            latLngList.clear();
            markerList.clear();
            binding.cbFillSafeZone.setChecked(false);
            binding.cbFillDangerZone.setChecked(false);
            binding.cbFillSafeZone.setEnabled(false);
            binding.btnDrawPolygon.setEnabled(false);
            binding.cbFillDangerZone.setEnabled(false);
            if (safeZonePolygon != null) safeZonePolygon.remove();
            if (dangerZonePolygon != null) dangerZonePolygon.remove();
        });

        binding.btnSaveSafeZone.setOnClickListener(v -> {
            AlertDialog.Builder mBuilder = new AlertDialog.Builder(ZoneActivity.this);
            DialogInterface.OnClickListener dListener = (dialog, which) -> {
                switch (which) {
                    case DialogInterface.BUTTON_NEGATIVE:
                        AlertDialog.Builder tBuilder = new AlertDialog.Builder(ZoneActivity.this);
                        sBinding = DialogAddSafeZoneBinding.inflate(getLayoutInflater(), null, false);
                        tBuilder.setView(sBinding.getRoot());
                        loadDependents(dialog);
                        setAddSafeZoneListeners();
                        alertAddSafeZone = tBuilder.create();
                        alertAddSafeZone.show();
                        break;
                    default:
                        dialog.dismiss();
                        break;
                }
            };
            mBuilder.setMessage("Are You Sure You Want To Save This Selected Zone as Safe Zone?")
                    .setNegativeButton("Yes", dListener)
                    .setPositiveButton("No", dListener)
                    .setCancelable(false)
                    .show();
        });

        binding.btnSaveDangerZone.setOnClickListener(v -> {
            AlertDialog.Builder mBuilder = new AlertDialog.Builder(ZoneActivity.this);
            DialogInterface.OnClickListener dListener = (dialog, which) -> {
                switch (which) {
                    case DialogInterface.BUTTON_NEGATIVE:
                        AlertDialog.Builder tBuilder = new AlertDialog.Builder(ZoneActivity.this);
                        sBinding = DialogAddSafeZoneBinding.inflate(getLayoutInflater(), null, false);
                        tBuilder.setView(sBinding.getRoot());
                        sBinding.lblSafeZone.setText("Add Danger Zone");
                        loadDependents(dialog);
                        setAddDangeroneListeners();
                        alertAddSafeZone = tBuilder.create();
                        alertAddSafeZone.show();
                        break;
                    default:
                        dialog.dismiss();
                        break;
                }
            };
            mBuilder.setMessage("Are You Sure You Want To Save This Selected Zone as Danger Zone?")
                    .setNegativeButton("Yes", dListener)
                    .setPositiveButton("No", dListener)
                    .setCancelable(false)
                    .show();
        });
    }

    private void loadDependents(DialogInterface d) {
        Users users = new UserPref(ZoneActivity.this).getUser();
        fs.getAllDependents(users.getUserID(), new SimpleRequestListener() {
            @Override
            public void onSuccessDependent(List<Dependents> dep) {
                dependentsList = dep;
                List<String> nameList = new ArrayList<>();
                for (Dependents d : dependentsList) nameList.add(d.getName());
                dAdapter = new SelectDeviceAdapter(ZoneActivity.this, nameList);
                sBinding.spinDependent.setAdapter(dAdapter);
                sBinding.spinDependent.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selectedDependents = dependentsList.get(position);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        selectedDependents = null;
                    }
                });
            }

            @Override
            public void onError() {
                alertAddSafeZone.dismiss();
                d.dismiss();
                Toast.makeText(ZoneActivity.this, "There are no active dependents, please add first", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void setAddSafeZoneListeners() {

        sBinding.btnSave.setOnClickListener(v -> {
            if (selectedDependents == null) {
                Toast.makeText(ZoneActivity.this, "Please Choose First Dependents", Toast.LENGTH_SHORT).show();
            } else {
                pd.show();
                SafeZone sf = new SafeZone();
                sf.setDependentUserID(selectedDependents.getDeviceUserID());
                sf.setDependentDeviceID(selectedDependents.getDeviceID());
                sf.setUserID(selectedDependents.getUserID());
                sf.setLatLngRaw(new Gson().toJson(latLngList));
                fs.addSafeZone(sf, new SimpleRequestListener() {
                    @Override
                    public void onSuccess() {
                        pd.dismiss();
                        Toast.makeText(ZoneActivity.this, "Successfully Added Safe Zone", Toast.LENGTH_SHORT).show();
                        alertAddSafeZone.dismiss();
                    }

                    @Override
                    public void onError() {
                        pd.dismiss();
                        Toast.makeText(ZoneActivity.this, "Failed to add safe zone, please try again later", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void setAddDangeroneListeners() {

        sBinding.btnSave.setOnClickListener(v -> {
            if (selectedDependents == null) {
                Toast.makeText(ZoneActivity.this, "Please Choose First Dependents", Toast.LENGTH_SHORT).show();
            } else {
                pd.show();
                DangerZone sf = new DangerZone();
                sf.setDependentUserID(selectedDependents.getDeviceUserID());
                sf.setDependentDeviceID(selectedDependents.getDeviceID());
                sf.setUserID(selectedDependents.getUserID());
                sf.setLatLngRaw(new Gson().toJson(latLngList));
                fs.addDangerZone(sf, new SimpleRequestListener() {
                    @Override
                    public void onSuccess() {
                        pd.dismiss();
                        Toast.makeText(ZoneActivity.this, "Successfully Added Danger Zone", Toast.LENGTH_SHORT).show();
                        alertAddSafeZone.dismiss();
                    }

                    @Override
                    public void onError() {
                        pd.dismiss();
                        Toast.makeText(ZoneActivity.this, "Failed to add danger zone, please try again later", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        Polyline polyline1 = mMap.addPolyline(new PolylineOptions()
                .clickable(true)
                .add(new LatLng(7.91173, 125.09199)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(7.91173, 125.09199), 15));
        mMap.setOnMapClickListener(latLng -> {
            binding.btnDrawPolygon.setEnabled(true);
//            if (localPolygon == null) {
//                binding.cbFillSafeZone.setEnabled(false);
//                binding.cbFillDangerZone.setEnabled(false);
//            } else {
//                binding.cbFillSafeZone.setEnabled(true);
//                binding.cbFillDangerZone.setEnabled(true);
//
//            }
            MarkerOptions options = new MarkerOptions().position(latLng);
            Marker lMarker = mMap.addMarker(options);
            latLngList.add(latLng);
            markerList.add(lMarker);
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_load_zones:
                AlertDialog.Builder lBuilder = new AlertDialog.Builder(ZoneActivity.this);
                selectDependentBinding = DialogSelectDeviceBinding.inflate(getLayoutInflater(), null, false);
                lBuilder.setView(selectDependentBinding.getRoot());
                selectedDependents = null;
                loadSpinDependents();
                selectDependentBinding.spinMe.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selectedDependents = dependentsList.get(position);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        selectedDependents = null;
                    }
                });
                selectDependentBinding.btnSelectDevice.setOnClickListener(v -> {
                    if (selectedDependents != null) {
                        binding.btnClear.performClick();
                        loadSafeZone();
                        loadDangerZone();
                        alertLoadZones.dismiss();
                    }
                });
                alertLoadZones = lBuilder.create();
                alertLoadZones.show();
                return true;
            case R.id.action_clear:
                if(selectSafeZone == null && selectDangerZone ==null){
                    Toast.makeText(this, "Please load the zones first", Toast.LENGTH_SHORT).show();
                    return true;
                }else{
                    if (selectedDependents != null) {
                        pd.show();
                        if(selectSafeZone!=null){
                            fs.deleteSafeZone(selectSafeZone.getDocID(), new SimpleRequestListener() {
                                @Override
                                public void onSuccess() {

                                    if (safeZonePolygon != null) safeZonePolygon.remove();
                                    if (safeZoneLatLng.size() > 0) safeZoneLatLng.clear();
                                    if (dangerZoneLatLng.size() > 0) {
                                        fs.deleteDangerZone(selectDangerZone.getDocID(), new SimpleRequestListener() {
                                            @Override
                                            public void onSuccess() {
                                                pd.dismiss();
                                                Toast.makeText(ZoneActivity.this, "Successfully cleared zones", Toast.LENGTH_SHORT).show();
                                                if (dangerZonePolygon != null)
                                                    dangerZonePolygon.remove();
                                                if (dangerZoneLatLng.size() > 0)
                                                    dangerZoneLatLng.clear();
                                            }

                                            @Override
                                            public void onError() {
                                                pd.dismiss();
                                                SimpleRequestListener.super.onError();
                                            }
                                        });
                                    } else {
                                        pd.dismiss();
                                        Toast.makeText(ZoneActivity.this, "Successfully cleared zones", Toast.LENGTH_SHORT).show();
//                                        alertClearZones.dismiss();
                                    }
                                }

                                @Override
                                public void onError() {
                                    pd.dismiss();
                                    if (dangerZoneLatLng.size() > 0) {
                                        pd.show();
                                        fs.deleteDangerZone(selectDangerZone.getDocID(), new SimpleRequestListener() {
                                            @Override
                                            public void onSuccess() {
                                                pd.dismiss();
                                                Toast.makeText(ZoneActivity.this, "Successfully cleared zones", Toast.LENGTH_SHORT).show();
                                                if (dangerZonePolygon != null)
                                                    dangerZonePolygon.remove();
                                                if (dangerZoneLatLng.size() > 0)
                                                    dangerZoneLatLng.clear();
//                                                alertClearZones.dismiss();
                                            }

                                            @Override
                                            public void onError() {
                                                pd.dismiss();
                                                SimpleRequestListener.super.onError();
                                            }
                                        });
                                    }
                                }
                            });
                        }else{
                            if (dangerZoneLatLng.size() > 0) {
                                fs.deleteDangerZone(selectDangerZone.getDocID(), new SimpleRequestListener() {
                                    @Override
                                    public void onSuccess() {
                                        pd.dismiss();
                                        Toast.makeText(ZoneActivity.this, "Successfully cleared zones", Toast.LENGTH_SHORT).show();
                                        if (dangerZonePolygon != null)
                                            dangerZonePolygon.remove();
                                        if (dangerZoneLatLng.size() > 0)
                                            dangerZoneLatLng.clear();
                                    }

                                    @Override
                                    public void onError() {
                                        pd.dismiss();
                                        SimpleRequestListener.super.onError();
                                    }
                                });
                            }
                        }


                    }
                }
//                AlertDialog.Builder cBuilder = new AlertDialog.Builder(ZoneActivity.this);
//                selectDependentBinding = DialogSelectDeviceBinding.inflate(getLayoutInflater(), null, false);
//                cBuilder.setView(selectDependentBinding.getRoot());
//                selectedDependents = null;
//                loadSpinDependents();
//                selectDependentBinding.spinMe.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//                    @Override
//                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                        selectedDependents = dependentsList.get(position);
//                    }
//
//                    @Override
//                    public void onNothingSelected(AdapterView<?> parent) {
//                        selectedDependents = null;
//                    }
//                });
//                selectDependentBinding.btnSelectDevice.setOnClickListener(v -> {
//
//                });
//                alertClearZones = cBuilder.create();
//                alertClearZones.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void loadDangerZone() {
        if (selectedDependents != null) {
            fs.getDangerZones(selectedDependents.getUserID(), selectedDependents.getDeviceID(), new SimpleRequestListener() {
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
        if (selectedDependents != null) {
            fs.getSafeZones(selectedDependents.getUserID(), selectedDependents.getDeviceID(), new SimpleRequestListener() {
                @Override
                public void onSuccess(SafeZone safeZone) {
                    selectSafeZone = safeZone;
                    if (safeZonePolygon != null) safeZonePolygon.remove();
                    List<LatLng> list = new Gson().fromJson(safeZone.getLatLngRaw(), new TypeToken<List<LatLng>>() {
                    }.getType());

                    if (list != null) {
                        safeZoneLatLng = list;
                        PolygonOptions options = new PolygonOptions().addAll(list)
                                .clickable(true);
                        safeZonePolygon = mMap.addPolygon(options);
                        safeZonePolygon.setStrokeColor(Color.rgb(0, 200, 0));
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

    private void loadSpinDependents() {
        Users users = new UserPref(ZoneActivity.this).getUser();
        fs.getAllDependents(users.getUserID(), new SimpleRequestListener() {
            @Override
            public void onSuccessDependent(List<Dependents> d) {
                dependentsList = d;
                List<String> names = new ArrayList<>();
                for (Dependents dep : d) names.add(dep.getName());
                dAdapter = new SelectDeviceAdapter(ZoneActivity.this, names);
                selectDependentBinding.spinMe.setAdapter(dAdapter);
                selectDependentBinding.btnSelectDevice.setEnabled(true);
            }

            @Override
            public void onError() {
                SimpleRequestListener.super.onError();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_zone, menu);
        return true;
    }
}