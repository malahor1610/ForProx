package com.github.malahor.forprox.validation;

import com.github.malahor.forprox.connection.Connection;
import com.github.malahor.forprox.server.Communication;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HostValidator {

  private static final String BANNED_HOSTS_LIST = "banned.txt";

  public static void validateHost(Connection connection, Communication communication)
      throws ForbiddenException, IOException {
    var host = connection.host();
    log.info("Resolved host: {}", host);
    if (HostValidator.isBanned(host)) {
      forbidConnection(communication);
      throw new ForbiddenException(host);
    }
  }

  private static boolean isBanned(String host) {
    var classloader = Thread.currentThread().getContextClassLoader();
    try (var stream =
        new BufferedReader(
                new InputStreamReader(classloader.getResourceAsStream(BANNED_HOSTS_LIST)))
            .lines()) {
      return stream.anyMatch(line -> line.equals(host));
    }
  }

  private static void forbidConnection(Communication communication) throws IOException {
    var out = communication.getClientOut();
    out.write("HTTP/1.1 403 Forbidden\r\n\r\n".getBytes());
    out.flush();
  }
}
