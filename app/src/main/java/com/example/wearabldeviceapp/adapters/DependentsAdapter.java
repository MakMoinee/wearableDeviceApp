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
import com.example.wearabldeviceapp.models.Dependents;

import java.util.List;

public class DependentsAdapter extends RecyclerView.Adapter<DependentsAdapter.ViewHolder> {

    Context mContext;
    List<Dependents> dependentsList;
    AdapterListener listener;

    public DependentsAdapter(Context mContext, List<Dependents> dependentsList, AdapterListener listener) {
        this.mContext = mContext;
        this.dependentsList = dependentsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DependentsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(mContext).inflate(R.layout.item_dependents, parent, false);
        return new ViewHolder(mView);
    }

    @Override
    public void onBindViewHolder(@NonNull DependentsAdapter.ViewHolder holder, int position) {
        Dependents dependents = dependentsList.get(position);
        holder.txtName.setText(String.format("Name: %s", dependents.getName()));
        holder.txtAge.setText(String.format("Age: %s", Integer.toString(dependents.getAge())));
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
        return dependentsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView txtName, txtAge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtAge = itemView.findViewById(R.id.txtAge);
            txtName = itemView.findViewById(R.id.txtName);
        }
    }
}
