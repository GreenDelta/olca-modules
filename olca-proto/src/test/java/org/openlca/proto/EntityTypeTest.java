package org.openlca.proto;

import static org.junit.Assert.*;

import java.util.UUID;
import java.util.function.BiConsumer;

import org.junit.Test;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.proto.generated.Proto;
import org.openlca.proto.output.Out;
import org.openlca.proto.output.Refs;
import org.openlca.util.Strings;

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
      assertEquals(
        modelType.getModelClass().getSimpleName(),
        typeValue.toString());
    }
  }

  @Test
  public void testWriteToDescriptors() throws Exception {

    BiConsumer<ModelType, Proto.Ref> check = (type, ref) -> {
      assertTrue(Strings.notEmpty(ref.getId()));
      assertEquals(
        type.getModelClass().getSimpleName(),
        ref.getEntityType().name());
    };

    for (var modelType : ModelType.values()) {
      if (!modelType.isCategorized())
        continue;
      var id = UUID.randomUUID().toString();
      var instance = modelType.getModelClass()
        .getConstructor()
        .newInstance();
      instance.refId = id;
      check.accept(modelType, Refs.refOf(instance).build());
      check.accept(modelType, Refs.refOf(Descriptor.of(instance)).build());
    }
  }

}
