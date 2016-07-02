package com.darkgem.framework.io.http;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.darkgem.framework.support.callback.Callback;
import com.darkgem.framework.support.kit.ThreadPoolKit;
import com.darkgem.framework.support.tuple.Tuple2;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * OkHttp
 */
public class OkHttpIo {

    static volatile OkHttpIo instance;
    Context context;
    Handler handler;
    OkHttpClient okHttpClient;

    static public OkHttpIo getInstance(Context context) {
        if (instance == null) {
            synchronized (OkHttpIo.class) {
                if (instance == null) {
                    instance = new OkHttpIo(context);
                }
            }
        }
        return instance;
    }

    public OkHttpIo(Context context) {
        this.context = context;
        this.handler = new Handler(Looper.getMainLooper());
        //配置 client
        {
            Dispatcher dispatcher = new Dispatcher(ThreadPoolKit.createThreadPoolExecutor(1, 12, 60l, TimeUnit.SECONDS, "app-http"));
            //最大并发访问
            dispatcher.setMaxRequests(10);
            this.okHttpClient = new OkHttpClient.Builder()
                    .dispatcher(dispatcher)
                    .build();
        }

    }

    /**
     * 发起 GET 请求
     */
    public void get(@NonNull String url, @Nullable final Callback<Tuple2<Integer, String>, Exception> callback) {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        okHttpClient.newCall(request).enqueue(new okhttp3.Callback() {

            @Override
            public void onResponse(Response response) throws IOException {
                final int code = response.code();
                final String string = response.body().string();
                if (callback != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(new Tuple2<Integer, String>(code, string));
                        }
                    });
                }

            }

            @Override
            public void onFailure(Request request, IOException e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    /**
     * 发起 GET 请求
     */
    public Call get(@NonNull String url) {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        return okHttpClient.newCall(request);
    }

    /**
     * 发起 POST
     */
    public void post(@NonNull String url, @Nullable RequestBody requestBody, @Nullable final Callback<Tuple2<Integer, String>, Exception> callback) {
        //如果请求的数据为空，则给定一个空的表单，进行提交
        if (requestBody == null) {
            requestBody = new FormBody.Builder().build();
        }
        //构造请求
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        okHttpClient.newCall(request).enqueue(new okhttp3.Callback() {

            @Override
            public void onResponse(Response response) throws IOException {
                final int code = response.code();
                final String string = response.body().string();
                if (callback != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(new Tuple2<Integer, String>(code, string));
                        }
                    });
                }
            }

            @Override
            public void onFailure(Request request, IOException e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    /**
     * 发起 POST
     */
    public Call post(@NonNull String url, @Nullable RequestBody requestBody) {
        //如果请求的数据为空，则给定一个空的表单，进行提交
        if (requestBody == null) {
            requestBody = new FormBody.Builder().build();
        }
        //构造请求
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        return okHttpClient.newCall(request);
    }
}
