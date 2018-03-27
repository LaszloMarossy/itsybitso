package com.itsybitso.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Trades {
  private boolean success;
  private Trade[] payload;

  public Trades(@JsonProperty("success") boolean success,
                @JsonProperty("payload") Trade[] payload) {
    this.success = success;
    this.payload = payload;
  }

  public boolean isSuccess() {
    return success;
  }

  public Trade[] getPayload() {
    return payload;
  }


}

