package com.example.wearabldeviceapp.interfaces;

import com.example.wearabldeviceapp.models.DangerZone;
import com.example.wearabldeviceapp.models.Dependents;
import com.example.wearabldeviceapp.models.LocalGPS;
import com.example.wearabldeviceapp.models.SafeZone;
import com.example.wearabldeviceapp.models.Users;

import java.util.List;

public interface SimpleRequestListener {

    default void onSuccess() {
        /**
         * Default implementation
         */
    }

    default void onSuccess(DangerZone safeZone) {
        /**
         * Default implementation
         */
    }
    default void onSuccess(SafeZone safeZone) {
        /**
         * Default implementation
         */
    }


    default void onSuccess(List<LocalGPS> gpsList) {
        /**
         * Default implementation
         */
    }

    default void onSuccessDependent(List<Dependents> dependentsList) {
        /**
         * Default implementation
         */
    }

    default void onSuccessWithUserData(Users users) {
        /**
         * Default implementation
         */
    }

    default void onSuccessWithStr(String uuid) {
        /**
         * Default implementation
         */
    }

    default void onError() {
        /**
         * Default implementation
         */
    }
}
