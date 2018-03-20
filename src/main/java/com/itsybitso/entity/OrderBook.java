package com.itsybitso.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderBook {
  boolean success;
  OrderBookPayload payload;

  public OrderBook(@JsonProperty("success") boolean success,
                   @JsonProperty("payload") OrderBookPayload payload) {
    this.success = success;
    this.payload = payload;
  }

  public boolean isSuccess() {
    return success;
  }

  public OrderBookPayload getPayload() {
    return payload;
  }


}

