package com.github.malahor.forprox;

import static org.junit.jupiter.api.Assertions.*;

import com.github.malahor.forprox.server.Server;
import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.routing.DefaultProxyRoutePlanner;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ServerTests {

  private static final int proxyPort = 8989;
  private static final Server server = new Server();
  private static Thread proxyThread;
  private static HttpClient httpClient;

  @BeforeAll
  public static void setUp() throws Exception {
    proxyThread = new Thread(server::start);
    proxyThread.start();
    // Wait for proxy to start up
    Thread.sleep(5000);
    initializeHttpClient();
  }

  public static void initializeHttpClient() {
    var proxyRoutePlanner = new DefaultProxyRoutePlanner(new HttpHost("localhost", proxyPort));
    httpClient = HttpClients.custom().setRoutePlanner(proxyRoutePlanner).build();
  }

  @AfterAll
  public static void tearDown() {
    proxyThread.interrupt();
  }

  @Test
  public void testHttpsRequestThroughProxy() throws Exception {
    var httpGet = new HttpGet("https://httpbin.org/ip");
    try (var response = httpClient.execute(httpGet, httpResponse -> httpResponse)) {
      assertEquals(HttpStatus.SC_OK, response.getCode());
    }
  }

  @Test
  public void testHttpRequestThroughProxy() throws Exception {
    var httpGet = new HttpGet("http://httpbin.org/ip");
    try (var response = httpClient.execute(httpGet, httpResponse -> httpResponse)) {
      assertEquals(HttpStatus.SC_OK, response.getCode());
    }
  }

  @Test
  public void testBannedRequestThroughProxy() {
    Exception exception =
        assertThrows(
            ClientProtocolException.class,
            () -> {
              var httpGet = new HttpGet("https://www.facebook.com");
              httpClient.execute(httpGet, httpResponse -> httpResponse);
            });
    assertTrue(exception.getMessage().contains("403 Forbidden"));
  }
}
