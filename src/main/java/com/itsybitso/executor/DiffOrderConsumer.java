package com.itsybitso.executor;

import com.itsybitso.controller.MessageQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Asynchronous parallel executor of consuming the queue; called by the AppController REST method
 */
public class DiffOrderConsumer extends AsyncExecutor {

  private static final Logger LOGGER = LoggerFactory.getLogger(DiffOrderConsumer.class);
  static ExecutorService exe;

  static {
    exe = Executors.newFixedThreadPool(defaultNumThreads);
  }
  /**
   * main method called by the AppController REST service
   *
   * @return result string
   */
  public static Future<String> startAsyncConsumeQueue() {

    LOGGER.info("started the startAsyncConsumeQueue execution ");
    Callable<String> call = () -> {
      String result = "no result";
      while (true) {
        if (MessageQueue.sizeOfQueue() == 0) {
          LOGGER.info("|||| SLEEPING ");
          Thread.sleep(1000);
        } else {
//          LOGGER.info("<=== consuming " + MessageQueue.sizeOfQueue());
          // this little delay is needed so the while loop is not going crazy and not actually calling
          // exe.submit() without actually calling processJobFromQueue() but filling the logs with the
          // above output
          Thread.sleep(200);
          Future<String> futureResult = null;
          try {
            futureResult = exe.submit(processJobFromQueue());
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

    return exe.submit(call);

  }

  public static void switchPool(String consumeWith) throws NumberFormatException, InterruptedException {
    int numThreads = Integer.parseInt(consumeWith);

    exe.shutdown();
    LOGGER.info("SHUTTING DOWN DiffOrderConsumer thread pool");
    boolean terminated = exe.awaitTermination(3, TimeUnit.MINUTES);
    LOGGER.info("TERMINATED DiffOrderConsumer thread pool " + terminated);
    exe = Executors.newFixedThreadPool(numThreads);
    LOGGER.info("STARTING DiffOrderConsumer WITH " + consumeWith);
  }

  /**
   * method called for parallel processing of logic on the item level
   *
   * @return the total produced by the item to aggregate
   */
  private static Callable<String> processJobFromQueue() {
    return () -> {
      String job = MessageQueue.getFirstJob();

      Thread.sleep(ThreadLocalRandom.current().nextInt(500, 1000));

      LOGGER.info("<<<<< queue size " + MessageQueue.sizeOfQueue() +
          " <== " + job);
      return job;
    };
  }
}
