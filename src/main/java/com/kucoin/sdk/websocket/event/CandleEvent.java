package com.kucoin.sdk.websocket.event;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class CandleEvent {


    private String symbol;
    private String[] candles;
    //周期时间
    private long time;

}
