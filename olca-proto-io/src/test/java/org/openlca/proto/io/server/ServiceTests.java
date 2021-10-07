package org.openlca.proto.io.server;

import java.io.IOException;
import java.util.UUID;
import java.util.function.Consumer;

import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.openlca.proto.io.Tests;

class ServiceTests {

  private ServiceTests() {
  }

  static void on(Consumer<ManagedChannel> fn) {
    try {
      var name = UUID.randomUUID().toString();
      var db = Tests.db();
      var server = InProcessServerBuilder
        .forName(name)
        .directExecutor()
        .addService(new DataFetchService(db))
        .addService(new DataUpdateService(db))
        .addService(new FlowMapService(db))
        .addService(new ResultService(db))
        .addService(new AboutService(db))
        .build()
        .start();
      var channel = InProcessChannelBuilder
        .forName(name)
        .directExecutor()
        .usePlaintext()
        .build();

      fn.accept(channel);

      channel.shutdown();
      server.shutdown();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
