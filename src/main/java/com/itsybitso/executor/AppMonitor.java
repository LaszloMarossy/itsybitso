package com.itsybitso.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itsybitso.controller.DiffOrdersQueue;
import com.itsybitso.entity.InternalOrderBook;
import com.itsybitso.entity.Monitor;
import com.itsybitso.entity.Order;
import com.itsybitso.util.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;


/**
 * Asynchronous process that gathers UI info periodically; to be used by the websocket to update the UI
 */
public class AppMonitor extends AsyncExecutor {

  private static final Logger LOGGER = LoggerFactory.getLogger(AppMonitor.class);
  // declaring this here vs. the base class as then there would be only a single thread pool for populate and consume
  private static ExecutorService exe;
  private static ExecutorService exe2;

  private static Monitor monitor = new Monitor(0,0,0,0, new ArrayList<>(), new ArrayList<>());
  private static ObjectMapper objectMapper = new ObjectMapper();

  private static final Comparator<Order> PRICE_COMPARATOR = Comparator.comparing(Order::getPrice);
  private static final Comparator<Order> PRICE_COMPARATOR_REVERSE = (p1, p2) -> p2.getPrice().compareTo(p1.getPrice());

  private static int topX;

  private static List<Order> topAsks;
  private static List<Order> topBids;


  static {
    exe = Executors.newSingleThreadExecutor();
    exe2 = Executors.newSingleThreadExecutor();
    topX = Integer.parseInt(PropertiesUtil.getProperty("monitor.topx"));
  }

  /**
   * main method to start continuous monitoring.
   *
   * @return result string - ignored for now
   */
  public static String startAppMonitor() throws Exception{
    Callable<String> call = () -> {
      LOGGER.info("started the startAsyncPopulateQueue execution ");
      try {
        LOGGER.info("**** ||||| ****");
        while(true) {
          StringBuilder sb = new StringBuilder();

          calculateTopOrders();

          monitor.setDiffOrderQueueSize(DiffOrdersQueue.sizeOfQueue());
          monitor.setNumberOfConsumingThreads(DiffOrderConsumer.sizeOfThreadPool());
          monitor.setOrderBookAskSize(InternalOrderBook.sizeOfAsks());
          monitor.setOrderBookBidSize(InternalOrderBook.sizeOfBids());

          Thread.sleep(1000 * Integer.parseInt(PropertiesUtil.getProperty("monitor.freq")));

          LOGGER.info(sb.append("8888888888888888 q ").append(monitor.getDiffOrderQueueSize()).append(" t ")
          .append(monitor.getNumberOfConsumingThreads()).append(" a ").append(monitor.getOrderBookAskSize())
          .append(" b ").append(monitor.getOrderBookBidSize()).toString());
        }
      } catch (Exception e) {
        LOGGER.error(e.getMessage(), e);
      }

      return "done?";
    };
    Future<String> f = exe.submit(call);
    return objectMapper.writeValueAsString(monitor);
  }

  public static String getCurrentMonitor() throws Exception {
    return objectMapper.writeValueAsString(monitor);
  }

  public static List<Order> getTopAsks() throws Exception {
    return topAsks;
  }

  public static List<Order> getTopBids() throws Exception {
    return topBids;
  }

  // execute async so it does not block
  private static void calculateTopOrders() {
    Callable<String> call = () -> {
      // todo separate the refreshing of these out to a different
      monitor.setTopAsks(prepareAggregations(InternalOrderBook.getAsks(),PRICE_COMPARATOR, topX));
      monitor.setTopBids(prepareAggregations(InternalOrderBook.getBids(),PRICE_COMPARATOR_REVERSE, topX));
      return "";
    };
    Future<String> f = exe2.submit(call);
  }

  private static List<Order> prepareAggregations(ConcurrentHashMap<String, Order> orderMap, Comparator<Order> priceComparator, int topX) {
    List<Order> sortedOrders;
//    Set<Map.Entry<String, Order>> orderEntrySet = orderMap.entrySet();
    Set<Order> orderSet = new HashSet<>(orderMap.values());
    sortedOrders = orderSet.stream().sorted(priceComparator).collect(Collectors.toList());
    return sortedOrders.subList(0, topX);
  }



}
