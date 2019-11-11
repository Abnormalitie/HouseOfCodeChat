package com.pallesohn.houseofcodechat.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.pallesohn.houseofcodechat.R;

public class SplashScreenActivity extends Activity {

    private long loadTime = 5000;
    private Handler myHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        myHandler = new Handler();

        myHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(loginIntent);
                finish();
            }
        }, loadTime);

    }
}
