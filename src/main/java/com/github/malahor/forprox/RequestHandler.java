package com.github.malahor.forprox;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class RequestHandler {

  private static final int HTTP_PORT = 80;
  private final byte[] buffer = new byte[4096];
  private Socket clientSocket;

  public void handleRequest() {
    try {
      var request = readRequest();
      log.info("Forwarding request: {}", request);
      var hostName = extractTargetHost(request);
      var address = InetAddress.getByName(hostName);
      var targetSocket = new Socket(address, HTTP_PORT);
      log.info("Connection opened to: {}:{}", address, HTTP_PORT);
      forwardRequest(targetSocket, request);
      writeResponse(targetSocket);
      targetSocket.close();
      clientSocket.close();
      log.info("Finished request handling");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private String readRequest() throws IOException {
    var clientIn = clientSocket.getInputStream();
    var bytesRead = clientIn.read(buffer);
    return new String(buffer, 0, bytesRead);
  }

  private String extractTargetHost(String request) {
    var lines = request.split("\\r\\n");
    var hostLine =
        Arrays.stream(lines).filter(line -> line.startsWith("Host")).findFirst().orElseThrow();
    return hostLine.substring(hostLine.indexOf(":") + 1).trim();
  }

  private void forwardRequest(Socket targetSocket, String request) throws IOException {
    var targetOut = targetSocket.getOutputStream();
    targetOut.write(request.getBytes());
    targetOut.flush();
  }

  private void writeResponse(Socket targetSocket) throws IOException {
    var targetIn = targetSocket.getInputStream();
    var clientOut = clientSocket.getOutputStream();
    int bytesRead;
    while ((bytesRead = targetIn.read(buffer)) != -1) {
      clientOut.write(buffer, 0, bytesRead);
      clientOut.flush();
    }
  }
}
