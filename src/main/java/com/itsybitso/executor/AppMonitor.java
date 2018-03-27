package com.itsybitso.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itsybitso.controller.DiffOrdersQueue;
import com.itsybitso.entity.DisplayData;
import com.itsybitso.entity.InternalOrderBook;
import com.itsybitso.entity.Order;
import com.itsybitso.entity.Trade;
import com.itsybitso.util.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
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

  // value object that carries info for the UI - to be pushed by Websockets
  private static DisplayData displayData = new DisplayData(0,0,0,0,
      new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

  private static ObjectMapper objectMapper = new ObjectMapper();

  private static final Comparator<Order> PRICE_COMPARATOR = Comparator.comparing(Order::getPrice);
  private static final Comparator<Order> PRICE_COMPARATOR_REVERSE = (p1, p2) -> p2.getPrice().compareTo(p1.getPrice());

  private static int topX;



  static {
    exe = Executors.newSingleThreadExecutor();
    exe2 = Executors.newSingleThreadExecutor();
    topX = Integer.parseInt(PropertiesUtil.getProperty("displaydata.topx"));
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

          // top asks/bids calculation updated in separate async process
          calculateTopOrders();

          displayData.setDiffOrderQueueSize(DiffOrdersQueue.sizeOfQueue());
          displayData.setNumberOfConsumingThreads(DiffOrderConsumer.sizeOfThreadPool());
          displayData.setOrderBookAskSize(InternalOrderBook.sizeOfAsks());
          displayData.setOrderBookBidSize(InternalOrderBook.sizeOfBids());

          Thread.sleep(1000 * Integer.parseInt(PropertiesUtil.getProperty("displaydata.freq")));

          LOGGER.info(sb.append("8888888888888888 q ").append(displayData.getDiffOrderQueueSize()).append(" t ")
          .append(displayData.getNumberOfConsumingThreads()).append(" a ").append(displayData.getOrderBookAskSize())
          .append(" b ").append(displayData.getOrderBookBidSize()).toString());
        }
      } catch (Exception e) {
        LOGGER.error(e.getMessage(), e);
      }

      return "done?";
    };
    Future<String> f = exe.submit(call);
    return objectMapper.writeValueAsString(displayData);
  }

  public static String getDisplayDataString() throws Exception {
    return objectMapper.writeValueAsString(displayData);
  }

  static DisplayData getDisplayData() throws Exception {
    return displayData;
  }

  // this method is called externally as it is tied to the polling of the trade data, that can have different frequency
  static void setRecentTrades(Trade[] trades) {
    if (trades != null && trades.length > 0) {
      displayData.setRecentTrades(Arrays.asList(trades));
    }
  }


  // execute async so it does not block
  private static void calculateTopOrders() {
    Callable<String> call = () -> {
      displayData.setTopAsks(prepareAggregations(InternalOrderBook.getAsks(),PRICE_COMPARATOR, topX));
      displayData.setTopBids(prepareAggregations(InternalOrderBook.getBids(),PRICE_COMPARATOR_REVERSE, topX));
      return "";
    };
    Future<String> f = exe2.submit(call);
  }

  private static List<Order> prepareAggregations(ConcurrentHashMap<String, Order> orderMap, Comparator<Order> priceComparator, int topX) {
    Set<Order> orderSet = new HashSet<>(orderMap.values());
    List<Order> sortedOrders = orderSet.stream().sorted(priceComparator).collect(Collectors.toList());
    return sortedOrders.subList(0, topX);
  }

}
