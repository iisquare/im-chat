package com.iisquare.im.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.iisquare.im.R;
import com.iisquare.im.core.Share;
import com.iisquare.util.DPUtil;

public class MainActivity extends AppCompatActivity implements Runnable {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {}
        SharedPreferences preferences = Share.session(this);
        String token = preferences.getString("token", "");
        Intent intent = new Intent();
        if (DPUtil.empty(token)) {
            intent.setClass(MainActivity.this, LoginActivity.class);
        } else {
            intent.setClass(MainActivity.this, ContactActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
