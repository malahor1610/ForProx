package com.github.malahor.forprox;

import static org.junit.jupiter.api.Assertions.*;

import com.github.malahor.forprox.server.Server;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.BasicHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.routing.DefaultProxyRoutePlanner;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.junit.jupiter.api.*;

class ServerTests {

  private static final int proxyPort = 9898;
  private static Thread proxyThread;
  private static HttpClient httpClient;

  @BeforeAll
  public static void setUp() throws Exception {
    proxyThread = new Thread(Server::startProxy);
    proxyThread.start();
    // Wait for proxy to start up
    Thread.sleep(5000);
    initializeHttpClient();
  }

  public static void initializeHttpClient()
      throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
    TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;
    var sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
    var sslSocketFactory =
        new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
    var socketFactoryRegistry =
        RegistryBuilder.<ConnectionSocketFactory>create()
            .register("https", sslSocketFactory)
            .register("http", new PlainConnectionSocketFactory())
            .build();
    var connectionManager = new BasicHttpClientConnectionManager(socketFactoryRegistry);
    var proxy = new HttpHost("https", "localhost", proxyPort);
    var routePlanner = new DefaultProxyRoutePlanner(proxy);
    httpClient =
        HttpClients.custom()
            .setConnectionManager(connectionManager)
            .setRoutePlanner(routePlanner)
            .build();
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
