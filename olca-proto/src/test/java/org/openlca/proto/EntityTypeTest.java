package org.openlca.proto;

import static org.junit.Assert.*;

import java.util.UUID;

import org.junit.Test;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.proto.generated.EntityType;
import org.openlca.proto.output.Out;

public class EntityTypeTest {

  private final IDatabase db = Tests.db();

  @Test
  public void testWriteToModels() throws Exception {
    for (var modelType : ModelType.values()) {
      if (!modelType.isCategorized())
        continue;
      var id = UUID.randomUUID().toString();
      var instance = modelType.getModelClass()
        .getConstructor()
        .newInstance();
      instance.refId = id;
      var proto = Out.toProto(db, instance);
      var field = proto.getDescriptorForType()
        .findFieldByName("entity_type");
      var typeValue = proto.getField(field);
      assertTrue(typeValue instanceof EntityType);
      assertEquals(
        modelType.getModelClass().getSimpleName(),
        ((EntityType)typeValue).name());
    }
  }

}
