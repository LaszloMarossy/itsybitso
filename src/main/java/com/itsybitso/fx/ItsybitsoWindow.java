package com.itsybitso.fx;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itsybitso.entity.DisplayData;
import com.itsybitso.entity.Order;
import com.itsybitso.entity.Trade;
import com.itsybitso.entity.WindowConfig;
import com.itsybitso.util.PropertiesUtil;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.util.stream.Collectors;

import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;


@ClientEndpoint
public class ItsybitsoWindow extends Application {

  private Session session;
  private TableView<DisplayData> monitorTable = new TableView<>();
  private TableView<DisplayData> performanceTable = new TableView<>();
  private TableView<Order> topAsksTable = new TableView<>();
  private TableView<Order> topBidsTable = new TableView<>();
  private TableView<Trade> recentTradesTable = new TableView<>();
  private ObjectMapper objectMapper = new ObjectMapper();

  private TextField ups = new TextField ();
  private TextField downs = new TextField ();
  private WindowConfig windowConfig = null;


  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) {

    connectToWebSocket();

    ups.setText(PropertiesUtil.getProperty("trade.up_m"));
    downs.setText(PropertiesUtil.getProperty("trade.down_n"));

    HBox configBox = new HBox();
    configBox.getChildren().addAll(ups, downs);
    configBox.setSpacing(10);


    Label appStatusLabel = new Label("App Status");
    appStatusLabel.setFont(new Font("Arial", 40));

    Button startServiceButton = new Button("Startup");
    startServiceButton.setPrefSize(500, 37);
    startServiceButton.setFont(new Font("Arial", 25));
    startServiceButton.setOnAction(event -> startBackEnd(appStatusLabel));

    Button monitorButton = new Button("Start Monitoring!");
    monitorButton.setPrefSize(500, 37);
    monitorButton.setFont(new Font("Arial", 25));
    monitorButton.setOnAction(event -> triggerListening(primaryStage));


    final Label note = new Label("main thing to watch here is that the queue size does not continually grow; if it does, "
        + "increase the number of consuming threads");
    note.setFont(new Font("Arial", 16));

    monitorTable.setEditable(true);
    monitorTable.setFixedCellSize(55);
    monitorTable.prefHeightProperty().bind(Bindings.size(monitorTable.getItems()).multiply(monitorTable.getFixedCellSize()).add(200));

    performanceTable.setEditable(true);
    performanceTable.setFixedCellSize(55);
    performanceTable.prefHeightProperty().bind(Bindings.size(performanceTable.getItems()).multiply(performanceTable.getFixedCellSize()).add(200));

    TableColumn queueSizeCol = new TableColumn("Diff-order queue size");
    TableColumn askCol = new TableColumn("# of Asks");
    TableColumn bidCol = new TableColumn("# of Bids");
    TableColumn threadCountCol = new TableColumn("# of Consuming Threads");

    TableColumn currencyBalance = new TableColumn("CURR BAL");
    TableColumn coinBalance = new TableColumn("COIN BAL");
    TableColumn latestPrice = new TableColumn("LATEST PRICE");
    TableColumn startValue = new TableColumn("START VAL");
    TableColumn accountValue = new TableColumn("ACCT VAL");
    TableColumn profit = new TableColumn("PROFIT");

    queueSizeCol.setCellValueFactory(new PropertyValueFactory<DisplayData, String>("diffOrderQueueSize"));
    askCol.setCellValueFactory(new PropertyValueFactory<DisplayData, String>("orderBookAskSize"));
    bidCol.setCellValueFactory(new PropertyValueFactory<DisplayData, String>("orderBookBidSize"));
    threadCountCol.setCellValueFactory(new PropertyValueFactory<DisplayData, String>("numberOfConsumingThreads"));

    currencyBalance.setCellValueFactory(new PropertyValueFactory<DisplayData, String>("currencyBalance"));
    coinBalance.setCellValueFactory(new PropertyValueFactory<DisplayData, String>("coinBalance"));
    latestPrice.setCellValueFactory(new PropertyValueFactory<DisplayData, String>("latestPrice"));
    startValue.setCellValueFactory(new PropertyValueFactory<DisplayData, String>("startingAccountValue"));
    accountValue.setCellValueFactory(new PropertyValueFactory<DisplayData, String>("accountValue"));
    profit.setCellValueFactory(new PropertyValueFactory<DisplayData, String>("profit"));

    queueSizeCol.prefWidthProperty().bind(monitorTable.widthProperty().divide(4)); // w * 1/4
    askCol.prefWidthProperty().bind(monitorTable.widthProperty().divide(4)); // w * 1/2
    bidCol.prefWidthProperty().bind(monitorTable.widthProperty().divide(4));
    threadCountCol.prefWidthProperty().bind(monitorTable.widthProperty().divide(4));

    currencyBalance.prefWidthProperty().bind(performanceTable.widthProperty().divide(6));
    coinBalance.prefWidthProperty().bind(performanceTable.widthProperty().divide(6));
    latestPrice.prefWidthProperty().bind(performanceTable.widthProperty().divide(6));
    startValue.prefWidthProperty().bind(performanceTable.widthProperty().divide(8));
    accountValue.prefWidthProperty().bind(performanceTable.widthProperty().divide(6));
    profit.prefWidthProperty().bind(performanceTable.widthProperty().divide(6));

    monitorTable.getColumns().addAll(
        queueSizeCol, askCol, bidCol, threadCountCol);
    performanceTable.getColumns().addAll(
        currencyBalance, coinBalance, latestPrice, startValue, accountValue, profit);

    topAsksTable.setEditable(true);
    topAsksTable.setPrefSize(500, 300);
    TableColumn oidCol = new TableColumn("OID");
    TableColumn priceCol = new TableColumn("Ask price");
    TableColumn amtCol = new TableColumn("amount");

    oidCol.setCellValueFactory(new PropertyValueFactory<Order, String>("oid"));
    priceCol.setCellValueFactory(new PropertyValueFactory<Order, String>("price"));
    amtCol.setCellValueFactory(new PropertyValueFactory<Order, String>("amount"));

    oidCol.prefWidthProperty().bind(topAsksTable.widthProperty().divide(2.5)); // w * 1/4
    priceCol.prefWidthProperty().bind(topAsksTable.widthProperty().divide(4)); // w * 1/2
    amtCol.prefWidthProperty().bind(topAsksTable.widthProperty().divide(4));

    topAsksTable.getColumns().addAll(oidCol, priceCol, amtCol);

    topBidsTable.setEditable(true);
    topBidsTable.setPrefSize(500, 300);

    TableColumn oidBCol = new TableColumn("OID");
    TableColumn priceBCol = new TableColumn("Bid price");
    TableColumn amtBCol = new TableColumn("amount");

    oidBCol.setCellValueFactory(new PropertyValueFactory<Order, String>("oid"));
    priceBCol.setCellValueFactory(new PropertyValueFactory<Order, String>("price"));
    amtBCol.setCellValueFactory(new PropertyValueFactory<Order, String>("amount"));

    oidBCol.prefWidthProperty().bind(topBidsTable.widthProperty().divide(2.5)); // w * 1/4
    priceBCol.prefWidthProperty().bind(topBidsTable.widthProperty().divide(4)); // w * 1/2
    amtBCol.prefWidthProperty().bind(topBidsTable.widthProperty().divide(4));

    topBidsTable.getColumns().addAll(oidBCol, priceBCol, amtBCol);

    String numTrades = PropertiesUtil.getProperty("displaydata.numberoftrades");
    String topX = PropertiesUtil.getProperty("displaydata.topx");

    final Label topOrdersLabel = new Label("Top " + topX + " orders");
    topOrdersLabel.setFont(new Font("Arial", 40));


    HBox tops = new HBox(10, topAsksTable, topBidsTable);
    tops.setMinWidth(1000);

    recentTradesTable.setEditable(true);
    TableColumn createdAtCol = new TableColumn("created at");
    TableColumn amountCol = new TableColumn("amount");
    TableColumn tradePriceCol = new TableColumn("price");
    TableColumn tickCol = new TableColumn("tick");
    TableColumn tidCol = new TableColumn("trade id");
    TableColumn makerSideCol = new TableColumn("maker side");
    TableColumn nStatusCol = new TableColumn("n-th Status");

    createdAtCol.setCellValueFactory(new PropertyValueFactory<Trade, String>("createdAt"));
    makerSideCol.setCellValueFactory(new PropertyValueFactory<Trade, String>("makerSide"));
    amountCol.setCellValueFactory(new PropertyValueFactory<Trade, String>("amount"));
    tradePriceCol.setCellValueFactory(new PropertyValueFactory<Trade, String>("price"));
    tidCol.setCellValueFactory(new PropertyValueFactory<Trade, String>("tid"));
    tickCol.setCellValueFactory(new PropertyValueFactory<Trade, String>("tick"));
    nStatusCol.setCellValueFactory(new PropertyValueFactory<Trade, String>("nthStatus"));

    createdAtCol.prefWidthProperty().bind(recentTradesTable.widthProperty().divide(7));
    amountCol.prefWidthProperty().bind(recentTradesTable.widthProperty().divide(8));
    tradePriceCol.prefWidthProperty().bind(recentTradesTable.widthProperty().divide(8));
    tidCol.prefWidthProperty().bind(recentTradesTable.widthProperty().divide(8));
    makerSideCol.prefWidthProperty().bind(recentTradesTable.widthProperty().divide(7));
    tickCol.prefWidthProperty().bind(recentTradesTable.widthProperty().divide(7));
    nStatusCol.prefWidthProperty().bind(recentTradesTable.widthProperty().divide(7));

    recentTradesTable.getColumns().addAll(
        tidCol, tickCol, amountCol, tradePriceCol, createdAtCol, makerSideCol, nStatusCol);

    final Label label3 = new Label("Top " + numTrades + " trades");
    label3.setFont(new Font("Arial", 40));


    VBox root = new VBox(10, configBox, startServiceButton, appStatusLabel, monitorButton,
        note, performanceTable, recentTradesTable, monitorTable, topOrdersLabel, tops, label3);
    root.setPadding(new Insets(10));


    primaryStage.setTitle("---ITSY-BITSO---");
    Scene primaryScene = new Scene(root, 1500, 1200);
    primaryScene.getStylesheets().add(getClass().getResource("/itsybitso.css").toExternalForm());

//    primaryScene.setUserAgentStylesheet("/itsybitso.css");
    primaryStage.setScene(primaryScene);
    primaryStage.show();
  }


  @OnOpen
  public void onOpen(Session session) {
    this.session = session;
  }


  @OnMessage
  public void onMessage(InputStream input) {
    String incoming = read(input);
    Platform.runLater(
        () -> {
          try {
            DisplayData displayData = objectMapper.readValue(incoming, DisplayData.class);
            System.out.println("WebSocket message Received: " + displayData.getAccountValue());
            final ObservableList<DisplayData> data = FXCollections.observableArrayList(displayData);
            final ObservableList<Order> listOfAsks = FXCollections.observableList(displayData.getTopAsks());
            final ObservableList<Order> listOfBids = FXCollections.observableList(displayData.getTopBids());
            final ObservableList<Trade> recentTrades = FXCollections.observableList(displayData.getRecentTrades());

            monitorTable.setItems(data);
            performanceTable.setItems(data);
            topAsksTable.setItems(listOfAsks);
            topBidsTable.setItems(listOfBids);
            recentTradesTable.setItems(recentTrades);
            Thread.sleep(100);
          } catch (InterruptedException | IOException e) {
            e.printStackTrace();
          }
        }
    );
  }

  @OnClose
  public void onClose() {
//    connectToWebSocket();
  }

  private void startBackEnd(Label label) {
    try {
      String serverUrl = PropertiesUtil.getProperty("server.rest.url");
      String deployment = PropertiesUtil.getProperty("server.deployment");
      CloseableHttpClient httpClient = HttpClientBuilder.create().build();
      windowConfig = new WindowConfig(ups.getText(), downs.getText());

//      startService(1, httpClient, serverUrl, deployment.concat("/service/orderbook"));
//      startService(1, httpClient, serverUrl, deployment.concat("/service/startmonitor"));
      startService(4, httpClient, serverUrl, deployment.concat("/service/trade/" +
          windowConfig.getId() + "/" + ups.getText() + "/" + downs.getText()));
//      startService(2, httpClient, serverUrl, deployment.concat("/service/populatequeue"));
//      startService(5, httpClient, serverUrl, deployment.concat("/service/consumequeue"));

      httpClient.close();
      label.setText("App Status: configuration " + windowConfig.getId()
          + " running; ups " + ups.getText() + " downs " + downs.getText());

    } catch (Exception ex) {
      System.out.println(ex.getMessage());
    }
  }

  private void startService(int delay, CloseableHttpClient httpClient, String serverUrl, String url) throws Exception {
    Thread.sleep(delay * 1000);
    HttpGet httpGetRequest = new HttpGet(serverUrl + url);
    CloseableHttpResponse httpResponse = httpClient.execute(httpGetRequest);
    httpResponse.close();
    System.out.println("++++++++++++++++ " + url + " " +httpResponse.getStatusLine());
  }


  private void triggerListening(Stage stage) {
    try (OutputStream output = session.getBasicRemote().getSendStream()) {

      output.write((objectMapper.writeValueAsString(windowConfig)).getBytes());

    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  private void connectToWebSocket() {
    WebSocketContainer container = ContainerProvider.getWebSocketContainer();
    String wsUrl = PropertiesUtil.getProperty("server.ws.url");
    System.out.println(">>" + wsUrl);
    String deployment = PropertiesUtil.getProperty("server.deployment");
    try {
//      URI uri = URI.create(wsUrl.concat(deployment).concat("/monitor"));
      URI uri = URI.create(wsUrl.concat(deployment).concat("/configurablemonitor"));
//      System.out.println(uri.toString());
      this.session = container.connectToServer(this, uri);
    } catch (DeploymentException | IOException ex) {
      ex.printStackTrace();
      System.exit(-1);
    }
  }

  private static String read(InputStream input) {
    String retValue = null;
    try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
      retValue = buffer.lines().collect(Collectors.joining("\n"));
    } catch (IOException e) {
      e.printStackTrace();
    }
    return retValue;
  }

}