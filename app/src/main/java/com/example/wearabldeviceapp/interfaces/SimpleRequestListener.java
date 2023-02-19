package com.example.wearabldeviceapp.interfaces;

import com.example.wearabldeviceapp.models.Users;

public interface SimpleRequestListener {

    default void onSuccess(){
        /**
         * Default implementation
         */
    }

    default void onSuccessWithUserData(Users users){
        /**
         * Default implementation
         */
    }

    default void onSuccessWithStr(String uuid){
        /**
         * Default implementation
         */
    }

    default void onError(){
        /**
         * Default implementation
         */
    }
}
