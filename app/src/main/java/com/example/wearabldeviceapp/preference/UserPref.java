package com.example.wearabldeviceapp.preference;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.wearabldeviceapp.models.Users;

public class UserPref {
    Context context;
    SharedPreferences pref;

    public UserPref(Context context) {
        this.context = context;
        pref = this.context.getSharedPreferences("users", Context.MODE_PRIVATE);
    }


    public void storeUser(Users users) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("userID", users.getUserID());
        editor.putString("name", users.getName());
        editor.putString("email", users.getEmail());
        editor.putString("password", users.getPassword());
        editor.commit();
        editor.apply();
    }


    public Users getUser() {
        Users users = new Users();
        String userID = pref.getString("userID", "");
        if (userID != "") {
            users.setUserID(userID);
            users.setName(pref.getString("name", ""));
            users.setEmail(pref.getString("email", ""));
            users.setPassword(pref.getString("password", ""));
            return users;
        }

        return null;
    }
}
