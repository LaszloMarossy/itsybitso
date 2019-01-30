package com.itsybitso.executor;

import com.itsybitso.controller.DiffOrdersQueue;
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
public class DiffOrderQueuer extends AsyncExecutor {

  private static final Logger LOGGER = LoggerFactory.getLogger(DiffOrderQueuer.class);
  // declaring this here vs. the base class as then there would be only a single thread pool for populate and consume
  private static ExecutorService exe;
  private static boolean isSubmitted = false;

  static {
    // overwrite default in base class
    AsyncExecutor.defaultNumThreads = 10;
    exe = Executors.newFixedThreadPool(defaultNumThreads);
  }

  /**
   * main method called by the AppController REST service.  Starts populating the internal queue with
   * the WebSocket messsages received.
   *
   * @return result string - ignored for now
   */
  public static String startAsyncPopulateQueue() {
    if (!isSubmitted) {
      Callable<String> call = () -> {
        LOGGER.info("started the startAsyncPopulateQueue execution ");
        try {

          // open websocket and subscribe to diff_orders
          BitsoWsClient clientEndPoint = new BitsoWsClient(new URI("wss://ws.bitso.com/"));
          LOGGER.info("############ CONNECTED to Bitso");
          Thread.sleep(6000);

        } catch (Exception e) {
          LOGGER.error(e.getMessage(), e);
        }

        return "done?";
      };
      exe.submit(call);
      isSubmitted = true;
      return "startAsyncPopulateQueue started";
    } else {
      return "startAsyncPopulateQueue was already started";
    }
  }


  /**
   * this method will be called by the BitsoWsClient onReceipt() method for each message received.  There could be
   * some delay in adding to the queue in theory so the Future return and multithreading is justified.
   *
   * @param msg received from WebSocket client
   * @return Future will be ignored for now
   */
  static Future<String> addToQueue(String msg) {

    return exe.submit(() -> {
      DiffOrdersQueue.addJobToEnd(msg);

      LOGGER.info(">>>>> " + msg);
      return "returning addJobToQueue with job ID " + msg;
    });

  }


}
