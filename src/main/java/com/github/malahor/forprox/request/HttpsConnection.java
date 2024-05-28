package com.github.malahor.forprox.request;

import com.github.malahor.forprox.Communication;

public class HttpsConnection extends Connection {

  public HttpsConnection(String host) {
    super(host);
  }

  public static HttpsConnection initialize(String request) {
    var host = determineHost(request);
    return new HttpsConnection(host);
  }

  private static String determineHost(String request) {
    var lines = request.split("\\s");
    return lines[1].split(":")[0];
  }

  @Override
  public int port() {
    return HTTPS_PORT;
  }

  @Override
  public void forwardRequest(Communication communication) {
    new Thread(() -> forwardData(communication.getClientIn(), communication.getTargetOut()))
        .start();
  }
}
