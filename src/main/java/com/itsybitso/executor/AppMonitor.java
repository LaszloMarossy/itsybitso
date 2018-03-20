package com.itsybitso.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itsybitso.controller.DiffOrdersQueue;
import com.itsybitso.entity.InternalOrderBook;
import com.itsybitso.entity.Monitor;
import com.itsybitso.util.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/**
 * Asynchronous parallel executor of populating the internal queue; called by the AppController REST method
 */
public class AppMonitor extends AsyncExecutor {

  private static final Logger LOGGER = LoggerFactory.getLogger(AppMonitor.class);
  // declaring this here vs. the base class as then there would be only a single thread pool for populate and consume
  private static ExecutorService exe;

  private static Monitor monitor = new Monitor(0,0,0,0);
  private static ObjectMapper objectMapper = new ObjectMapper();

  static {
    exe = Executors.newSingleThreadExecutor();
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



}
