package com.example.wearabldeviceapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
        LocalGPS gps = gpsList.get(position);
        holder.txtUserID.setText(Integer.toString(gps.getUserID()));
        holder.txtDeviceID.setText(Integer.toString(gps.getDeviceID()));
        holder.itemView.setOnClickListener(v -> listener.onItemClickListener(holder.getAdapterPosition()));
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                listener.onItemLongClickListener(holder.getAdapterPosition());
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return gpsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txtUserID;
        public TextView txtDeviceID;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtUserID = itemView.findViewById(R.id.txtUserID);
            txtDeviceID = itemView.findViewById(R.id.txtDeviceID);
        }
    }
}
