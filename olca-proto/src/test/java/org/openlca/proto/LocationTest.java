package org.openlca.proto;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import com.google.protobuf.util.JsonFormat;
import org.junit.Test;

public class LocationTest {

  @Test
  public void testParseJson() throws Exception {
    try (var stream = getClass().getResourceAsStream("location.json");
         var reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
         var buffer = new BufferedReader(reader)) {
      var builder = Proto.Location.newBuilder();
      JsonFormat.parser()
        .ignoringUnknownFields()
        .merge(buffer, builder);
      var proto = builder.build();
      assertEquals("Some", proto.getName());
      assertEquals("Some", proto.getCode());
      assertEquals(42.24, proto.getLatitude(), 1e-10);
      assertEquals(24.42, proto.getLongitude(), 1e-10);

      // `geometry` is ignored
      assertTrue(proto.getGeometryBytes().isEmpty());
    }
  }
}
