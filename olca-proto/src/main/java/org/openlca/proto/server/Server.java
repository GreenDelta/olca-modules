package org.openlca.proto.server;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.openlca.core.database.IDatabase;
import org.openlca.core.DataDir;
import org.openlca.core.database.Derby;
import org.openlca.julia.Julia;
import org.slf4j.LoggerFactory;

import io.grpc.ServerBuilder;

public class Server {

  private final int port;
  private final io.grpc.Server server;

  public Server(IDatabase db, int port) {
    this.port = port;
    this.server = ServerBuilder.forPort(port)
      .maxInboundMessageSize(1024 * 1024 * 1024)
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
    String nativeArg = null;

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
        case "-native":
          nativeArg = arg;
          break;
        default:
          System.err.println("Unknown flag: " + flag);
          return;
      }
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

      // try to load the native libraries
      if (nativeArg == null) {
        System.out.println("Load native libraries from "
          + Julia.getDefaultDir());
        Julia.load();
      } else {
        var nativeLibDir = new File(nativeArg);
        if (!nativeLibDir.exists() || !nativeLibDir.isDirectory()) {
          System.err.println(nativeLibDir.getAbsolutePath()
            + " is not a directory");
          System.exit(-1);
        }
        System.out.println("Load native libraries from "
          + nativeLibDir.getAbsolutePath());
        Julia.loadFromDir(nativeLibDir);
      }

      // try to open the database
      IDatabase db;
      if (dbArg == null) {
        var defaultDb = new File("database");
        System.out.println("Open default database " + defaultDb);
        db = new Derby(defaultDb);
      } else {
        var dataDbDir = DataDir.getDatabaseDir(dbArg);
        if (dataDbDir.exists()) {
          System.out.println("Open database " + dataDbDir);
          db = new Derby(dataDbDir);
        } else {
          var dbDir = new File(dbArg);
          System.out.println("Open database " + dbDir);
          db = new Derby(dbDir);
        }
      }

      System.out.println("Start server");
      new Server(db, port).start();
      System.out.println("close database...");
      db.close();
      System.out.println("database closed.");
    } catch (Exception e) {
      System.err.println("Server error: " + e.getMessage());
    }
  }
}
