package com.itsybitso.bl;

import com.itsybitso.executor.BitsoRestClient;

import com.itsybitso.entity.Tick;
import com.itsybitso.entity.Trade;
import com.itsybitso.executor.TradesPoller;
import com.itsybitso.util.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import static com.itsybitso.entity.Tick.*;

/**
 * this class has the business logic for analyzing the most recent trades and updating their additional tick fields
 *
 * @author laszlo
 */
public class TradeAnalyzer extends BusinessLogic {

  private static final Logger LOGGER = LoggerFactory.getLogger(TradeAnalyzer.class);
  private static final String BOOK = "btc_mxn";
  private static final String MARKER_SIDE_SELL = "PRETEND sell";
  private static final String MARKER_SIDE_BUY = "PRETEND buy";
  private static String DOWN_N = PropertiesUtil.getProperty("trade.down_n");
  private static String UP_M = PropertiesUtil.getProperty("trade.up_m");

  // list of trades Itsybitso WOULD make
  private static ArrayList<Trade> pretendTrades = new ArrayList<>();
  // the trade with the largest tid received so far; need to know to process batches of trades potentially coming in with the next REST request
  private static Long mostRecentTradeTidFromLastBatch = 0L;


  public static Trade[] enhanceRecentTrades() throws Exception {
    Trade[] trades = BitsoRestClient.getBitsoTrades();
    trades = mergePretendTrades(trades);
    return updateTradesWithTicks(trades);
  }

  /**
   * multiply trade ID-s by 10 so we can insert another pretend-trade in between,
   * then merge with pretend-trades
   * @param trades array of trades
   */
  public static Trade[] mergePretendTrades(Trade[] trades) {

    adjustTradeIds(trades);
    Stream<Trade> pretendStream = pretendTrades.stream();
    Stream<Trade> tradesStream = Arrays.stream(trades);
    Trade[] result = Stream.concat(tradesStream, pretendStream).toArray(Trade[]::new);
    // sort by tid so pretend-trades can be merged into the display array
    Arrays.sort(result);
    return result;
  }


  public static Trade[] updateTradesWithTicks(Trade[] trades) throws Exception {
    ArrayList<String> ticks = new ArrayList<>();
    Comparator<Trade> reverseTid = Comparator.comparing(Trade::getTid);
    // reverse the order of tid-s so that we can go from oldest to newest for tick info gathering
    Trade[] reversedTrades = Arrays.stream(trades).sorted(reverseTid).toArray(Trade[]::new);
    // set tick for each trade in order of trade id
    setTickAndStatus(reversedTrades);
    // reverse list for display..

    return Arrays.stream(trades).sorted(reverseTid.reversed()).toArray(Trade[]::new);

  }

  /**
   * called from TradePoller after each BATCH of new trades received; even though we poll for new trades every second,
   * it is possible that with a new request a larger group of new trades would come in that we have not seen before;
   * this implementation goes with the way it makes the most sense (before clarifying the requirements further):
   * if an array of trades are received, we should make at MOST one trade - one buy (if going back from the biggest tid
   * value we find a nthStatus value that is TICK_UP[M] where M == param("trade.up_m") OR one sell (if going back from
   * the biggest tid value we find a nthStatus value that is TICK_DOWN[N] where N == param("trade.down_n").
   *
   * For example, we could have our trade.up_m parameter set to 3, meaning that if we receive a trade with the nthStatus
   * of TICK_UP3 we SHOULD execute a buy trade, and we could be receiving 6 new trades with the new REST request that
   * contain in ascending tid order a TICK_UP1, TICK_UP2, TICK_UP3, TICK_UP4, TICK_UP5, TICK_DOWN1.  Since all tid's are
   * new, we go through all of them, starting from the latest with nthStatus TICK_DOWN1 going backward.  At the 4th
   * trade we will find the one with nthStatus TICK_UP3 so we call to execute a trade, using price info from the most
   * recent trade (with nthStatus TICK_DOWN1)
   *
   * It is also possible that we passed our trade condition many times in the set of new trades received - if we imagine
   * lots of new trades with two or more ups-and-downs, that would satisfy our buy criteria multiple times, but until
   * clarification of requirements this implementation <b>executes only one buy OR one sell per batch of new trades
   * received, whichever occurred more recently</b>.
   *
   * Our latest trade is a TICK_DOWN1, so we will use <b>its</b> price (closest to market price) for our
   * trade-to-be-executed.
   *
   * TODO clarify logic with requirements
   */
  public static void makeTradeDecision(List<Trade> recentTrades)  {
    int i = 0;
    while (recentTrades.get(i) != null && recentTrades.get(i).getTid().compareTo(mostRecentTradeTidFromLastBatch) > 0) {
      Trade nextNewTrade = recentTrades.get(i);
      if (nextNewTrade.getNthStatus().equals(TICK_DOWN.toString() + DOWN_N) &&
          nextNewTrade.getTick().equals(TICK_DOWN)) {
        Future<Trade> f = TradesPoller.asyncTrade(recentTrades.get(0), MARKER_SIDE_SELL);
        break;
      }
      if (nextNewTrade.getNthStatus().equals(TICK_UP.toString() + UP_M) &&
          nextNewTrade.getTick().equals(TICK_UP)) {
        Future<Trade> f = TradesPoller.asyncTrade(recentTrades.get(0), MARKER_SIDE_BUY);
        break;
      }
      i++;
    }
    mostRecentTradeTidFromLastBatch = recentTrades.get(0).getTid();
  }

  /**
   * this method could be called from makeTradeDecision but instead we call it through TradesPoller so we can run it async.
   * @param mostRecentTrade the most recent trade received through REST, but already enhanced with tick info
   * @param typeOfTrade MARKER_SIDE_SELL or MARKER_SIDE_BUY
   * @return the pretend-trade created
   */
  public static Trade trade(Trade mostRecentTrade, String typeOfTrade) {
    Trade pretendTrade = new Trade( BOOK, mostRecentTrade.getCreatedAt(), new BigDecimal(1), typeOfTrade,
        mostRecentTrade.getPrice(), mostRecentTrade.getTid() + 5);
      LOGGER.info("$$$$$$$$$$$$$$$$$$$$$$$$$$$ " + pretendTrade.getTid());
    pretendTrades.add(pretendTrade);
    return pretendTrade;
  }

  /**
   * logic to set tick and nthStatus values for each trade
   * @param trades array of trades
   */
  private static void setTickAndStatus(Trade[] trades) {
    Trade previousTrade = null;
    // we make up this value for the unknown value of the trade that was right before the first one we have
    Tick mostRecentNonZeroTick = TICK_UP; //will only be TICK_UP or TICK_DOWN
    int currentStatusCount = 1;
    for (Trade trade : trades) {
      if (previousTrade != null) {
        switch (trade.getPrice().compareTo(previousTrade.getPrice())) {
          case -1:
            trade.setTick(TICK_DOWN);
            switch (previousTrade.getTick()) {
              case TICK_DOWN:
                // previous DOWN current DOWN -> DOWN N+
                mostRecentNonZeroTick = TICK_DOWN;
                currentStatusCount++;
                break;
              case TICK_ZERO:
                // previous ZERO current DOWN -> DOWN N+ or DOWN 1 (dep on mostRecentNonZeroTick)
                switch (mostRecentNonZeroTick) {
                  case TICK_DOWN:
                    // one before ZERO was DOWN, so increase DOWN counter; type unchanged
                    currentStatusCount++;
                    break;
                  default: //case TICK_UP:
                    // one before ZERO was UP, so reset counter and type
                    currentStatusCount = 1;
                    mostRecentNonZeroTick = TICK_DOWN;
                }
                break;
              default: //case TICK_UP:
                // previous UP current DOWN -> DOWN 1
                currentStatusCount = 1;
                mostRecentNonZeroTick = TICK_DOWN;
            }
            break;
          case 1:
            trade.setTick(TICK_UP);
            switch (previousTrade.getTick()) {
              case TICK_DOWN:
                // previous DOWN current UP -> UP 1
                mostRecentNonZeroTick = TICK_UP;
                currentStatusCount = 1;
                break;
              case TICK_ZERO:
                // previous ZERO current UP -> UP N+ or UP 1 (dep on mostRecentNonZeroTick)
                switch (mostRecentNonZeroTick) {
                  case TICK_DOWN:
                    // one before ZERO was DOWN, so reset counter and type
                    currentStatusCount = 1;
                    mostRecentNonZeroTick = TICK_UP;
                    break;
                  default: //case TICK_UP:
                    // one before ZERO was UP, so increase UP counter; type unchanged
                    currentStatusCount++;
                }
                break;
              default: //case TICK_UP:
                // previous UP current UP -> UP N+ ; so increase UP counter; type unchanged
                currentStatusCount++;
            }
            break;
          default: //case 0:
            trade.setTick(TICK_ZERO);
            // previous DOWN || ZERO || UP current ZERO -> PREVIOUS-N => currentStatusCount && mostRecentNonZeroTick unchanged
        }
        trade.setNthStatus(mostRecentNonZeroTick.toString() + currentStatusCount);
      } else {
        // the earliest trade will never have previous tick info, so we make it up for it
        trade.setTick(TICK_UP);
        trade.setNthStatus(mostRecentNonZeroTick.toString() + currentStatusCount);
      }
      previousTrade = trade;
    }

  }

  /**
   * multiply trade ID-s by 10 so we can insert another pretend-trade in between
   * @param trades array of trades
   */
  private static void adjustTradeIds(Trade[] trades) {
    for (Trade trade : trades) {
      trade.setTid(trade.getTid() * 10);
    }
  }

  public static void setPretendTrades(Trade[] trades) {
    pretendTrades = new ArrayList<>(Arrays.asList(trades));
  }

  // for testing
  public static Trade[] getPretendTrades() {
    return pretendTrades.toArray(new Trade[pretendTrades.size()]);
  }

  public static void setDownN(String downN) {
    DOWN_N = downN;
  }

  public static void setUpM(String upM) {
    UP_M = upM;
  }

  public static void setMostRecentTradeTidFromLastBatch(Long mostRecentTradeTidFromLastBatch) {
    TradeAnalyzer.mostRecentTradeTidFromLastBatch = mostRecentTradeTidFromLastBatch;
  }
}
