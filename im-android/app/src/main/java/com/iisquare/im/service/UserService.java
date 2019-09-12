package com.iisquare.im.service;

import android.content.Context;
import android.content.SharedPreferences;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.iisquare.im.core.ServiceBase;

import okhttp3.Callback;

public class UserService extends ServiceBase {

    private static UserService instance = null;

    private UserService() {}

    public SharedPreferences session(Context context) {
        return context.getSharedPreferences("session", Context.MODE_PRIVATE);
    }

    public static UserService getInstance() {
        if (instance == null) {
            synchronized (UserService.class) {
                if (instance == null) {
                    instance = new UserService();
                }
            }
        }
        return instance;
    }

    public void token(ObjectNode param, Callback callback) {
        post("/user/token", param, callback);
    }

}
