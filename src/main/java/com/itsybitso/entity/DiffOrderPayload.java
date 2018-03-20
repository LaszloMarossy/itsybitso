package com.itsybitso.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class DiffOrderPayload {

  // unix timestamp millis
  long d;
  // rate
  BigDecimal r;
  // 0=buy; 1=sell
  int t;
  // amount
  BigDecimal a;
  // value ?
  BigDecimal v;
  // oid
  String o;
  // status ?
  String s;

  public DiffOrderPayload(@JsonProperty("d") long d,
                          @JsonProperty("r") BigDecimal r,
                          @JsonProperty("t") int t,
                          @JsonProperty("a") BigDecimal a,
                          @JsonProperty("v") BigDecimal v,
                          @JsonProperty("s") String s,
                          @JsonProperty("o") String o) {
    this.d = d;
    this.r = r;
    this.t = t;
    this.a = a;
    this.v = v;
    this.s = s;
    this.o = o;
  }

  public long getD() {
    return d;
  }

  public BigDecimal getR() {
    return r;
  }

  public int getT() {
    return t;
  }

  public BigDecimal getA() {
    return a;
  }

  public BigDecimal getV() {
    return v;
  }

  public String getO() {
    return o;
  }

  public String getS() {
    return s;
  }
}

