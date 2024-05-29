package com.github.malahor.forprox.validation;

import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Stream;

import com.github.malahor.forprox.Communication;
import com.github.malahor.forprox.connection.Connection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ResourceUtils;

@Slf4j
public class HostValidator {

  private static final String BANNED_HOSTS_LIST = "classpath:banned.txt";

  public static void validateHost(Connection connection, Communication communication)
          throws ForbiddenException, IOException {
    var host = connection.host();
    log.info("Resolved host: {}", host);
    if (HostValidator.isBanned(host)) {
      forbidConnection(communication);
      throw new ForbiddenException(host);
    }
    // TODO: check host in scope of banned/warned/whatever sites
  }

  private static boolean isBanned(String host) {
    try (Stream<String> stream = Files.lines(ResourceUtils.getFile(BANNED_HOSTS_LIST).toPath())) {
      return stream.anyMatch(line -> line.equals(host));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static void forbidConnection(Communication communication) throws IOException {
    var out = communication.getClientOut();
    out.write("HTTP/1.1 403 Forbidden\r\n\r\n".getBytes());
    out.flush();
  }
}
