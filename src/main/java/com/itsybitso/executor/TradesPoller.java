package com.itsybitso.executor;

import com.itsybitso.bl.ConfigurableTrader;
import com.itsybitso.bl.TradeAnalyzer;
import com.itsybitso.entity.Trade;
import com.itsybitso.entity.WindowConfig;
import com.itsybitso.util.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/**
 * Asynchronous single-threaded executor of refreshing internal copy of recent trades; called by the AppController REST method
 */
public class TradesPoller extends AsyncExecutor {

  private static TradesPoller tradesPoller = null;

  private static final Logger LOGGER = LoggerFactory.getLogger(TradesPoller.class);
  private static final String TRADES_POLL_INTERVAL = "poller.interval";
  private static int intervalSec = 1;
  private static BigDecimal usdMxn = new BigDecimal(1.0);

  // declaring this here vs. the base class as then there would be only a single thread pool for populate and consume
  private ExecutorService tradePollingExe;
  private ExecutorService exchRateExe;
  private ExecutorService tradeAnalyzingExe;
  private ExecutorService tradeExe;

  private ArrayList<WindowConfig> configs = new ArrayList<>();
  private ConcurrentHashMap<String, ConfigurableTrader> traders = new ConcurrentHashMap<>();
  private Trade[] bitsoTrades;


  public static TradesPoller getInstance() {
    if (tradesPoller == null) {
      tradesPoller = new TradesPoller();
    }
    return tradesPoller;
  }

  private TradesPoller()   {
    tradePollingExe = Executors.newFixedThreadPool(3);
    tradeAnalyzingExe = Executors.newFixedThreadPool(5);
    tradeExe = Executors.newFixedThreadPool(3);
    exchRateExe = Executors.newSingleThreadExecutor();
  }

  public void getXchangeRate() {
    Callable<String> call = () -> {
      LOGGER.info("getting exchange rate ");
      while(true) {
        try {
          // get external trades once
          usdMxn = BitsoRestClient.getUsdMxn();
          LOGGER.info("USDMXN=".concat(usdMxn.toString()));

        } catch (Exception e) {
          LOGGER.error(e.getMessage(), e);
          Thread.sleep(2000);
        }
        Thread.sleep(60 * 60000);
      }
    };
    exchRateExe.submit(call);
  }

  /**
   * main method called by system startup.  Starts refreshing the internal recent trades list
   * with polled trades REST info.
   *
   * @return result future string - ignored for now
   */
  public void getRecentBitsoTrades() {
    Callable<String> call = () -> {
      LOGGER.info("started the addNewConfiguration execution ");
      while(true) {
        try {
          // get external trades once
          bitsoTrades = BitsoRestClient.getBitsoTrades();
          if (bitsoTrades != null && bitsoTrades.length > 0) {

            LOGGER.info("latest BITSO trade: ".concat(bitsoTrades[0].getTid().toString())
                .concat(" ").concat(bitsoTrades[0].getMakerSide())
                .concat(" p ").concat(bitsoTrades[0].getPrice().toPlainString()
                .concat(" at ")).concat(bitsoTrades[0].getCreatedAt()));
          }
//
//          // set clone of new trades
//          traders.forEach((key, trader) -> trader.setWipTrades(bitsoTrades.clone()));

          // call to async analysis and trade
          traders.forEach((key, trader) -> doConfigSpecificTrading(trader));

          intervalSec = Integer.parseInt(PropertiesUtil.getProperty(TRADES_POLL_INTERVAL));
          Thread.sleep(intervalSec * 1000);

        } catch (Exception e) {
          LOGGER.error(e.getMessage(), e);
          Thread.sleep(2000);
        }
      }
    };
    tradePollingExe.submit(call);
  }



  /**
   * main method called by the AppController REST service.  Starts refreshing the internal recent trades list
   * with polled trades REST info.
   *
   * @return result future string - ignored for now
   */
  public String addNewConfiguration(WindowConfig windowConfig) {
    configs.add(windowConfig);
    traders.put(windowConfig.getId(), new ConfigurableTrader(windowConfig));
    AppMonitors.addDisplayData(windowConfig);
    LOGGER.info("ADDED " + windowConfig.getId());
    return "ADDED " + windowConfig.getId();
  }

  private void doConfigSpecificTrading(ConfigurableTrader configurableTrader) {
    Callable<String> call = () -> {
      try {
        if (bitsoTrades != null && bitsoTrades.length > 0) {
          Trade[] trades = Arrays.stream(bitsoTrades).map(Trade::new).toArray(Trade[]::new);
          configurableTrader.setWipTrades(trades);
          configurableTrader.getAndEnhanceRecentTrades();
          configurableTrader.makeTradeDecision();
          configurableTrader.updateDisplay();
        }
      } catch (Exception e) {
        LOGGER.error("SHIIIIIIT", e);
      }
      return "done " + configurableTrader.getId();
    };
    tradeAnalyzingExe.submit(call);
  }


  /**
   * an async executor wrapper for making trades; future-proof for real trading, but for pretend-trading it is not so
   * necessary.
   *
   * @param mostRecentTrade the most recent trade
   * @param typeOfTrade buy or sell constants of TradeAnalyzer
   * @return future trade
   */
  public Future<Trade> asyncTrade(Trade mostRecentTrade, String typeOfTrade) {
    Callable<Trade> call = () -> TradeAnalyzer.trade(mostRecentTrade, typeOfTrade);
    return tradeExe.submit(call);
  }

  /**
   * an async executor wrapper for making trades; future-proof for real trading, but for pretend-trading it is not so
   * necessary.
   *
   * @param configurableTrader the trader that is configured with its decisions
   * @param mostRecentTrade the most recent trade
   * @param typeOfTrade buy or sell constants of TradeAnalyzer
   * @return future trade
   */
  public Future<Trade> asyncTrade(ConfigurableTrader configurableTrader, Trade mostRecentTrade, String typeOfTrade) {
    Callable<Trade> call = () -> configurableTrader.trade(mostRecentTrade, typeOfTrade);
    return tradeExe.submit(call);
  }

  /**
   * main method called by the AppController REST service.  Starts refreshing the internal recent trades list
   * with polled trades REST info.
   *
   * @return result future string - ignored for now
   */
  public String startAsyncRefreshRecentTrades() {
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
    tradePollingExe.submit(call);
    return "startAsyncRefreshRecentTrades started";
  }

  public static BigDecimal getUsdMxn() {
    return usdMxn;
  }
}
