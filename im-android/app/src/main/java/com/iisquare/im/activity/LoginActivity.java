package com.iisquare.im.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.iisquare.im.R;
import com.iisquare.im.core.Share;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        final SharedPreferences session = Share.session(this);
        String userId = session.getString("userId", "");
        final EditText edtUserId = (EditText) findViewById(R.id.edtUserId);
        edtUserId.setText(userId);
        final Button btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnLogin.getText().equals("登录中")) return;
                btnLogin.setText("登录中");
                session.edit().putString("userId", edtUserId.getText().toString()).commit();
                Toast.makeText(getBaseContext(), "已更新", Toast.LENGTH_SHORT).show();
                btnLogin.setText("登录");
            }
        });
    }

}
