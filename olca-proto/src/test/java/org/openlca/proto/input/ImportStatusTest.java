package org.openlca.proto.input;

import static org.junit.Assert.*;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.proto.InMemoryProtoStore;
import org.openlca.proto.Tests;

public class ImportStatusTest {

  private final IDatabase db = Tests.db();
  private ProtoImport protoImport;

  @Before
  public void setup() {
    protoImport = new ProtoImport(InMemoryProtoStore.create(), db);
  }

  @Test
  public void testNullId() {
    for (var type : ModelType.values()) {
      if (!type.isCategorized())
        continue;
      var imp = protoImport.getImport(type);
      var status = imp.of(null);
      assertTrue(status.isError());
      assertNull(status.model());
      assertTrue(status.error()
        .startsWith("Could not resolve"));
    }
  }

  @Test
  public void testUnknownId() {
    for (var type : ModelType.values()) {
      if (!type.isCategorized())
        continue;
      var imp = protoImport.getImport(type);
      var status = imp.of(UUID.randomUUID().toString());
      assertTrue(status.isError());
      assertNull(status.model());
      assertTrue(status.error()
        .startsWith("Could not resolve"));
    }
  }

}
