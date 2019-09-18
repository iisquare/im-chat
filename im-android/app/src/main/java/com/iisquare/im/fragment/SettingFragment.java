package com.iisquare.im.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.iisquare.im.R;
import com.iisquare.im.activity.LoginActivity;
import com.iisquare.im.core.ActivityBase;
import com.iisquare.im.core.FragmentBase;
import com.iisquare.im.service.UserService;

public class SettingFragment extends FragmentBase implements View.OnClickListener {

    private UserService userService = UserService.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FragmentActivity activity = getActivity();
        TextView textUserId = activity.findViewById(R.id.textUserId);
        textUserId.setText(userService.session(activity).getString("userId", ""));
        Button btnLogout = activity.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnLogout:
                ActivityBase activity = (ActivityBase) getActivity();
                final SharedPreferences session = userService.session(activity);
                if (session.edit().remove("token").commit()) {
                    activity.forward(LoginActivity.class);
                    activity.finish();
                }
                break;
        }
    }
}
