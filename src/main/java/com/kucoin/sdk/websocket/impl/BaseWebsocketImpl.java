/**
 * Copyright 2019 Mek Global Limited.
 */
package com.kucoin.sdk.websocket.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.kucoin.sdk.KucoinObjectMapper;
import com.kucoin.sdk.model.InstanceServer;
import com.kucoin.sdk.rest.response.WebsocketTokenResponse;
import com.kucoin.sdk.websocket.ChooseServerStrategy;
import com.kucoin.sdk.websocket.KucoinAPICallback;
import com.kucoin.sdk.websocket.event.KucoinEvent;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketException;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by chenshiwei on 2019/1/18.
 */
public abstract class BaseWebsocketImpl implements Closeable {

    private Set<KucoinEvent<Void>> subTopics = Sets.newConcurrentHashSet();

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseWebsocketImpl.class);


    private final ChooseServerStrategy chooseServerStrategy;
    private final OkHttpClient client;
    private final WebSocketListener listener;

    protected WebSocket webSocket;

    protected BaseWebsocketImpl(OkHttpClient client, WebSocketListener listener, ChooseServerStrategy chooseServerStrategy) {
        this.client = client;
        this.listener = listener;
        this.chooseServerStrategy = chooseServerStrategy;
    }

    public void connect() throws IOException {

        this.webSocket = createNewWebSocket();
    }

    protected abstract WebsocketTokenResponse requestToken() throws IOException;

    protected abstract String getName();

    private WebSocket createNewWebSocket() throws IOException {
        WebsocketTokenResponse websocketToken = requestToken();
        InstanceServer instanceServer = chooseServerStrategy.choose(websocketToken.getInstanceServers());
        String streamingUrl = String.format("%s", instanceServer.getEndpoint()
                + "?token=" + websocketToken.getToken());
        Request request = new Request.Builder().url(streamingUrl).build();
        WebSocket webSocket = client.newWebSocket(request, listener);
//        if (Objects.nonNull(webSocket)) {
//            LOGGER.info("wsClient [{}] ,pingInterval [{}]", getName(), instanceServer.getPingInterval());
//            scheduledThreadPool.scheduleAtFixedRate(() -> {
//                String rst = ping(System.currentTimeMillis() + "");
//                if (LOGGER.isDebugEnabled()) {
//                    LOGGER.info("start auto wsClient [{}] ping [{}] ", this.getName(), rst);
//                }
//            }, instanceServer.getPingInterval(), instanceServer.getPingInterval() - 1000, TimeUnit.MILLISECONDS);
//        }
        if (subTopics.size() > 0) {
            for (KucoinEvent<Void> subTopic : subTopics) {
                LOGGER.warn("重连订阅 [{}],topic=> {}", webSocket.send(serialize(subTopic)), subTopic.toString());
            }
        }
        return webSocket;
    }


    protected String ping(String requestId) {
        KucoinEvent<Void> ping = new KucoinEvent<>();
        ping.setId(requestId);
        ping.setType("ping");
        if (webSocket.send(serialize(ping))) {
            return requestId;
        }
        return null;
    }

    protected String subscribe(String topic, boolean privateChannel, boolean response) {
        String uuid = UUID.randomUUID().toString();
        KucoinEvent<Void> subscribe = new KucoinEvent<>();
        subscribe.setId(uuid);
        subscribe.setType("subscribe");
        subscribe.setTopic(topic);
        subscribe.setPrivateChannel(privateChannel);
        subscribe.setResponse(response);
        LOGGER.info("subscribe=> {}", topic);
        if (webSocket.send(serialize(subscribe))) {
            subTopics.add(subscribe);
            return uuid;
        }
        return null;
    }

    private void doSend(Object obj) {
        webSocket.send(serialize(obj));
    }

    protected String unsubscribe(String topic, boolean privateChannel, boolean response) {
        String uuid = UUID.randomUUID().toString();
        KucoinEvent<Void> subscribe = new KucoinEvent<>();
        subscribe.setId(uuid);
        subscribe.setType("unsubscribe");
        subscribe.setTopic(topic);
        subscribe.setPrivateChannel(privateChannel);
        subscribe.setResponse(response);
        LOGGER.info("unsubscribe=> {}", topic);
        if (webSocket.send(serialize(subscribe))) {
            subTopics.remove(subscribe);
            return uuid;
        }
        return null;
    }

    @Override
    public void close() throws IOException {
        LOGGER.debug("Web Socket Close");
        client.dispatcher().executorService().shutdown();
    }

    private String serialize(Object o) {
        try {
            return KucoinObjectMapper.INSTANCE.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failure serializing object", e);
        }
    }
}