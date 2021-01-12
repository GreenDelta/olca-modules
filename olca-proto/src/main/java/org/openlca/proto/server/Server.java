package org.openlca.proto.server;

import java.util.concurrent.TimeUnit;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.derby.DerbyDatabase;
import org.slf4j.LoggerFactory;

import io.grpc.ServerBuilder;

public class Server {

  private final int port;
  private final io.grpc.Server server;

  public Server(IDatabase db, int port) {
    this.port = port;
    this.server = ServerBuilder.forPort(port)
      .addService(new DataService(db))
      .addService(new FlowMapService(db))
      .addService(new ResultService(db))
      .build();
  }

  public void start() {
    try {
      var log = LoggerFactory.getLogger(getClass());
      log.info("start server: localhost:{}", port);
      server.start();
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        System.out.println("shut down server");
        try {
          Server.this.stop();
        } catch (Exception e) {
          e.printStackTrace(System.err);
        }
        System.out.println("server shut down");
      }));
      log.info("server waiting for connections");
      server.awaitTermination();
    } catch (Exception e) {
      throw new RuntimeException("failed to start server", e);
    }
  }

  public void stop() {
    if (server == null)
      return;
    try {
      server.shutdown().awaitTermination(5, TimeUnit.MINUTES);
    } catch (Exception e) {
      throw new RuntimeException("failed to stop server", e);
    }
  }

  public static void main(String[] args) {

    String dbArg = null;
    String portArg = null;

    String flag = null;
    for (var arg : args) {
      if (arg.startsWith("-")) {
        flag = arg;
        continue;
      }
      if (flag == null) {
        System.err.println("Invalid argument: " + arg);
        return;
      }
      switch (flag) {
        case "-db":
          dbArg = arg;
          break;
        case "-port":
          portArg = arg;
          break;
        default:
          System.err.println("Unknown flag: " + flag);
          return;
      }
    }

    if (dbArg == null) {
      System.err.println(
        "No database given. You can set it via `-db <database>`.");
      return;
    }

    int port;
    if (portArg == null) {
      System.out.println("No port given. Take 8080 as default");
      port = 8080;
    } else {
      try {
        port = Integer.parseInt(portArg, 10);
      } catch (Exception e) {
        System.err.println(portArg + " is not a valid port number.");
        return;
      }
    }

    try {
      var db = DerbyDatabase.fromDataDir(dbArg);
      new Server(db, port).start();
      System.out.println("close database...");
      db.close();
      System.out.println("database closed.");
    } catch (Exception e) {
      System.err.println("Server error: " + e.getMessage());
    }
  }
}
