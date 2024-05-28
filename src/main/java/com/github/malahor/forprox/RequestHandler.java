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
      log.info("Client: {}:{}", InetAddress.getLoopbackAddress(), clientSocket.getPort());
      communication.setupClient(clientSocket);
      var connection = initializeConnection(communication);
      validateHost(connection);
      establishConnection(connection, communication);
      connection.forwardRequest(communication);
      connection.forwardResponse(communication);
      log.info("Closing connection");
    } catch (IOException e) {
      log.error("Error occurred: {}", e.getMessage());
    }
  }

  private void validateHost(Connection connection) {
    var host = connection.host();
    log.info("Resolved host: {}", host);
    // TODO: check host in scope of banned/warned/whatever sites
  }

  private Connection initializeConnection(Communication communication) throws IOException {
    var request = readRequest(communication);
    log.info("Incoming request: {}", request);
    return Connection.initialize(request);
  }

  private String readRequest(Communication communication) throws IOException {
    return communication.clientReader().readLine();
  }

  private void establishConnection(Connection connection, Communication communication) throws IOException {
    var target = connection.establish();
    communication.setupTarget(target);
    if (connection instanceof HttpsConnection https) https.confirmConnection(communication);
    log.info("Connection established with: {}:{}", connection.address(), connection.port());
  }
}
