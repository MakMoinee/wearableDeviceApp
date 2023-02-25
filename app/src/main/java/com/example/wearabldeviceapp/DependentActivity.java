package com.example.wearabldeviceapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.wearabldeviceapp.adapters.DependentsAdapter;
import com.example.wearabldeviceapp.adapters.SelectDeviceAdapter;
import com.example.wearabldeviceapp.databinding.ActivityDependentBinding;
import com.example.wearabldeviceapp.databinding.DependentFormBinding;
import com.example.wearabldeviceapp.interfaces.AdapterListener;
import com.example.wearabldeviceapp.interfaces.SimpleRequestListener;
import com.example.wearabldeviceapp.models.Dependents;
import com.example.wearabldeviceapp.models.LocalGPS;
import com.example.wearabldeviceapp.models.Users;
import com.example.wearabldeviceapp.preference.UserPref;
import com.example.wearabldeviceapp.services.LocalFirestore;

import java.util.ArrayList;
import java.util.List;

public class DependentActivity extends AppCompatActivity {

    ActivityDependentBinding binding;
    DependentFormBinding fBinding;

    AlertDialog alertAddDependent;
    LocalFirestore fs;
    List<LocalGPS> gpsList;
    SelectDeviceAdapter adapter;
    LocalGPS selectedDevice;
    String userID = "";
    ProgressDialog pd;
    DependentsAdapter dAdapter;
    List<Dependents> dependentsList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDependentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        fs = new LocalFirestore(DependentActivity.this);
        pd = new ProgressDialog(DependentActivity.this);
        pd.setMessage("Sending Request ...");
        pd.setCancelable(false);
        setTitle("Dependents");
        loadAllDependents();
        setListeners();
    }

    private void loadAllDevices() {
        Users users = new UserPref(DependentActivity.this).getUser();
        userID = users.getUserID();
        fs.getAllDevice(users.getUserID(), new SimpleRequestListener() {
            @Override
            public void onSuccess(List<LocalGPS> g) {
                gpsList = g;
                List<String> str = new ArrayList<>();
                if (gpsList != null) {

                    for (LocalGPS gpss : gpsList) {
                        boolean isCutLoop = false;
                        if (dependentsList != null) {
                            for (Dependents dep : dependentsList) {
                                if (dep.getDeviceID() == gpss.getDeviceID()) {
                                    isCutLoop = true;
                                    break;
                                }
                            }
                        } else {
                            str.add(Integer.toString(gpss.getDeviceID()));
                        }
                        if (isCutLoop) {
                            break;
                        }
                    }
                }
                if (str.size() > 0) {
                    adapter = new SelectDeviceAdapter(DependentActivity.this, str);
                    fBinding.spinMe.setAdapter(adapter);
                    fBinding.spinMe.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedDevice = gpsList.get(position);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            selectedDevice = null;
                        }
                    });
                } else {
                    Toast.makeText(DependentActivity.this, "There are no available devices, please add device or remove independent which has device associated", Toast.LENGTH_SHORT).show();
                    alertAddDependent.dismiss();
                }
            }

            @Override
            public void onError() {
                Toast.makeText(DependentActivity.this, "There are no active devices associated, Please add device first", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void setListeners() {
        binding.btnAddDependent.setOnClickListener(v -> {
            AlertDialog.Builder sBuilder = new AlertDialog.Builder(DependentActivity.this);
            fBinding = DependentFormBinding.inflate(getLayoutInflater(), null, false);
            sBuilder.setView(fBinding.getRoot());
            fBinding.btnAddDependent.setOnClickListener(v1 -> {
                String name = fBinding.editName.getText().toString();
                String age = fBinding.editAge.getText().toString();

                if (name.equals("") || age.equals("") || selectedDevice == null) {
                    Toast.makeText(DependentActivity.this, "Please Don't Leave Empty Fields", Toast.LENGTH_SHORT).show();
                } else {
                    pd.show();
                    Dependents dependents = new Dependents();
                    dependents.setName(name);
                    dependents.setAge(Integer.parseInt(age));
                    dependents.setDeviceID(selectedDevice.getDeviceID());
                    dependents.setUserID(userID);
                    dependents.setDeviceUserID(selectedDevice.getUserID());
                    fs.addDependent(dependents, new SimpleRequestListener() {
                        @Override
                        public void onSuccess() {
                            pd.dismiss();
                            alertAddDependent.dismiss();
                            Toast.makeText(DependentActivity.this, "Successfully Added Dependent", Toast.LENGTH_SHORT).show();
                            loadAllDependents();
                        }

                        @Override
                        public void onError() {
                            pd.dismiss();
                            alertAddDependent.dismiss();
                            Toast.makeText(DependentActivity.this, "Failed to add dependent, please try again later", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
            loadAllDevices();
            alertAddDependent = sBuilder.create();
            alertAddDependent.show();
        });

    }


    private void loadAllDependents() {
        Users users = new UserPref(DependentActivity.this).getUser();
        userID = users.getUserID();
        fs.getAllDependents(userID, new SimpleRequestListener() {
            @Override
            public void onSuccessDependent(List<Dependents> d) {
                dependentsList = d;
                dAdapter = new DependentsAdapter(DependentActivity.this, dependentsList, new AdapterListener() {
                    @Override
                    public void onItemLongClickListener(int position) {
                        Dependents dep = dependentsList.get(position);
                        AlertDialog.Builder mBuilder = new AlertDialog.Builder(DependentActivity.this);
                        DialogInterface.OnClickListener dListener = (dialog, which) -> {
                            switch (which) {
                                case DialogInterface.BUTTON_NEGATIVE:
                                    fs.deleteDependent(dep.getDocID(), new SimpleRequestListener() {
                                        @Override
                                        public void onSuccess() {
                                            Toast.makeText(DependentActivity.this, "Successfully Deleted Dependent", Toast.LENGTH_SHORT).show();
                                            binding.recycler.setAdapter(null);
                                            loadAllDependents();
                                        }

                                        @Override
                                        public void onError() {
                                            Toast.makeText(DependentActivity.this, "Failed To Delete Dependent", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    break;
                                default:
                                    dialog.dismiss();
                                    break;
                            }
                        };
                        mBuilder.setMessage("Do You Want To Delete This Dependent?")
                                .setNegativeButton("Yes", dListener)
                                .setPositiveButton("No", dListener)
                                .setCancelable(false)
                                .show();
                    }
                });
                binding.recycler.setLayoutManager(new LinearLayoutManager(DependentActivity.this));
                binding.recycler.setItemAnimator(new DefaultItemAnimator());
                binding.recycler.setAdapter(dAdapter);
            }

            @Override
            public void onError() {
                Toast.makeText(DependentActivity.this, "There are no active dependents associated", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

    }
}
