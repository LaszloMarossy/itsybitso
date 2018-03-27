package com.itsybitso;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itsybitso.bl.TradeAnalyzer;
import com.itsybitso.entity.Tick;
import com.itsybitso.entity.Trade;
import com.itsybitso.entity.Trades;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;


import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class TradeAnalyzerTest {

  private ObjectMapper objectMapper = new ObjectMapper();

  @Before
  public void cleanup() {
    TradeAnalyzer.setPretendTrades(new Trade[0]);
    TradeAnalyzer.setMostRecentTradeTidFromLastBatch(0L);
  }


  @Test
  public void testMergePretendTrades() {

    try {
      Trades recentTrades = objectMapper.readValue(
          IOUtils.toString(this.getClass().getResourceAsStream("/recent-trades.json"), "UTF-8"), Trades.class);
      assertEquals(50, recentTrades.getPayload().length);
      Trades pretendTrades = objectMapper.readValue(
          IOUtils.toString(this.getClass().getResourceAsStream("/pretend-trades.json"), "UTF-8"), Trades.class);
      assertEquals(2, pretendTrades.getPayload().length);


      TradeAnalyzer.setPretendTrades(pretendTrades.getPayload());
      Trade[] mergedTrades = TradeAnalyzer.mergePretendTrades(recentTrades.getPayload());

      assertTrue("size does not match", mergedTrades.length == 52);
      // check if expected pretend-trade made it into the right place
      assertEquals( "did not find the pretend-trade at the expected location",
          new Long(61464785), mergedTrades[4].getTid());

    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  @Test
  public void testUpdateTradesWithTicks() {

    try {
      Trades recentTrades = objectMapper.readValue(
          IOUtils.toString(this.getClass().getResourceAsStream("/recent-trades.json"), "UTF-8"), Trades.class);
      Trade[] trades = recentTrades.getPayload();
      trades = TradeAnalyzer.updateTradesWithTicks(trades);
      assertTrue("array size not the same as before", trades.length == 50);
      assertTrue("tick info not properly set for first trade", trades[0].getTick() == Tick.TICK_UP);
      assertTrue("tick info not properly set for second trade", trades[1].getTick() == Tick.TICK_UP);
      assertTrue("tick info not properly set for third trade", trades[2].getTick() == Tick.TICK_DOWN);
      assertTrue("tick info not properly set for forth trade", trades[3].getTick() == Tick.TICK_UP);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testUpdateTradesWithTicksAndStatus() {

    try {
      Trades recentTrades = objectMapper.readValue(
          IOUtils.toString(this.getClass().getResourceAsStream("/status_test.json"), "UTF-8"), Trades.class);

      Trade[] trades = recentTrades.getPayload();
      trades = TradeAnalyzer.updateTradesWithTicks(trades);
      for (Trade trade : trades) {
        System.out.println(objectMapper.writeValueAsString(trade));
      }
      assertTrue("tick or nStatus wrong for trade 0", trades[0].getTick() == Tick.TICK_UP && trades[0].getNthStatus().equals("TICK_UP2"));
      assertTrue("tick or nStatus wrong for trade 1", trades[1].getTick() == Tick.TICK_UP && trades[1].getNthStatus().equals("TICK_UP1"));
      assertTrue("tick or nStatus wrong for trade 2", trades[2].getTick() == Tick.TICK_DOWN && trades[2].getNthStatus().equals("TICK_DOWN1"));
      assertTrue("tick or nStatus wrong for trade 3", trades[3].getTick() == Tick.TICK_UP && trades[3].getNthStatus().equals("TICK_UP1"));
      assertTrue("tick or nStatus wrong for trade 4", trades[4].getTick() == Tick.TICK_DOWN && trades[4].getNthStatus().equals("TICK_DOWN5"));
      assertTrue("tick or nStatus wrong for trade 5", trades[5].getTick() == Tick.TICK_ZERO && trades[5].getNthStatus().equals("TICK_DOWN4"));
      assertTrue("tick or nStatus wrong for trade 6", trades[6].getTick() == Tick.TICK_DOWN && trades[6].getNthStatus().equals("TICK_DOWN4"));
      assertTrue("tick or nStatus wrong for trade 7", trades[7].getTick() == Tick.TICK_ZERO && trades[7].getNthStatus().equals("TICK_DOWN3"));
      assertTrue("tick or nStatus wrong for trade 8", trades[8].getTick() == Tick.TICK_DOWN && trades[8].getNthStatus().equals("TICK_DOWN3"));
      assertTrue("tick or nStatus wrong for trade 9", trades[9].getTick() == Tick.TICK_DOWN && trades[9].getNthStatus().equals("TICK_DOWN2"));
      assertTrue("tick or nStatus wrong for trade 10", trades[10].getTick() == Tick.TICK_DOWN && trades[10].getNthStatus().equals("TICK_DOWN1"));
      assertTrue("tick or nStatus wrong for trade 11", trades[11].getTick() == Tick.TICK_ZERO && trades[11].getNthStatus().equals("TICK_UP3"));
      assertTrue("tick or nStatus wrong for trade 12", trades[12].getTick() == Tick.TICK_ZERO && trades[12].getNthStatus().equals("TICK_UP3"));
      assertTrue("tick or nStatus wrong for trade 13", trades[13].getTick() == Tick.TICK_UP && trades[13].getNthStatus().equals("TICK_UP3"));
      assertTrue("tick or nStatus wrong for trade 14", trades[14].getTick() == Tick.TICK_UP && trades[14].getNthStatus().equals("TICK_UP2"));
      assertTrue("tick or nStatus wrong for trade 15", trades[15].getTick() == Tick.TICK_UP && trades[15].getNthStatus().equals("TICK_UP1"));
      assertTrue("tick or nStatus wrong for trade 16", trades[16].getTick() == Tick.TICK_DOWN && trades[16].getNthStatus().equals("TICK_DOWN1"));
      assertTrue("tick or nStatus wrong for trade 17", trades[17].getTick() == Tick.TICK_UP && trades[17].getNthStatus().equals("TICK_UP2"));
      assertTrue("tick or nStatus wrong for trade 18", trades[18].getTick() == Tick.TICK_ZERO && trades[18].getNthStatus().equals("TICK_UP1"));
      assertTrue("tick or nStatus wrong for trade 19", trades[19].getTick() == Tick.TICK_UP && trades[19].getNthStatus().equals("TICK_UP1"));

    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  @Test
  public void testTrading() {
    doTrading();
    assertTrue("trade type is wrong for unmodified setting",
        TradeAnalyzer.getPretendTrades()[0].getMakerSide().equals("PRETEND buy"));

  }

  @Test
  public void testTradingWithModifiedUpMValue() {

    TradeAnalyzer.setUpM("3");

    doTrading();
    assertTrue("trade type is wrong for modified setting",
        TradeAnalyzer.getPretendTrades()[0].getMakerSide().equals("PRETEND sell"));

  }

  private void doTrading() {
    try {
      Trades recentTrades = objectMapper.readValue(
          IOUtils.toString(this.getClass().getResourceAsStream("/trade_test.json"), "UTF-8"), Trades.class);
      Trade[] trades = recentTrades.getPayload();

      TradeAnalyzer.makeTradeDecision(Arrays.asList(trades));
      Thread.sleep(3000);

      for (Trade trade : TradeAnalyzer.getPretendTrades()) {
        System.out.println(objectMapper.writeValueAsString(trade));
      }
      assertTrue("we have more or less pretend trades than we should", TradeAnalyzer.getPretendTrades().length == 1);
      assertTrue("tid is wrong for the pretend-trade", TradeAnalyzer.getPretendTrades()[0].getTid() == 45);

    } catch (Exception e) {
      e.printStackTrace();
      assertTrue(e.getMessage(),  false);
    }

  }
}
