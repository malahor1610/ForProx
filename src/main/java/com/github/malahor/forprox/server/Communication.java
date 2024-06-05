package com.github.malahor.forprox.server;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import lombok.Getter;

public class Communication implements AutoCloseable {

  private Socket clientSocket;
  @Getter private InputStream clientIn;
  @Getter private OutputStream clientOut;
  private Socket targetSocket;
  @Getter private InputStream targetIn;
  @Getter private OutputStream targetOut;
  private BufferedReader clientReader;

  public void setupClient(Socket socket) throws IOException {
    clientSocket = socket;
    clientSocket.setSoTimeout(10000);
    clientIn = clientSocket.getInputStream();
    clientOut = clientSocket.getOutputStream();
    clientReader = new BufferedReader(new InputStreamReader(clientIn));
    clientReader.mark(100);
  }

  public void setupTarget(Socket socket) throws IOException {
    targetSocket = socket;
    targetSocket.setSoTimeout(10000);
    targetIn = targetSocket.getInputStream();
    targetOut = targetSocket.getOutputStream();
  }

  public BufferedReader clientReader() throws IOException {
    clientReader.reset();
    clientReader.mark(100);
    return clientReader;
  }

  public PrintWriter targetWriter() {
    return new PrintWriter(targetOut);
  }

  public String client(){
    return InetAddress.getLoopbackAddress() + ":" + clientSocket.getPort();
  }

  @Override
  public void close() throws IOException {
    clientIn.close();
    clientOut.close();
    targetIn.close();
    targetOut.close();
    clientReader.close();
    targetSocket.close();
    clientSocket.close();
  }
}
