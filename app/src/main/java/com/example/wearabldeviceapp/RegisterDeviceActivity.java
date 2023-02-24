package com.example.wearabldeviceapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.wearabldeviceapp.adapters.RegisterDeviceAdapter;
import com.example.wearabldeviceapp.databinding.ActivityRegisterDeviceBinding;
import com.example.wearabldeviceapp.interfaces.AdapterListener;
import com.example.wearabldeviceapp.interfaces.SimpleRequestListener;
import com.example.wearabldeviceapp.models.LocalGPS;
import com.example.wearabldeviceapp.models.Users;
import com.example.wearabldeviceapp.preference.UserPref;
import com.example.wearabldeviceapp.services.LocalFirestore;
import com.example.wearabldeviceapp.services.LocalRequest;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

public class RegisterDeviceActivity extends AppCompatActivity {

    ActivityRegisterDeviceBinding binding;
    AlertDialog alertDialogAddDevice;
    LocalRequest req;
    ProgressDialog pd;
    RegisterDeviceAdapter adapter;

    LocalFirestore fs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterDeviceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setValues();
        setListeners();
        loadData();
    }

    private void loadData() {
        try {
            Users users = new UserPref(RegisterDeviceActivity.this).getUser();
            if (users != null) {
                fs.getAllDevice(users.getUserID(), new SimpleRequestListener() {
                    @Override
                    public void onSuccess(List<LocalGPS> gpsList) {
                        adapter = new RegisterDeviceAdapter(RegisterDeviceActivity.this, gpsList, new AdapterListener() {
                            @Override
                            public void onItemLongClickListener(int position) {
                                AlertDialog.Builder sBuilder = new AlertDialog.Builder(RegisterDeviceActivity.this);
                                DialogInterface.OnClickListener dListener = (dialog, which) -> {
                                    if (which == DialogInterface.BUTTON_NEGATIVE) {
                                        LocalGPS gps = gpsList.get(position);
                                        fs.deleteDevice(gps.getDocument(), new SimpleRequestListener() {
                                            @Override
                                            public void onSuccess() {
                                                Toast.makeText(RegisterDeviceActivity.this, "Successfully Deleted Device", Toast.LENGTH_SHORT).show();
                                                binding.recycler.setAdapter(null);
                                                loadData();
                                            }

                                            @Override
                                            public void onError() {
                                                Toast.makeText(RegisterDeviceActivity.this, "Failed To Delete Device", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } else {
                                        dialog.dismiss();
                                    }
                                };
                                sBuilder.setMessage("Are You Sure You Want To Delete Device?")
                                        .setNegativeButton("Yes", dListener)
                                        .setPositiveButton("No", dListener)
                                        .setCancelable(false)
                                        .show();
                            }
                        });
                        binding.recycler.setLayoutManager(new LinearLayoutManager(RegisterDeviceActivity.this));
                        binding.recycler.setAdapter(adapter);
                    }

                    @Override
                    public void onError() {
                        Toast.makeText(RegisterDeviceActivity.this, "There are no registered devices", Toast.LENGTH_SHORT).show();
                    }
                });
            }

        } catch (Exception e) {

        }
    }

    private void setListeners() {
        binding.btnAdd.setOnClickListener(v -> {
            AlertDialog.Builder mBuilder = new AlertDialog.Builder(RegisterDeviceActivity.this);
            DialogInterface.OnClickListener dListener = (dialog, which) -> {
                switch (which) {
                    case DialogInterface.BUTTON_NEGATIVE:
                        AlertDialog.Builder tBuilder = new AlertDialog.Builder(RegisterDeviceActivity.this);
                        View mView = getLayoutInflater().inflate(R.layout.dialog_add_device, null, false);
                        initDialogViews(mView);
                        tBuilder.setView(mView);
                        alertDialogAddDevice = tBuilder.create();
                        alertDialogAddDevice.show();

                        break;
                    default:
                        dialog.dismiss();
                        break;
                }
            };
            mBuilder.setMessage("Do You Want To Add A Device?")
                    .setNegativeButton("Yes", dListener)
                    .setPositiveButton("No", dListener)
                    .setCancelable(false)
                    .show();
        });

    }

    private void initDialogViews(View mView) {
        Button btnTestDevice = mView.findViewById(R.id.btnTestDevice);
        Button btnRegisterDevice = mView.findViewById(R.id.btnRegisterDevice);
        Button btnClear = mView.findViewById(R.id.btnClear);

        TextInputEditText editUserID = mView.findViewById(R.id.editUserID);
        TextInputEditText editDeviceID = mView.findViewById(R.id.editDeviceID);

        TextInputLayout layoutUserID = mView.findViewById(R.id.layoutUserID);
        TextInputLayout layoutDeviceID = mView.findViewById(R.id.layoutDeviceID);


        btnTestDevice.setOnClickListener(v -> {
            String userID = editUserID.getText().toString();
            String deviceID = editDeviceID.getText().toString();

            if (userID.equals("") || deviceID.equals("")) {
                Toast.makeText(RegisterDeviceActivity.this, "Please Don't Leave Empty Fields", Toast.LENGTH_SHORT).show();
            } else {
                pd.show();
                LocalGPS gps = new LocalGPS();
                gps.setUserID(Integer.parseInt(userID));
                gps.setDeviceID(Integer.parseInt(deviceID));
                req.getCoordinates(gps, new SimpleRequestListener() {
                    @Override
                    public void onSuccessWithStr(String uuid) {
                        pd.dismiss();
                        btnRegisterDevice.setEnabled(true);
                        btnClear.setEnabled(true);
                        layoutUserID.setEnabled(false);
                        layoutDeviceID.setEnabled(false);

                        Toast.makeText(mView.getContext(), "Device is available", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError() {
                        pd.dismiss();
                        Toast.makeText(RegisterDeviceActivity.this, "An unexpected error occurred, Please try again later", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        btnClear.setOnClickListener(v -> {
            btnRegisterDevice.setEnabled(false);
            btnClear.setEnabled(false);
            layoutUserID.setEnabled(true);
            layoutDeviceID.setEnabled(true);
            editUserID.setText("");
            editDeviceID.setText("");
        });

        btnRegisterDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userID = editUserID.getText().toString();
                String deviceID = editDeviceID.getText().toString();

                if (userID.equals("") || deviceID.equals("")) {
                    Toast.makeText(RegisterDeviceActivity.this, "Please Don't Leave Empty Fields", Toast.LENGTH_SHORT).show();
                } else {
                    Users users = new UserPref(RegisterDeviceActivity.this).getUser();
                    if (users != null) {
                        pd.show();
                        LocalGPS gps = new LocalGPS();
                        gps.setUserID(Integer.parseInt(userID));
                        gps.setDeviceID(Integer.parseInt(deviceID));
                        fs.addDevice(users.getUserID(), gps, new SimpleRequestListener() {
                            @Override
                            public void onSuccess() {
                                pd.dismiss();
                                alertDialogAddDevice.dismiss();
                                Toast.makeText(RegisterDeviceActivity.this, "Successfully Added Device", Toast.LENGTH_SHORT).show();
                                binding.recycler.setAdapter(null);
                                loadData();
                            }

                            @Override
                            public void onError() {
                                pd.dismiss();
                                Toast.makeText(RegisterDeviceActivity.this, "Failed To Add Device", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                }
            }
        });
    }

    private void setValues() {
        setTitle("Register Devices");
        req = new LocalRequest(RegisterDeviceActivity.this);
        pd = new ProgressDialog(RegisterDeviceActivity.this);
        pd.setMessage("Sending Request ...");
        pd.setCancelable(false);
        fs = new LocalFirestore(RegisterDeviceActivity.this);
    }
}
