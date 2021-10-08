package org.openlca.proto.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.UUID;
import java.util.function.BiConsumer;

import org.junit.Test;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.proto.ProtoRef;
import org.openlca.proto.io.output.Out;
import org.openlca.proto.io.output.Refs;
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

    BiConsumer<ModelType, ProtoRef> check = (type, ref) -> {
      assertTrue(Strings.notEmpty(ref.getId()));
      assertEquals(
        type.getModelClass().getSimpleName(),
        ref.getType().name());
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
