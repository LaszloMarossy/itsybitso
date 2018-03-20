package com.itsybitso.entity;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;


/**
 * this holds the copy of the WS-received full Bitso order book which is then maintained by the diff-orders websocket
 * events to keep it in sync.  Instead of maintaining searchable and sortable lists we assume multiple threads updating
 * the orders that should not be conflicting in any case
 */
public class InternalOrderBook {

  static InternalOrderBook ourInstance = new InternalOrderBook();

  static ConcurrentHashMap<String, Order> asks = new ConcurrentHashMap<>();
  static ConcurrentHashMap<String, Order> bids = new ConcurrentHashMap<>();
  static OrderBook orderBook = null;


  private InternalOrderBook() {
  }

  // here for testing/debugging purposes TODO delete after testing
  public static InternalOrderBook getInstance() {
    return ourInstance;
  }

  /**
   * take a new order book from Bitso REST and internalize that, overwriting the old one, if any
   *
   * @param orders representing the REST JSON response
   */
  public static void initialize(OrderBook orders) {
    orderBook = orders;
    Arrays.stream(orderBook.getPayload().asks).forEach(InternalOrderBook::addAsk);
    Arrays.stream(orderBook.getPayload().bids).forEach(InternalOrderBook::addBid);
  }

  /**
   * add an ask order to the last position of the asks queue
   *
   * @return the previous order associated with {@code key}, or
   *   {@code null} if there was no mapping for {@code key}
   * @param ask ask Order to be processed
   */
  public static Order addAsk(Order ask) {
    return asks.put(ask.oid, ask);
  }

  /**
   * add an bid order to the last position of the bids queue
   *
   * @return the previous order associated with {@code key}, or
   *   {@code null} if there was no mapping for {@code key}
   * @param bid bid Order to be processed
   */
  public static Order addBid(Order bid) {
    return bids.put(bid.oid, bid);
  }


  /**
   *
   * @param oid to be removed from internal asks map
   * @return the previous Order associated with {@code key}, or
   *         {@code null} if there was no mapping for {@code key}
   */
  public static Order removeAsk(String oid) {
    return asks.remove(oid);
  }


  /**
   *
   * @param oid to be removed from internal bids map
   * @return the previous Order associated with {@code key}, or
   *         {@code null} if there was no mapping for {@code key}
   */
  public static Order removeBid(String oid) {
    return bids.remove(oid);
  }


  // mostly for debugging/testing - TODO to be deleted later
  public static ConcurrentHashMap<String, Order> getAsks() {
    return asks;
  }

  // mostly for debugging/testing - TODO to be deleted later
  public static ConcurrentHashMap<String, Order> getBids() {
    return bids;
  }

  public static Order getAsk(String oid) {
    return asks.get(oid);
  }

  public static Order getBid(String oid) {
    return bids.get(oid);
  }

  public static int sizeOfAsks() throws InterruptedException {
    return asks.size();
  }

  public static int sizeOfBids() throws InterruptedException {
    return bids.size();
  }

  public static long getSequence() {
    if (orderBook == null){
      return 0;
    }
    return orderBook.getPayload().sequence;
  }

}
