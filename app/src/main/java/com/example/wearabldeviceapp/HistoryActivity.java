package com.example.wearabldeviceapp;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.wearabldeviceapp.adapters.HistoryAdapter;
import com.example.wearabldeviceapp.databinding.ActivityHistoryBinding;
import com.example.wearabldeviceapp.interfaces.AdapterListener;
import com.example.wearabldeviceapp.interfaces.SimpleRequestListener;
import com.example.wearabldeviceapp.models.History;
import com.example.wearabldeviceapp.models.Users;
import com.example.wearabldeviceapp.preference.UserPref;
import com.example.wearabldeviceapp.services.LocalFirestore;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    ActivityHistoryBinding binding;

    List<History> historyList;

    HistoryAdapter adapter;

    LocalFirestore fs;
    String userID = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("History");
        binding = ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initValues();
        setListeners();

    }

    private void setListeners() {
        binding.refresh.setOnRefreshListener(() -> {
            binding.recycler.setAdapter(null);
            loadValues();

        });
    }

    private void initValues() {
        historyList = new ArrayList<>();
        fs = new LocalFirestore(getApplicationContext());
        loadValues();
    }

    private void loadValues() {
        Users users = new UserPref(HistoryActivity.this).getUser();
        userID = users.getUserID();
        fs.getAllHistory(users.getUserID(), new SimpleRequestListener() {
            @Override
            public void onSuccessHistory(List<History> h) {
                binding.refresh.setRefreshing(false);
                historyList = h;
                adapter = new HistoryAdapter(HistoryActivity.this, historyList, new AdapterListener() {
                    @Override
                    public void onItemClickListener(int position) {
                        AdapterListener.super.onItemClickListener(position);
                    }
                });
                binding.recycler.setLayoutManager(new LinearLayoutManager(HistoryActivity.this));
                binding.recycler.setAdapter(adapter);
            }

            @Override
            public void onError() {
                Toast.makeText(HistoryActivity.this, "There are no history of dependents yet", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_history, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                AlertDialog.Builder dBuilder = new AlertDialog.Builder(HistoryActivity.this);
                DialogInterface.OnClickListener dListener = (dialog, which) -> {
                    switch (which) {
                        case DialogInterface.BUTTON_NEGATIVE:
                            fs.deleteHistory(userID, new SimpleRequestListener() {
                                @Override
                                public void onSuccess() {
                                    Toast.makeText(HistoryActivity.this, "Successfully Deleted History", Toast.LENGTH_SHORT).show();
                                    binding.recycler.setAdapter(null);
                                    loadValues();
                                }

                                @Override
                                public void onError() {
                                    Toast.makeText(HistoryActivity.this, "Failed to delete history", Toast.LENGTH_SHORT).show();
                                }
                            });
                            break;
                        default:
                            dialog.dismiss();
                            break;
                    }
                };
                dBuilder.setMessage("Are You Want To Delete History?")
                        .setNegativeButton("Yes", dListener)
                        .setPositiveButton("No", dListener)
                        .show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
