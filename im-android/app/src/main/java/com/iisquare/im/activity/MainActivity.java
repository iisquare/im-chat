package com.iisquare.im.activity;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.iisquare.im.R;
import com.iisquare.im.core.ActivityBase;
import com.iisquare.im.service.UserService;
import com.iisquare.util.DPUtil;

public class MainActivity extends ActivityBase implements Runnable {

    private UserService userService = UserService.getInstance();

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
        SharedPreferences preferences = userService.session(this);
        String token = preferences.getString("token", "");
        if (DPUtil.empty(token)) {
            forward(LoginActivity.class);
        } else {
            forward(ContactActivity.class);
        }
        finish();
    }
}
