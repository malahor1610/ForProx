package com.github.malahor.forprox;

import com.github.malahor.forprox.request.Connection;
import com.github.malahor.forprox.request.HttpsConnection;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class RequestHandler {

  private final Socket clientSocket;

  public void run() {
    try (var communication = new Communication()) {
      communication.setupClient(clientSocket);
      log.info("Client: {}:{}", InetAddress.getLoopbackAddress(), clientSocket.getPort());
      var request = readRequest(communication);
      log.info("Incoming request: {}", request);
      var connection = Connection.initialize(request);
      var host = connection.host();
      log.info("Resolved host: {}", host);
      // TODO: check host in scope of banned/warned/whatever sites
      var target = connection.establish();
      communication.setupTarget(target);
      if (connection instanceof HttpsConnection) {
        var out = communication.getClientOut();
        out.write("HTTP/1.1 200 Connection Established\r\n\r\n".getBytes());
        out.flush();
      }
      log.info("Connection established with: {}:{}", connection.address(), connection.port());
      connection.forwardRequest(communication);
      connection.forwardResponse(communication);
      log.info("Closing connection");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private String readRequest(Communication communication) throws IOException {
    return communication.clientReader().readLine();
  }
}
