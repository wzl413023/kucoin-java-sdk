/**
 * Copyright 2019 Mek Global Limited.
 */
package com.kucoin.sdk.websocket;

import com.kucoin.sdk.exception.KucoinApiException;
import okhttp3.Response;
import okhttp3.WebSocket;

/**
 * Created by chenshiwei on 2019/1/10.
 */
public interface KucoinAPICallback<T> {

    void onResponse(T response) throws KucoinApiException;

    default void onFailure(WebSocket webSocket, Throwable t, Response response) throws KucoinApiException{};
}
