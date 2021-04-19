package org.openlca.proto.server;

import java.util.UUID;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Version;
import org.openlca.proto.Messages;
import org.openlca.proto.Tests;
import org.openlca.proto.generated.data.DataFetchServiceGrpc;
import org.openlca.proto.generated.data.DataSet;
import org.openlca.proto.generated.data.DataUpdateServiceGrpc;
import org.openlca.proto.generated.data.DeleteRequest;
import org.openlca.proto.generated.data.FindRequest;
import org.openlca.proto.output.Out;

public class DataUpdateTest {

  private final IDatabase db = Tests.db();

  @Test
  public void testCrud() {
    ServiceTests.on(chan -> {

      var fetch = DataFetchServiceGrpc.newBlockingStub(chan);
      var update = DataUpdateServiceGrpc.newBlockingStub(chan);

      // create some instance of each categorized type and
      // insert it via the service
      int i = 0;
      try {
        for (var type : ModelType.values()) {

          // the ID of categories is calculated from the category
          // name; we do not test this behaviour here
          if (!type.isCategorized() || type == ModelType.CATEGORY)
            continue;
          i++;
          var id = UUID.randomUUID().toString();

          // check that it does not yet exist
          var dataSet = fetch.find(findRequestOf(type, id));
          assertTrue(Messages.isEmpty(dataSet));

          // create an instance and insert it
          var instance = type.getModelClass()
            .getConstructor()
            .newInstance();
          instance.refId = id;
          instance.version = Version.valueOf(0, 0, 1);
          var ref = update.put(DataUtil.toDataSet(db, instance).build());
          assertEquals(id, ref.getId());
          assertEquals("00.00.001", ref.getVersion());

          // check that we can get it
          dataSet = fetch.find(findRequestOf(type, id));
          assertEquals(id, getField(type, dataSet, "id"));
          assertEquals("00.00.001", getField(type, dataSet, "version"));

          // update it
          instance.name = type.name();
          instance.version = Version.valueOf(1, 0, 0);
          ref = update.put(DataUtil.toDataSet(db, instance).build());
          assertEquals(id, ref.getId());
          assertEquals(type.name(), ref.getName());
          assertEquals("01.00.000", ref.getVersion());

          // check that we get the updated thing
          dataSet = fetch.find(findRequestOf(type, id));
          assertEquals(id, getField(type, dataSet, "id"));
          assertEquals("01.00.000", getField(type, dataSet, "version"));
          assertEquals(type.name(), getField(type, dataSet, "name"));

          // delete it and check that we do not get it anymore
          update.delete(DeleteRequest.newBuilder()
            .setModelType(Out.modelTypeOf(type))
            .setId(id)
            .build());
          dataSet = fetch.find(findRequestOf(type, id));
          assertTrue(Messages.isEmpty(dataSet));

        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

      assertEquals(15, i);

    });
  }

  private FindRequest findRequestOf(ModelType type, String id) {
    return FindRequest.newBuilder()
      .setModelType(Out.modelTypeOf(type))
      .setId(id)
      .build();
  }

  private String getField(ModelType type, DataSet ds, String field) {
    var proto = switch (type) {
      case ACTOR -> ds.getActor();
      case CATEGORY -> ds.getCategory();
      case CURRENCY -> ds.getCurrency();
      case DQ_SYSTEM -> ds.getDqSystem();
      case FLOW -> ds.getFlow();
      case FLOW_PROPERTY -> ds.getFlowProperty();
      case IMPACT_CATEGORY -> ds.getImpactCategory();
      case IMPACT_METHOD -> ds.getImpactMethod();
      case LOCATION -> ds.getLocation();
      case PARAMETER -> ds.getParameter();
      case PROCESS -> ds.getProcess();
      case PRODUCT_SYSTEM -> ds.getProductSystem();
      case PROJECT -> ds.getProject();
      case SOCIAL_INDICATOR -> ds.getSocialIndicator();
      case SOURCE -> ds.getSource();
      case UNIT_GROUP -> ds.getUnitGroup();
      default -> null;
    };
    assertNotNull(proto);
    var fieldDef = proto.getDescriptorForType()
      .findFieldByName(field);
    var value = proto.getField(fieldDef);
    return value instanceof String
      ? value.toString()
      : null;
  }
}
