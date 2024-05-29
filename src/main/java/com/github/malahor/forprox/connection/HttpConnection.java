package com.github.malahor.forprox.connection;

import com.github.malahor.forprox.Communication;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpConnection extends Connection {

  public HttpConnection(String host) {
    super(host);
  }

  public static HttpConnection initialize(String request) throws MalformedURLException {
    var host = determineHost(request);
    return new HttpConnection(host);
  }

  private static String determineHost(String request) throws MalformedURLException {
    var lines = request.split("\\s");
    return new URL(lines[1]).getHost();
  }

  @Override
  public int port() {
    return HTTP_PORT;
  }

  @Override
  public void forwardRequest(Communication communication) throws IOException {
    var clientReader = communication.clientReader();
    var serverWriter = communication.targetWriter();
    String line;
    while (!(line = clientReader.readLine()).isEmpty()) serverWriter.println(line);
    serverWriter.println();
    serverWriter.flush();
  }
}
