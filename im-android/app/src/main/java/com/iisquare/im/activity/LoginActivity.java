package com.iisquare.im.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.iisquare.im.android.R;
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
        edtUserId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                session.edit().putString("userId", edtUserId.getText().toString()).commit();
                Toast.makeText(getApplicationContext(), "已更新", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
