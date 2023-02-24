package com.example.wearabldeviceapp.services;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.wearabldeviceapp.common.Constants;
import com.example.wearabldeviceapp.interfaces.SimpleRequestListener;
import com.example.wearabldeviceapp.models.LocalGPS;
import com.google.gson.Gson;

public class LocalRequest {
    Context mContext;

    public LocalRequest(Context mContext) {
        this.mContext = mContext;
    }

    public void getCoordinates(LocalGPS gps, SimpleRequestListener listener) {
        gps.setTimeZones("8:00");
        gps.setFirst(false);
        String req = new Gson().toJson(gps);
        Log.e("DATA", req);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.gpsURL, response -> {
            Log.e("SUCCESS_COORDINATE", response);
            listener.onSuccessWithStr(response);
        }, error -> {
            Log.e("ERROR_GET_COORDINATES", error.getMessage());
            listener.onError();
        }) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                return req.getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        RequestQueue queue = Volley.newRequestQueue(mContext);
        queue.add(stringRequest);
    }
}
