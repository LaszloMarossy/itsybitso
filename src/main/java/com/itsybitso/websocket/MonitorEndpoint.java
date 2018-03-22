package com.itsybitso.websocket;

import com.itsybitso.executor.AppMonitor;

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
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 * websocket endpoint to serve the javafx ui with app monitor info
 */
@ServerEndpoint("/monitor")
public class MonitorEndpoint {

  private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<Session>());
  private static boolean cont = true;
  private static int uniCounter = 0;
  ExecutorService exe = Executors.newFixedThreadPool(5);

  // async process to maintain static variable
//  static {
//    try {
//      Callable<String> call = () -> {
//        try {
//          while (cont) {
//            uniCounter++;
//            Thread.sleep(5000);
//          }
//        } catch (Exception e) {
//          Logger.getLogger(MonitorEndpoint.class.getName()).log(Level.SEVERE, null, e);
//        }
//        return "done?";
//      };
//      ExecutorService exe = Executors.newSingleThreadExecutor();
//      Future<String> f = exe.submit(call);
//
//    } catch (Exception ex) {
//      Logger.getLogger(MonitorEndpoint.class.getName()).log(Level.SEVERE, null, ex);
//    }
//
//  }


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


  @OnMessage
  public void onMessage(Session session, ByteBuffer byteBuffer) {
    //    for (Session session : sessions) { // if trying to display on all sessions, not only on one that connected

    try {
      Callable<String> call = () -> {
        try {
          while (cont) {
            byte[] message = (AppMonitor.getCurrentMonitor()).getBytes();
            session.getBasicRemote().sendBinary(ByteBuffer.wrap(message));
            Thread.sleep(1000);
          }
        } catch (Exception e) {
          Logger.getLogger(MonitorEndpoint.class.getName()).log(Level.SEVERE, null, e);
        }
        return "done?";
      };
      Future<String> f = exe.submit(call);

    } catch (Exception ex) {
      Logger.getLogger(MonitorEndpoint.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

}
