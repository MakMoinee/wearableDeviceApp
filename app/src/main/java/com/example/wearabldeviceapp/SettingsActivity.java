package com.example.wearabldeviceapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wearabldeviceapp.adapters.SettingsAdapter;
import com.example.wearabldeviceapp.databinding.ActivitySettingsBinding;
import com.example.wearabldeviceapp.interfaces.AdapterListener;
import com.example.wearabldeviceapp.models.LocalSetting;
import com.example.wearabldeviceapp.models.Users;
import com.example.wearabldeviceapp.preference.UserPref;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    ActivitySettingsBinding binding;
    List<LocalSetting> settingsList;
    SettingsAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initializers();
        initListeners();
    }

    private void initListeners() {

    }

    private void initializers() {
        setTitle("Settings");
        settingsList = new ArrayList<>();
        LocalSetting setting = new LocalSetting();
        setting.setImageID(R.drawable.ic_out);
        setting.setText("Log Out");
        settingsList.add(setting);
        adapter = new SettingsAdapter(SettingsActivity.this, settingsList, new AdapterListener() {
            @Override
            public void onItemClickListener(int position) {
                LocalSetting s = settingsList.get(position);
                switch (s.getText()) {
                    case "Log Out":
                        AlertDialog.Builder mBuilder = new AlertDialog.Builder(SettingsActivity.this);
                        DialogInterface.OnClickListener dListener = (dialog, which) -> {
                            switch (which) {
                                case DialogInterface.BUTTON_NEGATIVE:
                                    new UserPref(SettingsActivity.this).storeUser(new Users());
                                    startActivity(new Intent(SettingsActivity.this, MainActivity.class));
                                    finish();
                                    break;
                                default:
                                    dialog.dismiss();
                                    break;
                            }
                        };
                        mBuilder.setMessage("Are You Sure You Want To Log Out?")
                                .setNegativeButton("Yes", dListener)
                                .setPositiveButton("No", dListener)
                                .setCancelable(false)
                                .show();
                        break;
                    default:
                        break;
                }
            }
        });
        binding.recycler.setLayoutManager(new LinearLayoutManager(SettingsActivity.this, RecyclerView.VERTICAL, false));
        binding.recycler.addItemDecoration(new DividerItemDecoration(SettingsActivity.this, DividerItemDecoration.VERTICAL));
        binding.recycler.setAdapter(adapter);
    }
}
