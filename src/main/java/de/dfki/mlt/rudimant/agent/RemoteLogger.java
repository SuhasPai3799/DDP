package de.dfki.mlt.rudimant.agent;

import java.io.IOException;

import de.dfki.mlt.rudimant.common.LogPrinter;
import de.dfki.mlt.rudimant.common.RuleInfo;
import de.dfki.mlt.rudimant.common.SimpleClient;

public class RemoteLogger implements LogPrinter {
  private SimpleClient client;

  /** A client that connects to the server on localhost at the given port to
   *  send log information to the debugger.
   */
  public RemoteLogger(String hostname, int port) {
    client = new SimpleClient(hostname, port);
  }

  @Override
  public void printLog(RuleInfo ruleId, boolean[] result) {
    try {
      String[] toSend = new String[result.length + 2];
      toSend[0] = "printLog";
      toSend[1] = Integer.toString(ruleId.getId());
      for (int i = 0; i < result.length; ++i) {
        toSend[i + 2] = Boolean.toString(result[i]);
      }
      client.send(toSend);
    } catch (IOException e) {
      Agent.logger.error(e.getMessage());
    }
  }

}
