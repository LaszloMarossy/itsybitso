package com.itsybitso.executor;

import java.io.IOException;
import java.net.URI;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;

import org.glassfish.tyrus.client.ClientManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

/**
 * Bitso WebSocket client
 */
@ClientEndpoint
public class BitsoWsClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(BitsoWsClient.class);

  /**
   * connect to the Bitso service upon construction
   *
   * @param endpointURI
   */
  public BitsoWsClient(URI endpointURI) {
    try {
      WebSocketContainer container = ClientManager.createClient();
      Session session = container.connectToServer(this, endpointURI);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Callback hook for Connection open events.
   *
   * @param userSession the userSession which is opened.
   */
  @OnOpen
  public void onOpen(Session userSession, EndpointConfig clientConfig) throws Exception {
    LOGGER.info("opening websocket");
    // send the subscription message
    userSession.getAsyncRemote().sendText("{ \"action\": \"subscribe\", \"book\": \"btc_mxn\", \"type\": \"diff-orders\" }");
  }

  /**
   * Callback hook for Connection close events.
   *
   * @param userSession the userSession which is getting closed.
   * @param reason      the reason for connection close
   */
  @OnClose
  public void onClose(Session userSession, CloseReason reason) throws IOException {
    System.out.println("closing websocket");
    userSession.close();
  }

  /**
   * Callback for messages received from Bitso
   *
   * @param msg     message from Bitso
   * @param session user session of Websockets
   * @throws Exception
   */
  @OnMessage
  public void onReceipt(String msg, Session session) throws Exception {
    DiffOrderQueuer.addToQueue(msg);
  }

}