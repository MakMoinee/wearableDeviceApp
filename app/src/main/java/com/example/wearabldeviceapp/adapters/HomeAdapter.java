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
import com.example.wearabldeviceapp.models.LocalSetting;

import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {

    Context mContext;
    List<LocalSetting> list;

    public HomeAdapter(Context mContext, List<LocalSetting> list) {
        this.mContext = mContext;
        this.list = list;
    }

    @NonNull
    @Override
    public HomeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(mContext).inflate(R.layout.item_home_menus, parent, false);
        return new ViewHolder(mView);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeAdapter.ViewHolder holder, int position) {
        LocalSetting setting = list.get(position);
        holder.txtTitle.setText(setting.getText());
        holder.imgLogo.setImageResource(setting.getImageID());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txtTitle;
        public ImageView imgLogo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            imgLogo = itemView.findViewById(R.id.imgLogo);
        }
    }
}
