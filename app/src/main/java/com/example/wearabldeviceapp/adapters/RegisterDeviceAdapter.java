package com.example.wearabldeviceapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wearabldeviceapp.R;
import com.example.wearabldeviceapp.interfaces.AdapterListener;
import com.example.wearabldeviceapp.models.LocalGPS;

import java.util.List;

public class RegisterDeviceAdapter extends RecyclerView.Adapter<RegisterDeviceAdapter.ViewHolder> {
    Context mContext;
    List<LocalGPS> gpsList;
    AdapterListener listener;

    public RegisterDeviceAdapter(Context mContext, List<LocalGPS> gpsList, AdapterListener listener) {
        this.mContext = mContext;
        this.gpsList = gpsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RegisterDeviceAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(mContext).inflate(R.layout.item_register_devices, parent, false);
        return new ViewHolder(mView);
    }

    @Override
    public void onBindViewHolder(@NonNull RegisterDeviceAdapter.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return gpsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
