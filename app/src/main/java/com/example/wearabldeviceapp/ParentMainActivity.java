package com.example.wearabldeviceapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wearabldeviceapp.databinding.ActivityParentsFormBinding;
import com.example.wearabldeviceapp.models.Users;
import com.example.wearabldeviceapp.preference.UserPref;
import com.google.android.material.navigation.NavigationBarView;

public class ParentMainActivity extends AppCompatActivity {

    private ActivityParentsFormBinding binding;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityParentsFormBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initListeners();
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
