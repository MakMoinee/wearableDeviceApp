package com.example.wearabldeviceapp.models;

import lombok.Data;

@Data
public class DangerZone {
    String docID;
    String userID;
    int dependentDeviceID;
    int dependentUserID;
    String latLngRaw;
}
