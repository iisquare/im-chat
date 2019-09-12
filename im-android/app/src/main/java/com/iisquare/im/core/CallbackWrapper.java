package com.iisquare.im.core;

import android.widget.Toast;

import com.fasterxml.jackson.databind.JsonNode;
import com.iisquare.util.DPUtil;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class CallbackWrapper implements Callback {

    protected ActivityBase activity;
    protected boolean showSuccess = false;
    protected boolean showWarning = true;
    protected boolean showError = true;

    public CallbackWrapper(ActivityBase activity) {
        this.activity = activity;
    }

    public void toast(final String message) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public CallbackWrapper success(boolean showSuccess) {
        this.showSuccess = showSuccess;
        return this;
    }

    public CallbackWrapper warning(boolean showWarning) {
        this.showWarning = showWarning;
        return this;
    }

    public CallbackWrapper error(boolean showError) {
        this.showError = showError;
        return this;
    }

    public boolean before(Call call) {
        return true;
    }

    public boolean response(Call call, JsonNode response) {
        return true;
    }

    public boolean failure(Call call, IOException e) {
        return true;
    }

    public boolean after(Call call) {
        return true;
    }

    @Override
    public void onFailure(Call call, IOException e) {
        if (!before(call)) return;
        if (failure(call, e) && showError) toast(e.getMessage());
        after(call);
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        if (!before(call)) return;
        JsonNode json = DPUtil.parseJSON(response.body().string());
        if (null == json || 0 != json.get("code").asInt()) {
            if (failure(call, null) && showWarning) {
                toast(null == json ? "parse error" : json.get("message").asText());
            }
        } else {
            if (response(call, json) && showSuccess) {
                this.toast(json.get("message").asText());
            }
        }
        after(call);
    }
}
