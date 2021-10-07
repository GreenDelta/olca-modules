package org.openlca.proto.io.server;

import static org.junit.Assert.*;

import com.google.protobuf.Empty;
import org.junit.Test;
import org.openlca.proto.grpc.AboutServiceGrpc;
import org.openlca.proto.io.Tests;

public class AboutTest {

  @Test
  public void testAbout() {

    var actual = new Object() {
      int version;
      int minSupportedVersion;
      String dbName;
    };

    ServiceTests.on(channel -> {
      var stub = AboutServiceGrpc.newBlockingStub(channel);
      var about = stub.about(Empty.newBuilder().build());
      actual.version = about.getVersion();
      actual.minSupportedVersion = about.getMinSupportedVersion();
      actual.dbName = about.getDatabase();
    });

    assertEquals(AboutService.VERSION, actual.version);
    assertEquals(
      AboutService.MIN_SUPPORTED_VERSION,
      actual.minSupportedVersion);
    assertEquals(Tests.db().getName(), actual.dbName);

  }

}
