package com.itsybitso.controller;

import java.util.concurrent.LinkedBlockingDeque;

/**
 * internal queue to keep the diff-orders websocket feed data till they are worked off
 */
public class DiffOrdersQueue {
  static DiffOrdersQueue ourInstance = new DiffOrdersQueue();
  static LinkedBlockingDeque<String> jobs = new LinkedBlockingDeque<>();


  public static DiffOrdersQueue getInstance() {
    return ourInstance;
  }

  /**
   * add an element to the last position of the queue
   * @param job to be processed
   */
  public static void addJobToEnd(String job) {
    boolean success = false;
    while (!success) {
      success = jobs.offerLast(job);
    }
  }

  /**
   * take the first element and wait for one becoming available
   * @return the first job to be processed
   * @throws InterruptedException if interrupted
   */
  public static String getFirstJob() throws InterruptedException {
    return jobs.takeFirst();
  }

  public static int sizeOfQueue() throws InterruptedException {
    return jobs.size();
  }

  private DiffOrdersQueue() {
  }
}
