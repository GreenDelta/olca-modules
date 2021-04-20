package org.openlca.proto.output;

import java.util.List;
import java.util.function.Consumer;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.Version;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.proto.Tests;
import org.openlca.proto.generated.EntityType;
import org.openlca.proto.generated.Proto;
import org.openlca.util.Strings;

public class RefsTest {

  private static Flow flow;
  private static Process process;

  @BeforeClass
  public static void setup() {
    var db = Tests.db();

    var units = db.insert(UnitGroup.of("Mass units", "kg"));
    var mass = db.insert(FlowProperty.of("Mass", units));
    var location = db.insert(Location.of("Germany", "DE"));
    flow = Flow.product("Steel", mass);
    flow.description = "an example product";
    flow.version = Version.valueOf(1, 0, 0);
    flow.location = location;
    flow.category = CategoryDao.sync(
      db, ModelType.FLOW, "products", "materials", "steel");
    db.insert(flow);

    process = Process.of("Steel production", flow);
    process.description = "an example process";
    process.version = Version.valueOf(1, 0, 0);
    process.location = location;
    process.category = CategoryDao.sync(
      db, ModelType.PROCESS, "materials", "steel");
    process.processType = ProcessType.UNIT_PROCESS;
    db.insert(process);
  }

  @AfterClass
  public static void tearDown() {
    List.of(
      process,
      flow,
      process.category,
      flow.category,
      flow.location,
      flow.referenceFlowProperty,
      flow.referenceFlowProperty.unitGroup)
      .forEach(Tests.db()::delete);
  }

  @Test
  public void testEntityRefs() {
    checkAllFields(Refs.refOf(process));
    checkAllFields(Refs.refOf(flow));
  }

  @Test
  public void testDescriptorRefs() {
    checkBaseFields(Refs.refOf(Descriptor.of(process)));
    checkBaseFields(Refs.refOf(Descriptor.of(flow)));
  }

  @Test
  public void testRefData() {
    var refData = Refs.dataOf(Tests.db());
    checkAllFields(Refs.refOf(Descriptor.of(process), refData));
    checkAllFields(Refs.refOf(Descriptor.of(flow), refData));
  }

  @Test
  public void testTinyRefs() {
    Consumer<Descriptor> fn = d -> {
      var ref = Refs.tinyRefOf(d);
      assertEquals(d.refId, ref.getId());
      Proto.Ref.getDescriptor()
        .getFields()
        .stream()
        .filter(field -> {
          var f = field.getName();
          return !f.equals("id") && !f.equals("entity_type");
        })
        .forEach(field -> {
          if (field.isRepeated()) {
            assertEquals(0, ref.getRepeatedFieldCount(field));
          } else {
            assertFalse(ref.hasField(field));
          }
        });
    };
    fn.accept(Descriptor.of(flow));
    fn.accept(Descriptor.of(process));
  }

  private void checkAllFields(Proto.Ref.Builder ref) {
    checkBaseFields(ref);
    assertEquals("DE", ref.getLocation());
    String[] categoryPath;
    if (ref.getEntityType() == EntityType.Process) {
      categoryPath = new String[]{"materials", "steel"};
    } else {
      categoryPath = new String[]{
        "products", "materials", "steel"};
      assertEquals("kg", ref.getRefUnit());
    }
    for (int i = 0; i < categoryPath.length; i++) {
      assertEquals(categoryPath[i], ref.getCategoryPath(i));
    }
  }

  private void checkBaseFields(Proto.Ref.Builder ref) {
    assertEquals("01.00.000", ref.getVersion());
    assertTrue(Strings.notEmpty(ref.getLastChange()));
    if (ref.getEntityType() == EntityType.Process) {
      assertEquals(process.refId, ref.getId());
      assertEquals("Steel production", ref.getName());
      assertEquals("an example process", ref.getDescription());
      assertEquals(Proto.ProcessType.UNIT_PROCESS, ref.getProcessType());
    } else {
      assertEquals(flow.refId, ref.getId());
      assertEquals(EntityType.Flow, ref.getEntityType());
      assertEquals("Steel", ref.getName());
      assertEquals("an example product", ref.getDescription());
      assertEquals(Proto.FlowType.PRODUCT_FLOW, ref.getFlowType());
    }
  }
}
