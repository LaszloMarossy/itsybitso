package com.itsybitso.controller;

import java.io.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itsybitso.entity.OrderBook;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BitsoRestClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(BitsoRestClient.class);

  public final static void main(String[] args) {
    GetBitsoOrderBook();
  }

  public static OrderBook GetBitsoOrderBook() {
    CloseableHttpClient httpClient = HttpClientBuilder.create().build();
    ObjectMapper objectMapper = new ObjectMapper();
    OrderBook orderBook = null;
    try {
      HttpGet httpGetRequest = new HttpGet("https://api.bitso.com/v3/order_book/?book=btc_mxn&aggregate=false");
      HttpResponse httpResponse = httpClient.execute(httpGetRequest);
      HttpEntity entity = httpResponse.getEntity();

      if (entity != null) {
        InputStream inputStream = entity.getContent();
        try {

          orderBook = objectMapper.readValue(inputStream, OrderBook.class);
          LOGGER.info("size of Asks:" + orderBook.getPayload().getAsks().length +
              "size of Bids:" + orderBook.getPayload().getBids().length);
        } catch (Exception e) {
          e.printStackTrace();
        } finally {
          try { inputStream.close(); } catch (Exception ignore) {}
          httpClient.close();
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return orderBook;

  }
}
