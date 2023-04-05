package com.example.wearabldeviceapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wearabldeviceapp.R;
import com.example.wearabldeviceapp.interfaces.AdapterListener;
import com.example.wearabldeviceapp.models.LocalSetting;

import java.util.List;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.ViewHolder> {

    Context mContext;
    List<LocalSetting> list;
    AdapterListener listener;

    public SettingsAdapter(Context mContext, List<LocalSetting> list, AdapterListener listener) {
        this.mContext = mContext;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SettingsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(mContext).inflate(R.layout.item_setinngs, parent, false);
        return new ViewHolder(mView);
    }

    @Override
    public void onBindViewHolder(@NonNull SettingsAdapter.ViewHolder holder, int position) {
        LocalSetting setting = list.get(position);
        holder.txtText.setText(setting.getText());
        holder.imgLogo.setImageResource(setting.getImageID());
        holder.itemView.setOnClickListener(v -> listener.onItemClickListener(holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txtText;
        public ImageView imgLogo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtText = itemView.findViewById(R.id.txtSample);
            imgLogo = itemView.findViewById(R.id.imgLogo);
        }
    }
}
