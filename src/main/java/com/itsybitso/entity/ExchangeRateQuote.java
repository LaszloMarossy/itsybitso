package com.itsybitso.entity;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "USDMXN"
})
public class ExchangeRateQuote {

  @JsonProperty("USDMXN")
  private Double uSDMXN;

  /**
   * No args constructor for use in serialization
   */
  public ExchangeRateQuote() {
  }

  /**
   * @param uSDMXN
   */
  public ExchangeRateQuote(Double uSDMXN) {
    super();
    this.uSDMXN = uSDMXN;
  }

  @JsonProperty("USDMXN")
  public Double getUSDMXN() {
    return uSDMXN;
  }

  @JsonProperty("USDMXN")
  public void setUSDMXN(Double uSDMXN) {
    this.uSDMXN = uSDMXN;
  }

}