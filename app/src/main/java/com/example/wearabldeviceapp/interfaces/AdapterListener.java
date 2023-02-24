package com.example.wearabldeviceapp.interfaces;

public interface AdapterListener {

    default void onItemClickListener(int position) {
        /**
         * default implementation
         */
    }

    default void onItemLongClickListener(int position) {
        /**
         * default implementation
         */
    }
}
