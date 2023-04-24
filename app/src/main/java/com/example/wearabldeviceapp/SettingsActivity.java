package com.example.wearabldeviceapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebSettings;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wearabldeviceapp.adapters.SettingsAdapter;
import com.example.wearabldeviceapp.common.Constants;
import com.example.wearabldeviceapp.databinding.ActivitySettingsBinding;
import com.example.wearabldeviceapp.databinding.DialogTermsConditionBinding;
import com.example.wearabldeviceapp.interfaces.AdapterListener;
import com.example.wearabldeviceapp.interfaces.SimpleRequestListener;
import com.example.wearabldeviceapp.models.DangerZone;
import com.example.wearabldeviceapp.models.Dependents;
import com.example.wearabldeviceapp.models.LocalGPS;
import com.example.wearabldeviceapp.models.LocalSetting;
import com.example.wearabldeviceapp.models.SafeZone;
import com.example.wearabldeviceapp.models.Users;
import com.example.wearabldeviceapp.preference.UserPref;
import com.example.wearabldeviceapp.services.HistoryWorkerManager;
import com.example.wearabldeviceapp.services.LocalFirestore;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    ActivitySettingsBinding binding;
    DialogTermsConditionBinding termsConditionBinding;
    List<LocalSetting> settingsList;
    SettingsAdapter adapter;
    AlertDialog tcDialog;
    HistoryWorkerManager historyWorkerManager;
    LocalFirestore fs;

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
        fs = new LocalFirestore(getApplicationContext());
        historyWorkerManager = new HistoryWorkerManager(getApplicationContext());
        setTitle("Settings");
        settingsList = new ArrayList<>();
        LocalSetting setting = new LocalSetting();
        setting.setImageID(R.drawable.ic_terms);
        setting.setText("Terms and Conditions");
        settingsList.add(setting);


        setting = new LocalSetting();
        setting.setImageID(R.drawable.ic_out);
        setting.setText("Log Out");
        settingsList.add(setting);


        adapter = new SettingsAdapter(SettingsActivity.this, settingsList, new AdapterListener() {
            @Override
            public void onItemClickListener(int position) {
                LocalSetting s = settingsList.get(position);
                switch (s.getText()) {
                    case "Terms and Conditions":
                        termsConditionBinding = DialogTermsConditionBinding.inflate(getLayoutInflater(), null, false);
                        AlertDialog.Builder tBuilder = new AlertDialog.Builder(SettingsActivity.this);
                        tBuilder.setView(termsConditionBinding.getRoot());
                        WebSettings ws = termsConditionBinding.webView.getSettings();
                        ws.setJavaScriptEnabled(true);
                        termsConditionBinding.webView.loadUrl("https://www.app-privacy-policy.com/live.php?token=u6ypb2RuTNjEzNQzz2j26U5QtEbXgdi4");
                        tcDialog = tBuilder.create();
                        tcDialog.show();
                        break;
                    case "Log Out":
                        AlertDialog.Builder mBuilder = new AlertDialog.Builder(SettingsActivity.this);
                        DialogInterface.OnClickListener dListener = (dialog, which) -> {
                            switch (which) {
                                case DialogInterface.BUTTON_NEGATIVE:
                                    new UserPref(SettingsActivity.this).storeUser(new Users());
                                    if (Constants.scheduler != null)
                                        Constants.scheduler.shutdown();
                                    historyWorkerManager.stopHistoryWorker();
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
