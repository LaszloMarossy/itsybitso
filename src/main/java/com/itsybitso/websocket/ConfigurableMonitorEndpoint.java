package com.itsybitso.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itsybitso.entity.WindowConfig;
import com.itsybitso.executor.AppMonitor;
import com.itsybitso.executor.AppMonitors;
import com.itsybitso.util.PropertiesUtil;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * websocket endpoint to serve the javafx ui with app monitor info for different configurations
 */
@ServerEndpoint("/configurablemonitor")
public class ConfigurableMonitorEndpoint {

  private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<Session>());
  private static boolean cont = true;
  private static int uniCounter = 0;
  private static final String interval = PropertiesUtil.getProperty("");
  private static ObjectMapper objectMapper = new ObjectMapper();

  private ExecutorService exe = Executors.newFixedThreadPool(5);
  private WindowConfig windowConfig;



  @OnOpen
  public void onOpen(Session session) {
    sessions.add(session);
  }

  @OnClose
  public void onClose(Session session) {
    sessions.remove(session);
  }


  @OnError
  public void onError(Session session, Throwable thr) {
    sessions.remove(session);
    thr.printStackTrace();
  }


//  public void onMessage(Session session, ByteBuffer byteBuffer) {
  @OnMessage
  public void onMessage( Session session, byte[] byteBuffer) {
    //    for (Session session : sessions) { // if trying to display on all sessions, not only on one that connected

    try {
//      WindowConfig windowConfig = (WindowConfig)new ObjectInputStream(new ByteArrayInputStream(byteBuffer.array())).readObject();
      windowConfig = objectMapper.readValue(byteBuffer, WindowConfig.class);
      Callable<String> call = () -> {
        try {
          while (cont) {
            byte[] message = (AppMonitors.getDisplayDataString(windowConfig.getId())).getBytes();
            session.getBasicRemote().sendBinary(ByteBuffer.wrap(message));
            Thread.sleep(1000);
          }
        } catch (Exception e) {
          Logger.getLogger(ConfigurableMonitorEndpoint.class.getName()).log(Level.SEVERE, null, e);
        }
        return "done?";
      };
      Future<String> f = exe.submit(call);

    } catch (Exception ex) {
      Logger.getLogger(ConfigurableMonitorEndpoint.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

}
