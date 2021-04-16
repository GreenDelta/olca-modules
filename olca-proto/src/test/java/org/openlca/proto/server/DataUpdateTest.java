package org.openlca.proto.server;

import java.util.UUID;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openlca.core.model.ModelType;
import org.openlca.proto.Messages;
import org.openlca.proto.generated.data.DataFetchServiceGrpc;
import org.openlca.proto.generated.data.DataUpdateServiceGrpc;
import org.openlca.proto.generated.data.FindRequest;
import org.openlca.proto.output.Out;

public class DataUpdateTest {

  @Test
  public void testInsert() {
    ServiceTests.on(chan -> {

      var fetch = DataFetchServiceGrpc.newBlockingStub(chan);
      var update = DataUpdateServiceGrpc.newBlockingStub(chan);

      // create some instance of each categorized type and
      // insert it via the service
      int i = 0;
      try {
        for (var type : ModelType.values()) {
          if (!type.isCategorized())
            continue;
          i++;
          var id = UUID.randomUUID().toString();

          // create an instance
          var instance = type.getModelClass()
            .getConstructor()
            .newInstance();
          instance.refId = id;

          // check that it does not yet exist
          var dataSet = fetch.find(findRequestOf(type, id));
          assertTrue(Messages.isEmpty(dataSet));
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

      assertEquals(16, i);

    });
  }

  private FindRequest findRequestOf(ModelType type, String id) {
    return FindRequest.newBuilder()
      .setModelType(Out.modelTypeOf(type))
      .setId(id)
      .build();
  }

}
