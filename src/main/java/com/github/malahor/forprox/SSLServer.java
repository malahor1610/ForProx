package com.github.malahor.forprox;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import javax.net.ssl.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

@Slf4j
@Component
public class SSLServer {
  private static final int PORT = 9898;
  private static final String KEY_STORE = "classpath:proxykeystore.jks";

  public void start() {
    try {
      var sslServerSocketFactory = getSslServerSocketFactory();
      try (var serverSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(PORT)) {
        log.info("Proxy ssl server started at port: {}", PORT);
        while (true) {
          var clientSocket = serverSocket.accept();
          new ProxyThread(clientSocket).start();
        }
      } catch (IOException e) {
        log.error("Error occurred: {}", e.getMessage());
      }
    } catch (KeyStoreException
        | IOException
        | NoSuchAlgorithmException
        | CertificateException
        | UnrecoverableKeyException
        | KeyManagementException e) {
      log.error("Error occurred: {}", e.getMessage());
    }
  }

  private SSLServerSocketFactory getSslServerSocketFactory()
      throws KeyStoreException,
          IOException,
          NoSuchAlgorithmException,
          CertificateException,
          UnrecoverableKeyException,
          KeyManagementException {
    var keyStore = loadKeystore();
    var keyManagerFactory = initializeKeyManagerFactory(keyStore);
    var sslContext = initializeSslContext(keyManagerFactory);
    return sslContext.getServerSocketFactory();
  }

  private KeyStore loadKeystore()
      throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
    var keyStore = KeyStore.getInstance("JKS");
    try (var keyStoreStream = new FileInputStream(ResourceUtils.getFile(KEY_STORE))) {
      keyStore.load(keyStoreStream, "changeit".toCharArray());
    }
    return keyStore;
  }

  private KeyManagerFactory initializeKeyManagerFactory(KeyStore keyStore)
      throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
    var keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    keyManagerFactory.init(keyStore, "changeit".toCharArray());
    return keyManagerFactory;
  }

  private SSLContext initializeSslContext(KeyManagerFactory keyManagerFactory)
      throws NoSuchAlgorithmException, KeyManagementException {
    var sslContext = SSLContext.getInstance("TLS");
    sslContext.init(keyManagerFactory.getKeyManagers(), null, null);
    return sslContext;
  }
}
