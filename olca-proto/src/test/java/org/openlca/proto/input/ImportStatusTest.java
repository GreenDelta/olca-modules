package org.openlca.proto.input;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.UUID;

import com.google.protobuf.AbstractMessage;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Version;
import org.openlca.jsonld.input.UpdateMode;
import org.openlca.proto.InMemoryProtoStore;
import org.openlca.proto.Tests;
import org.openlca.proto.generated.Proto;
import org.openlca.proto.output.Out;

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

  @Test
  public void testUpdate() throws Exception {
    int i = 0;
    var db = Tests.db();
    var store = InMemoryProtoStore.create();
    var protoImport = new ProtoImport(store, db)
      .withUpdateMode(UpdateMode.IF_NEWER);

    for (var type : ModelType.values()) {
      if (!type.isCategorized() || type == ModelType.CATEGORY)
        continue;
      i++;

      var id = UUID.randomUUID().toString();
      var instance = type.getModelClass()
        .getConstructor()
        .newInstance();
      instance.refId = id;
      instance.name = type.name();
      instance.version = Version.valueOf(0, 0, 1);
      instance.lastChange = new Date().getTime();

      // create new object
      put(store, type, Out.toProto(db, instance));
      var imp = protoImport.getImport(type);
      var status1 = imp.of(id);
      assertTrue(status1.isCreated());
      assertEquals(id, status1.model().refId);

      // skip existing
      var status2 = imp.of(id);
      assertTrue(status2.isSkipped());
      assertEquals(id, status2.model().refId);

      // update existing
      instance.version = Version.valueOf(0, 0, 2);
      put(store, type, Out.toProto(db, instance));
      var status3 = imp.of(id);
      assertTrue(status3.isUpdated());
      assertEquals(id, status3.model().refId);

      // delete it from the database
      db.delete(status3.model());
    }
    assertEquals(15, i);
  }

  private void put(
    InMemoryProtoStore store,
    ModelType type,
    AbstractMessage proto) {

    switch (type) {
      case ACTOR -> store.putActor(
        (Proto.Actor) proto);
      case CATEGORY -> store.putCategory(
        (Proto.Category) proto);
      case CURRENCY -> store.putCurrency(
        (Proto.Currency) proto);
      case DQ_SYSTEM -> store.putDQSystem(
        (Proto.DQSystem) proto);
      case FLOW -> store.putFlow(
        (Proto.Flow) proto);
      case FLOW_PROPERTY -> store.putFlowProperty(
        (Proto.FlowProperty) proto);
      case IMPACT_CATEGORY -> store.putImpactCategory(
        (Proto.ImpactCategory) proto);
      case IMPACT_METHOD -> store.putImpactMethod(
        (Proto.ImpactMethod) proto);
      case LOCATION -> store.putLocation(
        (Proto.Location) proto);
      case PARAMETER -> store.putParameter(
        (Proto.Parameter) proto);
      case PROCESS -> store.putProcess(
        (Proto.Process) proto);
      case PRODUCT_SYSTEM -> store.putProductSystem(
        (Proto.ProductSystem) proto);
      case PROJECT -> store.putProject(
        (Proto.Project) proto);
      case SOCIAL_INDICATOR -> store.putSocialIndicator(
        (Proto.SocialIndicator) proto);
      case SOURCE -> store.putSource(
        (Proto.Source) proto);
      case UNIT_GROUP -> store.putUnitGroup(
        (Proto.UnitGroup) proto);
    }
  }

}
