package com.example.wearabldeviceapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.example.wearabldeviceapp.databinding.ItemDeviceBinding;

import java.util.List;

public class SelectDeviceAdapter extends BaseAdapter {

    Context mContext;
    List<String> deviceIds;

    ItemDeviceBinding binding;

    public SelectDeviceAdapter(Context mContext, List<String> deviceIds) {
        this.mContext = mContext;
        this.deviceIds = deviceIds;
    }

    @Override
    public int getCount() {
        return deviceIds.size();
    }

    @Override
    public Object getItem(int position) {
        return deviceIds.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        binding = ItemDeviceBinding.inflate(LayoutInflater.from(mContext), parent, false);
        String str = deviceIds.get(position);
        binding.txtDeviceID.setText(str);

        return binding.getRoot();
    }

}
