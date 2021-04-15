package org.openlca.proto.input;

import static org.junit.Assert.*;

import java.util.List;
import java.util.UUID;

import com.google.protobuf.AbstractMessage;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.proto.MemStore;
import org.openlca.proto.Tests;
import org.openlca.proto.generated.Proto;

public class ImportStatusTest {

  private final IDatabase db = Tests.db();
  private List<Import<? extends CategorizedEntity>> imports;
  private List<AbstractMessage.Builder> protos;

  @Before
  public void setup() {
    var imp = new ProtoImport(MemStore.create(), db);
    imports = List.of(
      new ActorImport(imp),
      new CategoryImport(imp),
      new CurrencyImport(imp),
      new DqSystemImport(imp),
      new FlowImport(imp),
      new FlowPropertyImport(imp),
      new ImpactCategoryImport(imp),
      new ImpactMethodImport(imp),
      new LocationImport(imp),
      new ParameterImport(imp),
      new ProcessImport(imp),
      new ProductSystemImport(imp),
      new ProjectImport(imp),
      new SocialIndicatorImport(imp),
      new SourceImport(imp),
      new UnitGroupImport(imp));
  }

  @Test
  public void testNullId() {
    for (var imp : imports) {
      var status = imp.of(null);
      assertTrue(status.isError());
      assertNull(status.model());
      assertTrue(status.error()
        .startsWith("Could not resolve"));
    }
  }

  @Test
  public void testUnknownId() {
    for (var imp : imports) {
      var status = imp.of(UUID.randomUUID().toString());
      assertTrue(status.isError());
      assertNull(status.model());
      assertTrue(status.error()
        .startsWith("Could not resolve"));
    }
  }

}
