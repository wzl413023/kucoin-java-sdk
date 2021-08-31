/**
 * Copyright 2019 Mek Global Limited.
 */
package com.kucoin.sdk.rest.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Created by devin@kucoin.com on 2018-12-27.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CurrencyResponse {

    private String fullName;

    private String currency;

    private String name;

    private int precision;

    private BigDecimal withdrawalMinSize;

    private BigDecimal withdrawalMinFee;

    @JsonProperty("isWithdrawEnabled")
    private Boolean isWithdrawEnabled;

    @JsonProperty("isDepositEnabled")
    private Boolean isDepositEnabled;

    private Boolean isMarginEnabled;

    private Boolean isDebitEnabled;

}
