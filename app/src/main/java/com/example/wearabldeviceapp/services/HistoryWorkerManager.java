package com.example.wearabldeviceapp.services;

import android.content.Context;

import androidx.work.BackoffPolicy;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.example.wearabldeviceapp.common.Constants;

import java.util.concurrent.TimeUnit;

public class HistoryWorkerManager {
    WorkManager wm;
    Context mContext;
    WorkRequest historyRequest;


    public HistoryWorkerManager(Context mContext) {
        this.mContext = mContext;
        wm = WorkManager.getInstance(this.mContext);
        historyRequest = new PeriodicWorkRequest.Builder(HistoryWorker.class, 15, TimeUnit.SECONDS)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
                .build();
        Constants.workManager = wm;
    }

    public void runHistoryWorker() {
        wm.enqueueUniquePeriodicWork("historyWorker", ExistingPeriodicWorkPolicy.KEEP, (PeriodicWorkRequest) historyRequest);
    }

    public void stopHistoryWorker() {
        // Cancel the work request by tag or unique name
        wm.cancelAllWorkByTag("historyWorker");
        // Or use wm.cancelUniqueWork("historyWorker") if unique name is used
    }
}
