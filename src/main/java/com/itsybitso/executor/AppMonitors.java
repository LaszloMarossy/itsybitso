package com.itsybitso.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itsybitso.bl.ConfigurableTrader;
import com.itsybitso.controller.DiffOrdersQueue;
import com.itsybitso.entity.DisplayData;
import com.itsybitso.entity.InternalOrderBook;
import com.itsybitso.entity.Order;
import com.itsybitso.entity.Trade;
import com.itsybitso.entity.WindowConfig;
import com.itsybitso.util.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
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
public class AppMonitors extends AsyncExecutor {

  private static final Logger LOGGER = LoggerFactory.getLogger(AppMonitors.class);
  // declaring this here vs. the base class as then there would be only a single thread pool for populate and consume
  private static ExecutorService exe;
  private static ExecutorService exe2;
  private static boolean isSubmitted = false;

  private static ConcurrentHashMap<String, DisplayData> displayDatas = new ConcurrentHashMap<>();

  // value object that carries info for the UI - to be pushed by Websockets
//  private static DisplayData displayData = new DisplayData(0,0,0,0,
//      new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

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
  public static String startMonitoring() throws Exception{
    if (!isSubmitted) {
      Callable<String> call = () -> {
        LOGGER.info("started the startMonitoring execution ");
        try {
          LOGGER.info("**** ||||| ****");
          while(true) {
            for (Map.Entry<String, DisplayData> displayDataEntry : displayDatas.entrySet()) {
              refreshDisplayData(displayDataEntry);
            }
          }
        } catch (Exception e) {
          LOGGER.error(e.getMessage(), e);
        }
        return "done?";
      };
      Future<String> f = exe.submit(call);
      isSubmitted = true;
      return "monitoring started";
    } else {
      return "monitoring already started";
    }

  }

  public static void addDisplayData(WindowConfig windowConfig) {
    DisplayData displayData = new DisplayData(new BigDecimal(0).setScale(2, RoundingMode.DOWN),
        new BigDecimal(PropertiesUtil.getProperty("starting.bal.currency")).setScale(2, RoundingMode.DOWN),
        new BigDecimal(PropertiesUtil.getProperty("starting.bal.coin")).setScale(4, RoundingMode.DOWN),
        new BigDecimal(0).setScale(2, RoundingMode.DOWN),
        0,0,0,0,
          new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    displayDatas.put(windowConfig.getId(), displayData);
  }

  private static void refreshDisplayData(Map.Entry<String, DisplayData> displayDataEntry) throws Exception {
    DisplayData displayData = displayDataEntry.getValue();
    StringBuilder sb = new StringBuilder();
    // top asks/bids calculation updated in separate async process
    calculateTopOrders(displayData);

    displayData.setDiffOrderQueueSize(DiffOrdersQueue.sizeOfQueue());
    displayData.setNumberOfConsumingThreads(DiffOrderConsumer.sizeOfThreadPool());
    displayData.setOrderBookAskSize(InternalOrderBook.sizeOfAsks());
    displayData.setOrderBookBidSize(InternalOrderBook.sizeOfBids());

    Thread.sleep(1000 * Integer.parseInt(PropertiesUtil.getProperty("displaydata.freq")));

    LOGGER.info(sb.append(">> ").append(displayDataEntry.getKey())
//        .append(" q ").append(displayData.getDiffOrderQueueSize())
//        .append(" t ").append(displayData.getNumberOfConsumingThreads())
//        .append(" a ").append(displayData.getOrderBookAskSize())
//        .append(" b ").append(displayData.getOrderBookBidSize())
        .toString());

  }

  // execute async so it does not block
  private static void calculateTopOrders(DisplayData displayData) {
    Callable<String> call = () -> {
      displayData.setTopAsks(prepareAggregations(InternalOrderBook.getAsks(),PRICE_COMPARATOR, topX));
      displayData.setTopBids(prepareAggregations(InternalOrderBook.getBids(),PRICE_COMPARATOR_REVERSE, topX));
      return "";
    };
    exe2.submit(call);
  }

  public static String getDisplayDataString(String id) throws Exception {
    return objectMapper.writeValueAsString(displayDatas.get(id));
  }

  public static DisplayData getDisplayData(String id) throws Exception {
    return displayDatas.get(id);
  }

  private static List<Order> prepareAggregations(ConcurrentHashMap<String, Order> orderMap, Comparator<Order> priceComparator, int topX) {
    Set<Order> orderSet = new HashSet<>(orderMap.values());
    List<Order> sortedOrders = orderSet.stream().sorted(priceComparator).collect(Collectors.toList());
    return sortedOrders.subList(0, topX);
  }

}
