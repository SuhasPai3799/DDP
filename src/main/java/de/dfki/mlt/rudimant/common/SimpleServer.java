package de.dfki.mlt.rudimant.common;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.function.Consumer;

import org.slf4j.LoggerFactory;

/**
 * establishes an server in the agent to be able to modify which rules are
 * logged, and to see watched objects in the DB in the future
 */
public class SimpleServer extends SimpleConnector {

  static { logger = LoggerFactory.getLogger(SimpleServer.class); }

  private ServerSocket serverSocket;

  public SimpleServer(Consumer<String[]> c, int port, String name) throws IOException {
    super(port, c, name);
  }

  public boolean isAlive() {
    return readerThread == null || readerThread.isAlive();
  }

  /** starts the debugging service for the agent */
  public boolean startServer() {
    Thread t = new Thread() {
      public void run() {
        while (! closeRequested) {
          if (socket == null || ! socket.isConnected()) {
            init();
          }
          try {
            sleep(1000);
          } catch (InterruptedException e) {
            return;
          }
        }
      }
    };
    t.setDaemon(true);
    t.setName("StartServer");
    t.start();
    return true;
  }

  protected boolean init() {
    try {
      if (socket == null || ! socket.isConnected()) {
        close();
        serverSocket = new ServerSocket(_portNumber);
        socket = serverSocket.accept();
        in = new InputStreamReader(socket.getInputStream(), "UTF-8");
        out = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
        startReading();
        logger.info("Agent debug server started");
      }
    }
    catch (IOException ex) {
      return false;
    }
    return true;
  }

  public void close()  {
    if (serverSocket == null) return;
    try {
      serverSocket.close();
      super.close();
    } catch (IOException ex) {
      logger.error("Error closing socket: {}", ex);
    } finally {
      serverSocket = null;
      socket = null;
    }
  }

  public void stop() {
    closeRequested = true;
    close();
  }

  public void send(String ... s) {
    try {
      super.send(s);
    } catch (IOException ex) {
      close();
    }
  }

  public static void main(String[] args) throws IOException {
    final SimpleServer simplServ = new SimpleServer(
        (s) -> {System.out.println(Arrays.toString(s));},
        3664, "testServer"
        );

    Thread sideThread = new Thread() {
      public void run() {
        try {
          while (true && simplServ.isAlive()) {
            System.out.println("Rödeln...");
            Thread.sleep(500);
            simplServ.send("one", "two");
          }
        } catch (InterruptedException v) {
          System.out.println(v);
        }
      }
    };

    sideThread.start();
    simplServ.startServer();
  }
}
