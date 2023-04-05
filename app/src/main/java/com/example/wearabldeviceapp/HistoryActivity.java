package com.example.wearabldeviceapp;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wearabldeviceapp.databinding.ActivityHistoryBinding;

public class HistoryActivity extends AppCompatActivity {

    ActivityHistoryBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("History");
        binding = ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}
