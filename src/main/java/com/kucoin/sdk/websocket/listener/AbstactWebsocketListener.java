package com.kucoin.sdk.websocket.listener;

import com.kucoin.sdk.websocket.KucoinAPICallback;
import com.kucoin.sdk.websocket.PrintCallback;
import com.kucoin.sdk.websocket.event.KucoinEvent;
import com.kucoin.sdk.websocket.impl.BaseWebsocketImpl;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

import java.io.IOException;
import java.util.concurrent.locks.LockSupport;

@Slf4j
@Data
public abstract class AbstactWebsocketListener extends WebSocketListener {
    protected BaseWebsocketImpl baseWebsocket;

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        int retry = 0;
        boolean isBreak = false;
        do {
            try {
                baseWebsocket.connect();
                isBreak = true;
            } catch (IOException e) {
                log.warn("ws 重连异常", e);
                retry++;
            }
            LockSupport.parkUntil(System.currentTimeMillis() + 10);
        } while (!isBreak || retry > 10);

        if (retry == 10) {
            throw new RuntimeException(t);
        }
    }
}
