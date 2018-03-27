# itsybitso
Bitso trading wrapper implementation with configurable algorithm for pretend-trading.

## setup
- build the application through maven using JDK 1.8
- deploy WAR artifact onto a Tomcat 9.0.6 app server (`[CATALINA_HOME]/webapps/` directory)
- start server
- monitor log file at `[CATALINA_HOME]/logs/itsybitso/main.log`

## application startup
After a successful deployment and webserver startup execute the following steps:
- run the class `com.itsybitso.fx.ItsybitsoWindow` to start the JavaFX UI 
- push the _**Startup**_ button to trigger server processes startup.  
- wait till the label below the button changes to _**"App Status: all services running, start monitoring!..."**_. During 
this step the following processes are triggered to start on the server in sequence (the int values represent the number 
of seconds delay before triggering the process):

    ```      
         startService(1, httpClient, serverUrl, "/itsybitso/service/orderbook");
         startService(1, httpClient, serverUrl, "/itsybitso/service/startmonitor");
         startService(4, httpClient, serverUrl, "/itsybitso/service/gettrades");
         startService(2, httpClient, serverUrl, "/itsybitso/service/populatequeue");
         startService(5, httpClient, serverUrl, "/itsybitso/service/consumequeue");
     ```
    The above server processes can also be triggered separately and/or selectively through the corresponding REST GET 
    calls mentioned above. Summary on the above processes:
     - `orderbook` - get a copy of the orderbook through Bitso REST GET and save it internally
     - `startmonitor` - start the application monitoring loop that gathers the data for the UI
     - `gettrades` - gets the most recent X trades from Bitso REST GET endpoint; enhances them for the internal business
     logic and makes the decision whether to execute a pretend-trade
     - `populatequeue` - connect to the Bitso diff-orders WS feed and save messages to an in-memory queue (NOTE: later 
     this could be replaced by an external message queue implementation or a no-sql type document DB like Redis); for the
     currently observed Bitso traffic the existing solution should be sufficient.
     - `consumequeue` - start consuming the diff-order messages from the internal queue and update the internal 
     in-memory orderbook (asks/bids)
     
- push the _**Start Monitoring**_ button to view the content of the application.

If the "_Diff-order queue size_" value of the first table keeps increasing, you can execute the following REST GET call 
to increase the number of consuming threads of the `consumequeue` process (default set to 5 that should be enough):
- `http://localhost:8080/itsybitso/service/consumewith/8` 

## pretend-trading
Pretend-trading (by adding cutom-made trades to the list of recent trades received from the Bitso REST endpoint) is
automatically called after receiving each batch of trades from Bitso, in configurable intervals of seconds (currently 1).

A pretend-trade is recognized by its _**marker side**_ value (in the last table displayed): _**"PRETEND sell"**_ or 
_**"PRETEND buy"**_. 

Once a trade-decision is made and is executed, the custom trade object is added to an internal list in memory of 
pretend-trades, that are merged into the real trades received from Bitso for the UI displa.  Currently the list of 
pretend-trades are NOT removed from the internal list, so they are 'piling up' on the bottom of the _Top X Trades_ table
, in addition to X.  This behavior can of course easily change, but I thought it useful to have a list of trades 
accessible somewhere while there is no application DB.  

For other assumptions made on the trading logic, see 
the Javadoc documentation of `com.itsybitso.bl.TradeAnalyzer.makeTradeDecision` method:

```
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
   * @throws Exception
   */
  public static void makeTradeDecision(List<Trade> recentTrades) throws Exception {

```

##Testing
Critical business logic is tested in `com.itsybitso.TradeAnalyzerTest`.  For now the correct behavior of asynchronous, 
concurrent multi-threaded execution was done by monitoring the logs, where the name and number of the executing thread 
and thread pool is displayed with each log line.

More automated integration and unit testing to come!

##Documentation
Besides this README file, also see Javadoc and inline comments.

##Architecture
The main entry point to all functionality is in `com.itsybitso.controller.AppController` which is a set of REST endpoints
that are also used by the JavaFX GUI client to start up all critical processes necessary.  Most calls are performed 
asynchronously so they do not block execution.  

Critical parts synchronous execution could cause workload pile-ups are
executed with multithreaded executors of `java.util.concurrent.ExecutorService.java`.  Populating the internal queue with
the Bitso Websocket messages, and consuming those messages to update the order book are executed this way.  Areas where 
this approach is not appropriate (for example periodic polling of Bitso trade data) are executed in a single asynchronous 
thread.

For the internal Orderbook implementation (`com.itsybitso.entity.InternalOrderBook`) in-memory `ConcurrentHashMap` is 
used.  In a production environment this may be replaced by a document No-SQL database, but this may not be necessary since after
startup this application can get itself updated using these solutions.  

For queuing the diff-order Websocket messages, I am using `java.util.concurrent.LinkedBlockingDeque` that allows concurrent
access for both population and consumption.  This could be replaced by a message queue such as kafka or ActiveMQ, but
again, this may not be necessary for the above-mentioned reason.

##Checklist
| Feature | File name | Method name |
| ------- | --------- | ----------- |
| Schedule the polling of trades over REST. | com.itsybitso.executor.TradesPoller | com.itsybitso.executor.TradesPoller.startAsyncRefreshRecentTrades |
| Request a book snapshot over REST. | com.itsybitso.executor.BitsoRestClient | com.itsybitso.executor.BitsoRestClient.getBitsoOrderBook |
| Listen for diff-orders over websocket. | com.itsybitso.executor.BitsoWsClient | com.itsybitso.executor.BitsoWsClient.onReceipt |
| Replay diff-orders. | com.itsybitso.executor.DiffOrderConsumer | com.itsybitso.executor.DiffOrderConsumer.startAsyncConsumeQueue |
| Use config option X to request  recent trades. | com.itsybitso.executor.BitsoRestClient | com.itsybitso.executor.BitsoRestClient.getBitsoTrades |
| Use config option X to limit number of ASKs displayed in UI. | com.itsybitso.executor.AppMonitor | com.itsybitso.executor.AppMonitor.calculateTopOrders |
| The loop that causes the trading algorithm to reevaluate. | com.itsybitso.executor.TradesPoller  | com.itsybitso.executor.TradesPoller.startAsyncRefreshRecentTrades and com.itsybitso.bl.TradeAnalyzer.makeTradeDecision |































