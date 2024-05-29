package com.github.malahor.forprox;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ServersRunner {

  private final Server server;
  private final SSLServer sslServer;
  private final Environment env;

  @EventListener(ApplicationReadyEvent.class)
  public void start() {
    new Thread(server::start).start();
    if (Boolean.TRUE.equals(env.getProperty("app.run.ssl-server", Boolean.class)))
      new Thread(sslServer::start).start();
  }
}
