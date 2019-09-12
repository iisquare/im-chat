package com.iisquare.im.core;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.iisquare.util.DPUtil;

import java.util.concurrent.TimeUnit;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class ServiceBase {

    public void request(final Request request, final Callback callback) {
        new Thread() {
            @Override
            public void run() {
                OkHttpClient.Builder builder = new OkHttpClient.Builder();
                builder.connectTimeout(1000, TimeUnit.MILLISECONDS);
                builder.readTimeout(30000, TimeUnit.MILLISECONDS);
                builder.build().newCall(request).enqueue(callback);
            }
        }.start();
    }

    public void post(final String uri, final ObjectNode data, final Callback callback) {
        MediaType media = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(DPUtil.stringify(data), media);
        String url = Configuration.SERVER_URI + uri;
        Request request = new Request.Builder().url(url).post(requestBody).build();
        this.request(request, callback);
    }

}
