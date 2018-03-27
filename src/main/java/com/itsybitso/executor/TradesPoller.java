package com.itsybitso.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itsybitso.bl.TradeAnalyzer;
import com.itsybitso.entity.Trade;
import com.itsybitso.util.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/**
 * Asynchronous single-threaded executor of refreshing internal copy of recent trades; called by the AppController REST method
 */
public class TradesPoller extends AsyncExecutor {

  private static final Logger LOGGER = LoggerFactory.getLogger(TradesPoller.class);
  private static final String TRADES_POLL_INTERVAL = "poller.interval";
  // declaring this here vs. the base class as then there would be only a single thread pool for populate and consume
  private static ExecutorService tradePollingExe;
  private static ExecutorService tradeExe;
  private static int intervalSec = 1;

  static {
    tradePollingExe = Executors.newSingleThreadExecutor();
    tradeExe = Executors.newSingleThreadExecutor();
  }

  /**
   * main method called by the AppController REST service.  Starts refreshing the internal recent trades list
   * with polled trades REST info.
   *
   * @return result future string - ignored for now
   */
  public static String startAsyncRefreshRecentTrades() {
    Callable<String> call = () -> {
      LOGGER.info("started the startAsyncRefreshRecentTrades execution ");
      while(true) {
        try {
          Trade[] trades = TradeAnalyzer.enhanceRecentTrades();
          AppMonitor.setRecentTrades(trades);
          TradeAnalyzer.makeTradeDecision(AppMonitor.getDisplayData().getRecentTrades());
          // property file entry can overwrite the hard-coded default here
          intervalSec = Integer.parseInt(PropertiesUtil.getProperty(TRADES_POLL_INTERVAL));
          Thread.sleep(intervalSec * 1000);

        } catch (Exception e) {
          LOGGER.error(e.getMessage(), e);
          break;
        }
      }
      return "done?";
    };
    Future<String> f = tradePollingExe.submit(call);
    return "startAsyncRefreshRecentTrades started";
  }

  /**
   * an async executor wrapper for making trades; future-proof for real trading, but for pretend-trading it is not so
   * necessary.
   *
   * @param mostRecentTrade the most recent trade
   * @param typeOfTrade buy or sell constants of TradeAnalyzer
   * @return future trade
   */
  public static Future<Trade> asyncTrade(Trade mostRecentTrade, String typeOfTrade) {
    Callable<Trade> call = () -> TradeAnalyzer.trade(mostRecentTrade, typeOfTrade);
    return tradeExe.submit(call);
  }


}
