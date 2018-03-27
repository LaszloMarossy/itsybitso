package com.itsybitso.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Comparator;

public class Trade implements Comparable<Trade> {
  private String book;
  private String createdAt;
  private BigDecimal amount;
  private String makerSide;
  private BigDecimal price;
  private Long tid;
  private Tick tick;
  // current status relative to previous tick values
  private String nthStatus;

    public Trade(@JsonProperty("book") String book,
                 @JsonProperty("created_at") String createdAt,
                 @JsonProperty("amount") BigDecimal amount,
                 @JsonProperty("maker_side") String makerSide,
                 @JsonProperty("price") BigDecimal price,
                 @JsonProperty("tid") Long tid) {
      this.book = book;
      this.createdAt = createdAt;
      this.amount = amount;
      this.makerSide = makerSide;
      this.price = price;
      this.tid = tid;
    }

  public String getBook() {
    return book;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public String getMakerSide() {
    return makerSide;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public Long getTid() {
    return tid;
  }

  public void setTid(Long tid) {
    this.tid = tid;
  }

  public Tick getTick() {
    return tick;
  }

  public void setTick(Tick tick) {
    this.tick = tick;
  }

  public String getNthStatus() {
    return nthStatus;
  }

  public void setNthStatus(String nthStatus) {
    this.nthStatus = nthStatus;
  }

  /**
   * comparing on trade-id so we can insert pretend-trades in between
   * @param t Trade to compare this to
   * @return -1 if this is less, 0 if equal, 1 if this is more than the passed Trade's tid
   */
  @Override
  public int compareTo(Trade t){
      return Comparator.comparing(Trade::getTid).compare(t, this);
  }
}
