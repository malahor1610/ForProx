package com.github.malahor.forprox;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.malahor.forprox.server.SSLServer;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SSLServerTests {

  private static final int proxyPort = 9898;
  private static final SSLServer server = new SSLServer();
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
    var sslContext = TrustCertificates.createTrustAllSslContext();
    var proxy = new HttpHost("localhost", proxyPort, "https");
    var routePlanner = new DefaultProxyRoutePlanner(proxy);
    try (var httpClient =
        HttpClients.custom()
            .setRoutePlanner(routePlanner)
            .setSSLContext(sslContext)
            .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
            .build()) {
      var httpGet = new HttpGet("https://httpbin.org/ip");
      var response = httpClient.execute(httpGet);
      assertEquals(200, response.getStatusLine().getStatusCode());
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
    var sslContext = TrustCertificates.createTrustAllSslContext();
    var proxy = new HttpHost("localhost", proxyPort, "https");
    var routePlanner = new DefaultProxyRoutePlanner(proxy);
    try (var httpClient =
        HttpClients.custom()
            .setRoutePlanner(routePlanner)
            .setSSLContext(sslContext)
            .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
            .build()) {
      var httpGet = new HttpGet("http://httpbin.org/ip");
      var response = httpClient.execute(httpGet);
      assertEquals(200, response.getStatusLine().getStatusCode());
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
    var sslContext = TrustCertificates.createTrustAllSslContext();
    var proxy = new HttpHost("localhost", proxyPort, "https");
    var routePlanner = new DefaultProxyRoutePlanner(proxy);
    try (var httpClient =
        HttpClients.custom()
            .setRoutePlanner(routePlanner)
            .setSSLContext(sslContext)
            .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
            .build()) {
      var httpGet = new HttpGet("https://www.facebook.com");
      var response = httpClient.execute(httpGet);
      assertEquals(403, response.getStatusLine().getStatusCode());
    }
  }
}
