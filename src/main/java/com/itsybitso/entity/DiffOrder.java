package com.itsybitso.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DiffOrder {
  String type;
  String book;
  long sequence;
  DiffOrderPayload[] payload;

  public DiffOrder(@JsonProperty("type") String type,
                   @JsonProperty("book") String book,
                   @JsonProperty("sequence") long sequence,
                   @JsonProperty("payload") DiffOrderPayload[] payload) {
    this.type = type;
    this.book = book;
    this.sequence = sequence;
    this.payload = payload;
  }

  public String getType() {
    return type;
  }

  public String getBook() {
    return book;
  }

  public long getSequence() {
    return sequence;
  }

  public DiffOrderPayload[] getPayload() {
    return payload;
  }
}

