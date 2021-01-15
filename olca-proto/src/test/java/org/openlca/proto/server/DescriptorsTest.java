package org.openlca.proto.server;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openlca.core.model.ImpactMethod;
import org.openlca.proto.Tests;
import org.openlca.proto.generated.Proto;
import org.openlca.proto.generated.Services;

public class DescriptorsTest {

  @Test
  public void testForName() {
    var db = Tests.db();
    var method = ImpactMethod.of("Something [like] this");
    db.insert(method);
    var stream = Descriptors.get(db,
      Services.DescriptorRequest.newBuilder()
        .setType(Proto.ModelType.IMPACT_METHOD)
        .setName(method.name)
        .build());
    var ref = stream.findFirst().orElseThrow();
    assertEquals(method.name, ref.getName());
  }
}
