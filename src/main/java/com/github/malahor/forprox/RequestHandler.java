package com.github.malahor.forprox;

import com.github.malahor.forprox.request.Connection;
import com.github.malahor.forprox.request.HttpsConnection;
import java.io.*;
import java.net.Socket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class RequestHandler {

  private final byte[] buffer = new byte[4096];
  private final Socket clientSocket;

  public void run() {
    try (var communication = new CommunicationManager()) {
      communication.setupClient(clientSocket);
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
      log.info("Forwarding response");
      writeResponse(communication);
      log.info("Finished request handling");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private String readRequest(CommunicationManager communication) throws IOException {
    return communication.clientReader().readLine();
  }

  public void writeResponse(CommunicationManager communicationManager) throws IOException {
    var response = communicationManager.getTargetIn();
    var out = communicationManager.getClientOut();
    try {
      int bytesRead;
      while ((bytesRead = response.read(buffer)) != -1) {
        out.write(buffer, 0, bytesRead);
        out.flush();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
