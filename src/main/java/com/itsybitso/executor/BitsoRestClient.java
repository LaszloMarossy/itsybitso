package com.itsybitso.executor;

import java.io.*;
import java.math.BigDecimal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itsybitso.entity.ExchangeRate;
import com.itsybitso.entity.OrderBook;
import com.itsybitso.entity.Trade;
import com.itsybitso.entity.Trades;
import com.itsybitso.util.PropertiesUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BitsoRestClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(BitsoRestClient.class);
  private static ObjectMapper objectMapper = new ObjectMapper();


  public static void main(String[] args) {
    getBitsoOrderBook();
  }

  /**
   * read in the full order book from Bitso REST endpoint
   * @return OrderBook
   */
  public static OrderBook getBitsoOrderBook() {
    OrderBook orderBook = null;
    try {
      CloseableHttpClient httpClient = HttpClientBuilder.create().build();
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
          LOGGER.error("error", e);
        } finally {
          try { inputStream.close(); } catch (Exception ignore) {}
          httpClient.close();
        }
      }
    } catch (Exception e) {
      LOGGER.error("error", e);
    }
    return orderBook;
  }

  public static Trade[] getBitsoTrades() {
    Trades trades = new Trades(false, null);
    try {
      CloseableHttpClient httpClient = HttpClientBuilder.create().build();
      HttpGet httpGetRequest = new HttpGet("https://api.bitso.com/v3/trades/?book=btc_mxn&limit="
          + PropertiesUtil.getProperty("displaydata.numberoftrades"));
      HttpResponse httpResponse = httpClient.execute(httpGetRequest);
      HttpEntity entity = httpResponse.getEntity();

      if (entity != null) {
        InputStream inputStream = entity.getContent();
        try {
          trades = objectMapper.readValue(inputStream, Trades.class);
        } catch (Exception e) {
          LOGGER.error("error", e);
        } finally {
          try { inputStream.close(); } catch (Exception ignore) {}
          httpClient.close();
        }
      }
    } catch (Exception e) {
      LOGGER.error("error", e);
    }
    return trades.getPayload();
  }

  public static BigDecimal getUsdMxn() {
    ExchangeRate exchangeRate = null;
    try {
      CloseableHttpClient httpClient = HttpClientBuilder.create().build();
      HttpGet httpGetRequest = new HttpGet(PropertiesUtil.getProperty("xchange.url"));
      HttpResponse httpResponse = httpClient.execute(httpGetRequest);
      HttpEntity entity = httpResponse.getEntity();
      if (entity != null) {
        InputStream inputStream = entity.getContent();
        try {
          exchangeRate = objectMapper.readValue(inputStream, ExchangeRate.class);
        } catch (Exception e) {
          LOGGER.error("error", e);
        } finally {
          try { inputStream.close(); } catch (Exception ignore) {}
          httpClient.close();
        }
      }
    } catch (Exception e) {
      LOGGER.error("error", e);
    }
    return new BigDecimal(exchangeRate.getQuotes().getUSDMXN());
  }

}
