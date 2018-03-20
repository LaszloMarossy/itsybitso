package com.itsybitso.entity;

public class Monitor {

  int diffOrderQueueSize;
  int orderBookAskSize;
  int orderBookBidSize;
  int numberOfConsumingThreads;

  public Monitor(int diffOrderQueueSize, int orderBookAskSize, int orderBookBidSize, int numberOfConsumingThreads) {
    this.diffOrderQueueSize = diffOrderQueueSize;
    this.orderBookAskSize = orderBookAskSize;
    this.orderBookBidSize = orderBookBidSize;
    this.numberOfConsumingThreads = numberOfConsumingThreads;
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
}
