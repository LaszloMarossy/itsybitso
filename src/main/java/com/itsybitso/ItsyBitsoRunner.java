package com.itsybitso;

import com.itsybitso.controller.AppController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;

public class ItsyBitsoRunner {
  private static final Logger LOGGER = LoggerFactory.getLogger(ItsyBitsoRunner.class);

  public static void main(String[] args) throws Exception {

    AppController appController =  new AppController();
    LOGGER.info("STARTING POPULATE QUEUE");
    Response response = appController.runPopulateQueue();
    LOGGER.info("POPULATE RESPONSE STATUS " + response.getStatus());
    AppController appController2 =  new AppController();
    LOGGER.info("STARTING CONSUME QUEUE");
    response = appController2.runConsumeQueue();
    LOGGER.info("CONSUME RESPONSE STATUS " + response.getStatus());
    Thread.sleep(70000);
    response = appController.runPopulateQueue();
  }

}
