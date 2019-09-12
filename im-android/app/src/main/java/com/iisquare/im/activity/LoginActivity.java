package com.iisquare.im.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.fasterxml.jackson.databind.JsonNode;
import com.iisquare.im.R;
import com.iisquare.im.core.ActivityBase;
import com.iisquare.im.core.CallbackWrapper;
import com.iisquare.im.service.UserService;
import com.iisquare.util.DPUtil;

import okhttp3.Call;
import okhttp3.Response;

public class LoginActivity extends ActivityBase implements View.OnClickListener {

    private UserService userService = UserService.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        final SharedPreferences session = userService.session(this);
        ((EditText) findViewById(R.id.edtUserId)).setText(session.getString("userId", ""));
        findViewById(R.id.btnLogin).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnLogin:
                final Button btnLogin = (Button) v;
                if (btnLogin.getText().equals("登录中")) return;
                btnLogin.setText("登录中");
                final String userId = ((EditText) findViewById(R.id.edtUserId)).getText().toString();
                final SharedPreferences session = userService.session(this);
                userService.token(DPUtil.objectNode().put("userId", userId), new CallbackWrapper(this) {
                    @Override
                    public boolean response(Call call, JsonNode response) {
                        JsonNode data = response.get("data");
                        session.edit().putString("userId", data.get("userId").asText())
                            .putString("token", data.get("token").asText()).commit();
                        activity.forward(ContactActivity.class);
                        finish();
                        return super.response(call, response);
                    }

                    @Override
                    public boolean after(Call call) {
                        btnLogin.post(new Runnable() {
                            @Override
                            public void run() {
                                btnLogin.setText("登录");
                            }
                        });
                        return super.after(call);
                    }
                }.success(true));
                break;
        }
    }
}
