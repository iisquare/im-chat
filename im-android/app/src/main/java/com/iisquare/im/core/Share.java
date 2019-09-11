package com.iisquare.im.core;

import android.content.Context;
import android.content.SharedPreferences;

public class Share {

    public static SharedPreferences session(Context context) {
        return context.getSharedPreferences("session", Context.MODE_PRIVATE);
    }

}
