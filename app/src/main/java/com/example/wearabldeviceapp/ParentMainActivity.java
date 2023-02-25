package com.example.wearabldeviceapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.wearabldeviceapp.adapters.HomeAdapter;
import com.example.wearabldeviceapp.databinding.ActivityParentsFormBinding;
import com.example.wearabldeviceapp.interfaces.AdapterListener;
import com.example.wearabldeviceapp.models.LocalSetting;
import com.example.wearabldeviceapp.models.Users;
import com.example.wearabldeviceapp.preference.UserPref;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.List;

public class ParentMainActivity extends AppCompatActivity {

    private ActivityParentsFormBinding binding;
    HomeAdapter adapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityParentsFormBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initializers();
        initListeners();
    }

    private void initializers() {
        List<LocalSetting> list = new ArrayList<>();
        LocalSetting setting = new LocalSetting();
        setting.setText("Register Device");
        setting.setImageID(R.drawable.device);
        list.add(setting);
        setting = new LocalSetting();
        setting.setText("Add Dependent");
        setting.setImageID(R.drawable.dependent);
        list.add(setting);

        setting = new LocalSetting();
        setting.setText("Track");
        setting.setImageID(R.drawable.tracking);
        list.add(setting);

        setting = new LocalSetting();
        setting.setText("Zones");
        setting.setImageID(R.drawable.zone);
        list.add(setting);

        adapter = new HomeAdapter(ParentMainActivity.this, list, new AdapterListener() {
            @Override
            public void onItemClickListener(int position) {
                switch (position) {
                    case 0:
                        startActivity(new Intent(ParentMainActivity.this, RegisterDeviceActivity.class));
                        break;
                    case 1:
                        startActivity(new Intent(ParentMainActivity.this, DependentActivity.class));
                        break;
                    case 2:
                        startActivity(new Intent(ParentMainActivity.this, TrackActivity.class));
                        break;
                    case 3:
                        startActivity(new Intent(ParentMainActivity.this, ZoneActivity.class));
                        break;
                    default:
                        break;
                }
            }
        });
        binding.recyler.setLayoutManager(new GridLayoutManager(ParentMainActivity.this, 2));
        binding.recyler.setAdapter(adapter);
    }

    private void initListeners() {
        binding.bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_settings:
                        startActivity(new Intent(ParentMainActivity.this, SettingsActivity.class));
                        break;
                    case R.id.nav_home:
                        break;
                }
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Users users = new UserPref(ParentMainActivity.this).getUser();
        if (users == null) {
            Intent intent = new Intent(ParentMainActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }
}
