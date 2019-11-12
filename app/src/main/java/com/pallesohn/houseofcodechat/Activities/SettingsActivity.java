package com.pallesohn.houseofcodechat.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pallesohn.houseofcodechat.R;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button updateAccountSettingsBtn;
    private EditText userName, userStatus;
    private CircleImageView userProfileImage;

    private String currentUserId;
    private FirebaseAuth mAuth;
    private DatabaseReference mRootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        mRootRef = FirebaseDatabase.getInstance().getReference();

        updateAccountSettingsBtn = findViewById(R.id.update_settings_button);
        userName = findViewById(R.id.set_user_name);
        userStatus = findViewById(R.id.set_profile_status);
        userProfileImage = findViewById(R.id.set_profile_image);

        userName.setVisibility(View.INVISIBLE);

        updateAccountSettingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSettings();
            }
        });

        retrieveUserInformation();
    }

    private void updateSettings() {
        String setUsername = userName.getText().toString();
        String setStatus = userStatus.getText().toString();

        if(TextUtils.isEmpty(setUsername)) {
            Toast.makeText(this, "Enter Username", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(setStatus)) {
            Toast.makeText(this, "Enter status", Toast.LENGTH_SHORT).show();
        } else {
            HashMap<String, String> profileMap = new HashMap<>();
            profileMap.put("uid", currentUserId);
            profileMap.put("name", setUsername);
            profileMap.put("status", setStatus);

            mRootRef.child("Users").child(currentUserId).setValue(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()) {
                        sendUserToMainActivity();
                        Toast.makeText(SettingsActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                    } else {
                        String message = task.getException().toString();
                        Toast.makeText(SettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void retrieveUserInformation() {
        mRootRef.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") && (dataSnapshot.hasChild("image")))) {
                    String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                    String retrieveUserStatus = dataSnapshot.child("status").getValue().toString();
                    String retrieveProfilePicture = dataSnapshot.child("image").getValue().toString();

                    userName.setText(retrieveUserName);
                    userStatus.setText(retrieveUserStatus);

                } else if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))) {
                    String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                    String retrieveUserStatus = dataSnapshot.child("status").getValue().toString();

                    userName.setText(retrieveUserName);
                    userStatus.setText(retrieveUserStatus);
                } else {
                    userName.setVisibility(View.VISIBLE);
                    Toast.makeText(SettingsActivity.this, "Set and update profile info", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
