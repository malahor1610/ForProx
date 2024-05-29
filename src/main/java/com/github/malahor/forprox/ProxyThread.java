package com.github.malahor.forprox;

import com.github.malahor.forprox.connection.Connection;
import com.github.malahor.forprox.connection.HttpsConnection;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

import com.github.malahor.forprox.validation.ForbiddenException;
import com.github.malahor.forprox.validation.HostValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ProxyThread extends Thread {

  private final Socket clientSocket;

  @Override
  public void run() {
    try (var communication = new Communication()) {
      log.info("Client: {}:{}", InetAddress.getLoopbackAddress(), clientSocket.getPort());
      communication.setupClient(clientSocket);
      var connection = initializeConnection(communication);
      HostValidator.validateHost(communication, connection);
      establishConnection(connection, communication);
      connection.forwardRequest(communication);
      connection.forwardResponse(communication);
      log.info(
          "Closing connection of {}:{}", InetAddress.getLoopbackAddress(), clientSocket.getPort());
    } catch (IOException e) {
      log.error("Error occurred: {}", e.getMessage());
    } catch (ForbiddenException e) {
      log.info("Host is forbidden {}", e.getMessage());
    }
  }

  private Connection initializeConnection(Communication communication) throws IOException {
    var request = readRequest(communication);
    log.info("Incoming request: {}", request);
    return Connection.initialize(request);
  }

  private String readRequest(Communication communication) throws IOException {
    return communication.clientReader().readLine();
  }

  private void establishConnection(Connection connection, Communication communication)
      throws IOException {
    var target = connection.establish();
    communication.setupTarget(target);
    if (connection instanceof HttpsConnection https) https.confirmConnection(communication);
    log.info("Connection established with: {}:{}", connection.address(), connection.port());
  }
}
