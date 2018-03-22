package com.itsybitso.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * represents info needed by the UI
 * TODO can be separated into diff areas and scheduled/polled separately
 */
public class Monitor {

  // app performance monitoring
  int diffOrderQueueSize;
  int orderBookAskSize;
  int orderBookBidSize;
  int numberOfConsumingThreads;

  // top X asks/bids info for UI
  List<Order> topAsks;
  List<Order> topBids;

  public Monitor(
      @JsonProperty("diffOrderQueueSize") int diffOrderQueueSize,
      @JsonProperty("orderBookAskSize") int orderBookAskSize,
      @JsonProperty("orderBookBidSize") int orderBookBidSize,
      @JsonProperty("numberOfConsumingThreads") int numberOfConsumingThreads,
      @JsonProperty("topAsks") List<Order> topAsks,
      @JsonProperty("topBids") List<Order> topBids) {
    this.diffOrderQueueSize = diffOrderQueueSize;
    this.orderBookAskSize = orderBookAskSize;
    this.orderBookBidSize = orderBookBidSize;
    this.numberOfConsumingThreads = numberOfConsumingThreads;
    this.topAsks = topAsks;
    this.topBids = topBids;
  }

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
}
