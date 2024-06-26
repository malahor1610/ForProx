package com.github.malahor.forprox.connection;

import com.github.malahor.forprox.server.Communication;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public abstract class Connection {

  protected static final int HTTP_PORT = 80;
  protected static final int HTTPS_PORT = 443;
  protected String host;

  public static Connection initialize(String request) throws IOException {
    return request.contains("CONNECT")
        ? HttpsConnection.initialize(request)
        : HttpConnection.initialize(request);
  }

  public abstract int port();

  public abstract void forwardRequest(Communication communication);

  public void forwardResponse(Communication communication) {
    forwardData(communication.getTargetIn(), communication.getClientOut());
  }

  protected void forwardData(InputStream in, OutputStream out) {
    try {
      byte[] buffer = new byte[8192];
      int bytesRead;
      while ((bytesRead = in.read(buffer)) != -1) {
        out.write(buffer, 0, bytesRead);
        out.flush();
      }
    } catch (IOException e) {
      log.error("Error occurred: {}", e.getMessage());
    }
  }

  public String host() {
    return host;
  }

  public InetAddress address() throws UnknownHostException {
    return InetAddress.getByName(host());
  }

  public Socket establish() throws IOException {
    return new Socket(address(), port());
  }
}
