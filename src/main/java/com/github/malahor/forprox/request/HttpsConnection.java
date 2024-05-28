package com.github.malahor.forprox.request;

import com.github.malahor.forprox.CommunicationManager;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class HttpsConnection extends Connection {
  private static final int HTTP_PORT = 443;

  public static HttpsConnection initialize(String request) {
    var lines = request.split("\\s");
    var host = lines[1].split(":")[0];
    return new HttpsConnection(host);
  }

  public HttpsConnection(String host) {
    super(host);
  }

  @Override
  public int port() {
    return HTTP_PORT;
  }

  @Override
  public void forwardRequest(CommunicationManager communicationManager) {
    new Thread(() -> forward(communicationManager)).start();
  }

  private void forward(CommunicationManager communicationManager) {
    var out = communicationManager.getTargetOut();
    var in = communicationManager.getClientIn();
    try {
      byte[] buffer = new byte[8192];
      int bytesRead;
      log.info("Forwarding request");
      while ((bytesRead = in.read(buffer)) != -1) {
        out.write(buffer, 0, bytesRead);
        out.flush();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
