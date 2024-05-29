package com.github.malahor.forprox;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.malahor.forprox.server.Server;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ServerTests {

  private static final int proxyPort = 8989;
  private static final Server server = new Server();
  private static Thread proxyThread;

  @BeforeAll
  public static void setUp() throws Exception {
    proxyThread = new Thread(server::start);
    proxyThread.start();
    // Wait for proxy to start up
    Thread.sleep(5000);
  }

  @AfterAll
  public static void tearDown() {
    proxyThread.interrupt();
  }

  @Test
  public void testHttpsRequestThroughProxy() throws Exception {
    var proxyRoutePlanner = new DefaultProxyRoutePlanner(new HttpHost("localhost", proxyPort));
    try (var proxiedHttpClient = HttpClients.custom().setRoutePlanner(proxyRoutePlanner).build()) {
      var httpGet = new HttpGet("https://httpbin.org/ip");
      var response = proxiedHttpClient.execute(httpGet);
      var responseBody = EntityUtils.toString(response.getEntity());
      assertEquals(
          """
                  {
                    "origin": "109.243.1.65"
                  }
                  """,
          responseBody);
    }
  }

  @Test
  public void testHttpRequestThroughProxy() throws Exception {
    var proxyRoutePlanner = new DefaultProxyRoutePlanner(new HttpHost("localhost", proxyPort));
    try (var proxiedHttpClient = HttpClients.custom().setRoutePlanner(proxyRoutePlanner).build()) {
      var httpGet = new HttpGet("http://httpbin.org/ip");
      var response = proxiedHttpClient.execute(httpGet);
      var responseBody = EntityUtils.toString(response.getEntity());
      assertEquals(
          """
                    {
                      "origin": "109.243.1.65"
                    }
                    """,
          responseBody);
    }
  }

  @Test
  public void testBannedRequestThroughProxy() throws Exception {
    var proxyRoutePlanner = new DefaultProxyRoutePlanner(new HttpHost("localhost", proxyPort));
    try (var proxiedHttpClient = HttpClients.custom().setRoutePlanner(proxyRoutePlanner).build()) {
      var httpGet = new HttpGet("https://www.facebook.com");
      var response = proxiedHttpClient.execute(httpGet);
      assertEquals(HttpStatus.SC_FORBIDDEN, response.getStatusLine().getStatusCode());
    }
  }
}
