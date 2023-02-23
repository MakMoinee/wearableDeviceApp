package com.example.wearabldeviceapp.common;

import com.example.wearabldeviceapp.models.LocalGPS;
import com.example.wearabldeviceapp.models.Users;

import java.util.HashMap;
import java.util.Map;

public class CommonMaps {

    public static Map<String, Object> getUserMap(Users users) {
        Map<String, Object> map = new HashMap<>();
        map.put("userID", users.getUserID());
        map.put("name", users.getName());
        map.put("email", users.getEmail());
        return map;
    }

    public static Map<String, Object> getGPSMap(String userID, LocalGPS gps) {
        Map<String, Object> map = new HashMap<>();
        map.put("userID", userID);
        map.put("deviceUserID", gps.getUserID());
        map.put("deviceID", gps.getDeviceID());
        return map;
    }
}
