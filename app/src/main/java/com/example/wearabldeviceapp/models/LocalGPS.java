package com.example.wearabldeviceapp.models;

import lombok.Data;

@Data
public class LocalGPS {
    int UserID;
    int DeviceID;
    boolean isFirst;
    String TimeZones;
}
