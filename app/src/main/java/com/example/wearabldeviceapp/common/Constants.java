package com.example.wearabldeviceapp.common;

import androidx.work.WorkManager;

import com.example.wearabldeviceapp.models.LocalGPS;

import java.util.concurrent.ScheduledExecutorService;

public class Constants {
    public static String IP_ADDRESS = "";
    public static final String protocol = "http://";
    public static final String gpsURL = "https://www.gps123.org/Ajax/DevicesAjax.asmx/GetDevicesByUserID";

    public static int SAFE_NOTIFY_INCREMENT = 0;
    public static int DANGER_NOTIFY_INCREMENT = 0;
    public static int HISTORY_INCREMENT = 0;

    public static WorkManager workManager;
    public static LocalGPS gps = new LocalGPS();
    public static String userID = "";
    public static ScheduledExecutorService scheduler;

    public static void stopWorker() {
        if (workManager != null) {
            workManager.cancelAllWork();
        }
    }

    public static void stopSchedule(){
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }
}
