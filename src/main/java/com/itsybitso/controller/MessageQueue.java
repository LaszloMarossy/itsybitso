package com.itsybitso.controller;

import java.util.concurrent.LinkedBlockingDeque;

public class MessageQueue {
  static MessageQueue ourInstance = new MessageQueue();
  static LinkedBlockingDeque<String> jobs = new LinkedBlockingDeque<>();


  public static MessageQueue getInstance() {
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

  private MessageQueue() {
  }
}
