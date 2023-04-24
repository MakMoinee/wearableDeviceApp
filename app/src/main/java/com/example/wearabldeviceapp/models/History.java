package com.example.wearabldeviceapp.models;

import com.google.firebase.Timestamp;

import java.util.Date;

import lombok.Data;

@Data
public class History {
    String docID;
    String userID;
    String zoneType;
    String dependentName;
    Timestamp timestamp;
    float latitude;
    float longitude;
}
