package com.example.wearabldeviceapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wearabldeviceapp.auth.LocalAuth;
import com.example.wearabldeviceapp.interfaces.SimpleRequestListener;
import com.example.wearabldeviceapp.models.Users;
import com.example.wearabldeviceapp.preference.UserPref;
import com.example.wearabldeviceapp.services.NotifReceiver;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

public class MainActivity extends AppCompatActivity {

    Button btnLogin, btnCreateAccount;

    TextInputLayout layoutPassword;
    TextInputEditText editEmail, editPass;
    AlertDialog dialog;
    ProgressDialog pD;
    LocalAuth mAuth;
    Boolean isPassClick = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Users users = new UserPref(MainActivity.this).getUser();
        if (users != null) {
            startActivity(new Intent(MainActivity.this, ParentMainActivity.class));
        }
        setContentView(R.layout.activity_main);
        initViews();
        initListeners();

        registerReceiver(new NotifReceiver(), new IntentFilter("com.example.TRIGGER_NOTIF"));
    }

    private void initListeners() {
        btnLogin.setOnClickListener(v -> {
            if (editEmail.getText().toString().equals("") || editPass.getText().toString().equals("")) {
                Toast.makeText(MainActivity.this, "Please Don't Leave Empty Fields", Toast.LENGTH_SHORT).show();
            } else {
                Users users = new Users();
                users.setEmail(editEmail.getText().toString());
                users.setPassword(editPass.getText().toString());
                pD.show();
                mAuth.login(users, new SimpleRequestListener() {
                    @Override
                    public void onSuccessWithUserData(Users users) {
                        pD.dismiss();
                        Toast.makeText(MainActivity.this, "Login Successfully", Toast.LENGTH_SHORT).show();
                        Log.e("USERS", new Gson().toJson(users));
                        new UserPref(MainActivity.this).storeUser(users);
                        startActivity(new Intent(MainActivity.this, ParentMainActivity.class));
                        finish();
                    }

                    @Override
                    public void onError() {
                        pD.dismiss();
                        Toast.makeText(MainActivity.this, "Wrong username or password", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        btnCreateAccount.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, CreateAccountActivity.class));
        });
        layoutPassword.setEndIconOnClickListener(v -> {
            isPassClick = !isPassClick;
            if (isPassClick) {
                layoutPassword.setEndIconDrawable(R.drawable.ic_eye_off);
                editPass.setInputType(InputType.TYPE_CLASS_TEXT);
                isPassClick = true;
            } else {
                layoutPassword.setEndIconDrawable(R.drawable.ic_eye);
                editPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                isPassClick = false;
            }
        });
    }

    private void initViews() {
        btnLogin = findViewById(R.id.btnLogin);
        editEmail = findViewById(R.id.editEmail);
        editPass = findViewById(R.id.editPassword);
        layoutPassword = findViewById(R.id.layoutPass);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        mAuth = new LocalAuth();
        pD = new ProgressDialog(MainActivity.this);
        pD.setMessage("Sending Request ...");
        pD.setCancelable(false);
    }
}