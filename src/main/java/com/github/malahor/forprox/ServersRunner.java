package com.github.malahor.forprox;

import com.github.malahor.forprox.server.SSLServer;
import com.github.malahor.forprox.server.Server;

public class ServersRunner {

  public static void main(String[] args) {
    var server = new Server();
    var sslServer = new SSLServer();
    new Thread(server::start).start();
    new Thread(sslServer::start).start();
  }
}
