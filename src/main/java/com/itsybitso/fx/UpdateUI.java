package com.itsybitso.fx;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itsybitso.entity.Monitor;
import com.itsybitso.entity.Order;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javafx.scene.layout.AnchorPane;

import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;


@ClientEndpoint
public class UpdateUI extends Application {

  private static final Logger LOGGER = Logger.getLogger(UpdateUI.class.getName());
  private Session session;
  boolean doRun = true;
  int iterationCount = 0;
  Label label = new Label("I am the label here");
  private TableView<Monitor> monitorTableView = new TableView<Monitor>();
  private TableView<Order> topAsks = new TableView<Order>();
  private TableView<Order> topBids = new TableView<Order>();
  ObjectMapper objectMapper = new ObjectMapper();

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) {

    connectToWebSocket();

    Button btn = new Button("Start Monitoring!");
    btn.setPrefSize(100, 27);
    btn.setOnAction(event -> triggerListening(primaryStage));

    AnchorPane pane = new AnchorPane();
    AnchorPane.setTopAnchor(btn, 0.0);
    AnchorPane.setLeftAnchor(btn, 0.0);
    AnchorPane.setRightAnchor(btn, 0.0);

    pane.getChildren().add(btn);

    final Label label = new Label("App Status");
    label.setFont(new Font("Arial", 20));

    monitorTableView.setEditable(true);
    monitorTableView.setFixedCellSize(25);
    monitorTableView.prefHeightProperty().bind(Bindings.size(monitorTableView.getItems()).multiply(monitorTableView.getFixedCellSize()).add(50));

    TableColumn queueSizeCol = new TableColumn("Diff-order queue size");
    TableColumn askCol = new TableColumn("# of Asks");
    TableColumn bidCol = new TableColumn("# of Bids");
    TableColumn threadCountCol = new TableColumn("# of Consuming Threads");

    queueSizeCol.setCellValueFactory(
        new PropertyValueFactory<Monitor, String>("diffOrderQueueSize"));
    askCol.setCellValueFactory(
        new PropertyValueFactory<Monitor, String>("orderBookAskSize"));
    bidCol.setCellValueFactory(
        new PropertyValueFactory<Monitor, String>("orderBookBidSize"));
    threadCountCol.setCellValueFactory(
        new PropertyValueFactory<Monitor, String>("numberOfConsumingThreads"));

    monitorTableView.getColumns().addAll(queueSizeCol, askCol, bidCol, threadCountCol);

    topAsks.setEditable(true);
    TableColumn oidCol = new TableColumn("OID");
    TableColumn priceCol = new TableColumn("Ask price");
    TableColumn amtCol = new TableColumn("amount");

    oidCol.setCellValueFactory(
        new PropertyValueFactory<Order, String>("oid"));
    priceCol.setCellValueFactory(
        new PropertyValueFactory<Order, String>("price"));
    amtCol.setCellValueFactory(
        new PropertyValueFactory<Order, String>("amount"));

    topAsks.getColumns().addAll(oidCol, priceCol, amtCol);

    topBids.setEditable(true);
    TableColumn oidBCol = new TableColumn("OID");
    TableColumn priceBCol = new TableColumn("Bid price");
    TableColumn amtBCol = new TableColumn("amount");

    oidBCol.setCellValueFactory(
        new PropertyValueFactory<Order, String>("oid"));
    priceBCol.setCellValueFactory(
        new PropertyValueFactory<Order, String>("price"));
    amtBCol.setCellValueFactory(
        new PropertyValueFactory<Order, String>("amount"));

    topBids.getColumns().addAll(oidBCol, priceBCol, amtBCol);

    HBox tops = new HBox(10, topAsks, topBids);


    VBox root = new VBox(10, new TextField(), pane, label, monitorTableView, tops);
    root.setPadding(new Insets(10));


    primaryStage.setTitle("Let's See this Baby!");
    primaryStage.setScene(new Scene(root, 800, 600));
    primaryStage.show();
  }


  @OnOpen
  public void onOpen(Session session) {
    this.session = session;
  }


  @OnMessage
  public void onMessage(InputStream input) {
    String incoming = read(input);
    System.out.println("WebSocket message Received: " + incoming);
    Platform.runLater(
      () -> {
        try {
          Monitor monitor = objectMapper.readValue(incoming, Monitor.class);
          final ObservableList<Monitor> data = FXCollections.observableArrayList(monitor);
          final ObservableList<Order> listOfAsks = FXCollections.observableList(monitor.getTopAsks());
          final ObservableList<Order> listOfBids = FXCollections.observableList(monitor.getTopBids());

          monitorTableView.setItems(data);
          topAsks.setItems(listOfAsks);
          topBids.setItems(listOfBids);
          label.setText(incoming);
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

  private void triggerListening(Stage stage) {
    try (OutputStream output = session.getBasicRemote().getSendStream()) {
        output.write(("Hello Dear Friends").getBytes());

    } catch (IOException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
    }
  }

  private void connectToWebSocket() {
    WebSocketContainer container = ContainerProvider.getWebSocketContainer();
    try {
      URI uri = URI.create("ws://localhost:8080/itsybitso/monitor");
      Session ssn = container.connectToServer(this, uri);
      this.session = ssn;
    } catch (DeploymentException | IOException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
      System.exit(-1);
    }
  }

  private static String read(InputStream input) {
    String retValue = null;
    try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
          retValue = buffer.lines().collect(Collectors.joining("\n"));
    } catch (IOException e) {
      LOGGER.log(Level.INFO, e.getMessage());
    }
    return retValue;
  }

}