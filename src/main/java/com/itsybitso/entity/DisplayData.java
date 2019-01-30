package com.itsybitso.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.itsybitso.executor.TradesPoller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * represents info needed by the UI
 * TODO can be separated into diff areas and scheduled/polled separately
 */
public class DisplayData {

  // app performance monitoring
  private int diffOrderQueueSize;
  private int orderBookAskSize;
  private int orderBookBidSize;
  private int numberOfConsumingThreads;

  // trade balance values
  private BigDecimal currencyBalance;
  private BigDecimal coinBalance;
  private BigDecimal latestPrice;
  private BigDecimal accountValue;
  private BigDecimal startingAccountValue;
  private BigDecimal profit;


  // top X trades/bids info for UI
  private List<Order> topAsks;
  private List<Order> topBids;

  // recent trades
  private List<Trade> recentTrades;

  // constructor for ObjectMapper
  public DisplayData(
      @JsonProperty("startingAccountValue") BigDecimal startingAccountValue,
      @JsonProperty("currencyBalance") BigDecimal currencyBalance,
      @JsonProperty("coinBalance") BigDecimal coinBalance,
      @JsonProperty("latestPrice") BigDecimal latestPrice,
      @JsonProperty("diffOrderQueueSize") int diffOrderQueueSize,
      @JsonProperty("orderBookAskSize") int orderBookAskSize,
      @JsonProperty("orderBookBidSize") int orderBookBidSize,
      @JsonProperty("numberOfConsumingThreads") int numberOfConsumingThreads,
      @JsonProperty("topAsks") List<Order> topAsks,
      @JsonProperty("topBids") List<Order> topBids,
      @JsonProperty("recentTrades") List<Trade> recentTrades
      ) {
    this.startingAccountValue = startingAccountValue;
    this.currencyBalance = currencyBalance;
    this.coinBalance = coinBalance;
    this.latestPrice = latestPrice;
    this.diffOrderQueueSize = diffOrderQueueSize;
    this.orderBookAskSize = orderBookAskSize;
    this.orderBookBidSize = orderBookBidSize;
    this.numberOfConsumingThreads = numberOfConsumingThreads;
    this.topAsks = topAsks;
    this.topBids = topBids;
    this.recentTrades = recentTrades;
  }

//  // constructor for ObjectMapper
//  public DisplayData(
//      @JsonProperty("diffOrderQueueSize") int diffOrderQueueSize,
//      @JsonProperty("orderBookAskSize") int orderBookAskSize,
//      @JsonProperty("orderBookBidSize") int orderBookBidSize,
//      @JsonProperty("numberOfConsumingThreads") int numberOfConsumingThreads,
//      @JsonProperty("topAsks") List<Order> topAsks,
//      @JsonProperty("topBids") List<Order> topBids,
//      @JsonProperty("recentTrades") List<Trade> recentTrades
//      ) {
//    this.currencyBalance = new BigDecimal(1000);
//    this.coinBalance = new BigDecimal(10);
//    this.latestPrice = new BigDecimal(0 );
//    this.diffOrderQueueSize = diffOrderQueueSize;
//    this.orderBookAskSize = orderBookAskSize;
//    this.orderBookBidSize = orderBookBidSize;
//    this.numberOfConsumingThreads = numberOfConsumingThreads;
//    this.topAsks = topAsks;
//    this.topBids = topBids;
//    this.recentTrades = recentTrades;
//  }

  public int getDiffOrderQueueSize() {
    return diffOrderQueueSize;
  }

  public void setDiffOrderQueueSize(int diffOrderQueueSize) {
    this.diffOrderQueueSize = diffOrderQueueSize;
  }

  public int getOrderBookAskSize() {
    return orderBookAskSize;
  }

  public void setOrderBookAskSize(int orderBookAskSize) {
    this.orderBookAskSize = orderBookAskSize;
  }

  public int getOrderBookBidSize() {
    return orderBookBidSize;
  }

  public void setOrderBookBidSize(int orderBookBidSize) {
    this.orderBookBidSize = orderBookBidSize;
  }

  public int getNumberOfConsumingThreads() {
    return numberOfConsumingThreads;
  }

  public void setNumberOfConsumingThreads(int numberOfConsumingThreads) {
    this.numberOfConsumingThreads = numberOfConsumingThreads;
  }

  public List<Order> getTopAsks() {
    return topAsks;
  }

  public void setTopAsks(List<Order> topAsks) {
    this.topAsks = topAsks;
  }

  public List<Order> getTopBids() {
    return topBids;
  }

  public void setTopBids(List<Order> topBids) {
    this.topBids = topBids;
  }

  public List<Trade> getRecentTrades() {
    return recentTrades;
  }

  public void setRecentTrades(List<Trade> recentTrades) {
    this.recentTrades = recentTrades;
  }

  public BigDecimal getCurrencyBalance() {
    return currencyBalance;
  }

  public void setCurrencyBalance(BigDecimal currencyBalance) {
    this.currencyBalance = currencyBalance;
  }

  public BigDecimal getCoinBalance() {
    return coinBalance;
  }

  public void setCoinBalance(BigDecimal coinBalance) {
    this.coinBalance = coinBalance;
  }

  public BigDecimal getLatestPrice() {
    return latestPrice;
  }

  public void setLatestPrice(BigDecimal latestPrice) {
    this.latestPrice = latestPrice;
  }

  public BigDecimal getAccountValue() {
    return accountValue;
  }

  public void setAccountValue(BigDecimal accountValue) {
    this.accountValue = accountValue;
  }

  public void setProfit(BigDecimal profit) {
    this.profit = profit;
  }

  public BigDecimal getStartingAccountValue() {
    return startingAccountValue;
  }

  public void setStartingAccountValue(BigDecimal startingAccountValue) {
    this.startingAccountValue = startingAccountValue;
  }

  public BigDecimal getProfit() {
    return profit;
  }
}
