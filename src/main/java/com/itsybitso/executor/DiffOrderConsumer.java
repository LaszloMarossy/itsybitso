package com.itsybitso.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itsybitso.bl.DiffOrderProcessor;
import com.itsybitso.controller.DiffOrdersQueue;
import com.itsybitso.entity.DiffOrder;
import com.itsybitso.entity.InternalOrderBook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Asynchronous parallel executor of consuming the queue; called by the AppController REST method
 */
public class DiffOrderConsumer extends AsyncExecutor {

  private static final Logger LOGGER = LoggerFactory.getLogger(DiffOrderConsumer.class);
  private static ExecutorService exe;
  private static ObjectMapper mapper = new ObjectMapper();
  // for the monitor
  private static int numberOfThreads = defaultNumThreads;

  static {
    exe = Executors.newFixedThreadPool(defaultNumThreads);
  }
  /**
   * main method called by the AppController REST service
   *
   * @return result string
   */
  public static String startAsyncConsumeQueue() {

    LOGGER.info("started the startAsyncConsumeQueue execution ");
    Callable<String> call = () -> {
      String result = "no result";
      while (true) {
        if (DiffOrdersQueue.sizeOfQueue() == 0) {
          LOGGER.info("|||| SLEEPING ");
          Thread.sleep(1000);
        } else {
//          LOGGER.info("<=== consuming " + DiffOrdersQueue.sizeOfQueue());
          // this little delay is needed so the while loop is not going crazy and not actually calling
          // exe.submit() without actually calling processDiffOrderFromQueue() but filling the logs with the
          // above output
          Thread.sleep(200);
          Future<String> futureResult = null;
          try {
            futureResult = exe.submit(processDiffOrderFromQueue());
            futures.add(futureResult);
          } catch (RejectedExecutionException e) {
            String leftOutItem = null;
            if (futureResult != null) {
              leftOutItem = futureResult.get();
            }
            LOGGER.error("&*(&*&*(&*(&*(&*(&(&*(&(*& " + leftOutItem);
            break;
          } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage(), e);
            break;
          }

        }
      }
      return result;
    };
    Future<String> f = exe.submit(call);

    return "started startAsyncConsumeQueue";

  }

  public static void switchPool(String consumeWith) throws NumberFormatException, InterruptedException {
    int numThreads = Integer.parseInt(consumeWith);
    exe.shutdown();
    LOGGER.info("SHUTTING DOWN DiffOrderConsumer thread pool");
    boolean terminated = exe.awaitTermination(3, TimeUnit.MINUTES);
    LOGGER.info("TERMINATED DiffOrderConsumer thread pool " + terminated);
    exe = Executors.newFixedThreadPool(numThreads);
    LOGGER.info("STARTING DiffOrderConsumer WITH " + consumeWith);
    numberOfThreads = numThreads;
  }

  static int sizeOfThreadPool() {
    return numberOfThreads;
  }

  /**
   * method called for parallel processing diff orders taken from the internal queue
   *
   * @return the total produced by the item to aggregate
   */
  private static Callable<String> processDiffOrderFromQueue() {
    return () -> {
      StringBuilder sb = new StringBuilder();
      String jobString = DiffOrdersQueue.getFirstJob();
      DiffOrder diffOrder = null;
      String previousOrderInfo;
      if (jobString.startsWith("{\"type\":\"diff-orders\"")) {
        try {
          diffOrder = mapper.readValue(jobString, DiffOrder.class);
          previousOrderInfo = DiffOrderProcessor.updateInternalOrderBook(diffOrder);
        } catch (Exception e) {
          LOGGER.error(e.getMessage() + " during processing " + jobString, e);
          return null;
        }

      } else {
        LOGGER.warn("NON-DIFF-ORDER MESSAGE ON QUEUE: " + jobString);
      }
      return "processDiffOrderFromQueue finished";
    };
  }
}
