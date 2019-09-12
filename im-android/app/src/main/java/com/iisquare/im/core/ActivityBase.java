package com.iisquare.im.core;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

public class ActivityBase extends AppCompatActivity {

    public void forward(Class target) {
        Intent intent = new Intent();
        intent.setClass(this, target);
        startActivity(intent);
    }

}
