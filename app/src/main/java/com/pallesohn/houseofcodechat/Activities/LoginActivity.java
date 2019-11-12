package com.pallesohn.houseofcodechat.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pallesohn.houseofcodechat.R;

public class LoginActivity extends AppCompatActivity {

    private Button facebookBtn, googleBtn;
    private FirebaseUser currentUser;

    private static FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        facebookBtn = findViewById(R.id.facebookBtn);
        googleBtn = findViewById(R.id.googleBtn);

        facebookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        googleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(currentUser != null) {
            sendUserToMainActivity();
        }
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(mainIntent);
    }
}
