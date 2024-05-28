package com.github.malahor.forprox.request;

import com.github.malahor.forprox.CommunicationManager;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

@Slf4j
public class HttpConnection extends Connection {
  private static final int HTTP_PORT = 80;

  public static HttpConnection initialize(String request) throws MalformedURLException {
    var lines = request.split("\\s");
    var host = new URL(lines[1]).getHost();
    return new HttpConnection(host);
  }
  public HttpConnection(String host) {
    super(host);
  }

  public int port() {
    return HTTP_PORT;
  }

  @Override
  public void forwardRequest(CommunicationManager communicationManager) throws IOException {
    var clientReader = communicationManager.clientReader();
    var serverWriter = new PrintWriter(communicationManager.getTargetOut());
    String headerLine;
    log.info("Forwarding request");
    while (!(headerLine = clientReader.readLine()).isEmpty()) {
      log.info(headerLine);
      serverWriter.println(headerLine);
    }
    serverWriter.println();
    serverWriter.flush();
  }
}
