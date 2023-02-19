package com.example.wearabldeviceapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wearabldeviceapp.auth.LocalAuth;
import com.example.wearabldeviceapp.databinding.ActivityCreateAccountBinding;
import com.example.wearabldeviceapp.interfaces.SimpleRequestListener;
import com.example.wearabldeviceapp.models.Users;
import com.example.wearabldeviceapp.services.LocalFirestore;

public class CreateAccountActivity extends AppCompatActivity {

    private ActivityCreateAccountBinding binding;
    LocalAuth auth;
    LocalFirestore fs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initializer();
        initListeners();
    }

    private void initializer() {
        auth = new LocalAuth();
        fs = new LocalFirestore(CreateAccountActivity.this);
    }

    private void initListeners() {
        binding.btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = binding.editName.getText().toString();
                String email = binding.editEmail.getText().toString();
                String pass = binding.editPass.getText().toString();
                String confirmPass = binding.editConfirmPass.getText().toString();
                if (name.equals("") ||
                        email.equals("") ||
                        pass.equals("") ||
                        confirmPass.equals("")) {
                    Toast.makeText(CreateAccountActivity.this, "Please Don't Leave Empty Fields", Toast.LENGTH_SHORT).show();
                } else {
                    if (confirmPass.equals(pass)) {
                        Users users = new Users();
                        users.setName(name);
                        users.setEmail(email);
                        users.setPassword(pass);

                        auth.createUserAccount(users, new SimpleRequestListener() {
                            @Override
                            public void onSuccessWithStr(String uuid) {
                                users.setUserID(uuid);
                                fs.createAccount(users, new SimpleRequestListener() {
                                    @Override
                                    public void onSuccess() {
                                        Toast.makeText(CreateAccountActivity.this, "Successfully Created Account", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }

                                    @Override
                                    public void onError() {
                                        Toast.makeText(CreateAccountActivity.this, "Failed To Create Account, Please Try Again", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onError() {
                                Toast.makeText(CreateAccountActivity.this, "Failed To Create Account, Please Try Again", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(CreateAccountActivity.this, "Passwords doesn't Match", Toast.LENGTH_SHORT).show();
                        binding.editPass.setError("Password doesn't Match");
                        binding.editConfirmPass.setError("Password doesn't Match");
                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
