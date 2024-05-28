package com.github.malahor.forprox.request;

import com.github.malahor.forprox.CommunicationManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.UnknownHostException;

public abstract class Connection {

  protected String host;

  public Connection(String host) {
    this.host = host;
  }

  public static Connection initialize(String request) throws IOException {
    return request.contains("CONNECT") ? HttpsConnection.initialize(request) : HttpConnection.initialize(request);
  }

  public String host() {
    return host;
  }

  public abstract int port();

  public abstract void forwardRequest(CommunicationManager communicationManager) throws IOException;

  public InetAddress address() throws UnknownHostException, MalformedURLException {
    return InetAddress.getByName(host());
  }

  public Socket establish() throws IOException {
    return new Socket(address(), port());
  }

}
