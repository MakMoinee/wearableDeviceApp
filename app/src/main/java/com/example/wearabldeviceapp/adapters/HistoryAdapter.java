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
import com.example.wearabldeviceapp.models.History;

import java.text.SimpleDateFormat;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    Context mContext;
    List<History> historyList;
    AdapterListener listener;

    public HistoryAdapter(Context mContext, List<History> historyList, AdapterListener listener) {
        this.mContext = mContext;
        this.historyList = historyList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(mContext).inflate(R.layout.item_history, parent, false);
        return new ViewHolder(mView);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryAdapter.ViewHolder holder, int position) {
        History history = historyList.get(position);
        holder.txtName.setText(history.getDependentName());
        holder.txtLatitude.setText(String.format("Latitude: %s", history.getLatitude()));
        holder.txtLongitude.setText(String.format("Longitude: %s", history.getLongitude()));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");

        holder.txtDateTime.setText(String.format("Date and Time: %s", dateFormat.format(history.getTimestamp().toDate())));
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtDateTime, txtLatitude, txtLongitude;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);
            txtDateTime = itemView.findViewById(R.id.txtDateTime);
            txtLatitude = itemView.findViewById(R.id.txtLatitude);
            txtLongitude = itemView.findViewById(R.id.txtLongitude);
        }
    }
}
