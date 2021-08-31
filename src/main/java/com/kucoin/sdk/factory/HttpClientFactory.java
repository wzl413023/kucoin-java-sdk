/**
 * Copyright 2019 Mek Global Limited.
 */
package com.kucoin.sdk.factory;

import com.kucoin.sdk.rest.interceptor.AuthenticationInterceptor;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;

/**
 * Created by chenshiwei on 2019/1/18.
 */
public class HttpClientFactory {

    public static OkHttpClient getPublicClient() {
        return buildHttpClient(null);
    }

    public static OkHttpClient getAuthClient(String apiKey, String secret, String passPhrase, Integer apiKeyVersion) {
        return buildHttpClient(new AuthenticationInterceptor(apiKey, secret, passPhrase, apiKeyVersion));
    }

    private static OkHttpClient buildHttpClient(Interceptor interceptor) {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequestsPerHost(100);
        dispatcher.setMaxRequests(100);
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .dispatcher(dispatcher);
        if (interceptor != null) {
            builder.addInterceptor(interceptor);
        }
        //builder.connectionPool(new ConnectionPool(3,3,TimeUnit.SECONDS));
        builder.pingInterval(1000 * 5, TimeUnit.MILLISECONDS);
        return builder.build();
    }

}
