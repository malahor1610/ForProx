package com.github.malahor.forprox;

import java.io.IOException;
import java.net.ServerSocket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Server {
  private static final int PORT = 8989;

  public void start() {
    try (var serverSocket = new ServerSocket(PORT)) {
      log.info("Proxy server started at port: {}", PORT);
      while (true) {
        var clientSocket = serverSocket.accept();
        new ProxyThread(clientSocket).start();
      }
    } catch (IOException e) {
      log.error("Error occurred: {}", e.getMessage());
      e.printStackTrace();
    }
  }
}
