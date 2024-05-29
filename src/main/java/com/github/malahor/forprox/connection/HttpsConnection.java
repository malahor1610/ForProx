package com.github.malahor.forprox.connection;

import com.github.malahor.forprox.Communication;

import java.io.IOException;

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
    forwardData(communication.getClientIn(), communication.getTargetOut());
  }

  public void confirmConnection(Communication communication) throws IOException {
    var out = communication.getClientOut();
    out.write("HTTP/1.1 200 Connection Established\r\n\r\n".getBytes());
    out.flush();
  }

}
