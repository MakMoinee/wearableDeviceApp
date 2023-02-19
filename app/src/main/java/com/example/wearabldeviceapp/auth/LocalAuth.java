package com.example.wearabldeviceapp.auth;

import android.util.Log;

import com.example.wearabldeviceapp.interfaces.SimpleRequestListener;
import com.example.wearabldeviceapp.models.Users;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LocalAuth {
    FirebaseAuth auth;

    public LocalAuth() {
        auth = FirebaseAuth.getInstance();
    }

    public void createUserAccount(Users user, SimpleRequestListener requestListener) {
        auth.createUserWithEmailAndPassword(user.getEmail(), user.getPassword())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user1 = auth.getCurrentUser();
                        if (user1 != null) {
                            requestListener.onSuccessWithStr(user1.getUid());
                        } else {
                            Log.e("ERROR", task.getException().getMessage());
                            requestListener.onError();
                        }

                    } else {
                        requestListener.onError();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ERROR_AUTH", e.getMessage());
                    requestListener.onError();
                });
    }

    public void login(Users users, SimpleRequestListener requestListener) {
        auth.signInWithEmailAndPassword(users.getEmail(), users.getPassword())
                .addOnCompleteListener(task -> {
                    FirebaseUser user = auth.getCurrentUser();
                    users.setUserID(user.getUid());
                    requestListener.onSuccessWithUserData(users);
                })
                .addOnFailureListener(e -> requestListener.onError());
    }

    public void logout(){
        auth.signOut();
    }
}
